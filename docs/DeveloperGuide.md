---
  layout: default.md
  title: "Developer Guide"
  pageNav: 3
---

# TutorFlow Developer Guide

<!-- * Table of Contents -->
<page-nav-print />

--------------------------------------------------------------------------------------------------------------------

## **Acknowledgements**

* This project is based on the [AddressBook-Level3](https://github.com/se-edu/addressbook-level3) project created by the [SE-EDU initiative](https://se-education.org). The architecture, the command and parser design and the testing approach are adapted from it.
* Libraries used: [JavaFX](https://openjfx.io/) (user interface), [Jackson](https://github.com/FasterXML/jackson) (JSON storage), [JUnit5](https://github.com/junit-team/junit5) (testing), [Checkstyle](https://checkstyle.sourceforge.io/) and [JaCoCo](https://www.jacoco.org/jacoco/) (code quality and coverage).
* Documentation is built with [MarkBind](https://markbind.org/) and the diagrams are drawn with [PlantUML](https://plantuml.com/).

--------------------------------------------------------------------------------------------------------------------

## **Setting up, getting started**

Refer to the guide [_Setting up and getting started_](SettingUp.md).

--------------------------------------------------------------------------------------------------------------------

## **Design**

### Architecture

<puml src="diagrams/ArchitectureDiagram.puml" width="280" />

The ***Architecture Diagram*** given above explains the high-level design of the App.

The following provides a quick overview of the main components and their interactions.

**Main components of the architecture**

**`Main`** (consisting of classes [`Main`](https://github.com/AY2627S1-CS2103T-F15-1/tp/tree/master/src/main/java/seedu/address/Main.java) and [`MainApp`](https://github.com/AY2627S1-CS2103T-F15-1/tp/tree/master/src/main/java/seedu/address/MainApp.java)) is in charge of the app launch and shut down.
* At app launch, it initializes the other components in the correct sequence, and connects them up with each other.
* At shut down, it shuts down the other components and invokes cleanup methods where necessary.

The bulk of the app's work is done by the following four components:

* [**`UI`**](#ui-component): The UI of the App.
* [**`Logic`**](#logic-component): The command executor.
* [**`Model`**](#model-component): Holds the data of the App in memory.
* [**`Storage`**](#storage-component): Reads data from, and writes data to, the hard disk.

[**`Commons`**](#common-classes) represents a collection of classes used by multiple other components.

**How the architecture components interact with each other**

The *Sequence Diagram* below shows how the components interact with each other for the scenario where the user issues the command `delete 1`.

<puml src="diagrams/ArchitectureSequenceDiagram.puml" width="574" />

Each of the four main components (also shown in the diagram above),

* defines its *API* in an `interface` with the same name as the Component.
* provides its functionality through a concrete `{Component Name}Manager` class that implements the corresponding API interface.

For example, the `Logic` component defines its API in `Logic.java` and implements it in `LogicManager.java`. Other components interact with a component through its interface rather than its concrete class, preventing them from coupling to that component's implementation, as illustrated in the following partial class diagram.

<puml src="diagrams/ComponentManagers.puml" width="300" />

The sections below give more details of each component.

### UI component

The **API** of this component is specified in [`Ui.java`](https://github.com/AY2627S1-CS2103T-F15-1/tp/tree/master/src/main/java/seedu/address/ui/Ui.java)

<puml src="diagrams/UiClassDiagram.puml" alt="Structure of the UI Component"/>

The UI consists of a `MainWindow` and its parts, such as `CommandBox`, `ResultDisplay`, `PersonListPanel`, and `StatusBarFooter`. All of these, including `MainWindow`, inherit from the abstract `UiPart` class, which captures common behavior among classes that represent visible GUI parts.

The `UI` component uses the JavaFX UI framework. The layouts of these UI parts are defined in matching `.fxml` files in `src/main/resources/view`. For example, [`MainWindow.fxml`](https://github.com/AY2627S1-CS2103T-F15-1/tp/tree/master/src/main/resources/view/MainWindow.fxml) specifies the layout of [`MainWindow`](https://github.com/AY2627S1-CS2103T-F15-1/tp/tree/master/src/main/java/seedu/address/ui/MainWindow.java).

The `UI` component,

* executes user commands using the `Logic` component.
* listens for changes to `Model` data so that the UI can be updated with the modified data.
* keeps a reference to the `Logic` component, because the `UI` relies on the `Logic` to execute commands.
* depends on some classes in the `Model` component because it displays `Person` objects from the model.

### Logic component

**API** : [`Logic.java`](https://github.com/AY2627S1-CS2103T-F15-1/tp/tree/master/src/main/java/seedu/address/logic/Logic.java)

Here's a (partial) class diagram of the `Logic` component:

<puml src="diagrams/LogicClassDiagram.puml" width="550"/>

The sequence diagram below illustrates the interactions within the `Logic` component, taking `execute("delete 1")` API call as an example.

<puml src="diagrams/DeleteSequenceDiagram.puml" alt="Interactions Inside the Logic Component for the `delete 1` Command" />

<box type="info" seamless>

**Note:** The lifeline for `DeleteCommandParser` should end at the destroy marker (X), but due to a limitation of PlantUML, the lifeline continues till the end of diagram.
</box>


How the `Logic` component works:

1. When `Logic` is called upon to execute a command, the command is passed to an `AddressBookParser` object, which in turn creates a parser that matches the command (e.g., `DeleteCommandParser`) and uses it to parse the command.
1. This results in a `Command` object (more precisely, an object of one of its subclasses e.g., `DeleteCommand`) which is executed by the `LogicManager`.
1. The command can communicate with the `Model` when it is executed (e.g. to delete a person).<br>
   Note that although this is shown as a single step in the diagram above for simplicity, the code can require several interactions between the command object and the `Model` to complete the operation.
1. The result of the command execution is encapsulated as a `CommandResult` object which is returned from `Logic`.
1. Around the call, `LogicManager` also keeps a copy of the data for `undo` when the command declares an `undoLabel()`, and saves the data when its saved representation changed. [Undoing destructive commands](#undoing-destructive-commands) gives the details.

Here are the other classes in `Logic` (omitted from the class diagram above) that are used for parsing a user command:

<puml src="diagrams/ParserClasses.puml" width="600"/>

How the parsing works:
* When called upon to parse a user command, the `AddressBookParser` class creates an `XYZCommandParser` (`XYZ` is a placeholder for the specific command name, e.g., `AddCommandParser`). The parser uses the other classes shown above to parse the user command and create an `XYZCommand` object (e.g., `AddCommand`). The `AddressBookParser` returns that object as a `Command` object.
* All `XYZCommandParser` classes, such as `AddCommandParser` and `DeleteCommandParser`, implement the `Parser` interface so they can be treated similarly where appropriate, for example during testing.

### Model component
**API** : [`Model.java`](https://github.com/AY2627S1-CS2103T-F15-1/tp/tree/master/src/main/java/seedu/address/model/Model.java)

<puml src="diagrams/ModelClassDiagram.puml" width="450" />


The `Model` component,

* stores the address book data i.e., all `Person` objects, each of which represents a student with a name, phone number, level, subjects and rate, and optionally an email, a venue and a remark (the `Person` objects are contained in a `UniquePersonList` object). A student can be archived. Two `Person` objects are the same student if they have the same phone number and the same name, ignoring case, but every `Person` also has a `UUID` that never changes and that other records use to refer to the student.
* stores the `Lesson` objects, each of which is a lesson with its own `UUID`, the `UUID` of its student (and a copy of the student's name and phone number), a subject, a date, a start time, a duration, a status, the level of the student when the lesson was booked, and optionally preparation notes, a venue, teaching notes and the `UUID` of a weekly series (the `Lesson` objects are contained in a `UniqueLessonList` object). Deleting a student deletes their lessons, and editing the name or phone number of a student keeps their lessons attached.
* stores the other records of the aggregate in the same `AddressBook`: `Homework` (with a `HomeworkStatus`), `LedgerEntry` events (charges, payments and reversals, in `Money`), `FollowUpTask` objects and the travel buffer in minutes. Each has a `UUID` and, except for the buffer, the `UUID` of its student. Balances, dues, overdue homework and travel warnings are derived by `FinanceQueries`, `HomeworkQueries`, `TaskQueries` and `TravelWarnings` instead of being stored.
* replaces the whole aggregate only through `AddressBook#resetData`, which builds a complete copy, checks that students and lessons are unique, that the travel buffer is in range and that every task has its student and its target, and swaps the copy in only if those checks pass. Homework and ledger entries are copied as they are, so `resetData` is only as safe as its source: `undo` restores a copy that was valid when it was taken.
* holds at most one `UndoSnapshot` (see [Undoing destructive commands](#undoing-destructive-commands)). It is never saved.
* keeps the rows of the last numbered homework, payment and task lists as immutable lists, so a number stays attached to the same record until the next list is shown.
* stores the `Person` objects selected by the current filter, such as search results, in a separate _filtered_ list, and does the same for lessons. It exposes this list as an unmodifiable `ObservableList<Person>` that the UI can observe and bind to, so the UI updates when the list changes.
* stores a `UserPrefs` object that represents the user’s preferences (currently, just the GUI settings). This is exposed to the outside as a `ReadOnlyUserPrefs` object.
* does not depend on any of the other three components (as the `Model` represents data entities of the domain, they should make sense on their own without depending on other components)

### Storage component

**API** : [`Storage.java`](https://github.com/AY2627S1-CS2103T-F15-1/tp/tree/master/src/main/java/seedu/address/storage/Storage.java)

<puml src="diagrams/StorageClassDiagram.puml" width="550" />

The `Storage` component,
* can save both address book data and user preference data in JSON format, and read them back into corresponding objects.
* is implemented by `StorageManager`, which delegates the actual JSON file access to `JsonAddressBookStorage` and `JsonUserPrefsStorage` (one class per data file). `MainApp` supplies a `VersionedAddressBookStorage`, a `JsonAddressBookStorage` that reads the current file `tutorflow-v14.json`, and reads the legacy `addressbook.json` only when the current file is positively absent.
* saves the students, lessons, homework, ledger entries, follow-up tasks and the travel buffer in the same data file. `StorageSchema` defines the supported versions (14 to 17). A file from an older version loads with defaults for the fields it lacks, and a file from a newer version is refused with `UnsupportedSchemaException` before anything is loaded.
* sets aside a data file that cannot be read (`DataFileRecovery`), so that starting afresh never destroys the only copy of the records.
* depends on some classes in the `Model` component (because the `Storage` component's job is to save/retrieve objects that belong to the `Model`)

### Common classes

Classes used by multiple components are in the `seedu.address.commons` package.

--------------------------------------------------------------------------------------------------------------------

## **Implementation**

This section describes some noteworthy details on how certain features are implemented.

### Lesson scheduling, agenda and conflict detection

The lesson commands are `lesson add`, `lesson repeat`, `lesson series cancel`, `lesson list`, `lesson prep`, `lesson move`, `lesson cancel`, `lesson done` and `lesson missed`. They share the `lesson` command word, which `AddressBookParser` hands to `LessonCommandParser`. That parser reads the next word and passes the rest of the arguments to the parser of that command. `agenda` is its own command word.

The sequence diagram below shows `execute("lesson move 1 d/2026-12-24")`.

<puml src="diagrams/LessonMoveSequenceDiagram.puml" alt="Interactions Inside the Logic and Model Components for the `lesson move 1 d/2026-12-24` Command" />

How the pieces fit together:

* **Lesson indices.** `Model#getFilteredLessonList()` returns the lessons that `agenda` or `lesson list` last chose, sorted by date, then start time, then student name. `agenda` and `lesson list` number the lessons they show in that order, and `lesson move` and `lesson cancel` take the same number, so the number a tutor sees is always the one that the next command acts on. `lesson add` keeps the current filter. If the new lesson shifts the numbers that are on screen, the result shows the complete current lesson list with fresh numbers.
* **Overlaps.** `Lesson#overlaps(Lesson)` is true when both lessons are scheduled, are on the same date, and each starts before the other ends. It compares minutes since the start of the day, so back-to-back lessons are not an overlap and a lesson that ends at midnight can still be compared. `Model#getConflictingLessons(Lesson)` returns the other lessons that overlap a lesson, and `lesson add` and `lesson move` append the result to their message as a warning. `agenda` marks every lesson that has at least one overlap.
* **Cancelling.** `Lesson#cancel(String)` returns a copy with the status `CANCELLED` and the reason. The lesson is kept, so it stays in the agenda for billing, but it is no longer scheduled and so it never overlaps.
* **Immutability.** `Lesson` is immutable. `lesson move` and `lesson cancel` build a changed copy and ask the model to replace the old lesson with it, in the same way that `edit` replaces a `Person`.
* **Defaults to type less.** `lesson add` takes the subject from the student when the student has only one, uses 60 minutes if no duration is given, and uses the student's venue if no venue is given.

#### Design considerations

**Aspect: how a lesson refers to its student**

* **Alternative 1 (current choice):** store the student's `UUID` in the lesson, together with a copy of the student's name and phone number.
    * Pros: The saved file stays flat and a lesson can be shown without loading students. The `UUID` tells apart two students who share a name, and later records (homework, charges, tasks) use the same kind of link. A file written before `UUID`s existed is linked by name and phone number when it is loaded.
    * Cons: `AddressBook` must update the copy when it edits or deletes a student. This is done in `setPerson` and `removePerson`, so no command has to remember it.
* **Alternative 2:** store a reference to the `Person` object.
    * Pros: No update is needed when a student is edited.
    * Cons: `Person` is immutable and is replaced on every edit, so the reference would have to be replaced as well, and the saved file would still need an id to write.

**Aspect: what to do when lessons overlap**

* **Alternative 1 (current choice):** warn, but still schedule or move the lesson.
    * Pros: A tutor may double book on purpose, for example for a make-up lesson or two siblings. Blocking would force the tutor to delete a real lesson to record another.
    * Cons: A warning in the result box is easy to miss, so `agenda` also marks overlapping lessons.
* **Alternative 2:** reject the lesson.
    * Pros: No overlaps can exist.
    * Cons: This is overzealous validation and has no workaround.

### Lesson completion and teaching notes

`lesson done INDEX [n/NOTES]` is routed through `LessonCommandParser` to `LessonDoneCommandParser`. The parser validates the lesson index, rejects repeated `n/` prefixes and uses `ParserUtil#parseNotes` to normalise and validate optional notes. `Lesson` validates notes again at the model boundary, and `JsonAdaptedLesson` rejects invalid saved notes with an `IllegalValueException`.

`LessonDoneCommand` uses the current filtered lesson list, rejects cancelled lessons and dates after the local current date, then replaces the lesson with the immutable copy returned by `Lesson#complete`. A `Clock` can be injected into the command to test date boundaries without changing the system clock. Completing a lesson does not reset the active filter, so it can disappear from an upcoming-only list; `lesson list st/INDEX all/` includes completed lessons for later note corrections.

When notes are supplied again on a completed lesson, they replace the existing notes without creating another lesson. Omitting notes preserves them. The existing `LogicManager` save path persists the status and notes before returning success, and `view` includes these notes and the completed-lesson count.

### Missed lessons

`lesson missed INDEX` is routed through `LessonCommandParser` to `LessonMissedCommandParser`. It accepts only a positive lesson index, with no extra parameters. `LessonMissedCommand` resolves this index against the current filtered lesson list, rejects cancelled and completed lessons, and checks that the date is not later than the local current date. Like the completion command, it accepts an injected `Clock` for deterministic date-boundary tests.

The command replaces a scheduled lesson with the immutable copy returned by `Lesson#miss`, which retains all booking details, notes and identity fields. Repeating it on an already-missed lesson returns an informative message without replacing the record. Existing lesson filters, overlap checks, `view` history counts and JSON storage already support `LessonStatus.MISSED`; no new storage format is needed. A missed lesson can disappear from an upcoming-only filter, but remains accessible with `lesson list st/INDEX all/`. The existing `lesson done` command can correct a missed status to completed. `lesson move` rejects a missed lesson, because the moved copy would keep `MISSED` on its new date and the record of the missed slot would be lost. A make-up lesson is scheduled with `lesson add` instead.

`LessonMissedIntegrationTest` exercises command parsing, status changes, student history and JSON reload together. It also verifies that invalid arguments, invalid indices and rejected status/date changes leave the saved file unchanged.

### Undoing destructive commands

`undo` restores the data as it was before the latest destructive command of the session. It is deliberately one step deep and lives only in memory: a tutor who deletes the wrong student needs one reliable way back, not a history that can silently erase newer work.

#### Implementation

The sequence diagram below shows a destructive command followed by `undo`.

<puml src="diagrams/UndoSequenceDiagram.puml" alt="Interactions between Logic, Model and Storage for a destructive command followed by undo" />

The `Model` holds an optional `UndoSnapshot` (a label such as "student deletion" and a complete private copy of the `AddressBook`). `Model#setUndoSnapshot`, `Model#getUndoSnapshot` and `Model#clearUndoSnapshot` manage it, and it is never written to the data file.

A command declares that it is destructive by overriding `Command#undoLabel()`, which is empty by default. `DeleteCommand` (student deletion), `ClearCommand`, `HomeworkDeleteCommand` and `TaskDeleteCommand` return a label. The decision about what to keep lives in `LogicManager#execute`, because it already compares the saved representation before and after every command:

1. Before the command runs, `LogicManager` makes a copy of the data only if the command has an undo label.
2. After it runs, if the saved representation did not change (a read-only view, a preview that asks for `confirm`, or a rejected command), the existing snapshot is left alone.
3. If the data changed and the command had a label, the copy becomes the new snapshot. If it changed and the command had no label, the snapshot is cleared, so an unrelated edit can never be rolled back over by an old `undo`. `UndoCommand` is the exception because it consumes the snapshot itself.
4. Only then does `LogicManager` try to save. A failed save therefore never loses the snapshot, and `undo` still works after the deletion that could not be saved.

`UndoCommand` restores the snapshot with `Model#setAddressBook`, which is `AddressBook#resetData`. That method builds a complete scratch aggregate first, checks students and lessons for duplicates, the travel buffer for its range and every task for its links, and swaps it in only on success. Homework and ledger entries are copied without such checks, which is safe here because the snapshot was a copy of valid data. Students, lessons, homework, payments and charges, tasks and the travel buffer return together with their original UUIDs. The command then clears the snapshot, which is why a second `undo` is rejected, and `LogicManager` saves the restored data as for any other change. If that save fails, the restored data stays in the session and is flagged unsaved.

Numbered selections need no special handling. Homework, payment and task rows hold UUIDs that are resolved against the current data on use, so a row that pointed at a deleted record becomes valid again once the record is restored, and never points at a different one.

#### Design considerations

**Aspect: how much history to keep:**

* **Alternative 1 (current choice):** One snapshot, invalidated by any other change.
  * Pros: Cannot overwrite newer work; trivial to reason about; memory is one copy of the data, and the records themselves are immutable and shared.
  * Cons: Only the latest destructive command can be undone, and there is no redo.

* **Alternative 2:** A history of states with a pointer, as in the AddressBook-Level3 proposal.
  * Pros: Several undo and redo steps.
  * Cons: Every mutating command must commit, redo semantics must be defined for the financial history, and a bug in one command's commit silently breaks the chain.

**Aspect: who decides when to capture:**

* **Alternative 1 (current choice):** Commands only declare a label; `LogicManager` captures and invalidates.
  * Pros: The rule is in one place, it uses the same change detection as saving, and a new command cannot forget to invalidate the snapshot.
  * Cons: A command executed directly against a `Model` in a test does not create a snapshot.

* **Alternative 2:** Each destructive command records its own snapshot.
  * Pros: Easy to test a single command.
  * Cons: Invalidation by unrelated commands would still have to be added to every other mutating command.

--------------------------------------------------------------------------------------------------------------------

## **Documentation, logging, testing, dev-ops**

* [Documentation guide](Documentation.md)
* [Testing guide](Testing.md)
* [Logging guide](Logging.md)
* [DevOps guide](DevOps.md)

--------------------------------------------------------------------------------------------------------------------

## **Appendix: Requirements**

### Product scope

**Target user profile**:

* is an independent private tutor who teaches multiple students (around 20)
* teaches at several venues (students' homes, cafés and online), often with short travel gaps between lessons
* currently tracks lessons, homework, lesson notes and fees across calendars, chat apps, notebooks and spreadsheets
* prefers desktop apps over other types of applications, and works from a laptop between lessons
* can type fast and prefers typing to mouse interactions
* is reasonably comfortable using CLI apps
* is unwilling to spend a lot of time migrating existing data

**Value proposition**: TutorFlow gives a private tutor one place to organise and access information about their students, lessons, homework and payments. It answers "what does today require, what does each student need, and who still owes me" fast enough to use in the fifteen minutes between two lessons, which is faster than tracking the same information across spreadsheets, chats, calendars and personal notes.


### User stories

Priorities: High (must have) - `* * *`, Medium (nice to have) - `* *`, Low (unlikely to have) - `*`

**Student management**

| Priority | As a …                                    | I want to …                                                                                          | So that I can…                                                                               |
|----------|--------------------------------------------|-------------------------------------------------------------------------------------------------------|-----------------------------------------------------------------------------------------------|
| `* * *`  | private tutor                              | create a student profile with contact details, subjects, academic level and lesson rate               | keep the information needed to teach them in one place                                        |
| `* * *`  | private tutor                              | view a student overview with their next lesson, outstanding homework, recent lesson notes and unpaid balance | prepare for the student without checking multiple sections                             |
| `* * *`  | private tutor                              | search for students by name                                                                           | access a student's record quickly                                                             |
| `* *`    | private tutor                              | filter students by subject or academic level                                                          | find relevant groups of students                                                              |
| `* * *`  | private tutor                              | update a student's profile                                                                            | keep their information accurate when their circumstances change                               |
| `*`      | private tutor                              | archive a student who has stopped taking lessons                                                      | keep them out of my active list while their teaching and payment history remains available    |
| `* * *`  | private tutor                              | be warned about a possible duplicate when creating a student                                          | avoid maintaining multiple records for the same person                                        |

**Lesson scheduling**

| Priority | As a …                                    | I want to …                                                                                          | So that I can…                                                                               |
|----------|--------------------------------------------|-------------------------------------------------------------------------------------------------------|-----------------------------------------------------------------------------------------------|
| `* * *`  | private tutor                              | schedule a lesson with its student, subject, date, time, duration and venue                           | have all the information needed to conduct the lesson                                         |
| `* *`    | private tutor                              | schedule recurring lessons                                                                            | avoid entering regular weekly lessons individually                                            |
| `* * *`  | private tutor                              | view my lessons in a daily or weekly agenda                                                           | plan my teaching schedule and travel                                                          |
| `* * *`  | private tutor                              | view all upcoming lessons for a specific student                                                      | review my commitments with them                                                               |
| `* * *`  | private tutor                              | reschedule a lesson while retaining its existing details                                              | record timetable changes efficiently                                                          |
| `* * *`  | private tutor                              | cancel a lesson and record its cancellation status                                                    | keep my schedule and payment records accurate                                                 |
| `* * *`  | private tutor                              | be warned when a lesson conflicts with another commitment                                             | avoid double-booking myself                                                                   |
| `*`      | travelling tutor                           | see consecutive lessons with insufficient travel time between their venues                            | identify schedules that may be impractical                                                    |
| `* * *`  | private tutor                              | mark a lesson as completed, cancelled or missed                                                       | have an accurate record of what happened                                                      |
| `* * *`  | private tutor                              | record preparation notes before a lesson and teaching notes afterward                                 | continue effectively from one lesson to the next                                              |

**Homework management**

| Priority | As a …                                    | I want to …                                                                                          | So that I can…                                                                               |
|----------|--------------------------------------------|-------------------------------------------------------------------------------------------------------|-----------------------------------------------------------------------------------------------|
| `* *`    | private tutor                              | assign homework to a student with a description and due date                                          | know what the student is expected to complete                                                 |
| `* *`    | private tutor                              | view all outstanding and overdue homework across my students                                          | identify which students require follow-up                                                     |
| `* *`    | private tutor                              | update homework as assigned, submitted, reviewed or overdue                                           | see its current status at a glance                                                            |
| `* *`    | private tutor                              | edit an assignment's details                                                                          | correct mistakes or accommodate changes                                                       |
| `* *`    | private tutor                              | record feedback on submitted homework                                                                 | remember the issues to discuss during the next lesson                                         |
| `*`      | private tutor                              | view a student's homework history                                                                     | identify patterns in their completion and progress                                            |

**Payments**

| Priority | As a …                                    | I want to …                                                                                          | So that I can…                                                                               |
|----------|--------------------------------------------|-------------------------------------------------------------------------------------------------------|-----------------------------------------------------------------------------------------------|
| `* *`    | private tutor                              | record the fee charged for each completed lesson                                                      | calculate what each student owes                                                              |
| `* *`    | private tutor                              | record a payment and associate it with the relevant student                                           | keep their balance accurate                                                                   |
| `* *`    | private tutor                              | view each student's charges, payments and current balance                                             | answer payment questions clearly                                                              |
| `* *`    | private tutor                              | view all outstanding payments ordered by how long they have been unpaid                               | prioritise payment follow-ups                                                                 |
| `* *`    | private tutor                              | correct or reverse an incorrectly entered payment                                                     | prevent errors from permanently distorting my records                                         |
| `*`      | private tutor                              | export payment records for a selected period                                                          | use them for accounting and tax reporting                                                     |

**Daily workflow**

| Priority | As a …                                    | I want to …                                                                                          | So that I can…                                                                               |
|----------|--------------------------------------------|-------------------------------------------------------------------------------------------------------|-----------------------------------------------------------------------------------------------|
| `* * *`  | private tutor                              | view a dashboard showing today's lessons, overdue homework and outstanding payments                   | know what requires my attention                                                               |
| `*`      | private tutor                              | create follow-up tasks linked to students, lessons, homework or payments                              | make sure important actions are not forgotten                                                 |
| `* *`    | private tutor                              | undo a recent destructive action                                                                      | recover from an accidental change                                                             |
| `* * *`  | new user                                   | access in-app guidance and examples                                                                   | learn TutorFlow without relying on external documentation                                     |
| `*`      | new user                                   | experiment with sample records and remove them when I am ready                                        | learn the application before entering real information                                        |
| `*`      | private tutor moving from another tracking system | import student records from a common file format                                               | avoid re-entering all my existing information manually                                        |

### Use cases

(For all use cases below, the **System** is `TutorFlow` and the **Actor** is the `tutor`, unless specified otherwise)

**Use case: UC1 - Add a student**

**MSS**

1.  Tutor requests to add a student, providing the student's name, phone number, level, subject(s) and lesson rate, and optionally an email and usual venue
2.  TutorFlow adds the student and confirms the addition

    Use case ends.

**Extensions**

* 1a. A required detail is missing or invalid.

    * 1a1. TutorFlow shows an error message stating which detail is wrong and the accepted format.

      Use case resumes at step 1.

* 1b. A student with the same name and phone number already exists.

    * 1b1. TutorFlow rejects the request and tells the tutor that the student already exists.

      Use case ends.

* 1c. A student with the same name but a different phone number already exists.

    * 1c1. TutorFlow adds the student and warns the tutor that another student has the same name.

      Use case ends.

* 2a. The data cannot be saved to disk.

    * 2a1. TutorFlow tells the tutor that the change was applied in this session but could not be saved.

      Use case ends.

**Use case: UC2 - Schedule a lesson**

**MSS**

1.  Tutor requests to list students
2.  TutorFlow shows a list of students
3.  Tutor requests to schedule a lesson for a specific student, providing the subject, date, time and duration, and optionally a venue
4.  TutorFlow adds the lesson and shows its details

    Use case ends.

**Extensions**

* 2a. The list is empty.

  Use case ends.

* 3a. The given student does not exist.

    * 3a1. TutorFlow shows an error message.

      Use case resumes at step 2.

* 3b. A lesson detail is invalid, for example a date in the past or a lesson that would run past midnight.

    * 3b1. TutorFlow shows an error message stating which detail is wrong.

      Use case resumes at step 3.

* 3c. The subject is not one of the subjects the student is recorded as taking.

    * 3c1. TutorFlow shows an error message.

      Use case resumes at step 3.

* 3d. An identical lesson already exists.

    * 3d1. TutorFlow rejects the request and tells the tutor that the lesson already exists.

      Use case ends.

* 4a. The new lesson overlaps another scheduled lesson.

    * 4a1. TutorFlow keeps the newly scheduled lesson and warns the tutor of the overlapping lesson.

      The warning does not reject the scheduling request. See **Overlap** in the glossary for the conflict boundary; back-to-back lessons do not overlap.

      Use case ends.

**Use case: UC3 - Reschedule a lesson**

**MSS**

1.  Tutor requests to view the lessons of a specific student
2.  TutorFlow shows a numbered list of the student's lessons
3.  Tutor requests to reschedule a specific lesson, providing the new date, time, duration and/or venue
4.  TutorFlow updates the lesson, keeping the details that were not changed, and shows the old and new details

    Use case ends.

**Extensions**

* 2a. The student has no lessons.

  Use case ends.

* 3a. The given lesson does not exist in the list.

    * 3a1. TutorFlow shows an error message.

      Use case resumes at step 2.

* 3b. The lesson is already completed or cancelled.

    * 3b1. TutorFlow tells the tutor that the lesson can no longer be rescheduled.

      Use case ends.

* 3c. The new date or time is invalid, for example a date in the past.

    * 3c1. TutorFlow shows an error message stating which detail is wrong.

      Use case resumes at step 3.

* 4a. The new time overlaps another scheduled lesson.

    * 4a1. TutorFlow keeps the updated lesson details and warns the tutor of the overlapping lesson.

      The warning does not undo the rescheduling request. Only scheduled lessons are considered for overlap, as defined in the glossary.

      Use case ends.

**Use case: UC4 - Record the outcome of a lesson**

**MSS**

1.  Tutor requests to view the agenda for a day
2.  TutorFlow shows the lessons on that day
3.  Tutor requests to mark a specific lesson as completed, and provides teaching notes
4.  TutorFlow marks the lesson as completed, saves the notes and confirms the update

    Use case ends.

**Extensions**

* 2a. There are no lessons on that day.

  Use case ends.

* 3a. The given lesson does not exist in the list.

    * 3a1. TutorFlow shows an error message.

      Use case resumes at step 2.

* 3b. The lesson is cancelled, or is scheduled for a future date.

    * 3b1. TutorFlow tells the tutor that the lesson cannot be marked as completed.

      Use case ends.

* 3c. The lesson is already completed.

    * 3c1. TutorFlow replaces the existing notes with the new notes and confirms the update.

      Use case ends.

* 3d. The tutor requests to mark the lesson as missed instead.

    * 3d1. TutorFlow marks the lesson as missed and confirms the update.

      Use case ends.

**Use case: UC5 - Assign homework and record feedback**

**MSS**

1.  Tutor requests to assign homework to a specific student, providing a description and a due date
2.  TutorFlow records the homework as assigned and confirms the addition
3.  Tutor requests to mark the homework as submitted
4.  TutorFlow updates the status of the homework
5.  Tutor requests to record feedback on the homework
6.  TutorFlow saves the feedback and confirms the update

    Use case ends.

**Extensions**

* 1a. The description is empty or the due date is invalid or in the past.

    * 1a1. TutorFlow shows an error message stating which detail is wrong.

      Use case resumes at step 1.

* 1b. The student already has identical homework with the same due date.

    * 1b1. TutorFlow rejects the request and tells the tutor that the homework already exists.

      Use case ends.

* 3a. The given homework does not exist.

    * 3a1. TutorFlow shows an error message.

      Use case resumes at step 3.

* 5a. The homework has not been submitted.

    * 5a1. TutorFlow tells the tutor that feedback can only be recorded on submitted homework.

      Use case resumes at step 3.

**Use case: UC6 - Record a payment and check a student's balance**

**MSS**

1.  Tutor requests to record a payment from a specific student, providing the amount and optionally the date and notes
2.  TutorFlow records the payment against the student and shows the student's updated balance
3.  Tutor requests to view the student's account
4.  TutorFlow shows the student's charges, payments and running balance

    Use case ends.

**Extensions**

* 1a. The student does not exist.

    * 1a1. TutorFlow shows an error message.

      Use case resumes at step 1.

* 1b. The amount is not a positive number or the date is in the future.

    * 1b1. TutorFlow shows an error message stating which detail is wrong.

      Use case resumes at step 1.

* 2a. The payment is larger than the amount the student owes.

    * 2a1. TutorFlow records the payment and shows that the student is in credit.

      Use case resumes at step 3.

**Use case: UC7 - Delete a student**

**MSS**

1.  Tutor requests to list students
2.  TutorFlow shows a list of students
3.  Tutor requests to delete a specific student in the list
4.  TutorFlow tells the tutor which lessons, homework and payment records will be deleted along with the student, and asks for confirmation
5.  Tutor confirms the deletion
6.  TutorFlow deletes the student and all of their records, and confirms the deletion

    Use case ends.

**Extensions**

* 2a. The list is empty.

  Use case ends.

* 3a. The given index is invalid.

    * 3a1. TutorFlow shows an error message.

      Use case resumes at step 2.

* 4a. The student has no lessons, homework or payments.

    * 4a1. TutorFlow deletes the student without asking for confirmation.

      Use case ends.

* 5a. The tutor does not confirm the deletion.

    * 5a1. TutorFlow keeps the student and all of their records.

      Use case ends.

**Use case: UC8 - Prepare for the day and for a lesson**

**MSS**

1.  Tutor opens TutorFlow
2.  TutorFlow loads the saved data and shows the dashboard, listing today's lessons, lessons awaiting notes, overdue homework and outstanding payments
3.  Tutor requests to view the overview of a specific student
4.  TutorFlow shows the student's details, next lesson, recent lesson notes, outstanding homework and unpaid balance

    Use case ends.

**Extensions**

* 2a. There is no saved data.

    * 2a1. TutorFlow starts with an empty data set and shows a welcome message.

      Use case ends.

* 2b. The saved data cannot be read.

    * 2b1. TutorFlow keeps the unreadable file aside, starts with an empty data set and warns the tutor.

      Use case ends.

* 3a. The given student does not exist.

    * 3a1. TutorFlow shows an error message.

      Use case resumes at step 3.

### Non-Functional Requirements

1.  Should work on any _mainstream OS_ as long as it has Java `25` or above installed.
2.  Should work fully offline, and should not make any network connections.
3.  Should be able to hold up to 200 students and 10,000 lessons without noticeable sluggishness in performance for typical usage.
4.  Searching, listing and viewing commands should show their results within 1 second on a typical laptop with the data set size in NFR 3.
5.  Should start up and show the dashboard within 5 seconds on a typical laptop with the data set size in NFR 3.
6.  A user with above average typing speed for regular English text (i.e. not code, not system admin commands) should be able to accomplish most of the tasks faster using commands than using the mouse.
7.  Should save every change to disk before reporting that the command succeeded, so that no data is lost when the application is closed normally or when it is terminated after a command has completed.
8.  Should validate all user input before storing it, and should reject invalid dates, times, amounts and contact details with a message that names the invalid field and the accepted format.
9.  Should ask for confirmation before permanently deleting a student who has lessons, homework or payments, and before clearing all data.
10. Should store all data only in a local file on the user's computer, and should not write students' contact details or fees to log files.
11. Should store data in a human-readable text format that is documented in the Developer Guide, so that a tutor can back up the data by copying the data file.
12. Should be able to load any data file saved by an earlier version of the same major version.
13. Should be usable on screens with a resolution of 1280x720 and higher.
14. Is for a single user only, and does not need to support several users editing the same data file.

### Glossary

* **Mainstream OS**: Windows, Linux, Unix, or macOS
* **Tutor**: The user of TutorFlow, an independent private tutor who teaches multiple students
* **Student**: A person the tutor teaches, recorded with contact details, academic level, subjects, lesson rate and an optional usual venue
* **Level**: A student's academic level in the Singapore education system: `P1` to `P6` (primary), `S1` to `S5` (secondary) or `J1` to `J2` (junior college)
* **Rate**: The amount a student is charged for one hour of lesson, in Singapore dollars (SGD)
* **Duplicate student**: A student with the same name and phone number as another student, after ignoring letter case and extra spaces
* **Lesson**: A teaching session with one student, with a subject, date, start time, duration and venue
* **Lesson status**: The state of a lesson: _scheduled_ (has not happened yet), _completed_ (took place), _cancelled_ (called off in advance) or _missed_ (the student did not attend)
* **Overlap**: Two scheduled lessons on the same date whose time ranges share at least one minute. Lessons that end exactly when the next one starts do not overlap
* **Agenda**: A view of the tutor's lessons in time order for a day or a week
* **Dashboard**: The view shown on startup that lists what needs the tutor's attention today, such as today's lessons, lessons awaiting notes, overdue homework and outstanding payments
* **Student overview**: A single view of a student's details, next lesson, recent lesson notes, outstanding homework and unpaid balance
* **Lesson notes**: Free text that the tutor records about a lesson, such as what was covered and what the student needs to improve
* **Homework status**: The state of a homework item: _assigned_, _submitted_ or _reviewed_. Homework that is still assigned after its due date is _overdue_
* **Charge**: The fee for one completed lesson, calculated from the student's rate and the lesson duration unless the tutor specifies another amount
* **Payment**: Money received from a student, recorded against the student rather than against a particular lesson
* **Balance**: The total charges minus the total payments of a student. A positive balance is _outstanding_, and a negative balance means the student is _in credit_
* **Index**: The position of an item in the list currently shown on screen, used by commands to refer to that item
* **Prefix**: A short label ending in `/`, such as `n/` or `d/`, that marks the start of a command parameter
* **Data file**: The local file in which TutorFlow saves all of its data

--------------------------------------------------------------------------------------------------------------------

## **Appendix: Instructions for manual testing**

Given below are instructions to test the app manually.

<box type="info" seamless>

**Note:** These instructions only provide a starting point for testers to work on;
testers are expected to do more *exploratory* testing.
</box>

### Launch and shutdown

1. Initial launch

   1. Download the JAR file and copy it into an empty folder.

   1. Double-click the JAR file.<br>
      Expected: The GUI opens empty with welcome and demo instructions. The window size may not be optimal.

1. Saving window preferences

   1. Resize the window to an optimal size. Move the window to a different location. Close the window.

   1. Relaunch the app by double-clicking the JAR file.<br>
       Expected: The most recent window size and location are retained.


### Deleting a person

1. Deleting a person while all persons are being shown

   1. Prerequisites: List all persons using the `list` command, with multiple persons in the list.

   1. Test case: `delete 1`<br>
      Expected: The first contact is deleted from the list. The status message shows the deleted contact's details.

   1. Test case: `delete 0`<br>
      Expected: No person is deleted. The status message shows error details.

   1. Other incorrect delete commands to try: `delete`, `delete x`, `...` (where x is larger than the list size)<br>
      Expected: Similar to previous.


### Lessons

1. Scheduling a lesson

   1. Prerequisites: Use `list`, with at least two students in the list. The 1st student takes one subject and the 2nd student takes more than one.

   1. Test case: `lesson add st/1 d/<a date next week> t/16:30 dur/90`<br>
      Expected: A lesson is scheduled in the student's only subject at the student's venue. The result shows `Scheduled: ...`.

   1. Test case: the same command again<br>
      Expected: The lesson is rejected as an exact duplicate.

   1. Test case: `lesson add st/2 d/<the same date> t/17:00` (without `s/`)<br>
      Expected: Rejected, because the student takes several subjects. The result names the subjects.

   1. Test case: `lesson add st/2 s/<a subject of the student> d/<the same date> t/17:00`<br>
      Expected: Scheduled, and the result warns that it overlaps the first lesson.

   1. Other incorrect commands to try: `lesson add st/1 d/2020-01-01 t/16:30` (past date), `lesson add st/1 d/<next week> t/23:00 dur/120` (past midnight), `lesson add st/0 d/<next week> t/16:30`, `lesson add st/1`<br>
      Expected: Nothing is scheduled. The result shows an error message.

1. Viewing lessons

   1. Prerequisites: Schedule the two lessons above.

   1. Test case: `agenda d/<the same date>`<br>
      Expected: Both lessons are listed in time order, each marked `⚠ overlaps`.

   1. Test case: `agenda week/ d/<the same date>`<br>
      Expected: Seven days are shown, and days without lessons show `— no lessons —`.

   1. Test case: `agenda week/yes`<br>
      Expected: An error that the flag takes no value.

   1. Test case: `lesson list st/1`<br>
      Expected: The upcoming lessons of the 1st student are listed and numbered.

1. Rescheduling and cancelling a lesson

   1. Prerequisites: Run `agenda` for the date of the lessons, so that the lessons are numbered.

   1. Test case: `lesson move 1 t/20:00`<br>
      Expected: The 1st lesson moves to 20:00 and keeps its subject and venue. The result shows the old and the new slot.

   1. Test case: `lesson move 1`<br>
      Expected: An error that at least one of `d/`, `t/`, `dur/` or `v/` is needed.

   1. Test case: `lesson cancel 1 r/Student unwell`<br>
      Expected: The lesson is cancelled with its reason. Run `agenda` again to see it listed as cancelled and no longer overlapping.

   1. Test case: `lesson cancel 1` again, then `lesson move 1 d/<next week>`<br>
      Expected: Both are rejected because the lesson is already cancelled.

   1. Test case: `lesson cancel 99`<br>
      Expected: An error that the lesson index is invalid.

1. Completing a lesson

   1. Prerequisites: Schedule a lesson dated today, then run `agenda` and note its lesson number.

   1. Test case: `lesson done INDEX n/Covered algebra`<br>
      Expected: `Completed: ...`. Run `view STUDENT_INDEX` to see the notes and completed count. Reopen the app and check they are still present.

   1. Test case: Run `lesson list st/STUDENT_INDEX all/`, check the current lesson number, then run `lesson done INDEX n/Revised notes` followed by `lesson done INDEX`<br>
      Expected: Notes change to `Revised notes`, then remain unchanged. No extra lesson is created.

   1. Other incorrect commands to try: completion of a future or cancelled lesson, `lesson done 0`, `lesson done INDEX n/`, or `lesson done INDEX n/First n/Second`<br>
      Expected: Rejected without changing the lesson or notes.

### Saving data

1. Dealing with missing/corrupted data files

   1. Prerequisites: Close TutorFlow, then open `data/tutorflow-v14.json` and damage it, for example by deleting its last `}`.

   1. Test case: Launch TutorFlow.<br>
      Expected: TutorFlow starts with an empty list. The result box says that the data file could not be read and has been set aside as `data/tutorflow-v14.json.corrupt`, and the damaged file is in the `data` folder under that name.

   1. Test case: Add a student with `add n/Test p/91234567 l/S4 s/Math r/50`, close TutorFlow and launch it again.<br>
      Expected: The student is still there, because it was saved to a new `tutorflow-v14.json`. The `.corrupt` file is unchanged.

   1. Test case: Close TutorFlow, delete `data/tutorflow-v14.json` and launch TutorFlow.<br>
      Expected: With no legacy addressbook.json either, TutorFlow starts empty. Use demo confirm to load sample students.

1. Dealing with a data file that cannot be written

   1. Prerequisites: Make `data/tutorflow-v14.json` read-only, as a user who is not an administrator.

   1. Test case: Run any command, such as `list`.<br>
      Expected: The result box says that the change was applied in this session but could not be saved, and why. `exit` does not close TutorFlow until the file can be written again.


### Undoing a deletion

1. Undoing a deletion

   1. Prerequisites: At least two students are listed.

   1. Test case: `delete 1` (add `confirm` if TutorFlow asks for it), then `undo`<br>
      Expected: The student, with their lessons and other records, is back. A second `undo` is rejected.

   1. Test case: `delete 1`, then `remark 1 r/Changed`, then `undo`<br>
      Expected: `undo` is rejected, because another change happened after the deletion.

### v1.4 continuity and domain extension contract

`Person.getId()` and `Lesson.getId()` are persisted UUIDs, distinct from visible indices and
from value equality. `Lesson.getStudentId()` links records across profile edits. Legacy lessons
are linked during loading; their missing academic level remains `Optional.empty()`. Newly
scheduled lessons use `snapshotStudent` exactly at creation. Reschedule, status changes,
preparation, profile edits, aggregate copying, and JSON round trips retain the historical level.
Archive and preparation participate in value equality, as does academic level; UUIDs do not.
Equal cancelled lessons remain separate records and exact-ID lookup chooses the edited record.

All domain extensions must update `ReadOnlyAddressBook`, `AddressBook.resetData` and its copy
constructor/equality, the real JSON root/adapters, and focused round-trip tests together.
`Storage.snapshotAddressBook` uses that exact persisted representation for dirty detection,
so UI-only equality must never decide whether a write is required. The current schema is written to
`tutorflow-v14.json`; `VersionedAddressBookStorage` reads legacy data only when the current
path is positively absent. The logic constructor's initial-save flag explicitly persists
migrated IDs even before a command mutates the model. Failed initial writes remain dirty.
The atomic writer creates only parent directories before its sibling temporary file. It never
precreates the current destination, so an interrupted first save leaves legacy fallback available
on restart. The storage writeSnapshot seam passes the actual serialized bytes to this writer.
Unreadable source protection is retained until positive absence or successful preservation.

`StorageSchema.MINIMUM_VERSION` and `CURRENT_VERSION` centralize the supported range (14 through 17 now).
The shared `tutorflow-v14.json` path stays fixed as later domains are added. Homework uses
schema 15; finance uses 16, and recurrence, travel and tasks use 17. Each build accepts unversioned legacy
data and versions 14 through its current version, with explicit defaults for missing older fields.
Add prior-version loading and future-version refusal tests alongside each schema increment.

Storage checks the raw JSON schema before binding current student/lesson fields. An unsupported
version produces `UnsupportedSchemaException` as the cause of `DataLoadingException`; `MainApp.init`
propagates it before constructing a model, logic, or UI. The original stays at its original path,
and storage refuses subsequent saves. Never route this exception through corruption recovery or
silently ignore newer domain fields. Users need a compatible binary or a matching backup.

The command-result seam for numbered lessons is
`CommandResult.getDisplayedLessons()`: when present, it is an immutable typed snapshot in the
same order as rendered lesson numbers. `withDisplayedLessons` and `withFeedback` retain it
across numbering refreshes and unsaved warnings. The logic layer uses this data to avoid
rendering duplicate refreshes; it does not parse command feedback.
Later homework/payment/task lists must likewise snapshot shown stable IDs or visibly refresh
the numbered list when a mutation changes the mapping, including when persistence fails.


### Homework domain and integration seams

`Homework` is immutable and persists its UUID, student UUID, normalized description, due date,
`HomeworkStatus`, and optional feedback. Its full equality includes identity and all persisted
fields. Overdue is computed from `ASSIGNED` and the caller's local date, never stored as a fourth
status. `HomeworkQueries` centralizes selection, ordering, current student labels, and summaries.
`HomeworkCommandParser(Clock)` makes date bounds and `today`/`tmr` deterministic. New assignments
and changed due dates use today through one calendar year; unchanged historical dates are retained.

`AddressBook` owns the homework list and validates duplicate assignments/IDs and student links.
Copy/reset/equality/hash, student cascade deletion, clear, and the real JSON adapter include the
domain. Missing homework in older files defaults explicitly to empty. Schema validation still
precedes field binding. `Storage.snapshotAddressBook` includes every homework field, so feedback,
status, and description-only changes participate in dirty detection and retry.

`Model.showHomework(List<Homework>)` stores an immutable snapshot only when `hw list` renders a
new numbered list. `getDisplayedHomework()` retains those rows even after mutations or deletion;
it must never be treated as the current database. Indexed commands take the shown row's UUID and
resolve it against `getAddressBook().getHomeworkList()`. A deleted ID produces an error without
renumbering remaining rows. Changes, archive, student rename, reset, and failed writes leave the
selection untouched. A restored snapshot with the same identity can be selected again after undo.
A future linked task must resolve homework indices by this same shown-ID rule, then persist UUIDs.

`CommandResult.getDisplayedHomework()` exposes optional immutable typed rows for the numbered
list. `withFeedback` retains them across unsaved warnings. Non-indexed overview/dashboard
summaries deliberately do not replace the selection, and no feedback-string parsing is required.
Finance can extend summaries independently. Undo snapshots copy the complete `AddressBook`, including
homework, and the task-parent guards wrap homework and student deletion so that no task is left without
its parent.

Focused regression tests cover historical feedback, schema progression and refusal, duplicate/orphan
rejection, due-date boundaries, lifecycle correction, guarded deletion, filtered numbering, archive,
rename, actual JSON restart, failed saves, protected original bytes, and retry before exit.


### Finance domain and integration seams

`LedgerEntry` is an immutable event record with UUID, student UUID, type, date, and exact BigDecimal
amount. CHARGE stores lesson UUID, historical hourly rate, duration, subject description, and whether
an override was used. PAYMENT stores optional notes. REVERSAL points to an earlier original event
and carries its exact positive amount; it never replaces or deletes the original event. Root append
validation rejects duplicate IDs, dangling or cross-student links, multiple active charges for one
lesson UUID, reversal of reversals, and repeated reversals. Identical payments with distinct IDs are valid.

`Ledger` is the shared accounting service: chronological account effects, active totals, balance,
and oldest-charge-first allocation. Dates use stable persisted append order as their tie breaker.
All active payments form available credit for FIFO allocation across active charges. Dues order by
oldest *remaining* charge after allocation; account history retains original movements and dated
compensating reversals. `FinanceQueries` formats complete account/dues summaries without modifying
any selection. Totals after reversals, running balance, and unpaid allocation agree.

`Money.charge` multiplies the exact rate by minutes, then divides by 60 with HALF_UP scale 2.
Input amounts must already have at most two decimals and be positive; no floating-point arithmetic
or silent input rounding is used. A zero computed charge creates no financial event, with explicit
completion feedback. First completion snapshots a charge only if there is no charge history for the
actual lesson UUID; repeated completions, legacy completed lessons, and note correction never bill
again. Manual recharging after an explicit reversal is permitted. Active charge reversal is required
before cancellation or rescheduling. A lesson with financial history cannot be deleted in isolation.

`ReadOnlyAddressBook.getLedgerEntries`, `AddressBook` copy/reset/equality/hash, and JSON adapters
include every financial event. Schema 16 defaults missing legacy/14/15 finance to empty; it never
infers historical prices for existing completed lessons. Raw schema preflight/startup refusal,
atomic parent-only writes, migration retry, dirty detection via exact `Storage.snapshotAddressBook`,
and failed-exit protections remain intact. Student deletion and clear are explicit confirmed parent
cascades; archive preserves all obligations. Future task-parent guards must run before these cascades.

`Model.showPayments` / `getDisplayedPayments` hold immutable last-shown PAYMENT rows only. They stay
fixed across mutations, resets, reversals, archive, rename, and save failure. Resolve a shown UUID
against current ledger data before acting. Deleted IDs fail without shifting later rows; a restored
snapshot with that identity can be resolved again after undo. `CommandResult.getDisplayedPayments`
is an optional typed immutable snapshot, preserved by `withFeedback`,
`withDisplayedLessons`, and `withDisplayedHomework`. Do not parse feedback strings. Only `pay list`
sets this selection; account/dues/overview/dashboard remain unnumbered. Task links should resolve
`pay/INDEX` by this same shown-ID rule and store the original payment UUID, including after reversal.

Payment export can select PAYMENT originals by their inclusive date range and locate compensation
with `reversalOf`; retain original/reversal identities and dates in export metadata. Do not export
only active payments, which would hide corrections. Undo must snapshot the complete AddressBook
and its ledger; payment/charge corrections themselves remain explicit reversals. `DateParser.parse`
provides clock-injectable today/tmr aliases now shared by homework and payments; later command date
unification can reuse it. No new dependencies or unused UI components were introduced.

### Finite recurrence (schema 17)

Each recurring occurrence is a full lesson with its own UUID and a shared
optional series UUID. Immutable lesson transformations preserve that link.
Older schemas default to no series. Recurrence and series cancellation build
a complete proposed AddressBook, validating duplicates, date limits, and
financial guards before installing it once. No scheduler or network is
required. Typed visible lesson rows are refreshed after a batch.

### Travel settings and warnings

AddressBook owns the persisted travelBufferMinutes setting; schema 16 and
earlier default it to 30. Copy/reset/equality/hash and exact JSON save
snapshots include it. TravelWarnings compares chronological adjacent
scheduled lessons using full local date-times and duration, including
midnight boundaries. It limits presentation to pairs touching relevant
visible or changed lessons; it makes no network calls.

### Follow-up task lifecycle (schema 17)

FollowUpTask is immutable: UUID id, UUID studentId, LinkType, UUID targetId,
printable description, optional LocalDate dueDate, and boolean done. The root
validates student ownership, target existence, unique task IDs, and PAYMENT
originals. Content/status edits retain identity and link. Archive and reversal
retain tasks; student deletion cascades every task for that student, and
individual homework/uncharged-lesson deletion cascades direct task links.

Model.showTasks/getDisplayedTasks retain immutable last-numbered rows;
TaskCommandSupport resolves their UUID against the current root before any
action. Adds, edits, completion, reset, and deletions do not renumber that
selection. TaskQueries provides complete filtered queries and live linked
history rendering. CommandResult.getDisplayedTasks is typed immutable data
and survives withFeedback and the other typed result setters.

JSON restores tasks after every parent domain and rejects orphan, mismatched,
duplicate, missing, or corrupt task fields. Schemas through 16 default to
empty tasks, no series membership, and a 30-minute buffer. Root copy/reset,
equality/hash, clear, demo safety, and storage dirty snapshots cover all
workflow state. Destructive commands preview task cascades before mutation.
