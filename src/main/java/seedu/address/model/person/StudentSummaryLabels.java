package seedu.address.model.person;

import static java.util.Objects.requireNonNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import seedu.address.model.ReadOnlyAddressBook;

/**
 * Labels scoped to textual homework, dues and student-overview summaries, not contact cards or exports.
 * Build once per rendered summary; each row then looks up its label without scanning the student list.
 */
public final class StudentSummaryLabels {
    private final Map<UUID, String> labels = new HashMap<>();

    /** Includes active and archived students, exposing only four trailing phone digits for colliding names. */
    public StudentSummaryLabels(ReadOnlyAddressBook book) {
        requireNonNull(book);
        Map<String, List<Person>> groups = book.getPersonList().stream().collect(Collectors.groupingBy(
                student -> student.getName().fullName, () -> new TreeMap<>(String.CASE_INSENSITIVE_ORDER),
                Collectors.toList()));
        for (List<Person> group : groups.values()) {
            Map<String, Long> suffixCounts = count(group, StudentSummaryLabels::phoneSuffix);
            Map<String, Long> referenceCounts = count(group, StudentSummaryLabels::shortReference);
            for (Person student : group) {
                String details = "";
                if (group.size() > 1) {
                    details = "phone ending " + phoneSuffix(student);
                    if (suffixCounts.get(phoneSuffix(student)) > 1) {
                        String reference = referenceCounts.get(shortReference(student)) > 1
                                ? student.getId().toString() : shortReference(student);
                        details += ", ref " + reference;
                    }
                }
                if (student.isArchived()) {
                    details += details.isEmpty() ? "archived" : ", archived";
                }
                labels.put(student.getId(), student.getName() + (details.isEmpty() ? "" : " (" + details + ")"));
            }
        }
    }

    /** Returns the label captured for this student when the summary began rendering. */
    public String format(UUID studentId) {
        return requireNonNull(labels.get(requireNonNull(studentId)), "Student is absent from the summary snapshot");
    }

    private static Map<String, Long> count(List<Person> students, Function<Person, String> key) {
        return students.stream().collect(Collectors.groupingBy(key, Collectors.counting()));
    }

    private static String phoneSuffix(Person student) {
        String phone = student.getPhone().value;
        return phone.substring(Math.max(0, phone.length() - 4));
    }

    private static String shortReference(Person student) {
        return student.getId().toString().substring(0, 8);
    }
}
