# TutorFlow User Guide

TutorFlow is a **desktop application that keeps your students and lessons in one place, optimized for use through a Command Line Interface (CLI)** while retaining the benefits of a Graphical User Interface (GUI).
For power-users, TutorFlow can help you manage your students and lessons faster than traditional GUI applications.

<!-- * Table of Contents -->
<page-nav-print />

--------------------------------------------------------------------------------------------------------------------

## Quick start

1. Ensure that Java `25` or later is installed on your computer.<br>
   **Mac users:** Ensure you have the precise JDK version prescribed [here](https://se-education.org/guides/tutorials/javaInstallationMac.html).

1. Download the latest `.jar` file from [the TutorFlow releases page](https://github.com/AY2627S1-CS2103T-F15-1/tp/releases/latest).

1. Copy the file to the folder you want to use as the _home folder_ for TutorFlow.

1. Open a terminal, `cd` to the folder containing the JAR file, and run `java -jar tutorflow.jar`.<br>
   A GUI similar to the one below should appear in a few seconds. A new dataset starts empty, and the first time that TutorFlow opens, a short [guided tour](#taking-the-guided-tour-tour) shows you around the window. You can leave the tour at any time and start it again with `tour`. The screenshot below shows a list that already contains students; to look around with sample students instead of your own, type `demo`, then `demo confirm`, and type `demo exit` to go back to your own data.<br>
   ![Ui](images/Ui.png)

1. Type a command in the command box and press Enter to execute it. For example, type **`help`** and press Enter to see the list of commands.<br>
   Some example commands you can try:

   * `list` : Lists all students.

   * `add n/John Doe p/98765432 l/S4 s/Math r/50` : Adds a student named `John Doe`, who is in Secondary 4, learns Math and pays $50 per hour.

   * `delete 3` : Deletes the 3rd student shown in the current list.

   * `clear confirm` : Deletes all students, lessons and homework.

   * `exit` : Exits the app.

1. Refer to the [Features](#features) section below for details of each command.

--------------------------------------------------------------------------------------------------------------------

## The window

TutorFlow shows your records in a window and takes your commands in the command box. Everything that a button in the window does is also a command that you can type, so you can use the keyboard, the mouse or both.

| Part | What it shows |
|------|---------------|
| **Tabs** (left) | The `Students`, `Lessons` and `Homework` lists, each with the numbers that commands use. Press `Ctrl+1`, `Ctrl+2` or `Ctrl+3` to switch between them. |
| **Detail pane** (right) | Today's dashboard, an agenda, or the overview of a student, and it scrolls when there is more than fits. |
| **Result box** | The answer to every command, or the reason why it was refused. |
| **Command box** | Where you type commands. |
| **Orange strip** | Appears only while the [demo](#trying-sample-data-demo) runs, and has a button to leave it. |
| **Confirm bar** | Appears above the command box when a command needs your confirmation. |
| **Status bar** | The data file that TutorFlow saves to. |

The window can be made as small as 640 by 480 pixels, and the divider between the tabs and the detail pane can be dragged.

### Tabs and their buttons

Each tab starts with buttons that run a command that you could type. Point at a button to see which command it is.

| Tab | Buttons | Commands |
|-----|---------|----------|
| `Students` | Active, Archived, All | `list`, `list archived/`, `list all/` |
| `Lessons` | Today, This week | `agenda`, `agenda week/` |
| `Homework` | All, Overdue, Outstanding | `hw list`, `hw list overdue/`, `hw list outstanding/` |

* The `Students` tab shows the students that [`list`](#listing-students-list) and [`find`](#locating-students-by-name-find) select, with the same numbers. An archived student has an `ARCHIVED` badge.
* The `Lessons` tab shows the lessons that the lesson commands count from, so it is empty until you run [`agenda`](#viewing-the-agenda-agenda) or [`lesson list`](#listing-the-lessons-of-a-student-lesson-list). It then shows the lessons of that list and keeps in step when a lesson is scheduled, moved or cancelled.
* The `Homework` tab shows the homework of the last [`hw list`](#listing-homework-hw-list) and is drawn again after every command, so a row shows the homework as it is. A row for homework that has been deleted since stays in place, struck through and marked `DELETED`, so a number never comes to mean another item.
* A command that fills a list, such as `agenda` or `hw list`, brings its tab to the front, and so does adding a student.

Each row has small buttons. Each one has a tooltip that says what it does, and a screen reader reads the same text.

| Button | On | What it does |
|--------|----|--------------|
| Pencil | A student, a lesson that is still to be taught, or homework | Puts the start of the command in the command box, for you to complete and run: `edit N `, `lesson move N ` or `hw edit N desc/... due/...` with the current description and due date. Nothing runs until you press Enter. |
| Box | An active student | Runs [`archive N`](#archiving-and-restoring-students-archive-and-restore). |
| Arrow out of a tray | An archived student | Runs `restore N`. |
| Cross | A lesson that is still to be taught | Asks `Cancel lesson N with NAME?` and, if you confirm, runs `lesson cancel N`. |
| Bin | A student, or homework | Asks `Delete ... ?` and, if you confirm, runs `delete N` or `hw delete N`. |

### The confirm bar

A button never deletes at once. It asks first in the confirm bar, with `Confirm` and `Cancel` buttons. If the command then needs the confirmation that it also needs when typed, for example because the student has lessons, the result box shows what would be deleted and the confirm bar asks again, this time for `delete N confirm`.

Press `Cancel` or `Esc` to leave everything as it is. Running any other command also takes the bar away.

### The detail pane

* When TutorFlow starts, and after `today`, the pane shows the [dashboard](#viewing-todays-dashboard-today). It is drawn again after every command, so it never shows old data.
* [`agenda`](#viewing-the-agenda-agenda) and [`view`](#viewing-a-students-overview-view) draw their answer in the pane, and the result box names what is shown there. These views are not redrawn by later commands: they show the data as it was when you asked.
* Scroll with the mouse, or press `Page Up` and `Page Down` to scroll by most of a screen, and `Ctrl+Up` and `Ctrl+Down` to scroll by a few lines, without leaving the command box.
* A warning, such as the reminder of unsaved changes, stays in the result box while a view is shown.

--------------------------------------------------------------------------------------------------------------------

## Features

<box type="info" seamless>

**Notes about the command format:**<br>

* Words in `UPPER_CASE` are the parameters to be supplied by the user.<br>
  For example, in `add n/NAME`, replace `NAME` with a value such as `John Doe`.

* Items in square brackets are optional.<br>
  For example, `n/NAME [e/EMAIL]` can be used as `n/John Doe e/john@example.com` or as `n/John Doe`.

* Items followed by `...` can appear zero or more times.<br>
  For example, `s/SUBJECT...` can be written as `s/Math` or `s/Math s/Physics`.

* Items such as `week/` and `all/` are flags: write them on their own, without a value.<br>
  For example, `agenda week/` is accepted, but `agenda week/yes` is rejected.

* Parameters can be in any order.<br>
  For example, if the command specifies `n/NAME p/PHONE_NUMBER`, `p/PHONE_NUMBER n/NAME` is also acceptable.

* `list` accepts academic filters and archive flags; invalid or extra arguments are rejected.<br>
  For example, `list l/S4 s/Math` shows active S4 Math students.

* `today`, `save`, `tour` and `exit` take no parameters. If you type anything after them, TutorFlow shows an error and does not run them.<br>
  For example, `exit now` is rejected, so a mistyped command cannot close the app.

* If you enter an unknown command, such as `foo`, TutorFlow shows `Unknown command. Type "help" for list of commands.` Type `help` to see the available commands.

* If you are using a PDF version of this document, be careful when copying and pasting commands that span multiple lines as space characters surrounding line-breaks may be omitted when copied over to the application.
</box>

### Viewing help: `help`

Shows every command, or how to use one command, without leaving TutorFlow.

Format: `help [COMMAND]`

* `help` shows every command with a one-line summary.
* `help COMMAND` shows what the command does, its parameters and an example. `COMMAND` is the command as you type it, so for the lesson commands write both words, such as `help lesson add`.
* `help lesson` lists the commands that start with `lesson`.
* `COMMAND` is case-sensitive, like every command. For a name that is not a command, such as `help foo`, TutorFlow says `No help available for 'foo'. Type 'help' for the full list.`
* To read this guide inside TutorFlow, use the `Help` menu or press `F1`. The guide opens in a resizable window and works offline. Use its Contents links to jump to any feature. External links are disabled inside the offline viewer, which shows a message when you activate one. It shows the User Guide bundled with your version of the app. Click `Reload guide` to return to that bundled guide.
* The command list and `help COMMAND` are short syntax references. The full guide includes the field limits, examples and behavior rules.
* Documentation changes appear in the next build of the app. When running from source, restart with `./gradlew run` (Windows: `.\gradlew.bat run`) to bundle the current `docs/UserGuide.md`. For a JAR, rebuild with `./gradlew shadowJar` or download an updated release, then restart with that JAR. `Reload guide` does not download newer documentation into an older JAR.

Examples:
* `help` lists all commands.
* `help lesson move` shows how to reschedule a lesson.

### Adding a student: `add`

Adds a student to TutorFlow.

Format: `add n/NAME p/PHONE_NUMBER l/LEVEL s/SUBJECT... r/RATE [e/EMAIL] [v/VENUE]`

* `NAME` is 1-100 printable characters after trimming and must contain at least one letter. It may contain letters in any language (such as `José Álvarez` or `陈伟明`), digits, spaces and `. , ' ( ) / - @`, such as `Tan Wei Ming (twin A)`, `Priya d/o Kumar` or `John @ Tan`. It does not matter how an accent is typed, so `José` written with one character and with a letter and a separate accent is the same name. Avoid typing a `/` right after a space when it would look like a prefix, such as `s/o`, because TutorFlow reads it as the start of the next parameter.
* `LEVEL` is one of `P1` to `P6` (primary), `S1` to `S5` (secondary) or `J1` to `J2` (junior college). It is not case-sensitive, so `s3` is the same as `S3`.
* `PHONE_NUMBER` has 8 to 15 digits and can start with `+`, for example `91234567` or `+6591234567`. Spaces and hyphens between the digits are allowed and are removed, so `9123 4567` is stored as `91234567`.
* `SUBJECT` is free text of up to 30 letters in any language, digits, spaces, `&` and `-`, such as `Math`, `English & Literature` or `华文`. Subjects are not case-sensitive.
* `RATE` is the amount charged per hour in SGD, from `0` to `9999.99` with at most 2 decimal places. A leading `$` is accepted, so `50`, `50.00` and `$50` are the same rate.
* `VENUE` is where the student is usually taught, up to 100 characters.
* Extra spaces in a `NAME` are ignored, so `John  Tan` is stored as `John Tan`.
* A student can have at most 10 different subjects, so a command with more than 10 `s/SUBJECT` values is rejected. The same limit applies when you [edit](#editing-a-student-edit) a student.
* Two students are duplicates if they have the same phone number and a name that matches when letter case and extra spaces are ignored. A duplicate is not added.
* Two different students can share a name, for example two students called `Tan Wei Ming` with different phone numbers. TutorFlow adds the student and shows a note, so that you can spot a double entry.

<box type="tip" seamless>

**Tip:** A student must have between 1 and 10 subjects. The email and the venue are optional.
</box>

Examples:
* `add n/John Doe p/98765432 l/S4 s/Math r/50`
* `add n/Betsy Crowe p/1234567 l/J1 s/Physics s/Chemistry r/62.50 e/betsycrowe@example.com v/Blk 123 Bishan St 13`

### Listing students: `list`

Shows the students that you work with, and numbers them again so that the other commands can refer to them.

Format: `list [l/LEVEL] [s/SUBJECT] [archived/|all/]`

* `list` on its own shows the **active** students, which are all the students who are not archived. The result says `Listed active students.`
* `l/LEVEL` keeps only the students of that level, and `s/SUBJECT` keeps only the students who take that subject. If both are given, a student must match both. Their values follow the same rules as for [`add`](#adding-a-student-add).
* `archived/` shows only the archived students, and `all/` shows the active and the archived students together. They are flags: write them on their own, without a value, and not both at once.
* When any of these is given, the result says how many students match, such as `Listed 3 matching student(s).`
* The numbers on screen are the numbers that `edit`, `view`, `delete`, `archive`, `restore`, `lesson add` and `hw add` take, so list again after a filter before you use a number.
* An archived student is shown with an `ARCHIVED` badge.

Examples:
* `list` shows the active students.
* `list l/S3 s/Math` shows the active students who are in Secondary 3 and learn Math.
* `list archived/` shows the archived students, so that you can restore one.
* `list all/` shows everyone.

### Editing a student: `edit`

Edits an existing student in TutorFlow.

Format: `edit INDEX [n/NAME] [p/PHONE_NUMBER] [e/EMAIL] [l/LEVEL] [s/SUBJECT]... [r/RATE] [v/VENUE]`

* Edits the student at the specified `INDEX`. The index refers to the index number shown in the displayed student list. The index **must be a positive integer** 1, 2, 3, ...
* At least one of the optional fields must be provided.
* Existing values will be updated to the input values.
* When editing subjects, all of the student's existing subjects are replaced; adding subjects is not cumulative. A student must keep at least one subject.
* After changing a student's name or phone number, run `lesson list st/STUDENT_INDEX` again before using lesson numbers.
* The values for the level, subjects, rate and venue follow the same rules as for [`add`](#adding-a-student-add).
* If the edit gives the student the name of another student, it is accepted and TutorFlow shows a note. If the phone number is also the same as that student's, the edit is rejected as a duplicate.

Examples:
*  `edit 1 p/91234567 e/johndoe@example.com` Edits the phone number and email address of the 1st student to be `91234567` and `johndoe@example.com` respectively.
*  `edit 2 l/S4 s/Math s/Chemistry` Changes the 2nd student to Secondary 4, and the subjects to `Math` and `Chemistry`.

### Locating students by name: `find`

Finds students whose names contain any of the given keywords.

Format: `find KEYWORD [MORE_KEYWORDS]`

* The search is case-insensitive; for example, `hans` matches `Hans`.
* Keyword order does not matter; for example, `Hans Bo` matches `Bo Hans`.
* The search considers only names.
* A keyword matches any part of a name; for example, `Han` matches `Hans` and `Johan`, and `wei` matches `Tan Wei Ming`.
* A keyword can be up to 100 characters long.
* Students matching at least one keyword are returned (an `OR` search); for example, `Hans Bo` returns `Hans Gruber` and `Bo Yang`.

Examples:
* `find John` returns `john` and `John Doe`
* `find alex david` lists `Alex Yeoh` and `David Li` if those students are recorded. If only those two students match, the result says `2 student(s) listed!` and the student list shows their current details with new index numbers.

### Viewing a student's overview: `view`

Shows what you need to prepare for one student, on one screen.

Format: `view INDEX`

* Shows the student at the specified `INDEX`.
* The index refers to the index number shown in the displayed student list.
* The index **must be a positive integer** 1, 2, 3, ...
* The overview shows, in this order:
  * the student's level, subjects, rate, phone number and, if recorded, email, usual venue and remark
  * the **next lesson**: the date, time, subject and venue of the first scheduled lesson from today onwards
  * the **recent lesson notes**: the notes of the 3 most recent completed lessons, newest first
  * the **lesson history**: how many lessons are completed, cancelled and missed
  * the **outstanding homework**: assignments awaiting submission or review, with due dates and feedback
* A section with nothing to show says so, for example `No upcoming lessons.`, instead of disappearing.
* The command only shows information. It does not change the student list, so the indexes you use for other commands stay the same.
* The overview is drawn in the [detail pane](#the-detail-pane), and the result box says `Shown in the detail pane.`

Examples:
* `list` followed by `view 2` shows the overview of the 2nd student.
* `find Betsy` followed by `view 1` shows the overview of the 1st student in the results of the `find` command.

### Adding a remark to a student: `remark`

Sets a free-text remark on a student, such as a preference or something to remember.

Format: `remark INDEX r/REMARK`

* Sets the remark of the student at the specified `INDEX`, which is the index number shown in the displayed student list and **must be a positive integer** 1, 2, 3, ...
* The new remark replaces the old one. Writing `r/` with nothing after it removes the remark.
* A remark is shown in the student's card and in [`view`](#viewing-a-students-overview-view).

Examples:
* `remark 1 r/Prefers morning lessons` sets the remark of the 1st student.
* `remark 1 r/` removes the remark of the 1st student.

### Deleting a student: `delete`

Deletes the specified student from TutorFlow.

Format: `delete INDEX [confirm]`

* Deletes the student at the specified `INDEX`.
* The index refers to the index number shown in the displayed student list.
* The index **must be a positive integer** 1, 2, 3, ...
* A student with no lessons or homework is deleted immediately.
* Deleting a student also deletes **all of their lessons and homework**, including feedback, and this cannot be undone. If the student has linked records, TutorFlow shows their name and phone number, how many lessons and homework items would be deleted, how many of them are upcoming, and how many homework items have feedback. It deletes nothing and asks you to repeat the command with `confirm`.
* If you only want the student out of your lists, use [`archive`](#archiving-and-restoring-students-archive-and-restore) instead. It keeps the lessons, the homework and the history, and when the student has homework, the preview of `delete` says so.
* The keyword `confirm` is in lower case.
* Once a student is deleted, the result says what else was deleted with them, such as `Deleted student: ..., along with 2 lesson(s) and 3 homework item(s).`

Examples:
* `list` followed by `delete 2` deletes the 2nd student, if they have no lessons or homework.
* `delete 3` on a student with 12 lessons shows a prompt; `delete 3 confirm` then deletes the student and the 12 lessons.
* `find Betsy` followed by `delete 1` deletes the 1st student in the results of the `find` command.

<box type="tip" seamless>

**In the window:** the bin button on a student asks first, and then runs `delete`. If the student has linked records, the confirm bar asks a second time before it runs `delete INDEX confirm`.
</box>

### Archiving and restoring students: `archive` and `restore`

Puts a student away without losing anything, and brings the student back.

Format: `archive INDEX` and `restore INDEX`

* `archive` takes the student at the specified `INDEX` of the displayed list out of the active list. The student's lessons, homework and feedback are all kept, and the lessons stay on the agenda.
* An archived student cannot be given new lessons or new homework. Status changes and feedback on the existing homework can still be recorded.
* To restore a student, show the archived students with `list archived/` first and then use `restore INDEX`. The student returns to the active list with everything intact.
* Archiving a student who is archived already, or restoring one who is active already, is refused and changes nothing.
* After either command the student list refreshes, so use the numbers that are shown.

Examples:
* `archive 3` archives the 3rd student.
* `list archived/` followed by `restore 1` restores the first archived student.

### Writing student commands together: `student`

Format: `student COMMAND [ARGUMENTS]`

Every student command can also be written after the word `student`: `student add`, `student edit`, `student delete`, `student find`, `student view`, `student list`, `student archive` and `student restore`. They take the same arguments as the commands without `student`, so `student find wei` is the same as `find wei`.

### Sorting students by name: `sort`

Sorts all students by name, ignoring upper and lower case.

Format: `sort [asc|desc]`

* `asc` sorts from A to Z and `desc` sorts from Z to A. If no order is given, `asc` is used.
* All students are shown after sorting, even if the list was filtered by an earlier `find` command.
* The new order is saved, so the students stay in this order the next time you open the app.

Examples:
* `sort` sorts all students from A to Z.
* `sort desc` sorts all students from Z to A.

### Importing students from a file: `import students`

Adds many students at once from a spreadsheet that you saved as a CSV file.

Format: `import students file/PATH [confirm]`

* `PATH` is where the file is, such as `students.csv`. The path runs to the end of the command, so a path with spaces needs no quotes. Put it in double quotes only if it starts or ends with a space, or if it ends in the word `confirm`.
* The first line of the file names the columns: `name`, `phone`, `level`, `subjects` and `rate` are required, and `email` and `venue` are optional. The columns can be in any order, and upper or lower case does not matter in their names. Separate several subjects with `;`, such as `Math;Physics`. A value that has a comma in it goes in double quotes, as spreadsheet programs write it.
* The file must be UTF-8 text of at most 5 MB, with at most 5000 students.
* Every field is checked by the rules of [`add`](#adding-a-student-add). The import is all or none: if any row has an error, nothing is imported, and the result lists the rows and fields to fix.
* A row for a student who is already in TutorFlow, with the same name and phone number, is skipped. A row with the same name but a different phone number is imported, with a warning, as for `add`.
* Without `confirm`, the command only reads and checks the file and reports how many students are ready to add, how many rows are skipped, and any warnings and errors. Nothing is added. Run the command again with `confirm`, which the result tells you to do, to add the students. In the window the confirm bar offers the same step as a button.
* The students that were added are selected in the student list, so that you can see them.

Examples:
* `import students file/students.csv` checks the file and tells you what an import would do.
* `import students file/students.csv confirm` adds the students.

A file that works:

```
name,phone,level,subjects,rate,email,venue
Alex Tan,91234567,S3,Math;Physics,50,alex@example.com,Blk 512 Bishan St 13
Bella Lim,92345678,P6,Science,45,,
```

### Scheduling a lesson: `lesson add`

Schedules a lesson for a student.

Format: `lesson add st/STUDENT_INDEX [s/SUBJECT] d/DATE t/TIME [dur/MINUTES] [v/VENUE]`

* `STUDENT_INDEX` is the index number shown in the displayed student list, and **must be a positive integer** 1, 2, 3, ...
* `DATE` is written as `YYYY-MM-DD`, such as `2026-12-22`, or as `D/M/YYYY`, such as `22/12/2026`. It must be today or later, and no more than 2 years ahead, which catches a mistyped year.
* `TIME` is written in 24-hour time, such as `16:30`. `4:30pm` and `1630` are also accepted.
* `MINUTES` is the length of the lesson, from `15` to `480` in steps of `15`. If it is left out, it is `60`.
* `SUBJECT` must be one of the student's subjects, and is not case-sensitive. If the student has only one subject, it can be left out. If the student has several, it must be given.
* `VENUE` is up to 100 characters. If it is left out, the student's usual venue is used, or `—` if the student has none.
* A lesson must end on the day it starts, so `t/23:00 dur/120` is rejected. Split it into two lessons.
* Two lessons are duplicates if they have the same student, subject, date, time and duration. A duplicate is rejected.
* An archived student cannot be given a lesson. Restore the student first.
* Adding a lesson keeps the current day, week or student lesson filter. If the new lesson shifts existing lesson numbers, the result shows the complete current lesson list with fresh numbers. Use those numbers for your next lesson command.

<box type="warning" seamless>

**Overlaps only warn:** if the new lesson overlaps another scheduled lesson by at least one minute on the same date, it is still scheduled and the result lists the lessons it overlaps with. Lessons that end exactly when the next one starts do not overlap. Cancelled lessons are ignored.
</box>

Examples:
* `lesson add st/1 s/Math d/2026-12-22 t/16:30 dur/90` Schedules a 90-minute Math lesson for the 1st student at the student's usual venue.
* `lesson add st/2 d/2026-12-23 t/19:00 v/Online` Schedules a 60-minute lesson online for the 2nd student, in the only subject that student takes.

### Viewing the agenda: `agenda`

Shows the lessons of a day or of a week, in the order that they take place.

Format: `agenda [d/DATE] [week/]`

* Without `d/DATE`, it shows today. `DATE` is written as for [`lesson add`](#scheduling-a-lesson-lesson-add).
* With `week/`, it shows the Monday to Sunday week that contains the date, one day after another. A day without lessons shows `— no lessons —`.
* Each line shows the lesson number, the time, the student, the subject, the venue, and the status if the lesson is not simply scheduled. Lessons that overlap another lesson are marked `⚠ overlaps`.
* Cancelled lessons are listed but are not counted in the number of lessons and the total time.
* The lesson numbers are the ones that `lesson move`, `lesson cancel`, `lesson done` and `lesson missed` take, so run `agenda` or `lesson list` first and then use the number you see.
* Lessons are numbered in time order, so moving a lesson to another time can change its number, and the numbers of others. Run `agenda` or `lesson list` again before you use a number a second time. The result of `lesson move` and `lesson cancel` names the lesson that was changed, so you can check that it was the right one.
* A day without lessons is not an error: it shows `No lessons on Tue 22 Dec 2026.`
* The agenda is drawn in the [detail pane](#the-detail-pane), and the lessons are listed with the same numbers in the `Lessons` tab.

Examples:
* `agenda` Shows today's lessons.
* `agenda d/2026-12-22 week/` Shows the week of Monday 21 December 2026.

### Viewing today's dashboard: `today`

Shows what today requires: today's lessons, past lessons not yet marked completed, and overdue homework. TutorFlow also shows this when it starts.

Format: `today`

* The first section, `TODAY'S LESSONS`, shows the number of lessons today and their total time, followed by the time, student, subject and venue of each lesson. Lessons that overlap another lesson are marked `⚠ overlaps`. Cancelled lessons are left out.
* The second section, `LESSONS AWAITING NOTES`, shows the scheduled lessons from earlier days that were not marked completed, the most recent first.
* The third section, `OVERDUE HOMEWORK`, shows assigned work due before today, or `No overdue homework.`. Run `hw list overdue/` to get selectable numbers.
* An empty section is not an error: it shows `No lessons today.` or `All past lessons are recorded.`
* Today is the date on your computer, so a wrong system clock shows the wrong day.
* The dashboard is drawn in the [detail pane](#the-detail-pane), where it is redrawn after every command, so it never shows old data.

Examples:
* `today` on Thursday 17 September 2026 might show:
  ```
  Thursday, 17 September 2026

  TODAY'S LESSONS (2, 2h 30m)
    16:30-18:00  Tan Wei Ming       Math       Blk 512 Bishan St 13  ⚠ overlaps
    17:00-18:00  Priya s/o Kumar    Physics    Online  ⚠ overlaps

  LESSONS AWAITING NOTES (1)
    Tue 15 Sep  Lim Jia Hui        Science    not yet marked completed
  ```

### Listing the lessons of a student: `lesson list`

Shows the lessons of one student.

Format: `lesson list st/STUDENT_INDEX [all/]`

* `STUDENT_INDEX` is the index number shown in the displayed student list.
* By default, only upcoming lessons are shown, which means scheduled lessons from today onwards. With `all/`, past, completed, cancelled and missed lessons are shown as well.
* The lessons are numbered in date order. These are the numbers that `lesson move`, `lesson cancel`, `lesson done` and `lesson missed` take.
* A lesson records the academic level that the student had when it was booked, so changing the student's level later does not rewrite history. With `all/`, each lesson shows its recorded level. Lessons from before levels were recorded show `unknown (not recorded)`, because their past level cannot be worked out from the present profile.

Examples:
* `lesson list st/1` Shows the upcoming lessons of the 1st student.
* `lesson list st/1 all/` Shows every lesson of the 1st student.

### Recording how you will prepare for a lesson: `lesson prep`

Records what you plan to prepare for a lesson, apart from the notes that you write after it.

Format: `lesson prep INDEX n/NOTES`

* `INDEX` is the lesson number in the list that is currently shown by `agenda` or `lesson list`.
* `NOTES` is 1–1000 printable characters on one line.
* The preparation does not complete the lesson and does not change its teaching notes. It appears in `lesson list` and in the next-lesson section of [`view`](#viewing-a-students-overview-view).

Examples:
* `lesson prep 1 n/Bring the mock paper` records the preparation for the 1st lesson.

### Rescheduling a lesson: `lesson move`

Moves a lesson to another date, time, duration or venue, and keeps everything else.

Format: `lesson move INDEX [d/DATE] [t/TIME] [dur/MINUTES] [v/VENUE]`

* `INDEX` is the lesson number in the list that is currently shown by `agenda` or `lesson list`, and **must be a positive integer** 1, 2, 3, ...
* At least one of the optional fields must be given. Fields that are not given stay as they were.
* The values follow the same rules as for [`lesson add`](#scheduling-a-lesson-lesson-add). A new date must be today or later. Only a date that you give is checked, so you can still change the time of a lesson that has already passed.
* A completed, cancelled or missed lesson cannot be rescheduled. Moving a missed lesson would lose the record that it was missed, so schedule a make-up lesson with [`lesson add`](#scheduling-a-lesson-lesson-add) instead.
* If the move makes the lesson identical to another lesson, it is rejected. If it makes the lesson overlap another lesson, it is moved and the result warns you.

Examples:
* `lesson move 1 d/2026-12-24` Moves the 1st lesson to 24 December 2026 at the same time.
* `lesson move 2 t/17:00 v/Online` Moves the 2nd lesson to 17:00 and changes its venue to `Online`.

### Cancelling a lesson: `lesson cancel`

Cancels a lesson but keeps its record, so that you can still see that the slot was booked.

Format: `lesson cancel INDEX [r/REASON]`

* `INDEX` is the lesson number in the list that is currently shown by `agenda` or `lesson list`.
* `REASON` is up to 200 characters on one line, such as `Student unwell`.
* The lesson stays in the agenda as cancelled and no longer overlaps other lessons.
* A lesson that is already cancelled or already completed cannot be cancelled.
* A cancelled lesson does not block its slot. To book that time again, use `lesson add` with the same details. The cancelled record stays in your history.

Examples:
* `lesson cancel 1` Cancels the 1st lesson.
* `lesson cancel 3 r/Student unwell` Cancels the 3rd lesson and records the reason.

### Completing a lesson and recording notes: `lesson done`

Marks a lesson completed and optionally records what was taught.

Format: `lesson done INDEX [n/NOTES]`

* `INDEX` is the lesson number from the current `agenda` or `lesson list`, not the student number. It must be a positive integer.
* Only lessons dated today or earlier can be completed, using your computer's local date. A cancelled lesson cannot be completed.
* `NOTES` is optional free text of 1–1000 printable characters on one line. Leading/trailing whitespace is trimmed and repeated spaces are collapsed. An empty `n/`, line breaks and repeated `n/` parameters are rejected.
* Completing a lesson retains its student, subject, date, time, duration and venue. The status and notes are saved automatically. Use `view STUDENT_INDEX` to see recent notes and the completed-lesson count.
* Repeating the command with `n/NOTES` updates the notes on the same completed lesson. Omitting `n/` retains existing notes. A lesson previously marked missed can also be changed to completed.
* A completed lesson disappears from an upcoming-only `lesson list`. Use `lesson list st/STUDENT_INDEX all/` to include it again; check the current lesson number before updating notes.
* A space followed by `n/` starts a note parameter, so it cannot also appear literally inside the note text.

Examples:
* `agenda` followed by `lesson done 1` completes today's 1st lesson without adding notes.
* `lesson list st/1 all/` followed by `lesson done 2 n/Covered quadratic roots. Revise factorisation.` completes the 2nd displayed lesson or updates its notes if already completed.

### Recording a missed lesson: `lesson missed`

Marks a scheduled lesson as missed without deleting its record.

Format: `lesson missed INDEX`

* `INDEX` is the positive lesson number from the current `agenda` or `lesson list`, not the student number. Use `lesson list st/STUDENT_INDEX all/` to select a past lesson.
* Only lessons dated today or earlier can be marked missed, using your computer's local date. Future, cancelled and completed lessons are rejected without changing data.
* The student, subject, date, time, duration, venue and existing notes are retained. The missed status is saved automatically and counted in `view STUDENT_INDEX` under lesson history.
* A missed lesson disappears from an upcoming-only list and no longer counts as a scheduling overlap. Use `all/` to include it again and check its current lesson number.
* Repeating `lesson missed` on the same missed lesson leaves it unchanged. If the lesson actually took place, use `lesson done INDEX [n/NOTES]` to correct it to completed.
* No extra parameters are accepted. This command does not record fees or payments.

Examples:
* `agenda` followed by `lesson missed 1` marks today's 1st displayed lesson as missed.
* `lesson list st/1 all/` followed by `lesson missed 2` marks the 2nd displayed lesson as missed, provided it is scheduled for today or earlier.

### Homework

Homework is an assignment that you give a student, with a due date, a status and, once it has been handed in, your feedback. TutorFlow keeps the whole history of a student's homework.

**Numbers.** `hw edit`, `hw status`, `hw feedback` and `hw delete` take the number of an item in the list that the last [`hw list`](#listing-homework-hw-list) showed, which is also what the `Homework` tab shows. The numbers stay as they are until you run `hw list` again: assigning, editing or deleting homework, archiving students and renaming them do not shift them. If you give the number of homework that has been deleted since, the result says so and nothing else is changed, so a number never selects another item. Numbers are not kept when you close TutorFlow, so run `hw list` first after you open it.

**Status.** New homework is `assigned`. It can be `submitted` and then `reviewed`. Homework that is still `assigned` after its due date is shown as `OVERDUE`. Overdue is worked out from the due date and cannot be set, and handing the work in clears it.

**Dates.** A date is written as `YYYY-MM-DD`, such as `2026-12-22`, or as `D/M/YYYY`, such as `22/12/2026`, and `today` and `tmr` also work. When you assign homework or change a due date, the date must be today or later and no more than one calendar year ahead, using your computer's date.

#### Assigning homework: `hw add`

Format: `hw add st/STUDENT_INDEX desc/DESCRIPTION due/DATE`

* `STUDENT_INDEX` is the index number in the displayed student list. The student must not be archived: restore the student first.
* `DESCRIPTION` is 1–200 printable characters on one line. Spaces around it are trimmed and repeated spaces become one.
* A description may contain words that look like prefixes of other commands, such as `n/`, because only `st/`, `desc/` and `due/` are read here.
* A student cannot have two assignments with the same description and due date. The comparison ignores letter case and repeated spaces, and it includes reviewed homework.
* Homework can be assigned to a student who has no upcoming lesson. The result then adds a note, and the homework is still recorded.
* The result tells you to run `hw list`, because the new assignment has no number yet.

Examples:
* `hw add st/1 desc/Algebra Q1-10 due/tmr`
* `hw add st/2 desc/Past year paper due/2026-12-22`

#### Listing homework: `hw list`

Format: `hw list [st/STUDENT_INDEX] [overdue/|outstanding/]`

* Without options, it lists the homework of every student, including reviewed homework and feedback, so it is also the complete history. With `st/STUDENT_INDEX`, only that student's homework is listed, and an archived student's history can be listed after `list archived/`.
* `overdue/` keeps the homework that is `assigned` after its due date. `outstanding/` keeps the homework that is `assigned` or `submitted`. They are flags without values, and they cannot be used together.
* Overdue homework comes first, then the earliest due date first. Homework that is due on the same day stays in the order in which it was assigned.
* The result numbers the items. A list with no match says `No homework matches those filters.` and TutorFlow says `No homework recorded yet.` when there is none at all.

Examples:
* `hw list`
* `hw list st/1 overdue/`

#### Correcting homework: `hw edit`

Format: `hw edit INDEX [desc/DESCRIPTION] [due/DATE]`

* `INDEX` is a number from the last `hw list`. At least one of `desc/` and `due/` must be given.
* The status and the feedback stay as they are, and so does a due date that is in the past if you only change the description. Typing the same old date again is also accepted.
* The new description and due date follow the rules of `hw add`, so the result cannot be a duplicate of another assignment of the same student.

Example: `hw edit 1 due/2026-12-24`

#### Changing the status: `hw status`

Format: `hw status INDEX s/STATUS`

* `STATUS` is `assigned`, `submitted` or `reviewed`, in any letter case. `overdue` cannot be set.
* Setting the status that the homework has already is refused: `That homework is already marked ...`.
* A change back to an earlier status, to correct a mistake, is allowed. The result adds a note, and the feedback that was recorded is kept.

Example: `hw status 1 s/submitted`

#### Recording feedback: `hw feedback`

Format: `hw feedback INDEX f/FEEDBACK`

* `FEEDBACK` is 1–1000 printable characters on one line.
* The homework must be `submitted` or `reviewed`. New feedback replaces the old feedback.

Example: `hw feedback 1 f/Sign errors throughout Q8-12`

#### Deleting homework: `hw delete`

Format: `hw delete INDEX [confirm]`

* Homework without feedback is deleted at once.
* If feedback is recorded, TutorFlow shows it and deletes nothing, and asks for `hw delete INDEX confirm`.
* The numbers of the other items stay as they are until you run `hw list` again.

Examples:
* `hw delete 2`
* `hw delete 2 confirm`

**A journey.** `hw add st/1 desc/Algebra Q1-10 due/tmr`, then `hw list`, `hw status 1 s/submitted`, `hw feedback 1 f/Revisit sign errors`, `hw status 1 s/reviewed`, and finally `hw list st/1` to see the history of the student.

**Where homework appears.** [`view`](#viewing-a-students-overview-view) shows a student's outstanding homework, and [`today`](#viewing-todays-dashboard-today) and the screen that TutorFlow shows when it starts show the overdue homework. They are summaries, and they do not change the numbers that `hw list` gave. Deleting a student, or [`clear`](#clearing-all-entries-clear), also deletes the homework and its feedback.

### Trying sample data: `demo`

Lets you look around with sample students without touching your own data.

Format: `demo [confirm|exit]`

* `demo` on its own shows how many sample students it would load, and loads nothing.
* `demo confirm` loads the sample students in place of your data **on screen only**. Your own students, lessons and homework are kept aside, nothing in the demo is saved, and the window shows an orange strip to say so.
* `demo exit` ends the demo and brings your own data back, with the lists numbered afresh. The strip has an `Exit demo` button that does the same.
* While the demo runs, you can add, edit and delete the sample records as you like. All of it goes away when you leave the demo, and `save` says `Nothing is saved in the demo.`
* A demo does not start while you have unsaved changes, because it would hide them. Run `save` first.
* Closing TutorFlow in the demo does not save the sample data. Your own data file is not touched.

Examples:
* `demo`, then `demo confirm`, then `view 1`.
* `demo exit` to go back.

### Taking the guided tour: `tour`

Shows you around the window, one part at a time.

Format: `tour`

* The tour opens by itself each time that TutorFlow starts, until you have finished or left it once. For a new tutor that is the first start, with an empty dataset, so a new tutor starts with a guide and not with sample data. After that it does not open again by itself, and you can see it again with `tour` or with `Help` > `Guided tour`.
* A card explains each part of the window, and a frame shows where it is. Use `Next` and `Back`, or `Skip tour` or `Esc` to leave at any time.
* Some steps offer an example. The button only puts the command in the command box for you to change, and nothing runs until you press Enter. The window stays usable while the tour is open.
* The tour never changes your data on its own.

### Clearing all entries: `clear`

Deletes **all** students, lessons, and homework from TutorFlow.

Format: `clear confirm`

* Typing `clear` on its own deletes nothing. It only reminds you what would be deleted and how to confirm.
* `confirm` must be written exactly in lower case. Anything else after `clear` is rejected, so a mistyped command can never wipe your data.
* Clearing cannot be undone. TutorFlow saves the empty data straight away, so copy `data/tutorflow-v14.json` first if you may want your records back.
* Clearing data that is already empty works and does nothing.

Examples:
* `clear` shows `This deletes ALL students, lessons, and homework. Type 'clear confirm' to proceed.`
* `clear confirm` deletes everything.

### Exiting the program: `exit`

Exits the program.

Format: `exit`

### Saving the data: `save`

Format: `save`

TutorFlow automatically saves after a command changes data. `save` saves again if an earlier save failed, and otherwise says `All changes have been saved.` It takes no parameters. If saving fails, fix the reported problem and use `save` to retry before closing. Read-only commands do not write the data file.

Each save is written completely to a temporary file beside the data file before replacing the previous save. If writing fails, the previous saved contents remain intact. If your file system cannot replace files atomically, TutorFlow reports a save error; move the app to a local folder that supports atomic file replacement and retry.

If a change cannot be saved, for example because the disk is full or the file is read-only, TutorFlow says `Your change was applied in this session but could not be saved`, and the reason. The change is still in the running app, so fix the problem (free some space, or make the file writable) and run `save` to retry saving. Read-only commands do not write the data file. Closing TutorFlow with `exit`, the window close button, or File > Exit retries saving and is refused if saving still fails, so that you do not lose the change.

Read-only commands do not routinely save. After a failed save, they display an unsaved-changes reminder. Run `save` after fixing the problem; a successful retry clears that reminder.

Only open one instance for a data file: concurrent instances use last-write-wins and can overwrite each other's changes.

Names, venues, reasons and notes must be printable text on one line; repeated spaces are collapsed. Email local parts may include `%`, the terminal domain label must contain at least two letters, and comparison ignores case.

Previously accepted names, short phone numbers, email terminal labels and venues remain readable through explicit legacy loading. Invalid subject counts (outside 1-10 distinct subjects) and invalid cancellation reasons reject the whole data file and use the recovery process below; individual records are never silently discarded.

### Editing the data file

TutorFlow data is saved automatically as a JSON file `data/tutorflow-v14.json` relative to the folder from which TutorFlow is launched. Advanced users are welcome to update data directly by editing that data file.

Every lesson and homework assignment must link to a student in the same file through its internal student ID. Legacy lessons are linked by name and phone during migration. Missing student links are treated as invalid, as described below.

<box type="warning" seamless>

**Caution:**
If your changes make the data file invalid, TutorFlow does not delete it. At the next run it renames the file to `tutorflow-v14.json.corrupt` (or `tutorflow-v14.json.corrupt.1` and so on, if that name is taken), starts with no students and lessons, and tells you where the file went. Fix the file and rename it back to `tutorflow-v14.json` while TutorFlow is closed to get your records back. We still recommend backing up the file before editing it.<br>
If the original cannot be renamed (for example, while another program holds it open), saving is blocked and retries preservation before replacing it. The original bytes are retained in the `.corrupt` copy once the lock clears.

Furthermore, certain edits can cause TutorFlow to behave in unexpected ways (e.g., if a value entered is outside of the acceptable range). Therefore, edit the data file only if you are confident that you can update it correctly.
</box>

### Moving from an older version

This version writes `data/tutorflow-v14.json`. If that file does not exist, TutorFlow reads the older `data/addressbook.json`, if there is one, gives every student and lesson a permanent internal ID, and saves the result as `tutorflow-v14.json` once the whole file has been written. The older file is left untouched, so an older version of TutorFlow can still open it and it serves as a backup. Keep both files. If both exist, `tutorflow-v14.json` is used.

* A data file records a schema number. This version writes schema 15, and it reads files without a schema number and files of schema 14 and 15. A file from before homework existed starts with no homework. An older build cannot open schema 15, which stops it from dropping your homework.
* A file of a schema that this version does not know is never changed or set aside. TutorFlow stops when it starts, says why, and leaves the file where it is. Open the data with a version of TutorFlow that knows its schema, or restore a backup. An older version cannot open data that a newer version saved with extra fields.
* A file that is damaged in another way is set aside as described in [Editing the data file](#editing-the-data-file).
* If the first save of a migration fails, the message at start says that changes are unsaved. Fix the access problem and use `save`. TutorFlow tries again before it exits and does not close through its normal exit flow while the save keeps failing. A restart can still resume the migration from the unchanged older file.

### Archiving data files `[coming in v2.0]`

_Details coming soon ..._

--------------------------------------------------------------------------------------------------------------------

## FAQ

**Q**: How do I transfer my data to another computer?<br>
**A**: Install the app on the other computer and overwrite the data file it creates with the data file from your previous TutorFlow home folder.

**Q**: How do I see the guided tour again?<br>
**A**: Type `tour`, or choose `Help` > `Guided tour`.

**Q**: I loaded the sample students with `demo confirm`. How do I get my own data back?<br>
**A**: Type `demo exit`, or press `Exit demo` on the orange strip. Nothing in the demo is saved, so your own records are exactly as you left them.

**Q**: Why are the `Lessons` and `Homework` tabs empty?<br>
**A**: They list what the last `agenda`, `lesson list` or `hw list` showed, with the numbers that the commands use. Press one of the buttons at the top of the tab, or type the command.

--------------------------------------------------------------------------------------------------------------------

## Known issues

1. **When using multiple screens**, if you move the application to a secondary screen, and later switch to using only the primary screen, the GUI will open off-screen. The remedy is to delete the `preferences.json` file created by the application before running the application again.
2. **If you minimize the Help Window** and then use the `Help` menu or the keyboard shortcut `F1` again, the original Help Window will remain minimized, and no new Help Window will appear. The remedy is to manually restore the minimized Help Window. The `help` command displays command information in the main window.
3. **Chinese and other wide characters** take up more room than Latin letters. In the result box, the columns of `agenda`, `today` and `lesson list` can therefore look misaligned when a name, subject or venue contains them. The information shown is still correct.

--------------------------------------------------------------------------------------------------------------------

## Command summary

Action     | Format, Examples
-----------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------
**Add**    | `add n/NAME p/PHONE_NUMBER l/LEVEL s/SUBJECT... r/RATE [e/EMAIL] [v/VENUE]` <br> e.g., `add n/James Ho p/22224444 l/S3 s/Math s/Physics r/55 e/jamesho@example.com v/Online`
**Clear**  | `clear confirm`
**Delete** | `delete INDEX [confirm]`<br> e.g., `delete 3`, `delete 3 confirm`
**Edit**   | `edit INDEX [n/NAME] [p/PHONE_NUMBER] [e/EMAIL] [l/LEVEL] [s/SUBJECT]... [r/RATE] [v/VENUE]`<br> e.g.,`edit 2 n/James Lee e/jameslee@example.com`
**Find**   | `find KEYWORD [MORE_KEYWORDS]`<br> e.g., `find James Jake`
**List**   | `list [l/LEVEL] [s/SUBJECT] [archived/\|all/]`<br> e.g., `list`, `list l/S3 s/Math`, `list archived/`
**Archive** | `archive INDEX`<br> e.g., `archive 3`
**Restore** | `restore INDEX` (after `list archived/`)<br> e.g., `restore 1`
**Student** | `student add\|edit\|delete\|find\|view\|list\|archive\|restore ...`<br> e.g., `student find wei`
**View**   | `view INDEX`<br> e.g., `view 1`
**Sort**   | `sort [asc\|desc]`<br> e.g., `sort desc`
**Remark** | `remark INDEX r/REMARK`<br> e.g., `remark 1 r/Prefers morning lessons`
**Import** | `import students file/PATH [confirm]`<br> e.g., `import students file/students.csv confirm`
**Demo**   | `demo [confirm\|exit]`<br> e.g., `demo`, `demo confirm`, `demo exit`
**Tour**   | `tour`
**Save**   | `save`
**Help**   | `help [COMMAND]`<br> e.g., `help`, `help lesson add`
**Exit**   | `exit`
**Lesson add** | `lesson add st/STUDENT_INDEX [s/SUBJECT] d/DATE t/TIME [dur/MINUTES] [v/VENUE]`<br> e.g., `lesson add st/1 s/Math d/2026-12-22 t/16:30 dur/90`
**Agenda** | `agenda [d/DATE] [week/]`<br> e.g., `agenda d/2026-12-22 week/`
**Today** | `today`
**Lesson list** | `lesson list st/STUDENT_INDEX [all/]`<br> e.g., `lesson list st/1 all/`
**Lesson prep** | `lesson prep INDEX n/NOTES`<br> e.g., `lesson prep 1 n/Bring the mock paper`
**Lesson move** | `lesson move INDEX [d/DATE] [t/TIME] [dur/MINUTES] [v/VENUE]`<br> e.g., `lesson move 1 d/2026-12-24 t/17:00`
**Lesson cancel** | `lesson cancel INDEX [r/REASON]`<br> e.g., `lesson cancel 1 r/Student unwell`
**Lesson done** | `lesson done INDEX [n/NOTES]`<br> e.g., `lesson done 1 n/Covered quadratic roots`
**Lesson missed** | `lesson missed INDEX`<br> e.g., `lesson missed 1`
**Homework add** | `hw add st/STUDENT_INDEX desc/DESCRIPTION due/DATE`<br> e.g., `hw add st/1 desc/Algebra Q1-10 due/tmr`
**Homework list** | `hw list [st/STUDENT_INDEX] [overdue/\|outstanding/]`<br> e.g., `hw list st/1 overdue/`
**Homework edit** | `hw edit INDEX [desc/DESCRIPTION] [due/DATE]`<br> e.g., `hw edit 1 due/2026-12-24`
**Homework status** | `hw status INDEX s/STATUS`<br> e.g., `hw status 1 s/submitted`
**Homework feedback** | `hw feedback INDEX f/FEEDBACK`<br> e.g., `hw feedback 1 f/Sign errors in Q8-12`
**Homework delete** | `hw delete INDEX [confirm]`<br> e.g., `hw delete 2 confirm`
