# v1.5 snapshot test runners

`FinalLessonCheck.java` and `GuiJourney.java` are **unmaintained snapshots for the
published TutorFlow v1.5 JAR**, checked on 1 November 2026. They are outside the
Gradle source sets: normal CI, JUnit and Checkstyle do not compile or run them.
They call implementation internals and are not guaranteed to compile or pass
against v1.6 or later. A compile failure on a later JAR does not establish a
product defect. Use the [reusable manual plan](../../docs/testing/LessonStatusManualTesting.md)
for testing the current release.

The [dated verification record](records/2026-11-01.md) contains the tested targets,
results and scene captures. The source links below are relative to this branch
or commit, so they can be opened before this PR is merged:

- [Command/persistence runner](FinalLessonCheck.java)
- [JavaFX GUI runner](GuiJourney.java)

## Target and prerequisites

Download `tutorflow.jar` from the [v1.5 release](https://github.com/AY2627S1-CS2103T-F15-1/tp/releases/tag/v1.5).
The tested asset has SHA-256:

```text
f0e6178c6833087b58669b17fce0b661b9819e6443104fdabd57e552e036d681
```

Use a Java 25 JDK that includes JavaFX (the recorded run used FX Zulu 25.0.3).
The GUI runner needs a graphical desktop. The recorded results are from macOS;
these runners do not establish Windows/Linux compatibility. Use `;` instead of
`:` in Java classpaths on Windows.

## Command and persistence checks

From the repository root, with the target JAR named `tutorflow.jar`, run:

```text
mkdir manual-check-classes
javac -cp tutorflow.jar -d manual-check-classes test/manual/FinalLessonCheck.java
java -ea -cp "tutorflow.jar:manual-check-classes" FinalLessonCheck manual-check-results "published v1.5"
```

Use a **new results directory** for each run. It contains the transcript, outcome
summary and isolated JSON datasets. The runner does not open or edit normal app data.

## JavaFX GUI checks

Use a **new, empty disposable folder** containing only a copy of `tutorflow.jar`.
Run the following commands from that folder, replacing the source path with the
absolute path to your checkout. Each phase opens and closes the actual application;
run the three phases in order in the same folder to retain their test records.
Do not use a folder containing real tutor records.

```text
mkdir gui-check-classes
javac -cp tutorflow.jar -d gui-check-classes /absolute/path/to/repository/test/manual/GuiJourney.java
java -ea -cp "tutorflow.jar:gui-check-classes" GuiJourney exercise
java -ea -cp "tutorflow.jar:gui-check-classes" GuiJourney reopen
java -ea -cp "tutorflow.jar:gui-check-classes" GuiJourney controls
```

These are automated GUI integration checks using synthetic key events, not human
physical keyboard/mouse testing. The runner writes transcripts and scene captures
into `evidence-exercise`, `evidence-reopen` and `evidence-controls`.
