package seedu.address.logic.parser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import seedu.address.logic.Messages;
import seedu.address.logic.parser.exceptions.ParseException;

/**
 * Stores mapping of prefixes to their respective arguments.
 * Each key may be associated with multiple argument values.
 * Values for a given key are stored in a list, and the insertion ordering is maintained.
 * Keys are unique, but the list of argument values may contain duplicate argument values, i.e. the same argument value
 * can be inserted multiple times for the same prefix.
 */
public class ArgumentMultimap {

    private static final Pattern PREFIX_TOKEN =
            Pattern.compile("(?<!\\S)(?<token>\\p{L}+/(?:\\p{L}+)?)");
    private static final Set<String> KNOWN_PREFIX_TOKENS = CliSyntax.ALL_PREFIXES.stream()
            .map(prefix -> prefix.toString().toLowerCase(Locale.ROOT))
            .collect(Collectors.toUnmodifiableSet());
    private static final Set<String> LIKELY_PREFIX_WORDS = Set.of(
            "name", "phone", "email", "level", "subject", "subjects", "sub", "rate", "venue", "address",
            "remark", "note", "notes", "date", "time", "duration", "amount", "student", "students", "description",
            "assignment", "homework", "payment", "payments", "count", "every", "week", "buffer", "past", "status",
            "feedback", "overdue", "outstanding", "all", "reason", "archived", "deadline", "duedate", "text",
            "minutes", "weeks");
    private static final Set<String> ALLOWED_SLASH_WORDS = Set.of("c/o", "d/o", "s/o", "n/a", "w/o");

    /** Prefixes mapped to their respective arguments */
    private final Map<Prefix, List<String>> argMultimap = new HashMap<>();
    private final String rawArguments;
    private final Set<Prefix> acceptedPrefixes;

    /** Creates an empty multimap without arguments to diagnose. */
    public ArgumentMultimap() {
        this("", List.of());
    }

    /** Creates a multimap that remembers what its tokenizer accepted and the text it read. */
    ArgumentMultimap(String rawArguments, List<Prefix> acceptedPrefixes) {
        this.rawArguments = rawArguments;
        this.acceptedPrefixes = Set.copyOf(acceptedPrefixes);
    }

    /**
     * Associates the specified argument value with {@code prefix} key in this map.
     * If the map previously contained a mapping for the key, the new value is appended to the list of existing values.
     *
     * @param prefix   Prefix key with which the specified argument value is to be associated
     * @param argValue Argument value to be associated with the specified prefix key
     */
    public void put(Prefix prefix, String argValue) {
        List<String> argValues = getAllValues(prefix);
        argValues.add(argValue);
        argMultimap.put(prefix, argValues);
    }

    /**
     * Returns the last value of {@code prefix}.
     */
    public Optional<String> getValue(Prefix prefix) {
        List<String> values = getAllValues(prefix);
        return values.isEmpty() ? Optional.empty() : Optional.of(values.getLast());
    }

    /**
     * Returns all values of {@code prefix}.
     * If the prefix does not exist or has no values, this will return an empty list.
     * Modifying the returned list will not affect the underlying data structure of the ArgumentMultimap.
     */
    public List<String> getAllValues(Prefix prefix) {
        if (!argMultimap.containsKey(prefix)) {
            return new ArrayList<>();
        }
        return new ArrayList<>(argMultimap.get(prefix));
    }

    /**
     * Returns the preamble (text before the first valid prefix). Trims any leading/trailing spaces.
     */
    public String getPreamble() {
        return getValue(new Prefix("")).orElse("");
    }

    /**
     * Rejects an unaccepted prefix-shaped word that would otherwise be folded into a value and reported as a
     * misleading field error or as a missing required prefix.
     *
     * <p>Free text may contain slash-bearing prose such as {@code c/o}, {@code n/a} and {@code p/3}. Names and venues
     * are short identifiers where a field name is almost certainly a typo, so they are not exempt. A structured free
     * text field such as a description also tolerates short real prefixes that commonly occur in prose, while a note,
     * feedback or remark is fully permissive because it may quote a command.
     *
     * @param textPrefixes accepted parameters whose values are free text
     * @param permissiveTextPrefixes values that may contain any likely prefix as prose
     * @throws ParseException if a token cannot be accepted by this command
     */
    public void verifyNoUnexpectedPrefixes(List<Prefix> textPrefixes, List<Prefix> permissiveTextPrefixes)
            throws ParseException {
        Prefix enclosingPrefix = null;
        Matcher matcher = PREFIX_TOKEN.matcher(rawArguments);
        while (matcher.find()) {
            String wordWithSlash = matcher.group("token");
            int slash = wordWithSlash.indexOf('/');
            String prefixToken = wordWithSlash.substring(0, slash + 1);
            String lowerCaseToken = prefixToken.toLowerCase(Locale.ROOT);
            Prefix tokenPrefix = new Prefix(prefixToken);
            if (acceptedPrefixes.contains(tokenPrefix)) {
                enclosingPrefix = tokenPrefix;
                continue;
            }
            boolean isInText = enclosingPrefix != null && textPrefixes.contains(enclosingPrefix);
            boolean isInPermissiveText = enclosingPrefix != null
                    && permissiveTextPrefixes.contains(enclosingPrefix);
            if (ALLOWED_SLASH_WORDS.contains(wordWithSlash.toLowerCase(Locale.ROOT))) {
                continue;
            }
            if (!isInPermissiveText && tokenPrefix.equals(CliSyntax.PREFIX_RETIRED_ADDRESS)) {
                throw unknownPrefix(prefixToken);
            }
            String word = lowerCaseToken.substring(0, lowerCaseToken.length() - 1);
            boolean isLikelyPrefix = LIKELY_PREFIX_WORDS.contains(word) || isKnownPrefix(lowerCaseToken);
            if (!isLikelyPrefix) {
                continue;
            }
            if (isInPermissiveText || (isInText && isShortKnownPrefix(lowerCaseToken))) {
                continue;
            }
            throw unknownPrefix(prefixToken);
        }
    }

    /** Returns whether {@code token} is a parameter of at least one command. */
    private static boolean isKnownPrefix(String token) {
        return KNOWN_PREFIX_TOKENS.contains(token);
    }

    /** Returns whether a known prefix is short enough to be ordinary prose inside a free-text value. */
    private static boolean isShortKnownPrefix(String token) {
        return isKnownPrefix(token) && token.length() <= 4;
    }

    /** Builds the diagnostic, retaining the retired address prefix's venue suggestion. */
    private static ParseException unknownPrefix(String token) {
        if (token.equals(CliSyntax.PREFIX_RETIRED_ADDRESS.toString())) {
            return new ParseException(CliSyntax.MESSAGE_RETIRED_ADDRESS_PREFIX);
        }
        return new ParseException("Unknown prefix for this command: " + token + ".");
    }

    /** Names empty values using only the prefixes recognized by the current command's tokenizer. */
    public void verifyNoEmptyValuesFor(Prefix... prefixes) throws ParseException {
        for (Prefix prefix : prefixes) {
            if (getAllValues(prefix).stream().anyMatch(String::isEmpty)) {
                throw new ParseException("Empty value for " + prefix + ". Enter a value after this prefix.");
            }
        }
    }

    /**
     * Throws a {@code ParseException} if any of the prefixes given in {@code prefixes} appeared more than
     * once among the arguments.
     */
    public void verifyNoDuplicatePrefixesFor(Prefix... prefixes) throws ParseException {
        Prefix[] duplicatedPrefixes = Stream.of(prefixes).distinct()
                .filter(prefix -> argMultimap.containsKey(prefix) && argMultimap.get(prefix).size() > 1)
                .toArray(Prefix[]::new);

        if (duplicatedPrefixes.length > 0) {
            throw new ParseException(Messages.getErrorMessageForDuplicatePrefixes(duplicatedPrefixes));
        }
    }
}
