package seedu.address.testutil;

import static seedu.address.logic.parser.CliSyntax.PREFIX_EMAIL;
import static seedu.address.logic.parser.CliSyntax.PREFIX_LEVEL;
import static seedu.address.logic.parser.CliSyntax.PREFIX_NAME;
import static seedu.address.logic.parser.CliSyntax.PREFIX_PHONE;
import static seedu.address.logic.parser.CliSyntax.PREFIX_RATE;
import static seedu.address.logic.parser.CliSyntax.PREFIX_SUBJECT;
import static seedu.address.logic.parser.CliSyntax.PREFIX_VENUE;

import java.util.Set;

import seedu.address.logic.commands.AddCommand;
import seedu.address.logic.commands.EditCommand.EditPersonDescriptor;
import seedu.address.model.person.Person;
import seedu.address.model.person.Student;
import seedu.address.model.person.Subject;

/**
 * A utility class for Person.
 */
public class PersonUtil {

    /**
     * Returns an add command string for adding the {@code person}.
     */
    public static String getAddCommand(Person person) {
        return AddCommand.COMMAND_WORD + " " + getPersonDetails(person);
    }

    /**
     * Returns the part of command string for the given {@code person}'s details.
     */
    public static String getPersonDetails(Person person) {
        StringBuilder sb = new StringBuilder();
        sb.append(PREFIX_NAME).append(person.getName().fullName).append(" ");
        sb.append(PREFIX_PHONE).append(person.getPhone().value).append(" ");
        if (person.getEmail() != null) {
            sb.append(PREFIX_EMAIL).append(person.getEmail().value).append(" ");
        }

        if (person instanceof Student student) {
            sb.append(PREFIX_LEVEL).append(student.getLevel().value).append(" ");
            student.getSubjects().forEach(
                s -> sb.append(PREFIX_SUBJECT).append(s.value).append(" ")
            );
            sb.append(PREFIX_RATE).append(student.getRate().toString()).append(" ");
            if (student.getVenue() != null) {
                sb.append(PREFIX_VENUE).append(student.getVenue().value).append(" ");
            }
        }
        return sb.toString();
    }

    /**
     * Returns the part of command string for the given {@code EditPersonDescriptor}'s details.
     */
    public static String getEditPersonDescriptorDetails(EditPersonDescriptor descriptor) {
        StringBuilder sb = new StringBuilder();
        descriptor.getName().ifPresent(name -> sb.append(PREFIX_NAME).append(name.fullName).append(" "));
        descriptor.getPhone().ifPresent(phone -> sb.append(PREFIX_PHONE).append(phone.value).append(" "));
        descriptor.getEmail().ifPresent(email -> sb.append(PREFIX_EMAIL).append(email.value).append(" "));
        descriptor.getLevel().ifPresent(level -> sb.append(PREFIX_LEVEL).append(level.value).append(" "));
        descriptor.getRate().ifPresent(rate -> sb.append(PREFIX_RATE).append(rate.toString()).append(" "));
        descriptor.getVenue().ifPresent(venue -> sb.append(PREFIX_VENUE).append(venue.value).append(" "));
        if (descriptor.getSubjects().isPresent()) {
            Set<Subject> subjects = descriptor.getSubjects().get();
            if (subjects.isEmpty()) {
                sb.append(PREFIX_SUBJECT);
            } else {
                subjects.forEach(s -> sb.append(PREFIX_SUBJECT).append(s.value).append(" "));
            }
        }
        return sb.toString();
    }
}
