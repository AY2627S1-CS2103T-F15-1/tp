---
  layout: default.md
  title: "Joshua Andrew's Project Portfolio Page"
---

### Project: TutorFlow
 
TutorFlow is a desktop application that helps independent private tutors organise and access information about their students, lessons, homework, and payments. The user interacts with it using a CLI, and it has a GUI created with JavaFX. It is written in Java.
 
Given below are my contributions to the project.
 
* **New feature**: Homework tracking
  * What it does: Lets a tutor assign homework to a student, correct it, mark its outcome, and list the homework history and what is still outstanding. Deleting homework is guarded so that a recorded outcome is not lost by accident.
  * Justification: Tutors need to know what each student still owes before the next lesson, and homework must survive restarts and data migration.
  * Highlights: Homework has its own persisted identity and lifecycle, descriptions are compared ignoring letter case, replacement and text parsing are hardened, and a homework record that fails to load names the bad field.
* **New feature**: Student filters, archiving and student identities
  * What it does: Adds filters for the student list and archive and restore workflows, with `find` still able to return archived students.
  * Justification: A tutor with many students needs to hide former students without losing their records.
  * Highlights: Students and lessons get persisted identities, same-name students are labelled in finance and lesson replies, and the archived marker is documented in finance replies.
* **New feature**: Data migration and safe storage
  * What it does: Migrates legacy data into separate v1.4 storage, refuses to start on an unsupported data schema, and publishes migrated data only after an atomic save.
  * Justification: A tutor's records are the most valuable thing in the app, so an interrupted write, a bad file or an old version must never destroy them.
  * Highlights: Failed legacy sources and unreadable data files are preserved and never set aside, saved data survives an interrupted write, unsaved changes are reported when a save fails, data files with a byte order mark are read, text after the JSON is rejected, and loading of large data files was sped up. Migration was verified with genuine legacy JSON.
* **New feature**: Account statement and credit withdrawals
  * What it does: Shows the account statement from the `balance` command with reversals displayed correctly, and adds credit withdrawals.
  * Justification: A tutor needs one place to check what a student has paid, owes and has had refunded.
  * Highlights: Amounts stay currency-neutral, rates are formatted with a fixed locale, and the withdrawal accounting totals are explained in the guides.
* **New feature**: Task payment links
  * What it does: Links a task to a payment with `pay/INDEX` in `task add` and stores the link in the data file.
  * Justification: A task can be traced back to the payment it relates to.
  * Highlights: The link is persisted, with the payment schema threshold named, and documented together with a design consideration in the Developer Guide.
* **New feature**: Command-assisted student cards
  * What it does: Adds Edit, Archive and Delete actions to each student card, field-level edit shortcuts, and a global Add Student button. Every shortcut fills the command box without running the command or changing data.
  * Justification: The CLI stays the single source of truth for changes, while the GUI helps a tutor construct the correct command without memorising every prefix.
  * Highlights: Keyboard and touch access, command drafts tracked per card, safe cancellation, and clearing an unchanged prepared draft when the displayed list changes.
* **New feature**: Command words and command history
  * What it does: Accepts command words and `confirm` in any letter case, and adds Up/Down history navigation in the command box.
  * Justification: Letter case should not decide whether a valid command runs, and recalling a failed command is faster than retyping it.
  * Highlights: Prefixes remain case-sensitive, and command execution and validation are unchanged.
* **Enhancements to existing features**:
  * Date parsing: read dates written in many ways (several separators, named months, two-digit years), read saved lesson dates as ISO only, limited agenda dates to 2000 to 2099, and used one injected clock for date parsing and the move, view and delete commands.
  * Parsing: extracted shared parsing helpers out of the command parsers and showed them in the parser class diagram. Malformed commands such as `st/` indices and homework now report the command's usage, and empty fields are validated after tokenisation.
  * Diagnostics: rejected mistyped, retired (address) and unknown single-letter prefixes with a clear message instead of blaming the preceding field, while allowing text such as `s/o` and `h/o` in names.
  * Input handling: limited student names to 100 characters, rejected controls before normalisation, treated composed and separate accents as the same name or subject, collapsed repeated spaces in remarks, and kept upper-case slash pairs in free text.
  * Large input: bounded large input, result sizes, payment and homework listings, and travel warnings in agenda output.
  * Behaviour fixes: rejected edits and lesson moves that change nothing, kept the displayed student list after `edit` and `remark`, refreshed lesson numbers after visible changes, corrected the travel warning count, and named the missing fields or prefixes in `hw edit` and lesson prep errors.
  * Undo: integrated undo with destructive command execution and failed-save handling, and fixed undo hook state.
  * Quality: added assertions and logging to core commands, removed AB3 leftovers, renamed boolean fields and methods to follow the coding standard, added Javadoc to undocumented public methods, and removed private payloads from application logs.
  * Capitalised subjects entered through commands, labelled tuition rates consistently per hour, and matched the specified wording of several lesson and delete messages.
* **Testing**: Added tests for the shared parsing classes, the data file start-up messages and supported schema versions, non-ASCII names and subjects surviving a save and load, the log holding no personal details, and the three delete commands treating Unicode whitespace alike. Wrote and extended the manual test cases for lessons, undo, dates, student aliases and saving, and the remaining commands.
* **Code contributed**: [RepoSense link](https://nus-cs2103-ay2627s1.github.io/tp-dashboard/?search=joshmode&breakdown=true). Over 200 commits and 45 merged pull requests on the team repository, 8000 functional LoCs.
* **Project management**:
  * Maintained the issue tracker, milestones and pull-request merge flow, and prepared the v1.4, v1.5 and v1.6 releases.
  * Split review work into focused branches, checked regression coverage before merging, and kept commit and branch names consistent with the project conventions.
  * Ran release audits of the User Guide, Developer Guide and product against the rubric and opened issues for the findings.
  * Coordinated peer review findings across the team's open pull requests.
* **Documentation**:
  * User Guide: documented command history, student card shortcuts, flexible dates, prefix diagnostics, homework, archiving and the finance workflow. Regrouped the guide, added the input limits, upgrade notes, screenshots of `view`, `agenda` and `help`, a planned enhancements section and a command summary that covers every help command.
  * Developer Guide: documented command parsing, undo integration, input handling, the student-card interaction model, the data file keys and migration. Added diagrams for archiving, deleting students and homework, replaced planning notes with the implemented design, and added the AI tool use declaration.
  * Reviewed the User Guide and Developer Guide against the implemented product and corrected stale examples, links, diagrams, in-app help text and limits. Replaced the design mock-up and site logo, credited reused icons, and fixed the website links and portfolio pages.
* **Community**:
  * Reviewed teammates' pull requests and reported reproducible defects, regressions and documentation mismatches.
  * Helped resolve merge conflicts and verify the combined behaviour after large feature merges.
  * Took part in course forum discussions, including but not limited to issues [#735](https://github.com/NUS-CS2103-AY2627-S1/forum/issues/735), [#734](https://github.com/NUS-CS2103-AY2627-S1/forum/issues/734) and [#724](https://github.com/NUS-CS2103-AY2627-S1/forum/issues/724).
* **Tools**:
  * JavaFX, Gradle, JUnit 5, Checkstyle, MarkBind and PlantUML.
 
