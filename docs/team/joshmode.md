---
  layout: default.md
  title: "Joshua Andrew's Project Portfolio Page"
---

### Project: TutorFlow

TutorFlow is a desktop application that helps independent private tutors organise and access information about their students, lessons, homework, and payments. The user interacts with it using a CLI, and it has a GUI created with JavaFX. It is written in Java.

Given below are my contributions to the project.

* **New feature**: Command-assisted student cards
  * What it does: Adds Edit, Archive and Delete actions to each student card, field-level edit shortcuts, and a global Add Student button. Every shortcut fills the command box without running the command or changing data.
  * Justification: The CLI stays the single source of truth for changes, while the GUI helps a tutor construct the correct command without memorising every prefix.
  * Highlights: Keyboard and touch access, command drafts tracked per card, safe cancellation, and clearing an unchanged prepared draft when the displayed list changes.

* **New feature**: Command words and command history
  * What it does: Accepts command words and `confirm` in any letter case, and adds Up/Down history navigation in the command box.
  * Justification: Letter case should not decide whether a valid command runs, and recalling a failed command is faster than retyping it.
  * Highlights: Prefixes remain case-sensitive, and command execution and validation are unchanged.

* **Enhancements to existing features**:
  * Added flexible date parsing, including several separators, named months and two-digit years.
  * Aligned the command parsers with the shared parsing rules and improved parser guidance messages.
  * Added travel-warning and weekly-series follow-up fixes.
  * Integrated undo with destructive command execution and failed-save handling.
  * Added clear diagnostics for mistyped and retired prefixes instead of blaming the preceding field.
  * Reviewed and hardened input limits, large-result rendering, JSON loading, documentation links and merged changes.

* **Code contributed**: [RepoSense link](https://nus-cs2103-ay2627s1.github.io/tp-dashboard/?search=joshmode&breakdown=true)

* **Project management**:
  * Maintained the issue tracker, milestones and pull-request merge flow.
  * Split review work into focused branches, checked regression coverage before merging, and kept commit and branch names consistent with the project conventions.
  * Coordinated peer review findings across the team's open pull requests.

* **Documentation**:
  * User Guide: documented command history, student card shortcuts, flexible dates, prefix diagnostics and the finance workflow.
  * Developer Guide: documented command parsing, undo integration, input handling and the student-card interaction model.
  * Reviewed the User Guide and Developer Guide against the implemented product and corrected stale examples, links, diagrams and limits.

* **Community**:
  * Reviewed teammates' pull requests and reported reproducible defects, regressions and documentation mismatches.
  * Helped resolve merge conflicts and verify the combined behaviour after large feature merges.

* **Tools**:
  * JavaFX, Gradle, JUnit 5, Checkstyle, MarkBind and PlantUML.
