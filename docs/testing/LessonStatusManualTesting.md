---
  layout: default.md
  title: "Lesson outcome and correction testing"
---

# Lesson outcome and correction testing

This plan covers lesson completion, missed status, teaching notes and the lookup/optional-value corrections.
It complements the other feature owners' manual tests in the [Developer Guide](../DeveloperGuide.md#appendix-instructions-for-manual-testing).

## Test environment and isolation

Use Java 25 and a **new, empty test folder**, with a copy of the JAR in it. Run `java -jar tutorflow.jar`
from that folder. Never run these checks against a tutor's real records. Each group below starts in a
separate empty folder unless it explicitly says to continue. Enter commands one at a time.
`today` and `tmr` use the computer's local date, so the examples do not expire on a fixed calendar date.
The setup creates one student with one subject, avoiding a missing-subject error.

Lesson numbers and student numbers are separate. Run the indicated `lesson list` or `agenda` before
using a lesson number. After reopening, list lessons again. If a result says the lesson numbers changed,
use the refreshed numbers for the next operation.

## 1. Completion, notes, billing and persistence

Start in a new test folder and enter:

```text
add n/Alice p/91234567 l/S3 s/Math r/60
lesson add st/1 d/today t/08:00 dur/90
lesson list st/1 all/
lesson done 1 n/Covered algebra
view 1
balance st/1
```

Expected: one completed lesson, notes `Covered algebra`, and one $90.00 charge for 90 minutes at
$60 per hour. The student's booking details remain unchanged.

Continue:

```text
lesson list st/1 all/
lesson done 1 n/Revised notes
lesson done 1
view 1
balance st/1
exit
```

Expected: the notes become `Revised notes`; omitting `n/` retains them. There is still one lesson and
one $90.00 charge. Repeating completion does not charge again. Relaunch the same JAR in the same
folder, then run `lesson list st/1 all/`, `view 1` and `balance st/1`. The completed status, notes and
balance must be unchanged.

To test zero fees, start a separate folder, use the same setup with `r/0`, and complete the lesson.
Expected: completion succeeds with an explanation that no automatic charge was recorded. A second
separate case with `r/0.01` and `dur/15` also creates no charge because its calculated fee rounds to zero.

## 2. Rejected transitions and malformed commands

Continue group 1, after relaunching and listing lessons, with:

```text
lesson missed 1
lesson done 0
lesson done -1
lesson done 99
lesson done 1 n/
lesson done 1 n/First n/Second
lesson done 1 notes/Unexpected
```

Expected: each command returns a normal diagnostic and changes neither the lesson, existing notes nor
balance. A completed lesson cannot be marked missed. There is no student or lesson numbered 0 or -1,
and the setup has no lesson 99. Notes cannot be empty, duplicated or supplied through an unknown prefix.

Then enter:

```text
lesson add st/1 d/tmr t/08:00
lesson list st/1 all/
lesson done 2 n/Future
lesson missed 2
lesson cancel 2 r/Test cancellation
lesson done 2
lesson missed 2
```

Expected: the future lesson is number 2. Both outcome changes are rejected while it is in the future.
Cancellation succeeds; neither completion nor missed status can then replace the cancellation.
The first lesson's notes and $90.00 charge remain unchanged. Cancelled lessons remain in this `all/` list.

## 3. Missed-to-completed correction

Start in another empty folder:

```text
add n/Alice p/91234567 l/S3 s/Math r/60
lesson add st/1 d/today t/10:00
lesson list st/1 all/
lesson missed 1
lesson missed 1
view 1
balance st/1
```

Expected: one missed lesson, no automatic charge, and an informative response to the repeated request.
`lesson missed 1 extra` and `lesson move 1 d/tmr` are rejected without mutation. Close and reopen,
list with `lesson list st/1 all/`, and check that the missed status remains.

Continue:

```text
lesson done 1 n/Actually taught
lesson done 1
view 1
balance st/1
```

Expected: the existing lesson becomes completed with `Actually taught`, and is charged once at $60.00.
No second lesson or charge is created. The completed count replaces the missed count for that lesson.

For the existing manual-charge guard, start another folder with the same initial lesson, then run:

```text
lesson missed 1
charge l/1 amt/25
lesson cancel 1
lesson done 1
balance st/1
```

Expected: cancellation is refused while the charge is active; completion succeeds without a second charge.
The balance remains $25.00. In a separate uncharged case, `lesson missed 1` followed by
`lesson cancel 1 r/Mistaken booking` succeeds. These checks describe current behaviour, not a proposal
to change the cancellation policy.

## 4. Lesson numbers after an outcome change

Start in another empty folder:

```text
add n/Alice p/91234567 l/S3 s/Math r/60
lesson add st/1 d/today t/08:00
lesson add st/1 d/today t/09:00
lesson list st/1
lesson done 1
lesson missed 1
lesson list st/1 all/
```

Expected: completing the 08:00 lesson removes it from the upcoming-only selection and displays the
refreshed lesson numbers. The remaining 09:00 lesson becomes number 1, so the following `lesson missed 1`
marks that lesson missed. The final history contains the 08:00 completed lesson and 09:00 missed lesson,
with one $60.00 charge. Check date/time and status, not just the row count. Reopening retains both records.

## 5. Note boundaries and adversarial input

Use a selected lesson from group 1. These are also generated exactly by the packaged-JAR runner below.

| Input | Expected |
|---|---|
| `lesson done 1 n/` plus exactly 1,000 `a` characters | Accepted, saved, and restored with all 1,000 characters |
| The same form with 1,001 characters | Rejected without changing notes or saved bytes |
| The same form with 200,000 characters | Normal rejection, with the app remaining usable |
| Notes containing a line break or Unicode U+2028 line separator | Rejected without mutation |
| `lesson done` with a 36-digit index | Normal invalid-index diagnostic, without an overflow crash |
| `lesson done 1 n/Covered   algebra. x² = 4.` | Repeated spaces normalized; the Unicode text retained |

The valid 1,000-character note needs **1,016 characters for the whole command**, including
`lesson done 1 n/`. A blanket 1,000-character command cap would reject it. Field bounds, command-box
bounds and compatibility of older save files are distinct concerns; do not introduce a storage limit
that rejects supported historical data merely to mirror a new command-input limit.

## 6. Optional contact and feedback corrections

Start in another empty folder:

```text
add n/Alice p/91234567 l/S3 s/Math r/60 e/alice@example.com v/Old tuition venue
lesson add st/1 d/today t/09:00
edit 1 e/
view 1
edit 1 v/
view 1
lesson list st/1 all/
```

Expected: the first edit clears only email; venue remains. The second clears only the student's current
venue. The already-booked lesson keeps its venue snapshot and student link. `edit 1 n/` and
`edit 1 e/ e/` are rejected without mutation. Reopening retains the cleared optional fields.

Continue:

```text
hw add st/1 desc/Algebra due/tmr
hw list
hw feedback 1 f/Too early
hw feedback 1 f/
hw status 1 s/submitted
hw feedback 1 f/For the wrong assignment
hw feedback 1 f/
hw feedback 1 f/
hw list
```

Expected: non-empty feedback on assigned homework is rejected. Clearing absent feedback is allowed.
After submission, feedback can be recorded and explicitly cleared, including repeatedly. The assignment,
due date, status and links remain; only feedback disappears. Reopening retains the submitted assignment
without feedback.

For the Unicode search correction, add `Işık` as a second student and run:

```text
add n/Işık p/92345678 l/S3 s/Math r/50
find ışık
```

Expected: `Işık` is found even though the search uses dotless lowercase i.

## Verification performed on 1 November 2026

The following results are from **programmatic command journeys against packaged JAR classes**, not a
claim that every step was clicked in the GUI. The runner invokes the real parser and `LogicManager`,
uses actual JSON files in separate disposable folders, compares saved and live snapshots after each
successful command, and checks both live records and saved bytes after rejected commands. Reopening
checks construct fresh model and logic instances from the saved file. GUI layout, operating-system
launch behaviour and other owners' full feature sets still require their separate checks.

| Target | Result |
|---|---|
| Published v1.5 JAR, SHA-256 `f0e6178c6833087b58669b17fce0b661b9819e6443104fdabd57e552e036d681` | 85 commands; 211 assertions; no failures |
| JAR built from master `3f0ed182` | Same 85 commands and 211 assertions; no failures |
| Focused parser, command, domain, persistence and alpha-correction JUnit tests on that master | 91 tests; no failures, errors or skips |
| Main/test Checkstyle and Shadow JAR build | Passed |

The selected JUnit classes were `*LessonDone*`, `*LessonMissed*`, `LessonStatusTest`,
`JsonAdaptedLessonTest`, `AlphaReportedIssuesTest` and `HomeworkFeedbackCommandTest`.
Passing these checks does not establish that all of TutorFlow is bug-free. In particular, the separate
finance/startup defect in issue #252 is outside these passing lesson/correction journeys.

### Reproduce the packaged-JAR checks

The runner is [`test/manual/FinalLessonCheck.java`](https://github.com/AY2627S1-CS2103T-F15-1/tp/blob/master/test/manual/FinalLessonCheck.java).
From the repository root, with the target JAR named `tutorflow.jar`, run:

```text
mkdir manual-check-classes
javac -cp tutorflow.jar -d manual-check-classes test/manual/FinalLessonCheck.java
java -ea -cp "tutorflow.jar:manual-check-classes" FinalLessonCheck manual-check-results "local target JAR"
```

On Windows, use `;` instead of `:` in the final classpath. Use a **new results directory** for each
run, because the test cases create records there. The runner writes `transcript.txt`, the outcome
summary and the isolated JSON datasets into that directory. It does not open or edit normal app data.
