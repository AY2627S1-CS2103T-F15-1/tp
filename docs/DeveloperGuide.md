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

The UI consists of a `MainWindow` and its parts. All of these, including `MainWindow`, inherit from the abstract `UiPart` class, which captures common behavior among classes that represent visible GUI parts. The window holds:

* the **tabs** (`PanelTabs`), which hold a `PersonListPanel`, a `LessonListPanel` and a `HomeworkListPanel`. Each list draws its rows as cards (`PersonCard`, `LessonCard` and `HomeworkCard`). A card shows the number that the commands use for its row, and has `IconButton`s for the actions that fit the row. Each list also has a toolbar of `QuickCommand` buttons that run the commands that fill it.
* the **detail pane** (`DetailPane`), which draws a `DetailView` from the Logic component.
* the `ResultDisplay`, the `CommandBox` and the `StatusBarFooter`, as in AddressBook-Level3.
* the `DemoBanner`, which is visible only while the demo runs, the `ConfirmBar`, which is visible only while a command waits for confirmation, and the `TourOverlay`, which is added to the window while the guided tour runs.

The `UI` component uses the JavaFX UI framework. The layouts of these UI parts are defined in matching `.fxml` files in `src/main/resources/view`. For example, [`MainWindow.fxml`](https://github.com/AY2627S1-CS2103T-F15-1/tp/tree/master/src/main/resources/view/MainWindow.fxml) specifies the layout of [`MainWindow`](https://github.com/AY2627S1-CS2103T-F15-1/tp/tree/master/src/main/java/seedu/address/ui/MainWindow.java).

The `UI` component,

* executes user commands using the `Logic` component.
* listens for changes to `Model` data so that the UI can be updated with the modified data. The student and lesson lists are `ObservableList`s that the list panels observe. The homework rows and the dashboard are not observable, so the window asks `Logic` for them again after every command.
* keeps a reference to the `Logic` component, because the `UI` relies on the `Logic` to execute commands.
* depends on some classes in the `Model` component because it displays `Person` and `Lesson` objects from the model, and on `HomeworkRow` and `DetailView` of the `Logic` component, which describe what the homework list and the detail pane draw.
* **never changes data itself.** A button asks the window to do something through the `PanelActions` interface, which has three operations: `run(command)` runs a command as if it had been typed, `prefill(text)` puts the start of a command in the command box, and `ask(question, command)` shows the `ConfirmBar` and runs the command only if the user confirms. Everything that a button does is therefore also something that the user can type, and the command keeps the only copy of the rules.
* **never reads the text of a result** to decide what to draw. A `CommandResult` says in typed fields which tab to show, what the detail pane draws, which students to highlight, and which command confirms a preview. This keeps a change of wording from breaking the window.

The sequence diagram below shows what happens when the user presses the bin button of a student who has lessons, and then confirms.

<puml src="diagrams/ConfirmSequenceDiagram.puml" alt="Interactions Between the UI Parts and Logic for a Deletion That Needs Confirmation" />

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
1. The result of the command execution is encapsulated as a `CommandResult` object which is returned from `Logic`. Besides the message, it carries what the window needs in typed fields: the `DetailView` for the detail pane, the lessons or homework that the command numbered, the students that it added, the command that confirms a preview such as `delete 3 confirm`, and whether to open the help window or the guided tour. `LogicManager` adds the warnings of a failed save and the refreshed lesson numbers to the result with `withAppendedText`, which also adds them to the result box of a detail view, so that they are not lost.

`LogicManager` also decides whether a command has to be saved, which is described in [Saving, recovery and migration](#saving-recovery-and-migration).

Here are the other classes in `Logic` (omitted from the class diagram above) that are used for parsing a user command:

<puml src="diagrams/ParserClasses.puml" width="600"/>

How the parsing works:
* When called upon to parse a user command, the `AddressBookParser` class creates an `XYZCommandParser` (`XYZ` is a placeholder for the specific command name, e.g., `AddCommandParser`). The parser uses the other classes shown above to parse the user command and create an `XYZCommand` object (e.g., `AddCommand`). The `AddressBookParser` returns that object as a `Command` object.
* All `XYZCommandParser` classes, such as `AddCommandParser` and `DeleteCommandParser`, implement the `Parser` interface so they can be treated similarly where appropriate, for example during testing.

### Model component
**API** : [`Model.java`](https://github.com/AY2627S1-CS2103T-F15-1/tp/tree/master/src/main/java/seedu/address/model/Model.java)

<puml src="diagrams/ModelClassDiagram.puml" width="450" />


The `Model` component,

* stores the address book data i.e., all `Person` objects, each of which represents a student with a name, phone number, level, subjects and rate, and optionally an email, a venue and a remark (the `Person` objects are contained in a `UniquePersonList` object). Every `Person` has a permanent `UUID` that is saved with it and never shown. Two `Person` objects are the same student if they have the same phone number and the same name, ignoring case. A student can be **archived**, which keeps all records but leaves the active list.
* stores the `Lesson` objects, each of which is a lesson with a student, a subject, a date, a start time, a duration, a status and optionally a venue, notes and a preparation text (the `Lesson` objects are contained in a `UniqueLessonList` object). A `Lesson` refers to its student by the student's `UUID`, so editing the name or the phone number of a student keeps the lessons attached, and it keeps the academic level that the student had at booking. Deleting a student deletes their lessons.
* stores the `Homework` objects, each of which is an assignment with a student, a description, a due date, a status and optional feedback. A `Homework` also refers to its student by `UUID`, and deleting a student deletes their homework.
* stores the `Person` objects selected by the current filter, such as search results, in a separate _filtered_ list, and the `Lesson` objects that the lesson commands count from in another. It exposes these as unmodifiable `ObservableList`s that the UI can observe and bind to, so the UI updates when a list changes. The homework that the last `hw list` showed is kept as an immutable snapshot, which `getDisplayedHomework()` returns.
* can run a **demo**: `startDemo` puts the sample data in place of the data and keeps the data aside, and `endDemo` puts it back. `isDemoMode()` says whether one is running.
* stores a `UserPrefs` object that represents the user’s preferences, which are the GUI settings and whether the guided tour has been seen. This is exposed to the outside as a `ReadOnlyUserPrefs` object.
* does not depend on any of the other three components (as the `Model` represents data entities of the domain, they should make sense on their own without depending on other components)

### Storage component

**API** : [`Storage.java`](https://github.com/AY2627S1-CS2103T-F15-1/tp/tree/master/src/main/java/seedu/address/storage/Storage.java)

<puml src="diagrams/StorageClassDiagram.puml" width="550" />

The `Storage` component,
* can save both address book data and user preference data in JSON format, and read them back into corresponding objects.
* is implemented by `StorageManager`, which delegates the actual JSON file access to `JsonAddressBookStorage` and `JsonUserPrefsStorage` (one class per data file).
* saves the students, the lessons and the homework in the same data file. A data file written before lessons or homework existed has no such list, and still loads with none.
* writes a **schema number** in the data file, and refuses to read a file whose number it does not support. `VersionedAddressBookStorage` reads the current file, and reads the legacy file only when the current one is absent, to migrate it. The details are in [Saving, recovery and migration](#saving-recovery-and-migration).
* depends on some classes in the `Model` component (because the `Storage` component's job is to save/retrieve objects that belong to the `Model`)

### Common classes

Classes used by multiple components are in the `seedu.address.commons` package.

--------------------------------------------------------------------------------------------------------------------

## **Implementation**

This section describes some noteworthy details on how certain features are implemented.

### Lesson scheduling, agenda and conflict detection

The lesson commands are `lesson add`, `lesson list`, `lesson move`, `lesson cancel`, `lesson done` and `lesson missed`. They share the `lesson` command word, which `AddressBookParser` hands to `LessonCommandParser`. That parser reads the next word and passes the rest of the arguments to the parser of that command. `agenda` is its own command word.

The sequence diagram below shows `execute("lesson move 1 d/2026-12-24")`.

<puml src="diagrams/LessonMoveSequenceDiagram.puml" alt="Interactions Inside the Logic and Model Components for the `lesson move 1 d/2026-12-24` Command" />

How the pieces fit together:

* **Lesson indices.** `Model#getFilteredLessonList()` returns the lessons that `agenda` or `lesson list` last chose, sorted by date, then start time, then student name. `agenda` and `lesson list` number the lessons they show in that order, and `lesson move` and `lesson cancel` take the same number, so the number a tutor sees is always the one that the next command acts on. `lesson add` keeps the current filter. If the new lesson shifts the numbers of the lessons that are shown, `LogicManager` appends the complete current list with its fresh numbers to the result and attaches it as the displayed lessons, so that the next lesson command is not given a number that has moved.
* **Overlaps.** `Lesson#overlaps(Lesson)` is true when both lessons are scheduled, are on the same date, and each starts before the other ends. It compares minutes since the start of the day, so back-to-back lessons are not an overlap and a lesson that ends at midnight can still be compared. `Model#getConflictingLessons(Lesson)` returns the other lessons that overlap a lesson, and `lesson add` and `lesson move` append the result to their message as a warning. `agenda` marks every lesson that has at least one overlap.
* **Cancelling.** `Lesson#cancel(String)` returns a copy with the status `CANCELLED` and the reason. The lesson is kept, so it stays in the agenda for billing, but it is no longer scheduled and so it never overlaps.
* **Immutability.** `Lesson` is immutable. `lesson move` and `lesson cancel` build a changed copy and ask the model to replace the old lesson with it, in the same way that `edit` replaces a `Person`.
* **Defaults to type less.** `lesson add` takes the subject from the student when the student has only one, uses 60 minutes if no duration is given, and uses the student's venue if no venue is given.

#### Design considerations

**Aspect: how a lesson refers to its student**

* **Alternative 1 (current choice):** store the `UUID` of the student in the lesson, with the name and phone number of the student as display details.
    * Pros: Editing the name or the phone number of a student cannot detach the lessons, and a change of the level cannot rewrite the level that a lesson was booked at, because the lesson keeps its own snapshot of it. The `UUID` also links the homework in the same way.
    * Cons: A file from before the `UUID` existed has to be migrated, and a lesson that cannot be linked to exactly one student makes the file invalid. `VersionedAddressBookStorage` links legacy lessons by name and phone while loading.
* **Alternative 2:** store the name and phone number of the student in the lesson, as the first version did.
    * Pros: The saved file is flat, and a lesson can be validated without loading students.
    * Cons: `AddressBook` has to update the lessons whenever it edits or deletes a student, and two students whose names and phone numbers change in turn can be confused.
* **Alternative 3:** store a reference to the `Person` object.
    * Pros: No update is needed when a student is edited.
    * Cons: `Person` is immutable and is replaced on every edit, so the reference would have to be replaced as well, and the saved file would need identifiers anyway.

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

### Commands at a glance

Every command is a `Command` subclass in `seedu.address.logic.commands`, built by a parser in `seedu.address.logic.parser`. A command that takes no parameters is built by `AddressBookParser` itself. `DiscoverabilityTest` fails when a command is missing from `help`, the parser, the User Guide or this guide.

| Command | Command class | Parser |
|---------|---------------|--------|
| `add` | `AddCommand` | `AddCommandParser` |
| `edit` | `EditCommand` | `EditCommandParser` |
| `delete` | `DeleteCommand` | `DeleteCommandParser` |
| `list` | `ListCommand` | `ListCommandParser` |
| `student` | (forwards to the command that follows it) | `StudentCommandParser` |
| `archive` | `ArchiveCommand` | `ArchiveCommandParser` |
| `restore` | `ArchiveCommand` | `ArchiveCommandParser` |
| `find` | `FindCommand` | `FindCommandParser` |
| `view` | `ViewCommand` | `ViewCommandParser` |
| `sort` | `SortCommand` | `SortCommandParser` |
| `remark` | `RemarkCommand` | `RemarkCommandParser` |
| `import students` | `ImportStudentsCommand` | `ImportCommandParser` |
| `demo` | `DemoCommand` | `DemoCommandParser` |
| `tour` | `TourCommand` | `AddressBookParser` |
| `lesson add` | `LessonAddCommand` | `LessonAddCommandParser` |
| `lesson list` | `LessonListCommand` | `LessonListCommandParser` |
| `lesson prep` | `LessonPrepCommand` | `LessonPrepCommandParser` |
| `lesson move` | `LessonMoveCommand` | `LessonMoveCommandParser` |
| `lesson cancel` | `LessonCancelCommand` | `LessonCancelCommandParser` |
| `lesson done` | `LessonDoneCommand` | `LessonDoneCommandParser` |
| `lesson missed` | `LessonMissedCommand` | `LessonMissedCommandParser` |
| `hw add` | `HomeworkAddCommand` | `HomeworkCommandParser` |
| `hw list` | `HomeworkListCommand` | `HomeworkCommandParser` |
| `hw edit` | `HomeworkEditCommand` | `HomeworkCommandParser` |
| `hw status` | `HomeworkStatusCommand` | `HomeworkCommandParser` |
| `hw feedback` | `HomeworkFeedbackCommand` | `HomeworkCommandParser` |
| `hw delete` | `HomeworkDeleteCommand` | `HomeworkCommandParser` |
| `agenda` | `AgendaCommand` | `AgendaCommandParser` |
| `today` | `TodayCommand` | `AddressBookParser` |
| `clear` | `ClearCommand` | `ClearCommandParser` |
| `save` | `SaveCommand` | `AddressBookParser` |
| `help` | `HelpCommand` | `HelpCommandParser` |
| `exit` | `ExitCommand` | `AddressBookParser` |


### Students, filters and archiving

**Identity.** `Person#getId()` is a `UUID` that is saved with the student, apart from the visible index and from value equality. Two students are equal when their fields are equal, and the `UUID` is not one of them, so a duplicate check never depends on it. A `Lesson` or a `Homework` refers to its student by that `UUID`, which keeps the link when the student is edited or renamed.

**Archiving.** A `Person` has an `isArchived` flag that takes part in equality. `ArchiveCommand` replaces the student with `Person#withArchived`, and then the filtered list refreshes. The active list is the default predicate (`Model#PREDICATE_SHOW_ACTIVE_PERSONS`), so an archived student leaves it without any record being touched. A lesson command or `hw add` that needs an active student rejects an archived one with a message that says to restore the student first, and status changes and feedback on existing homework are still accepted.

**Filters.** `ListCommand` builds one predicate from the level, the subject and the archive scope (`archived/` or `all/`, which exclude each other) and sets it on the filtered list. `ListCommandParser` rejects a flag with a value and the two flags together, as the other flags of TutorFlow do through `ParserUtil#parseFlag`. The `student` namespace (`StudentCommandParser`) forwards the rest of its input to `AddressBookParser`, so a student command has one parser however it is typed.

**Deleting.** `DeleteCommand` collects the records that deletion would remove into a `LinkedRecords` record (the lessons and the homework of the student). A student without any is deleted at once. For a student with records and no `confirm`, the command returns a preview and deletes nothing: the counts of lessons and homework, how many are upcoming, how many homework items have feedback, and, if there is homework, the advice to archive instead. The preview carries the confirming command (`delete 3 confirm`) in the `CommandResult`, which the window offers as a button.

### Homework

`Homework` is immutable and has a `UUID`, the `UUID` of its student, a normalized description, a due date, a `HomeworkStatus` and optional feedback. `HomeworkStatus` has only the persisted values `ASSIGNED`, `SUBMITTED` and `REVIEWED`. Overdue is worked out from `ASSIGNED` and the caller's date by `Homework#isOverdue(LocalDate)` and is never stored, so it cannot get out of step with the date.

`AddressBook` owns the homework list. It rejects a duplicate assignment (the same student, normalized description and due date, ignoring case), a repeated identity and a student that does not exist, and `setHomework` rejects a change of the student. Deleting a student, `clear` and `resetData` include the homework, so no homework is left without its student.

`HomeworkQueries` holds the rules that the list, the student overview and the dashboard share: which items match the filters, their order (overdue first, then due date, then the order of assignment), and how an item is written on one line or with its feedback. The dashboard and `view` use `HomeworkQueries#summaryOf`, which never changes the numbered selection.

`HomeworkCommandParser` takes a `Clock`, so that `today`, `tmr` and the bounds of a due date (today to one calendar year ahead) are deterministic in tests. `HomeworkCommandSupport` has the rules that the six commands share: resolving a number to the homework, resolving a student, and validating a due date.

**Numbers.** `hw list` selects the homework and stores it with `Model#showHomework(List<Homework>)`. This is an immutable snapshot of the rows that were numbered, and it is not the current data. `HomeworkCommandSupport#target` takes the row at the given number and looks the **same `UUID`** up in `AddressBook#getHomeworkList()`. If the homework is gone, the command fails with `That homework was deleted. Run 'hw list' to refresh.`, and no other row takes its number. Editing, changing the status, adding homework, archiving and renaming a student, and a failed save leave the snapshot as it is, so the numbers that the tutor sees stay true until the next `hw list`. `Logic#getHomeworkRows()` turns the snapshot into `HomeworkRow`s for the window by reading each item as it is now, and marks the rows that are gone as deleted.

**Using the numbers in another command.** A command that takes the number of a homework item, such as one that links a task to homework, must not read `Model#getDisplayedHomework()` as if it were the data. It calls `HomeworkCommandSupport#target(model, index)`, which returns the item as it is now, or fails with `That homework was deleted. Run 'hw list' to refresh.` if it is gone and with the invalid-index message if the number is outside the last list. A task should store the `UUID` of the item, never the number, and `Homework#getId()` is the key that `AddressBook#getHomeworkList()` and the saved file use.

#### Design considerations

**Aspect: what a number in a homework command refers to**

* **Alternative 1 (current choice):** the number refers to the row of the last `hw list`, and the row is resolved by its `UUID` when the command runs.
    * Pros: A number never selects another item, even when homework is added, edited or deleted after the list was shown. The window can show the same rows.
    * Cons: The model keeps a second list that must be refreshed, and the UI must draw deleted rows as deleted.
* **Alternative 2:** the number refers to a position in the current list of all homework.
    * Pros: There is no snapshot.
    * Cons: A number that was right when the tutor read it can select another item after any change. A tutor who deletes the third item and then types the same number again deletes the next one.

**Aspect: where overdue is kept**

* **Alternative 1 (current choice):** it is worked out from the status and the date when needed.
    * Pros: It cannot be wrong after midnight, and a file never says that something is overdue that is not.
    * Cons: Every place that shows homework must ask `isOverdue`, which `HomeworkQueries` and `HomeworkRow` do for all of them.
* **Alternative 2:** store `OVERDUE` as a status and update it when the application starts.
    * Pros: The status is one field.
    * Cons: It is wrong while the application runs across midnight, and `hw status` would have to refuse or accept it.

### Saving, recovery and migration

<puml src="diagrams/SaveActivityDiagram.puml" alt="What LogicManager Does for a Command That May Change Data" />

`LogicManager#execute` saves before it reports success, so that no change is lost when TutorFlow stops after a command. It does not ask the command whether it changed anything, because a command could forget to say so. It takes a **snapshot** of the data before and after the command with `Storage#snapshotAddressBook`, which is the exact text that would be written, and saves only if the two differ or an earlier save failed. A change that the file would not show, such as a different filter, therefore causes no write, and a change to a field that no equality check looks at, such as the feedback of a homework item, still does.

* **A failed save** does not undo the command. The change stays in the running application, the data stays marked as unsaved, and the result says `Your change was applied in this session but could not be saved: ...` with the reason, and `Unsaved changes: type 'save' to retry before closing TutorFlow.` `save` and `exit` try again, and `exit` is refused while the save still fails. `LogicManager#hasUnsavedChanges` is what `exit` and the window's close request check. Read-only commands after a failure show the same reminder.
* **The write** is atomic. `FileUtil#writeToFile` writes the serialized text to a temporary file next to the data file and then moves it over the data file in one step, and it reports an error if the file system cannot do that. It never creates the destination first, so a write that is interrupted during the first save leaves the legacy file available.
* **An invalid file** is not deleted. `DataFileRecovery#setAside` renames it to `tutorflow-v14.json.corrupt` (or `.corrupt.1` and so on), the application starts with no data and the result box says where the file went. If it cannot be renamed, saving is blocked until it can be preserved, so the original bytes are never overwritten.
* **A file that cannot be opened.** `JsonAddressBookStorage#isAccessFailure` tells a failure to open or read a file, which is an `IOException` that is not a `JsonProcessingException`, from invalid content. Only invalid content is corruption. A file that cannot be opened, because another program holds it or its permissions forbid it, can well be valid, so setting it aside would hide the records of a tutor, and for the legacy file it would stop them from ever being migrated. `MainApp` therefore refuses to start, in the same way as for an unsupported schema, with a message that names the file and the reason, and `JsonAddressBookStorage#requireOpenedSource` makes every save refuse to replace the file. The next start that can open the file loads or migrates it.
* **The schema number.** `StorageSchema.MINIMUM_VERSION` and `StorageSchema.CURRENT_VERSION` give the range of schemas that a build reads (14 to 15 now), and a build writes `CURRENT_VERSION`. Before any field is bound, `JsonAddressBookStorage` reads the number from the raw JSON. A number outside the range raises `UnsupportedSchemaException`, which is the cause of the `DataLoadingException`. `MainApp#init` passes it on before it creates a model, logic or UI, `MainApp#start` tells the user, and the file stays where it is. It must never go through corruption recovery, and later domain fields must never be ignored silently, because that would lose the data of a newer build.
* **Migration.** `VersionedAddressBookStorage` reads `tutorflow-v14.json`. Only if that path is positively absent does it read the legacy `addressbook.json`, which gives every student and lesson a `UUID`, links each legacy lesson to its student by name and phone, and leaves the academic level of the lesson empty, because it cannot be known. `LogicManager` is told that an initial save is required, so the migrated identifiers are written at once and not at the first change, and a failed initial save leaves the application unsaved. The legacy file is never changed.
* **Adding to the data.** A new kind of record must be added to `ReadOnlyAddressBook`, `AddressBook` (copy constructor, `resetData`, `equals` and `hashCode`), the JSON root and adapters, and be covered by a round-trip test together, and `StorageSchema.CURRENT_VERSION` must go up with prior-version loading and future-version refusal tests. Homework is schema 15. A build reads every schema from 14 to its own, with defaults for what an older file does not have.
* **Names.** `DataFiles` holds the paths of the data, legacy and preferences files, and `DataFiles#isUsedByTutorFlow` tells the CSV import that a path is one of them, or a copy or a temporary file of one, so that an import can never read the data file as a CSV file.
* **Logs.** TutorFlow does not write a student's name, phone number or fees to a log file. `LogPrivacyTest` runs the commands and checks the log.

#### Design considerations

**Aspect: how to decide whether to save**

* **Alternative 1 (current choice):** compare snapshots of the data before and after the command.
    * Pros: A command cannot forget to ask for a save, and a command that changes nothing never writes. The comparison uses the representation that is written, so UI-only equality never decides.
    * Cons: Two serializations for each command, which is acceptable for the data sizes of NFR 3.
* **Alternative 2:** each command says whether it changed the data.
    * Pros: No serialization to compare.
    * Cons: A new command that forgets loses the change at exit, which is the worst failure of a data application.

### Demo mode

<puml src="diagrams/DemoStateDiagram.puml" alt="The States of the Demo" />

The demo lets a tutor try TutorFlow with sample data without risking real records. `ModelManager` keeps a copy of the data in `dataBeforeDemo` while a demo runs, and `isDemoMode()` is true exactly while that copy exists. `DemoCommand` has three actions: `PREVIEW` says what would be loaded, `START` calls `Model#startDemo`, which keeps the data aside, puts the sample data in its place and resets what the numbers refer to, and `EXIT` calls `Model#endDemo`, which puts the data back and resets the numbers again.

The demo is **in memory only**. `LogicManager` does not save while a demo runs, and it does not report unsaved changes for the sample data. It also refuses to start a demo while there are unsaved changes (`MESSAGE_SAVE_BEFORE_DEMO`), because the data that `endDemo` brings back is what the file holds. `save` says that nothing is saved in the demo. `MainWindow` shows the `DemoBanner` while `Logic#isDemoMode()` is true, and its button runs `demo exit`.

#### Design considerations

**Aspect: where the sample data lives**

* **Alternative 1 (current choice):** in memory, in place of the data, with the data kept aside.
    * Pros: The data file is never written during a demo, so the tutor's records are safe even if TutorFlow is closed in it, and every command works on the samples as it does on real data.
    * Cons: The demo is lost on exit, which is the purpose of it.
* **Alternative 2:** load the samples into the data file, as the first version did when the data was empty.
    * Pros: Simple.
    * Cons: The samples have to be deleted by hand, and a tutor who already has records cannot look around at all.
* **Alternative 3:** open a second data file for the demo.
    * Pros: Isolated.
    * Cons: A second path to protect, to clean up and to explain.

### The window: detail pane, tabs and buttons

**Structured views.** `TodayCommand`, `AgendaCommand` and `ViewCommand` return a `DetailView` with their `CommandResult`: a title, a summary, a flag that says whether the view is live, and sections of `Line`s that say in typed flags whether they are struck through (a cancelled lesson) or a warning (an overlap). `DetailPane` draws the lines in a monospaced font, so that the columns line up, and it never reads the text of a line to decide how to draw it. The result box says `summary` and `Shown in the detail pane.`, and `withAppendedText` adds a warning of `LogicManager` to it.

A live view, the dashboard, is drawn again after every command by `MainWindow#refreshDashboard`, so that it never shows old data. The other views show the data as it was when they were asked for. `PageUp`, `PageDown`, `Ctrl+Up` and `Ctrl+Down` scroll the pane from the command box, through a key filter on the window.

**Tabs and rows.** `PanelTabs` holds the three lists. `LessonListPanel` binds to `Logic#getFilteredLessonList()`, which is the list that the lesson commands count from, and `HomeworkListPanel#show` is given the `HomeworkRow`s after every command. A command that numbers lessons or homework brings its tab to the front (`MainWindow#showPanelFor` reads `CommandResult#getDisplayedLessons()` and `getDisplayedHomework()`, which `AgendaCommand`, `LessonListCommand` and `HomeworkListCommand` fill), and so does a command that adds a student.

**Buttons and confirmation.** The rules of the window are in the `UI component`: a button runs, prefills or asks, and never edits data. A delete button asks first with `ask`. A command that needs confirmation returns a preview with `CommandResult#withConfirmationCommand`, and `MainWindow` shows the `ConfirmBar` for it, so the window never has to read `Type 'delete 1 confirm' to proceed.` to know the command. `DeleteCommand`, `HomeworkDeleteCommand`, `ClearCommand`, `ImportStudentsCommand` and the preview of `DemoCommand` do this. Any other command, an error, `Cancel` and `Esc` take the bar away.

`Icon` draws each picture as an `SVGPath` on a 16 by 16 grid in the colour of the text, so there are no image files. `IconButton` gives the button a tooltip and the same text for screen readers, and `QuickCommand` names its command in its tooltip.

#### Design considerations

**Aspect: how the window learns what to draw**

* **Alternative 1 (current choice):** typed fields on the `CommandResult`.
    * Pros: A change to a message cannot break the window, and a test can check what a command asks the window to draw without a window.
    * Cons: Every new kind of view needs a field and a place that handles it.
* **Alternative 2:** the window reads the text of the result.
    * Pros: Nothing to add to `CommandResult`.
    * Cons: The window and the wording become one thing, and a correction to the wording changes how the window works.

### Guided tour

`TourCommand` returns a result with `withShowTour()`. `MainWindow#handleTour` then adds a `TourOverlay` over the window. The overlay lies over the window without blocking it: its empty areas let mouse events through, so the tutor can still type in the command box. A step is a `TourStep` (a title, a text, the selector of the part to point at, and an optional example), and `GuidedTour#steps(LocalDate)` lists them. The overlay finds the part of the window with a selector, frames it and puts the card on the side where it fits, in the middle of the window if the step points at nothing or the part cannot be found, and again when the window is resized.

The tour is offered on the first run: `UserPrefs#isTourSeen` is false, `MainWindow` starts the tour when it has been built, and finishing or leaving the tour calls `Logic#markTourSeen()`, which the preferences keep. The example of a step is only put in the command box by `CommandBox#prefill`. `GuidedTourTest` checks that every example parses and every target exists in the window.

### Importing students from a CSV file

`ImportCommandParser` reads `import students file/PATH [confirm]`, with `PathArgument` working out the path (which runs to the end of the command, except a final `confirm`, or is in double quotes). `ImportStudentsCommand` checks the file path first (a folder, a missing file, and `DataFiles#isUsedByTutorFlow`) and then hands the file to `StudentCsvImport#read`, which reads at most 5 MB as strict UTF-8, parses it with `CsvUtil`, and validates every field with the same `ParserUtil` methods that `add` uses, so that an imported student obeys the rules of `add`. The result is a `Report` of the students that are ready, the rows that are duplicates, the warnings and the errors, with line numbers. Nothing is added unless the command is confirmed and the report has no error, and then the students are added together, and the result lists them as `addedStudents` for the window to select.

### Documentation and the discoverability of commands

`DiscoverabilityTest` fails when a command is in the list of `help` but is not known to the parser, is not described in the User Guide or in its command summary, or is not in the table of [commands at a glance](#commands-at-a-glance). It also fails when the command summary lists a command that `help` does not.

### \[Proposed\] Undo/redo feature

#### Proposed Implementation

The proposed undo/redo mechanism is facilitated by `VersionedAddressBook`. It extends `AddressBook` with an undo/redo history, stored internally as an `addressBookStateList` and `currentStatePointer`. Additionally, it implements the following operations:

* `VersionedAddressBook#commit()` -- Saves the current address book state in its history.
* `VersionedAddressBook#undo()` -- Restores the previous address book state from its history.
* `VersionedAddressBook#redo()` -- Restores a previously undone address book state from its history.

These operations are exposed in the `Model` interface as `Model#commitAddressBook()`, `Model#undoAddressBook()` and `Model#redoAddressBook()` respectively.

Given below is an example usage scenario and how the undo/redo mechanism behaves at each step.

Step 1. The user launches the application for the first time. The `VersionedAddressBook` will be initialized with the initial address book state, and the `currentStatePointer` pointing to that single address book state.

<puml src="diagrams/UndoRedoState0.puml" alt="UndoRedoState0" />

Step 2. The user executes `delete 5` command to delete the 5th person in the address book. The `delete` command calls `Model#commitAddressBook()`, causing the modified state of the address book after the `delete 5` command executes to be saved in the `addressBookStateList`, and the `currentStatePointer` is shifted to the newly inserted address book state.

<puml src="diagrams/UndoRedoState1.puml" alt="UndoRedoState1" />

Step 3. The user executes `add n/David …​` to add a new person. The `add` command also calls `Model#commitAddressBook()`, causing another modified address book state to be saved into the `addressBookStateList`.

<puml src="diagrams/UndoRedoState2.puml" alt="UndoRedoState2" />

<box type="info" seamless>

**Note:** If a command fails its execution, it will not call `Model#commitAddressBook()`, so the address book state will not be saved into the `addressBookStateList`.
</box>

Step 4. The user now decides that adding the person was a mistake, and decides to undo that action by executing the `undo` command. The `undo` command will call `Model#undoAddressBook()`, which will shift the `currentStatePointer` once to the left, pointing it to the previous address book state, and restores the address book to that state.

<puml src="diagrams/UndoRedoState3.puml" alt="UndoRedoState3" />


<box type="info" seamless>

**Note:** If the `currentStatePointer` is at index 0, pointing to the initial AddressBook state, then there are no previous AddressBook states to restore. The `undo` command uses `Model#canUndoAddressBook()` to check if this is the case. If so, it will return an error to the user rather
than attempting to perform the undo.
</box>

The following sequence diagram shows how an undo operation goes through the `Logic` component:

<puml src="diagrams/UndoSequenceDiagram-Logic.puml" alt="UndoSequenceDiagram-Logic" />

<box type="info" seamless>

**Note:** The lifeline for `UndoCommand` should end at the destroy marker (X), but due to a limitation of PlantUML, it continues to the end of the diagram.
</box>

Similarly, how an undo operation goes through the `Model` component is shown below:

<puml src="diagrams/UndoSequenceDiagram-Model.puml" alt="UndoSequenceDiagram-Model" />

The `redo` command does the opposite — it calls `Model#redoAddressBook()`, which shifts the `currentStatePointer` once to the right, pointing to the previously undone state, and restores the address book to that state.

<box type="info" seamless>

**Note:** If the `currentStatePointer` is at index `addressBookStateList.size() - 1`, pointing to the latest address book state, then there are no undone AddressBook states to restore. The `redo` command uses `Model#canRedoAddressBook()` to check if this is the case. If so, it will return an error to the user rather than attempting to perform the redo.
</box>

Step 5. The user then decides to execute the command `list`. Commands that do not modify the address book, such as `list`, will usually not call `Model#commitAddressBook()`, `Model#undoAddressBook()` or `Model#redoAddressBook()`. Thus, the `addressBookStateList` remains unchanged.

<puml src="diagrams/UndoRedoState4.puml" alt="UndoRedoState4" />

Step 6. The user executes `clear`, which calls `Model#commitAddressBook()`. Since the `currentStatePointer` is not pointing at the end of the `addressBookStateList`, all address book states after the `currentStatePointer` will be purged. Reason: It no longer makes sense to redo the `add n/David …` command. This is the behavior that most modern desktop applications follow.

<puml src="diagrams/UndoRedoState5.puml" alt="UndoRedoState5" />

The following activity diagram summarizes what happens when a user executes a new command:

<puml src="diagrams/CommitActivityDiagram.puml" width="250" />

#### Design considerations:

**Aspect: How undo & redo execute:**

* **Alternative 1 (current choice):** Saves the entire address book.
  * Pros: Easy to implement.
  * Cons: May have performance issues in terms of memory usage.

* **Alternative 2:** Individual command knows how to undo/redo by
  itself.
  * Pros: Will use less memory (e.g. for `delete`, just save the person being deleted).
  * Cons: We must ensure that the implementation of each individual command is correct.


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

**Use case: UC9 - Archive and restore a student**

**MSS**

1.  Tutor requests to archive a student who has stopped taking lessons
2.  TutorFlow archives the student, keeps their lessons, homework and history, and removes them from the active list
3.  Later, the tutor requests the archived students, and then to restore one
4.  TutorFlow restores the student to the active list with all records intact

    Use case ends.

**Extensions**

* 1a. The index is not in the displayed list.

    * 1a1. TutorFlow shows an error message that names the number of students in the list.

      Use case resumes at step 1.

* 1b. The student is archived already.

    * 1b1. TutorFlow tells the tutor that the student is archived already and changes nothing.

      Use case ends.

* 3a. The tutor tries to schedule a lesson or assign homework for the archived student.

    * 3a1. TutorFlow tells the tutor to restore the student first and changes nothing.

      Use case ends.

**Use case: UC10 - Find students by level and subject**

**MSS**

1.  Tutor requests the students of a level and a subject, such as `list l/S3 s/Math`
2.  TutorFlow lists the active students that match both, numbered afresh, and says how many there are

    Use case ends.

**Extensions**

* 1a. The tutor asks for the archived students (`archived/`) or for all students (`all/`).

    * 1a1. TutorFlow lists the archived students, or both, in the same way.

      Use case ends.

* 1b. The tutor gives both `archived/` and `all/`, or gives a value to one of them.

    * 1b1. TutorFlow shows an error message with the usage of `list`.

      Use case resumes at step 1.

* 2a. No student matches.

    * 2a1. TutorFlow shows an empty list and says that 0 students match.

      Use case ends.

**Use case: UC11 - Look around with sample data**

**MSS**

1.  Tutor requests the demo
2.  TutorFlow says how many sample students it would load and that the tutor's data is kept safe
3.  Tutor confirms
4.  TutorFlow keeps the tutor's data aside, loads the sample students and shows a strip that says nothing in the demo is saved
5.  Tutor tries commands on the samples and requests to leave the demo
6.  TutorFlow puts the tutor's own data back, exactly as it was

    Use case ends.

**Extensions**

* 3a. The tutor has changes that could not be saved.

    * 3a1. TutorFlow refuses to start the demo and asks the tutor to run `save` first.

      Use case ends.

* 5a. The tutor closes TutorFlow without leaving the demo.

    * 5a1. TutorFlow does not save the sample data, and the data file is as the tutor left it.

      Use case ends.

**Use case: UC12 - Import students from a file**

**MSS**

1.  Tutor requests to import the students of a CSV file
2.  TutorFlow reads and checks the file and reports how many students are ready, how many rows are duplicates, and any warnings
3.  Tutor confirms
4.  TutorFlow adds the students, selects them in the list and confirms the number that were added

    Use case ends.

**Extensions**

* 1a. The file does not exist, is a folder, is not UTF-8 text or is too large.

    * 1a1. TutorFlow tells the tutor why the file cannot be imported and changes nothing.

      Use case ends.

* 2a. A row has an invalid value, or the first line misses a required column.

    * 2a1. TutorFlow lists each line and field to fix, and imports nothing, even if the tutor confirms.

      Use case ends.

**Use case: UC13 - Open the data of an older version**

**MSS**

1.  Tutor starts TutorFlow with data that an older version saved
2.  TutorFlow reads the old file, gives every student and lesson a permanent identifier and saves the result as the current data file, leaving the old file untouched
3.  TutorFlow shows the data

    Use case ends.

**Extensions**

* 1a. The data file was saved by a newer version of TutorFlow.

    * 1a1. TutorFlow tells the tutor that the version is not supported, leaves the file where it is and stops.

      Use case ends.

* 1b. The data file is damaged.

    * 1b1. TutorFlow renames it so that it is kept, starts with no data and tells the tutor where the file went.

      Use case ends.

* 1c. The data file, or the older file that is to be migrated, cannot be opened, for example because another program holds it.

    * 1c1. TutorFlow tells the tutor why in a window, changes and renames nothing and does not start, so that the next start that can open the file migrates or loads it.

      Use case ends.

* 2a. The first save of the migration fails.

    * 2a1. TutorFlow says that there are unsaved changes, and does not close until the tutor fixes the problem and runs `save`.

      Use case ends.

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
15. Should show its command box and result box, and keep every list scrollable, when the window is as small as 640 by 480 pixels.

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
* **Archived student**: A student who is hidden from the active list and cannot be given new lessons or homework, while all of their records are kept. The tutor can restore them
* **Numbered list**: A list on screen whose rows carry the indices that commands use, such as the students, the lessons of the last `agenda` or `lesson list`, and the homework of the last `hw list`
* **Detail pane**: The part of the window that shows the dashboard, an agenda or a student overview
* **Demo**: A mode in which sample students replace the tutor's data on screen, without saving them, until the tutor leaves it and the data comes back
* **Guided tour**: The introduction to the window that opens at the first run and that `tour` starts again
* **Schema version**: The number that a data file records for the form of its data, which decides whether a build can read it

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
      Expected: The GUI opens with no students, the result box says `Welcome to TutorFlow. Add a student or type demo to preview sample data.`, and the guided tour opens over the window at its first step. The window size may not be optimal.

1. Saving window preferences

   1. Resize the window to an optimal size. Move the window to a different location. Close the window.

   1. Relaunch the app by double-clicking the JAR file.<br>
       Expected: The most recent window size and location are retained.


### Deleting a student

1. Deleting a student while all active students are being shown

   1. Prerequisites: List the students using the `list` command, with multiple students in the list and none with lessons or homework.

   1. Test case: `delete 1`<br>
      Expected: The first student is deleted from the list. The result shows `Deleted student:` and the student's details.

   1. Test case: `delete 0`<br>
      Expected: No student is deleted. The result shows error details.

   1. Other incorrect delete commands to try: `delete`, `delete x`, `delete 99` (where 99 is larger than the list size)<br>
      Expected: Similar to previous.

### Students

1. Filtering the list

   1. Prerequisites: Add three students, for example `add n/Alex Tan p/91234567 l/S3 s/Math r/50`, `add n/Bella Lim p/92345678 l/P6 s/Science r/45` and `add n/Chen Wei p/93456789 l/S3 s/Physics r/60`.

   1. Test case: `list l/S3`<br>
      Expected: Alex Tan and Chen Wei are listed, numbered 1 and 2, and the result says `Listed 2 matching student(s).`

   1. Test case: `list l/S3 s/Math`<br>
      Expected: Only Alex Tan is listed.

   1. Test case: `list archived/ all/`<br>
      Expected: An error with the usage of `list`, and the list is unchanged.

1. Archiving and restoring

   1. Prerequisites: The three students above, listed with `list`.

   1. Test case: `archive 1`<br>
      Expected: `Archived: Alex Tan. Lessons and history are retained. The student list has refreshed.` and Alex Tan has left the list.

   1. Test case: `list archived/`, then `hw add st/1 desc/Algebra due/tmr` and `lesson add st/1 d/<next week> t/16:30`<br>
      Expected: Both are rejected, because the student is archived and must be restored first.

   1. Test case: `list archived/`, then `restore 1`<br>
      Expected: `Restored: Alex Tan. ...`. `list` shows Alex Tan again with the same details.

   1. Test case: `restore 1` while the active list is shown<br>
      Expected: An error that the student is active already.

1. Deleting a student who has records

   1. Prerequisites: A student with a lesson and a homework item that has feedback.

   1. Test case: `delete 1`<br>
      Expected: Nothing is deleted. The result shows the name and phone number, the number of lessons, the number of homework items and how many are upcoming, how many items have feedback, and the advice to use `archive`. It ends with how to confirm, and in the window the confirm bar offers `Confirm`.

   1. Test case: `delete 1 confirm`<br>
      Expected: The student, the lessons and the homework are deleted. The result says what was deleted with the student.

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

### Homework

1. Assigning and listing

   1. Prerequisites: At least one active student, listed with `list`.

   1. Test case: `hw add st/1 desc/Algebra Q1-10 due/tmr`<br>
      Expected: `Homework assigned to ...: Algebra Q1-10, due <tomorrow>`, and a reminder to run `hw list`.

   1. Test case: the same command again, then the same command with `desc/ALGEBRA q1-10`<br>
      Expected: Both are rejected as duplicates.

   1. Test case: `hw add st/1 desc/Late due/2020-01-01`, `hw add st/1 desc/Far due/<two years ahead>` and `hw add st/1 desc/Algebra`<br>
      Expected: The first two are rejected because of the due date bounds, and the third because `due/` is missing.

   1. Test case: `hw list`<br>
      Expected: `1 homework item(s).` followed by the numbered items, and the `Homework` tab shows them with the same numbers.

1. Status, feedback and the numbers

   1. Prerequisites: Two homework items listed by `hw list`.

   1. Test case: `hw feedback 1 f/Good work`<br>
      Expected: Rejected, because the homework has not been submitted.

   1. Test case: `hw status 1 s/submitted`, then `hw feedback 1 f/Sign errors in Q8-12`, then `hw status 1 s/assigned`<br>
      Expected: The status changes, the feedback is recorded, and the move back shows `Note: moved back from submitted to assigned. Feedback already recorded is kept.`

   1. Test case: `hw status 1 s/assigned` again<br>
      Expected: `That homework is already marked assigned.`

   1. Test case: `hw delete 1`, then `hw edit 1 desc/x`<br>
      Expected: The first asks for `hw delete 1 confirm` because feedback is recorded, and deletes nothing. After `hw delete 1 confirm`, `hw edit 1 desc/x` says that the homework was deleted, and number 2 still refers to the second item.

   1. Test case: `hw list overdue/ outstanding/`<br>
      Expected: An error that says to choose one of them.

### Importing students

1. Importing a file

   1. Prerequisites: A file `students.csv` in the folder of the JAR file with the first line `name,phone,level,subjects,rate` and the line `Dana Koh,94567890,S2,Math;English,40`.

   1. Test case: `import students file/students.csv`<br>
      Expected: A report with `ready to add: 1`, and `Type 'import students file/students.csv confirm' to add 1 student.` No student is added.

   1. Test case: `import students file/students.csv confirm`<br>
      Expected: `Imported 1 student.` Dana Koh is added, selected and highlighted for a moment in the list.

   1. Test case: the same command again<br>
      Expected: `There are no new students to import.`, because the row is a duplicate.

   1. Test case: change the level in the file to `S9` and run the command with `confirm`<br>
      Expected: Nothing is imported, and the result names the line and the field to fix.

   1. Test case: `import students file/data`, `import students file/missing.csv`<br>
      Expected: Errors that the path is a folder and that no file is found.

### Demo

1. Looking around and coming back

   1. Prerequisites: At least one student of your own.

   1. Test case: `demo`<br>
      Expected: The number of sample students and how to load them. Nothing changes.

   1. Test case: `demo confirm`<br>
      Expected: The sample students replace the list, and an orange strip says that nothing is saved. `save` says `Nothing is saved in the demo. ...`

   1. Test case: `delete 1`, then close TutorFlow, and open it again<br>
      Expected: Your own students are listed exactly as before, and the data file has not changed.

   1. Test case: `demo confirm` again, then press `Exit demo` on the strip<br>
      Expected: `Demo ended. Your own data is back.`, and the strip is gone.

   1. Test case: make a change that cannot be saved (see Saving data), then `demo confirm`<br>
      Expected: Refused, with the advice to run `save` first.

### The window and the guided tour

1. Tabs and buttons

   1. Prerequisites: Two students, a lesson today for the first, and homework for the second.

   1. Test case: `agenda`, then `hw list`, then press `Ctrl+1`<br>
      Expected: After `agenda` the `Lessons` tab is in front with the lesson numbered 1, after `hw list` the `Homework` tab is, and `Ctrl+1` shows the students.

   1. Test case: press the pencil button of the first student<br>
      Expected: `edit 1 ` is in the command box with the cursor at the end, and nothing has changed.

   1. Test case: press the bin button of a student with a lesson, then `Confirm`<br>
      Expected: The confirm bar asks `Delete student ...?`. After `Confirm`, the result box shows what would be deleted and the bar asks again for `delete N confirm`. `Cancel` or `Esc` leaves the data as it is.

   1. Test case: press the box button of a student, then use `Archived` and the arrow button<br>
      Expected: The student leaves the list, shows with an `ARCHIVED` badge under `Archived`, and returns with the arrow.

   1. Test case: hover over a button of a toolbar<br>
      Expected: The tooltip says `Same as typing:` and the command.

1. Detail pane

   1. Test case: `today`, then `view 1`, then press `Page Down` in the command box<br>
      Expected: The pane shows the dashboard, then the overview, and scrolls without the cursor leaving the command box. The result box says `Shown in the detail pane.`

   1. Test case: make the window as small as it goes<br>
      Expected: It stops at 640 by 480 pixels, and the command box and the result box are visible.

1. Guided tour

   1. Test case: delete `preferences.json` and start TutorFlow<br>
      Expected: The tour opens at step 1 of 10, over a window that is still usable.

   1. Test case: press `Next` twice, then the example button<br>
      Expected: The example `add n/Alex Tan ...` is in the command box and nothing has been added.

   1. Test case: press `Esc`, close TutorFlow and start it again<br>
      Expected: The tour does not open again.

   1. Test case: `tour`, or `Help` > `Guided tour`<br>
      Expected: The tour opens at step 1 again.

### Saving data

1. Dealing with missing/corrupted data files

   1. Prerequisites: Close TutorFlow, then open `data/tutorflow-v14.json` and damage it, for example by deleting its last `}`.

   1. Test case: Launch TutorFlow.<br>
      Expected: TutorFlow starts with an empty list. The result box says that the data file could not be read and has been set aside as `data/tutorflow-v14.json.corrupt`, and the damaged file is in the `data` folder under that name.

   1. Test case: Add a student with `add n/Test p/91234567 l/S4 s/Math r/50`, close TutorFlow and launch it again.<br>
      Expected: The student is still there, because it was saved to a new `tutorflow-v14.json`. The `.corrupt` file is unchanged.

   1. Test case: Close TutorFlow, delete `data/tutorflow-v14.json` and launch TutorFlow.<br>
      Expected: With no legacy addressbook.json either, TutorFlow starts empty. Use demo confirm to load sample students.

1. Dealing with a data file that cannot be opened

   1. Prerequisites: Close TutorFlow. As a user who is not an administrator, remove the read permission of `data/tutorflow-v14.json` (for example `chmod 000`). To test the migration, do this to `data/addressbook.json` instead, with no `data/tutorflow-v14.json`.

   1. Test case: Launch TutorFlow.<br>
      Expected: A dialog says that TutorFlow could not open the file and why, and that nothing has been changed. TutorFlow does not start, the file is not renamed, and no `.corrupt` file or new data file appears.

   1. Test case: Restore the permission and launch TutorFlow.<br>
      Expected: The students are loaded, or, for the older file, migrated into a new `data/tutorflow-v14.json`.

1. Dealing with a data file that cannot be written

   1. Prerequisites: Make `data/tutorflow-v14.json` read-only, as a user who is not an administrator.

   1. Test case: Run any command, such as `list`.<br>
      Expected: The result box says that the change was applied in this session but could not be saved, and why. `exit` does not close TutorFlow until the file can be written again.

### Migrating data

1. Opening data from an older version

   1. Prerequisites: Close TutorFlow. Delete `data/tutorflow-v14.json` and put a data file from an older version at `data/addressbook.json`.

   1. Test case: Launch TutorFlow.<br>
      Expected: The students and lessons of the old file are listed. `data/tutorflow-v14.json` now exists, and `data/addressbook.json` is unchanged.

1. A data file from a newer version

   1. Prerequisites: Close TutorFlow, and change `schemaVersion` in `data/tutorflow-v14.json` to `99`.

   1. Test case: Launch TutorFlow.<br>
      Expected: A dialog says that TutorFlow cannot start and names the supported versions. The data file is unchanged and no `.corrupt` file is made.
