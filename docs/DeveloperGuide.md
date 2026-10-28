---
  layout: default.md
  title: "Developer Guide"
  pageNav: 3
---

# MediConnect Developer Guide

<!-- * Table of Contents -->
<page-nav-print />

--------------------------------------------------------------------------------------------------------------------

## **Acknowledgements**

* This project is based on the [AddressBook-Level3](https://se-education.org/addressbook-level3/) project created by the [SE-EDU initiative](https://se-education.org).
* Libraries used: [JavaFX](https://openjfx.io/), [Jackson](https://github.com/FasterXML/jackson), [JUnit5](https://github.com/junit-team/junit5)

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

**`Main`** (consisting of classes [`Main`](https://github.com/AY2627S1-CS2103T-W08-1/tp/blob/master/src/main/java/seedu/address/Main.java) and [`MainApp`](https://github.com/AY2627S1-CS2103T-W08-1/tp/blob/master/src/main/java/seedu/address/MainApp.java)) is in charge of the app launch and shut down.
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

The **API** of this component is specified in [`Ui.java`](https://github.com/AY2627S1-CS2103T-W08-1/tp/tree/master/src/main/java/seedu/address/ui/Ui.java)

<puml src="diagrams/UiClassDiagram.puml" alt="Structure of the UI Component"/>

The UI consists of a `MainWindow` and its parts, such as `CommandBox`, `ResultDisplay`, `PatientListPanel`, `PatientDetailPanel`, and `StatusBarFooter`. All of these, including `MainWindow`, inherit from the abstract `UiPart` class, which captures common behavior among classes that represent visible GUI parts.

The main window shows two panels side by side:

* `PatientListPanel` shows the displayed patient list, with one `PatientCard` per patient. It observes `Logic#getFilteredPatientList()`, so it updates when the list is filtered or changed.
* `PatientDetailPanel` shows the full details of the patient being viewed, including their medications, allergens, care instructions, contacts and appointments. It observes `Logic#getViewedPatient()` and redraws itself whenever that value changes, showing a placeholder message when no patient is being viewed. See the [view patient feature](#view-patient-feature) for how the viewed patient is set and cleared.

The `UI` component uses the JavaFX UI framework. The layouts of these UI parts are defined in matching `.fxml` files in `src/main/resources/view`. For example, [`MainWindow.fxml`](https://github.com/AY2627S1-CS2103T-W08-1/tp/tree/master/src/main/resources/view/MainWindow.fxml) specifies the layout of [`MainWindow`](https://github.com/AY2627S1-CS2103T-W08-1/tp/tree/master/src/main/java/seedu/address/ui/MainWindow.java).

The `UI` component,

* executes user commands using the `Logic` component.
* listens for changes to `Model` data so that the UI can be updated with the modified data.
* keeps a reference to the `Logic` component, because the `UI` relies on the `Logic` to execute commands.
* depends on some classes in the `Model` component because it displays `Patient` objects from the model.

### Logic component

**API** : [`Logic.java`](https://github.com/AY2627S1-CS2103T-W08-1/tp/blob/master/src/main/java/seedu/address/logic/Logic.java)

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
1. If the command changed the patient data, `LogicManager` saves it through `Storage`. Before executing the command, it takes a `Model.Snapshot` of the patient data, the patient list filter and the viewed patient. If the save fails, it restores the `Model` from the snapshot and reports the failure, so a command reported as failed leaves no change that would be lost on restart. Commands that change no patient data, such as `list` or `exit`, are not saved, so they still work when the data file is read-only.
1. The result of the command execution is encapsulated as a `CommandResult` object which is returned from `Logic`.

Here are the other classes in `Logic` (omitted from the class diagram above) that are used for parsing a user command:

<puml src="diagrams/ParserClasses.puml" width="600"/>

How the parsing works:
* Command words are matched case-sensitively by `AddressBookParser`; field prefixes are also case-sensitive.
  Both use lowercase spellings. For example, `LIST` produces `Unknown command.` rather than running `list`.
* When called upon to parse a user command, the `AddressBookParser` class creates an `XYZCommandParser` (`XYZ` is a placeholder for the specific command name, e.g., `AddCommandParser`). The parser uses the other classes shown above to parse the user command and create an `XYZCommand` object (e.g., `AddCommand`). The `AddressBookParser` returns that object as a `Command` object.
* All `XYZCommandParser` classes, such as `AddCommandParser` and `DeleteCommandParser`, implement the `Parser` interface so they can be treated similarly where appropriate, for example during testing.
* Field parsers use `ArgumentTokenizer#verifyNoInvalidPrefixes` before tokenization, declaring their free-text
  prefixes separately from their supported prefixes. Non-reserved slash text such as `and/or`, `mg/ml`, and
  `x/123` is allowed within those fields. Preambles and structured fields retain strict unknown-prefix checks.
  `CliSyntax#RESERVED_PREFIXES` contains all field prefixes, including ones unsupported by the current command;
  update this set when adding a prefix. Reservation checks lowercase candidates with `Locale.ROOT`, so
  wrong-case reserved prefixes are rejected with the existing unrecognized-parameter message. Tokenization
  still recognizes only the exact lowercase prefix. Literal `N/A` (optionally followed by punctuation) remains
  allowed in free text. Existing standalone slash-word exceptions and their punctuation corrections still apply.
  The validator also checks the first value word attached to a recognized prefix, carrying its free-text context
  but allowing no nested field prefixes: `a/x/123` is text, whereas `a/p/123` is rejected. This nested validation
  cannot recurse again because its set of valid prefixes is empty. Context switches follow tokenization's space
  boundary, so a tab does not create a free-text field. Tokenization itself is unchanged.

### Model component
**API** : [`Model.java`](https://github.com/AY2627S1-CS2103T-W08-1/tp/blob/master/src/main/java/seedu/address/model/Model.java)

<puml src="diagrams/ModelClassDiagram.puml" width="450" />

The diagram below shows the `Patient` class in more detail.

<puml src="diagrams/PatientClassDiagram.puml" width="450" />


The `Model` component,

* stores the patient data, i.e., all `Patient` objects (which are contained in a `UniquePatientList` object).
* stores the `Patient` objects selected by the current filter, such as search results, in a separate _filtered_ list. It exposes this list as an unmodifiable `ObservableList<Patient>` that the UI can observe and bind to, so the UI updates when the list changes.
* stores the patient being viewed, if any. It exposes this patient as an `ObservableValue<Patient>` that the UI observes, so the detail panel updates when the viewed patient changes.
* stores a `UserPrefs` object that represents the user’s preferences (currently, just the GUI settings). This is exposed to the outside as a `ReadOnlyUserPrefs` object.
* does not depend on any of the other three components (as the `Model` represents data entities of the domain, they should make sense on their own without depending on other components)


<box type="info" seamless>

**Patient records:** Each `Patient` is composed of `Name`, `Nric`, `Dob`, `Phone` and `Address` values (one of each),
an `archived` flag, and lists of care information, as shown in the second diagram:
* zero or more `Medication` objects, each with a description and a list of daily times (`LocalTime`),
* zero or more `Contact` objects, each with a name, role, phone and note,
* zero or more `Appointment` objects, each with a date, time, provider and note, kept sorted by date, then time,
* `allergens` and `careInstructions`, which are plain `String` lists.

These lists default to empty and are defensively copied with `List.copyOf`, so callers cannot modify them through
the getters. Updating a patient creates a replacement `Patient`, which the model stores using `setPatient`.
The diagram leaves out the constraints of each value class and the exceptions of `UniquePatientList`.

**Archived patients:** An archived patient stays in the `UniquePatientList`. `ModelManager` only changes which patients
its filtered list shows, using `Model#PREDICATE_SHOW_ACTIVE_PATIENTS` or `Model#PREDICATE_SHOW_ARCHIVED_PATIENTS`.

**Duplicate identity:** `Patient.isSamePatient` compares NRIC values. `Nric` stores input in uppercase, so NRIC identity
is case-insensitive. `UniquePatientList` rejects patients with the same NRIC; different patients can share a name.
</box>


### Storage component

**API** : [`Storage.java`](https://github.com/AY2627S1-CS2103T-W08-1/tp/blob/master/src/main/java/seedu/address/storage/Storage.java)

<puml src="diagrams/StorageClassDiagram.puml" width="550" />

The `Storage` component,
* can save both patient data and user preference data in JSON format, and read them back into corresponding objects.
* is implemented by `StorageManager`, which delegates the actual JSON file access to `JsonAddressBookStorage` and `JsonUserPrefsStorage` (one class per data file).
* depends on some classes in the `Model` component (because the `Storage` component's job is to save/retrieve objects that belong to the `Model`)

The patient data is saved in `data/mediconnect.json` and the user preferences in `preferences.json`, both relative to the folder the app is launched from. `MainApp` sets these paths when the app starts.

`JsonUtil#saveJsonFile()` writes both files through `FileUtil#writeThroughTemporaryFile()`: the JSON is written to a new temporary file in the same folder, which then replaces the file in one step. A save that fails partway, e.g., on a full disk, therefore leaves the last complete file in place, and the temporary file is deleted. An existing file that cannot be written to, e.g., because it is read-only, is not replaced, although on macOS and Linux its folder may let the temporary file take its place.

`JsonUtil#readJsonFile()` reads both files, and backups for `restore`. It reads the text with `FileUtil#readFromFile()`, which decodes it as strict UTF-8: a byte that is not valid UTF-8 makes the read fail, instead of being replaced by `�` and then saved into patient data. `JsonUtil` then ignores a UTF-8 byte-order mark at the start of the file, which some editors add when saving as UTF-8. It rejects a file with anything other than whitespace after its JSON data, so that text added to a file by mistake is not silently dropped on the next save.

Before mapping the JSON to objects, `JsonUtil` also rejects a file in which a name or a text value has half of a surrogate pair on its own. Only a JSON escape of a surrogate, such as `\ud800`, in a hand-edited file can give such text. It cannot be written as UTF-8, so the next save would silently turn it into `?`.

As the diagram shows, `JsonSerializableAddressBook` holds a list of `JsonAdaptedPatient` objects, and each
`JsonAdaptedPatient` holds lists of `JsonAdaptedMedication`, `JsonAdaptedContact` and `JsonAdaptedAppointment`
objects. Each patient is saved with its name, NRIC, date of birth, phone number and address as strings, its
allergens and care instructions as lists of strings, and an `archived` flag. A medication is saved with its description
and its times as `HH:MM` strings. A contact is saved with its name, role, phone and note, and an appointment with its
date, time, provider and note.
The list of patients is saved under the JSON key `persons`, kept from AB3. When the file is read,
`JsonAdaptedPatient#toModelType()` validates patient fields and converts medications, contacts and appointments using
their model constraints. If any patient, medication, contact or appointment is invalid, two patients share
an NRIC, a patient has duplicate contacts or appointments, or an allergen or care instruction is blank or duplicated,
the whole file is rejected. Care-entry duplicates ignore letter case and whitespace differences, as the care commands
do.

`JsonAddressBookStorage` reads the file with `JsonUtil#readJsonFileStrictly()`, which also rejects a property that the
JSON class does not have, e.g., a misspelled `careInstructons`, and a property given twice. Ignoring them, as Jackson
does by default, would drop their values without a warning the next time the data is saved. An unknown property is
reported as invalid data that names it, while its value is not shown, since it can be patient data. `preferences.json`
is still read leniently, so that a file from an older version keeps its known settings.

On a `DataLoadingException`, `MainApp` copies the unreadable file beside the original as
`mediconnect-unreadable-<UUID>.json` before creating an empty model. The copy uses no replacement option, so an
existing recovery file is never overwritten. The UI shows its absolute path and failure details in a startup warning.
`JsonSerializableAddressBook#toModelType()` prefixes validation failures with the one-based entry position in the
JSON `persons` array, including null entries and duplicate patients, while preserving the original exception as
the cause. The first invalid entry is reported. Both startup and `restore` use these diagnostics. `MainApp` shows
validation messages for `IllegalValueException` causes; other read failures receive a generic format/permissions
message rather than raw parser exceptions that can include file contents. Later commands
save normally to the original data path. If preservation fails, the exception stops initialization before commands
can overwrite the file. `MainApp#init()` records this `IOException` instead of letting JavaFX abort initialization.
`MainApp#start()` then uses `UiManager#showStartupError()` to show an error dialog without a main-window owner,
on the JavaFX application thread, before exiting. The dialog names the data-file path and distinguishes a
directory path, denied access, and other preservation failures without displaying raw parser exceptions.
The dialog and exit actions can be supplied through a package-private constructor for tests. A regression runs
the real startup-error method and checks that the error is presented before exit; the UI tests cover the dialog.
No model, logic or main window is created after a preservation failure, and `stop()` skips preference saving
when there is no model. JSON validation stays in storage; startup recovery stays in `MainApp`.

When startup reaches `initModelManager` and the data file is missing, `MainApp` uses sample patients and attempts
to save them once through `createDataFileIfMissing`. If that save throws `IOException`, startup continues and the existing startup warning
dialog explains that the samples are unsaved, giving the absolute data path and a reason formatted by
`Messages#describeWriteFailure`. `MainApp#start` shows the warning after `Ui#start`; no new UI is needed.
Successful initial saves produce no warning. Command-time save failures still roll back through `LogicManager`.

### Common classes

Classes used by multiple components are in the `seedu.address.commons` package.

--------------------------------------------------------------------------------------------------------------------

## **Implementation**

This section describes some noteworthy details on how certain features are implemented.

### View patient feature

The `view INDEX` command shows the full details of a patient in the `PatientDetailPanel`, next to the patient list.

#### Implementation

`ModelManager` keeps the patient being viewed in an `ObjectProperty<Patient>`. It is exposed through `Model#getViewedPatient()` and `Logic#getViewedPatient()` as a read-only `ObservableValue<Patient>`, whose value is `null` when no patient is being viewed.

The sequence diagram below shows how `view 1` goes through the `Logic` component.

<puml src="diagrams/ViewSequenceDiagram.puml" alt="Interactions Inside the Logic Component for the `view 1` Command" />

1. `ViewCommandParser` parses the index in the same way as `DeleteCommandParser`, so `view` accepts the same `INDEX` values as `delete`.
1. `ViewCommand#execute()` checks the index against the filtered patient list. An index beyond the list size produces the same invalid-index message as `delete`.
1. `ViewCommand` calls `Model#setViewedPatient()` with the selected patient.
1. `PatientDetailPanel` listens to `Logic#getViewedPatient()` and redraws itself whenever the value changes. It shows a placeholder message when the value is `null`, and `None recorded` for an empty medication, allergen, care instruction, contact or appointment list. Each entry is shown on one line by `PatientFormatter#toSingleLine()`, which exported records also use, so that a line break stored in a hand-edited entry cannot look like another numbered entry.

`ModelManager` keeps the viewed patient consistent with the address book:

* `setPatient(target, editedPatient)` replaces the viewed patient with `editedPatient` if `target` is being viewed, so the panel shows changes such as newly added care information. If the change archives the viewed patient, as `archive` does, it clears the viewed patient instead.
* `deletePatient(target)` clears the viewed patient if `target` is being viewed.
* `setAddressBook(...)`, which `clear confirm` and `restore` use, clears the viewed patient unless the new data has the same patient with the same details.

Filtering the patient list (e.g., with `find`) does not change the viewed patient.

Since the viewed patient can be filtered out of the list, `PatientDetailPanel` also observes `Logic#getFilteredPatientList()`. Under the name, it shows the viewed patient's one-based index in that list, which is the `INDEX` that commands use, or `Not in the displayed list`, and updates it whenever the list or the viewed patient changes. When it shows a patient with a different NRIC from the last one shown, it scrolls back to the top, so that the new patient's name is visible. The same patient redrawn, e.g., after `addcare`, keeps the scroll position.

The panel numbers the appointments with upcoming ones first, then past ones, in the order that `cancelappt` and `editappt` also use. Upcoming and past are decided at the time the patient was last shown, which `Model#getAppointmentsOrderedAt()` returns, so the numbers do not change when an appointment passes while the patient is shown. `Logic#getAppointmentLinesAsShown()` reads that time once, and uses it both to order the appointments and to mark each past one with `(past)` through `PatientFormatter#formatAppointments()`. The mark goes right after the date and time, which only the app writes, so a note or `WITH` text ending in `(past)` cannot look like it. `PatientFormatter#formatRecord()`, which the `export` command uses, calls the same method with the time of the export, so an exported record words appointments the same way as the panel.

#### Design considerations

**Aspect: Where the viewed patient is stored**

* **Alternative 1 (current choice):** In the `Model`, as an observable value.
  * Pros: Commands that change patients can keep the panel up to date without knowing about the UI. The behavior can be unit tested in `ModelManagerTest`.
  * Cons: Adds a small amount of UI-related state to the `Model`.
* **Alternative 2:** Return the patient in `CommandResult` and let `MainWindow` update the panel.
  * Pros: The `Model` stays unchanged.
  * Cons: The panel would show stale data after the patient is edited or deleted by another command.

**Aspect: How past appointments are shown**

* **Alternative 1 (current choice):** Keep one numbered list and add `(past)` after the date and time of each past appointment.
  * Pros: The numbers that `cancelappt` and `editappt` use stay in one list, so the panel and an exported record stay easy to read.
  * Cons: The mark adds a little text to each past appointment.
* **Alternative 2:** Split the list under Upcoming and Past headings, as `listappt INDEX` does.
  * Pros: Past appointments are grouped visibly.
  * Cons: One numbered list split across two headings can be read as two lists, each starting from `1.`.

### Clearing all patients

`AddressBookParser` delegates `clear` arguments to `ClearCommandParser`. It accepts only the lowercase
`confirm` token, with surrounding whitespace allowed. Missing confirmation or extra arguments produce a
`ParseException` with the warning and command format before any data is changed.

`ClearCommand#execute()` first calls `BackupCommand#execute()` to save all current data using the existing
backup implementation. A backup failure propagates as a `CommandException` before the model is changed.
Only after the backup succeeds does it replace the entire address book with an empty one through `Model#setAddressBook()`,
including archived patients and patients hidden by a filter. The existing model logic clears the viewed patient.
The success message includes the backup location. `LogicManager` saves the change and restores its snapshot
if saving fails; the automatic backup remains available. Backups include archived and filtered-out patients,
even for an empty address book, and can be recovered with `restore`.

### Edit patient feature

The `edit INDEX [n/NAME] [ic/NRIC] [dob/DOB] [p/PHONE] [a/ADDRESS]` command replaces one or more core details
of a patient in the displayed list while preserving all care-related data and state.

#### Implementation

The sequence diagram below shows the successful execution of `edit 1 p/91234567`. Error paths are omitted for
clarity.

<puml src="diagrams/EditSequenceDiagram.puml" alt="Interactions for the `edit` Command" />

1. `EditCommandParser` parses a positive patient index and the supplied fields. It requires at least one field,
   rejects repeated or unsupported reserved prefixes, and uses the same field and slash-text rules as `add`.
1. `EditCommand#execute()` resolves the index against the filtered patient list and creates an immutable replacement
   using the supplied values and the existing values for omitted fields.
1. The replacement preserves medications and their times, allergens, care instructions, contacts, appointments,
   and archived state. A changed NRIC is rejected if another patient already has it.
1. The command stores the replacement with `Model#setPatient(patient, editedPatient)`, then resets the displayed
   list to active patients, or archived patients when the edited patient is archived, with
   `Model#updateFilteredPatientList(...)`.
1. `Model#setViewedPatient(editedPatient)` shows the edited patient. The `PatientDetailPanel` listeners redraw its
   details and displayed index.

#### Design considerations

**Aspect: Preserving fields that `edit` cannot change**

* **Alternative 1 (current choice):** Build the replacement through `Patient#withDetails(...)`.
  * Pros: The model owns the copy logic, so care-related fields and state are preserved in one place.
  * Cons: The helper must be updated whenever `Patient` gains another field.
* **Alternative 2:** Construct a replacement directly in `EditCommand`.
  * Pros: Avoids a model helper used by one command.
  * Cons: Repeating the full constructor in the command makes it easier to drop an unrelated field during editing.

### Add care info feature

The `addcare INDEX [med/MEDICATION]... [al/ALLERGEN]... [ci/CARE_INSTRUCTION]...` command appends care
information to a patient in the displayed list.

#### Implementation

The sequence diagram below shows the successful execution of `addcare 1 med/Metformin`. Error paths are omitted for
clarity.

<puml src="diagrams/AddCareSequenceDiagram.puml" alt="Interactions for the `addcare` Command" />

1. `AddCareCommandParser` parses a positive index and the values supplied under `med/`, `al/`, and `ci/`. At least one
   care prefix is required, and every entry must be non-blank.
1. `AddCareCommand#execute()` resolves the index against the filtered patient list, trims each entry, and compares it
   case-insensitively with entries in the same category.
1. The command appends only unique entries, creates a replacement immutable `Patient`, and calls
   `Model#setPatient(patientToEdit, editedPatient)`.
1. If the edited patient is currently being viewed, `ModelManager#setPatient()` also updates `viewedPatient`. The
   observable listener then causes `PatientDetailPanel` to redraw immediately with the updated care information.

Each skipped duplicate is named in the command feedback. The command distinguishes an entry already stored for the
patient from an entry repeated earlier in the same command. Unique entries are still added; if every supplied entry
is a duplicate, the command fails without changing the patient.

#### Design considerations

**Aspect: Appending or overwriting care information**

* **Alternative 1 (current choice):** Append new entries to the patient's existing care information.
  * Pros: Existing medication, allergen, and instruction records are preserved, reducing the risk of accidental data
    loss.
  * Cons: Correcting or removing an existing entry requires a separate command in a future version.
* **Alternative 2:** Replace the full list whenever a care category is supplied.
  * Pros: Users could correct or remove entries with the same command.
  * Cons: Users would need to re-enter every value they want to keep, making accidental data loss more likely.

**Aspect: Handling duplicate entries**

* **Alternative 1 (current choice):** Skip trimmed, case-insensitive duplicates and continue adding other unique
  entries. Feedback names each skipped entry and whether it already existed or was repeated in the command.
  * Pros: Valid new information is retained even when the same command contains duplicates.
  * Cons: The success result can contain both an added-entry summary and duplicate warnings.
* **Alternative 2:** Reject the entire command when any duplicate is found.
  * Pros: The operation has simpler all-or-nothing behavior.
  * Cons: One duplicate prevents all valid new entries in the same command from being added.

### Backup and restore feature

The `backup` command saves a copy of all patient data to the `data/backups` folder, and `restore FILENAME` replaces all patient data with one of those copies.

#### Implementation

`BackupCommand#saveBackup(ReadOnlyAddressBook)` writes the given data to a new temporary file in the backup folder through `JsonAddressBookStorage`, the same class that saves `data/mediconnect.json`, then renames it to the backup's name and returns the path of the file. A backup that fails partway therefore never leaves a cut-off file under a backup's name, and `FileUtil#deleteTemporaryFile()` removes the temporary file. On macOS and Linux, a new temporary file can be opened only by the current user, and the backup keeps that when it is renamed. The file is named after the current time, as `mediconnect-YYYYMMDD-HHMMSS.json`. If a file with that name already exists, `-1`, `-2` and so on are added to the name, so no backup is overwritten. Checking whether a name is free and then saving under it are two steps, so two backups made at the same moment, e.g., in two MediConnect windows, or by `restore` or `clear confirm` while a backup is made, could otherwise choose the same name, and one would replace the other. `BackupCommand#saveUnderNextFreeName()` therefore does both while it holds a `FileLock` on the empty file `.backup.lock` in the backup folder, which makes backups in other processes wait. A process can hold that lock only once, so the backups of one MediConnect also take turns through a `synchronized` block first. `BackupCommand#execute()` calls `saveBackup` with the data in the `Model` and reports the path. If the file cannot be written, `Messages#describeWriteFailure()` turns the `IOException` into a short reason for the error message.

The sequence diagram below shows a successful `restore old.json`.

<puml src="diagrams/RestoreSequenceDiagram.puml" alt="Interactions for the `restore old.json` Command" />

1. `RestoreCommandParser` returns a `ListBackupsCommand` if no `FILENAME` is given. It lists the `.json` files in `data/backups` by the time they were last changed, newest first, so that the user can copy a name. Files changed at the same time, e.g., after being copied together, are listed by name, last first, with numbers compared by value, so that `-10` comes before `-9`. Otherwise, it checks that `FILENAME` is only a file name: it must not contain `/`, `\` or `:`, must not be `.` or `..`, and must be a name the operating system accepts. This keeps `restore` inside `data/backups` on every operating system.
1. `RestoreCommand#execute()` first finds the file. If no file has the given name, but one with `.json` added does, e.g., for `restore mediconnect-20261020-083015`, it restores that file, and its messages give that file's name. It reads the whole file through a `JsonAddressBookStorage` for that file, which checks every patient the same way as loading `data/mediconnect.json` at startup. A missing file, a file that MediConnect is not allowed to read, a file that is not valid UTF-8, a damaged file and a file with invalid data each make the command throw a `CommandException` with its own message, before anything is changed.
1. Only then does the command back up the current data, by calling `saveBackup` on a `BackupCommand` with the same clock and folder. If that fails, nothing is restored.
1. The command replaces the data with `Model#setAddressBook()`, which also clears the viewed patient unless the restored data has the same patient with the same details, and shows the active patients again with `Model#updateFilteredPatientList()`. The result gives the number of patients restored, how many of them are archived, since only active patients are then listed, and the path of the automatic backup.
1. As after every command that changes the patient data, `LogicManager` then saves the `Model`, which replaces `data/mediconnect.json`.

Both commands have a second constructor that takes a `java.time.Clock` and the backup folder. The usual constructor passes the system clock and `data/backups`, while tests pass a fixed clock and a temporary folder, so that file names and folders are predictable.

#### Design considerations

**Aspect: Keeping the current data safe during a restore**

* **Alternative 1 (current choice):** Check the whole backup first, then back up the current data automatically, and only then replace it.
  * Pros: A missing or invalid backup never changes the current data, and any restore can be undone by restoring the automatic backup it reports.
  * Cons: Every restore adds a file to `data/backups`.
* **Alternative 2:** Ask the user to confirm the restore, and keep no automatic backup.
  * Pros: No extra files.
  * Cons: Adds a step to every restore, and a restore confirmed by mistake cannot be undone.

**Aspect: What a backup copies**

* **Alternative 1 (current choice):** Save the patient data currently loaded in MediConnect, through `JsonAddressBookStorage`.
  * Pros: Needs no changes to `LogicManager` or `Storage`, and every backup holds valid data in the current format.
  * Cons: If `data/mediconnect.json` could not be loaded at startup, a backup holds the empty data MediConnect started with, not the unreadable file. `MainApp` already keeps that file as a recovery copy at startup.
* **Alternative 2:** Copy `data/mediconnect.json` byte for byte.
  * Pros: An unreadable data file is kept, for repair by hand, although the startup recovery copy already does this.
  * Cons: The path of the data file would have to be passed from `Storage` through `LogicManager` to the command, and a backup could hold data that `restore` then rejects.

### Export feature

The `export INDEX` command saves the full record of a patient in the displayed list to a text file in `data/exports`, for printing or for handing over, e.g., during a hospital visit.

#### Implementation

1. `ExportCommandParser` parses the index in the same way as `ViewCommandParser`. `ExportCommand#execute()` checks it against the filtered patient list, so `export` exports an active patient after `list` or `find`, and an archived patient after `listarchived`.
1. `ExportCommand#createRecord()` builds the lines of the record: a title, `Exported on` with the date and time from the command's `Clock`, a blank line, then the lines from `PatientFormatter#formatRecord()`. These are the patient's name, the details from `PatientFormatter#formatDetails()`, with `Status: Archived` for an archived patient, and each list under its heading, numbered by `PatientFormatter#formatEntries()`. Appointments are ordered and marked `(past)` at the time of the export, through the same `PatientFormatter#formatAppointments()` that the detail panel uses.
1. The file is named after the patient's NRIC, e.g., `S1234567A.txt`. Before writing, `ExportCommand#writeRecord()` creates the folder if it does not exist, and throws an `AccessDeniedException` if an earlier export of the patient is read-only, since the new record would otherwise replace it although the user made it read-only.
1. The record is written through `FileUtil#writeThroughTemporaryFile()`, as backups are, so a failed write never leaves a partial record in place of an earlier one, and on macOS and Linux, only the current user can open the file. If writing fails, `Messages#describeWriteFailure()` turns the `IOException` into a short reason for the error message.
1. The command does not change the `Model`. It logs only the export folder, not the file, since the file name is the patient's NRIC. The date is formatted with `Locale.ROOT`, so that it uses the digits 0 to 9 whatever the computer's language settings are.

Like `BackupCommand`, `ExportCommand` has a second constructor that takes a `java.time.Clock` and the export folder, which tests use to get a predictable date and folder.

#### Design considerations

**Aspect: How the exported file is named**

* **Alternative 1 (current choice):** After the patient's NRIC, e.g., `S1234567A.txt`.
  * Pros: NRICs are unique, and only use letters and digits, which every operating system allows in file names. Exporting the same patient again replaces their earlier record, so the folder holds one up-to-date record per patient.
  * Cons: The file name shows the NRIC. If the NRIC is changed, e.g., with `edit`, the earlier file is left behind under the old NRIC.
* **Alternative 2:** After the patient's name and the time of the export.
  * Pros: The name is easier to recognize, and earlier records are kept.
  * Cons: Names can contain `/`, e.g., in `s/o`, which file names cannot, and two patients can have the same name. Every export adds another file.

**Aspect: How the record is worded**

* **Alternative 1 (current choice):** Reuse `PatientFormatter`, which also words the detail panel.
  * Pros: The wording is defined in one place, so an exported record always reads the same as the panel.
  * Cons: A wording change meant for one of them also changes the other.
* **Alternative 2:** Format the record separately in `ExportCommand`.
  * Pros: The record can be worded differently from the panel.
  * Cons: The same wording is written twice, and the two can drift apart.

### Help window

The `help` command, `F1` and the **Help** menu open the `HelpWindow`, which lists every command with its format and a one-line description under group headings, below a link to the User Guide.

#### Implementation

1. `HelpCommand#execute()` returns a `CommandResult` that asks the UI to show help, and `MainWindow#handleHelp()` shows the help window if it is not already shown. The **Help** menu item, whose shortcut is `F1`, calls `handleHelp()` directly. It then calls `requestFocus()` on the main window, so that the user can keep typing commands while the help window is open. A minimized help window still counts as shown, and bringing it back from the Dock or the taskbar would move the focus to it, so `handleHelp()` hides it and shows it again instead. `HelpWindow#show()` first clears the minimized state, which a window hidden while minimized would otherwise keep.
1. The list is `HelpCommand#COMMAND_SUMMARY`, built once from each command's `COMMAND_WORD` or `COMMAND_FORMAT` and the prefixes in `CliSyntax`, which the parsers also use. A renamed command word or prefix therefore also changes the help text. `HelpCommandTest` checks that every command is listed once, in order, and that `AddressBookParser` parses every listed format, filled in with sample values, into that command.
1. `MainWindow` makes the main window the owner of the help window, so that the help window closes and minimizes with it. An event handler on the help window's own stage hides it on `Esc`, and each time the help window is shown, it adds the same handler to its owner as an event filter. A filter is used because it sees `Esc` before the focused command box does. It also keeps itself in front of other windows only while it or its owner has the focus, through a `BooleanBinding` on both windows' `focusedProperty()`. The binding is kept in a field, since the properties it observes refer to it only weakly. When the help window is hidden, the filter is removed and the owner's focus is no longer followed.

#### Design considerations

**Aspect: Keeping the help window in view while typing commands**

* **Alternative 1 (current choice):** Keep the help window in front only while MediConnect has the focus.
  * Pros: The list stays above the main window while the user types commands, but goes behind other apps, e.g., a browser with the User Guide open.
  * Cons: Needs a binding on the focus of both windows, and an `Esc` filter on the main window.
* **Alternative 2:** Keep the help window always in front.
  * Pros: Needs only one call to `setAlwaysOnTop(true)`.
  * Cons: The help window also covers other apps until it is closed.

### Contact management

`Contact` is immutable and contains a name, role, phone and optional note (stored as an empty string when absent).
It owns the text limits: `MAX_NAME_LENGTH` and `MAX_ROLE_LENGTH` are 100, and `MAX_NOTE_LENGTH` is 500.
Validators count Unicode code points after name normalization and role trimming. Notes are checked as supplied
to the model: command parsers trim their boundaries first, while stored note text is preserved and counted in full.
The constructor enforces these rules for commands, startup loading and restore through `JsonAdaptedContact`.
The edit parser validates supplied fields before creating a command, including a trimmed optional note.
Names are nonblank free text because contacts can be people or organisations, such as `Tan & Partners Clinic`.
They deliberately do not use patient `Name` constraints. Contact phones likewise have their own rules so clinic
and international numbers work without weakening patient-phone validation. `Contact#isValidPhone()` requires
3 to 15 digits, permits spaces and hyphens, and allows at most one leading `+` after trimming. The digit bounds
are named constants in `Contact`. Both contact parsers and `JsonAdaptedContact` use this model validation, so
the same limits apply to add, edit, startup loading and restore. Formatting and duplicate identity are unchanged.

`Patient` keeps an immutable contact list in insertion order. `withContacts` replaces only that list; `addcare`
copies it when rebuilding a patient. `Contact` rejects Unicode-whitespace-only names and roles, normalizes
name whitespace to single ordinary spaces, and trims role boundaries. Contact identity compares names ignoring
case and normalized whitespace, and phones ignoring spaces and hyphens, while retaining `+`.
The model rejects duplicate identities within a patient.
`Contact#isSameContact` converts names to Unicode NFC with `java.text.Normalizer` before comparing them
ignoring case, so composed and decomposed spellings such as `Café` and `Café` have the same identity.
This comparison preserves accents and does not change display or stored name text. Full `equals` and `hashCode`
still compare all stored fields. Add, edit and `Patient`'s duplicate validation share this identity rule, so
`JsonAdaptedPatient` also rejects these duplicates during startup loading and restore.

The three contact parsers use the shared prefixes in `CliSyntax`. Add and edit declare names, roles and notes
as free text for slash validation; phones remain structured. Repeated and unsupported reserved prefixes are
rejected. Standalone `s/o`, `d/o`, `c/o`, `a/l` and `a/p` remain explicit exceptions, ignoring case.
Contact validation and the add/edit parsers use `StringUtil#stripUnicodeWhitespace()` to trim Unicode boundaries,
including no-break spaces. The helper scans non-whitespace runs and preserves internal text. Notes from commands
use this helper; stored notes are
preserved when loading. Edit uses `Optional<String>` to distinguish
an omitted note (preserve) from a present empty note (clear). Commands use displayed patient indices and
contact indices from the detail panel. They validate indices and duplicates before calling `Model#setPatient`.
After updating the patient, each contact command calls `Model#setViewedPatient(editedPatient)`, so the panel
shows the affected patient even if another patient was previously viewed or the panel was empty. The existing
viewed-patient listener refreshes the panel; commands do not access UI controls. The patient-list filter is retained.
Rejected commands do not change the viewed patient. If saving fails, the existing `LogicManager` snapshot
restores the patient data, filter and previously viewed patient.

`JsonAdaptedContact` saves the four contact fields. Missing contact lists load as empty, and missing notes load as
empty strings. Null entries, invalid fields and duplicate identities reject the data file with a reason.
`PatientDetailPanel` numbers contacts in insertion order and shows `None recorded` for an empty list.

The diagram focuses on parsing and updating the patient; `LogicManager` performs its usual save after execution.

<puml src="diagrams/AddContactSequenceDiagram.puml" alt="Adding a contact to a patient" />

### Add appointment feature

The `addappt INDEX d/DATE t/TIME w/WITH [nt/NOTE]` command adds an appointment to a patient in the displayed list.

#### Implementation

The sequence diagram below shows the successful execution of `addappt 1 d/2027-11-05 t/09:30 w/Dr Tan`. Error paths
are omitted for clarity.

<puml src="diagrams/AddApptSequenceDiagram.puml" alt="Interactions for the `addappt` Command" />

1. `AddApptCommandParser` requires `d/`, `t/` and `w/` once each, with `w/` and `nt/` declared as free text for
   slash validation. It parses the index, the date, the time (with `ParserUtil#parseTime`, as for medication times) and who the appointment is with, and
   creates the `Appointment` `toAdd`, with an empty note if `nt/` is not given.
1. `AddApptCommand#execute()` resolves the index against the filtered patient list. It rejects `toAdd` if the patient
   already has an appointment at the same date and time with the same person, ignoring case and extra spaces.
1. The command creates a replacement immutable `Patient` with `toAdd` added, which keeps its appointments sorted by
   date, then time. `Model#setPatient()` stores it, and `Model#setViewedPatient()` shows it in the detail panel, so
   the new appointment and its number can be checked.
1. The command adds a note to the result if `toAdd` is in the past, and a warning for each of the patient's other
   appointments at the same date and time. The current time comes from a `java.time.Clock` given to the command:
   the system clock by default, and a fixed one in tests, so that the tests do not depend on when they run.
   `Appointment#isBefore` ignores seconds, so an appointment in the current minute is not in the past.

#### Design considerations

**Aspect: Appointments in the past or at the same time as another**

* **Alternative 1 (current choice):** Add the appointment, with a note or a warning in the result.
  * Pros: A caregiver can record a visit that already happened, or two appointments at the same time, e.g., with a
    doctor and a nurse together.
  * Cons: A mistyped date is only flagged, not prevented, so the user has to read the result.
* **Alternative 2:** Reject such appointments.
  * Pros: A mistyped date or time cannot be saved.
  * Cons: Past visits could not be recorded, and genuine overlapping appointments would need a workaround.

### Due medication feature

The `due [INDEX]` command lists the medication times of today, for all active patients or only for the patient at
`INDEX` in the displayed list.

#### Implementation

The sequence diagram below shows the successful execution of `due 1`. Error paths are omitted for clarity.

<puml src="diagrams/DueSequenceDiagram.puml" alt="Interactions for the `due 1` Command" />

1. `DueCommandParser` accepts either no arguments or a single positive index; anything else throws a `ParseException`
   with the usage message of `due`. It creates a `DueCommand` with a `java.time.Clock` from
   `Clock.systemDefaultZone()`, and with the index if one was given.
1. `DueCommand#execute()` chooses the patients to list. With an index, it resolves the index against the filtered
   patient list. Without one, it takes every patient in the address book, so that a `find` does not hide a dose.
   Archived patients are always left out.
1. It reads the current time as `LocalTime.now(clock)`, cut to the minute, and turns every time of every medication
   into a dose. The doses are sorted by time, then by patient name, then by NRIC.
1. It writes one line per dose, as `HH:MM NAME: DESCRIPTION`, and adds ` (earlier today)` to a dose whose time is
   before now. If there are no doses, it returns `No medications are due today.`, or
   `No medications are due today for NAME.` when an index was given.

`due` does not change the patient data, so `LogicManager` does not save after it.

#### Design considerations

**Aspect: Where the current time comes from**

* **Alternative 1 (current choice):** `DueCommand` receives a `Clock`, as `AddApptCommand` does.
  * Pros: Tests pass a fixed clock, so the marking of earlier doses does not depend on when the tests run.
  * Cons: Every constructor takes one more parameter, and `DueCommandParser` has to create the clock.
* **Alternative 2:** `DueCommand` calls `LocalTime.now()` itself.
  * Pros: Simpler constructors.
  * Cons: Tests of the `(earlier today)` marking would pass or fail depending on the time of the day.

**Aspect: Which patients are listed without an index**

* **Alternative 1 (current choice):** All active patients, not only the displayed ones.
  * Pros: The caregiver sees every dose due today, even after a `find` or after `listarchived`.
  * Cons: The result can differ from the patients shown in the list.
* **Alternative 2:** Only the displayed patients.
  * Pros: The result always matches the list.
  * Cons: A dose can be missed because an earlier `find` is still in effect.

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

Step 6. The user executes `clear confirm`, which calls `Model#commitAddressBook()`. Since the `currentStatePointer` is not pointing at the end of the `addressBookStateList`, all address book states after the `currentStatePointer` will be purged. Reason: It no longer makes sense to redo the `add n/David …` command. This is the behavior that most modern desktop applications follow.

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

_{more aspects and alternatives to be added}_

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

MediConnect is primarily for chronically ill patients and family caregivers in Singapore who coordinate care for
elderly or chronically ill patients. Community nurses conducting home visits are a secondary user group. A typical
user:

* manages care information across multiple healthcare providers and emergency contacts
* needs to track patient profiles, allergies, medications, appointments, and care instructions
* currently relies on fragmented sources such as messaging threads, paper notes, and memory
* needs quick access to accurate information during daily care, handovers, appointments, and emergencies
* is comfortable with everyday applications but may not be technically proficient
* prefers a local desktop application with a fast, keyboard-first workflow

**Value proposition**: MediConnect centralizes patient profiles, care contacts, allergies, appointment history,
medication schedules, and care instructions in one local desktop application. It gives patients and caregivers a
single, quickly accessible source of care information, reducing the risk of important details being missed and
making day-to-day coordination and care handovers easier.


### User stories

Priorities: High (must have) - `* * *`, Medium (nice to have) - `* *`, Low (unlikely to have) - `*`

| Priority | As a …                                    | I want to …                 | So that I can…                                                        |
|----------|--------------------------------------------|------------------------------|------------------------------------------------------------------------|
| `* * *`  | caregiver                                  | add a new patient profile with basic details (name, NRIC, date of birth, phone, address) | start tracking their care information |
| `* * *`  | caregiver                                  | view a list of all my patients | see at a glance who I am responsible for |
| `* * *`  | caregiver                                  | view a patient's full profile, including all recorded care information | review everything relevant to that patient in one place |
| `* * *`  | caregiver                                  | delete a patient profile | remove patients no longer under my care |
| `* * *`  | caregiver                                  | record a patient's medical conditions (e.g., diabetes, hypertension) | keep their diagnoses in one place for everyone involved in their care |
| `* * *`  | caregiver                                  | record a patient's allergies | make sure no one gives them a medication or food they react to |
| `* * *`  | caregiver                                  | add a medication to a patient's profile, including its dosage and frequency | keep track of what they need to take |
| `* * *`  | caregiver                                  | add a general care instruction to a patient's profile (e.g., "check blood sugar before breakfast") | remember non-medication routine tasks |
| `* *`    | caregiver                                  | edit a patient's profile details | correct or update their information as it changes |
| `* *`    | caregiver                                  | find a patient by name | locate their profile quickly without scrolling through the whole list |
| `* *`    | new user                                   | view a list of all available commands | learn how to use the app quickly |
| `* *`    | expert user                                | add a patient with all their details in a single command | work faster without going through multi-step prompts |
| `* *`    | caregiver                                  | archive a patient's profile instead of deleting it | keep their history without cluttering my active list |
| `* *`    | caregiver                                  | add a healthcare or emergency contact to a patient's profile | know who to reach for specific needs |
| `* *`    | caregiver                                  | edit a contact's details | keep contact information current |
| `* *`    | caregiver                                  | delete a contact from a patient's profile | stop outdated contacts from cluttering the record |
| `* *`    | caregiver                                  | attach a note to a contact | remember specific advice from each provider |
| `* *`    | caregiver                                  | see which medications are due today | give them to the patient on time |
| `* *`    | caregiver                                  | see the medication times of today for one patient only | prepare that patient's doses without reading the others |
| `* *`    | caregiver                                  | see which of today's medication times have already passed | check whether I missed a dose |
| `* *`    | caregiver                                  | view a patient's full medication schedule at once | spot overlaps or issues easily |
| `* *`    | caregiver                                  | add an appointment for a patient | keep track of upcoming visits |
| `* *`    | caregiver                                  | see a list of upcoming appointments across all patients | plan my schedule |
| `* *`    | caregiver                                  | record notes after an appointment | remember what was discussed for future reference |
| `* *`    | caregiver                                  | view a patient's appointment history | see their past visits at a glance |
| `* *`    | caregiver                                  | back up all my data to a local file | avoid losing information if something goes wrong |
| `* *`    | caregiver                                  | restore my data from a local backup | recover my records if data is lost |
| `* *`    | caregiver                                  | export a single patient's information to a text file | print it or hand it over during a hospital visit |
| `* *`    | caregiver                                  | edit a medication's description or daily times | keep the schedule correct when the doctor changes a prescription |
| `* *`    | caregiver                                  | clear all the daily times of a medication | stop it from being listed as due while I wait for the doctor's new instructions |
| `* *`    | caregiver                                  | delete a medication by its number in the patient's details | remove the right medication without retyping its description |
| `* *`    | caregiver                                  | remove an allergy that was recorded by mistake | keep the patient's allergy information accurate |
| `* *`    | caregiver                                  | edit or remove a care instruction | stop following routines that are no longer recommended |
| `* *`    | caregiver                                  | edit or cancel an appointment | keep the schedule accurate when a visit is rescheduled |
| `* *`    | new user                                   | see sample patient data when I first open the app | understand what a complete record looks like before entering real data |
| `* *`    | new user                                   | clear all sample data at once | start fresh with my own patients |
| `*`      | caregiver                                  | see a daily summary of everything due today across all patients | check all my responsibilities in one place |
| `*`      | caregiver                                  | mark a medication dose as taken | know that it has been given |
| `*`      | caregiver                                  | set a reminder for an upcoming medication dose | be alerted before it is due |
| `*`      | caregiver                                  | check for potential conflicts before adding a new medication | reduce the risk of harmful drug interactions |
| `*`      | caregiver                                  | set a reminder for an appointment | remember to bring the patient to it |
| `*`      | caregiver                                  | mark a care instruction as completed for the day | keep track of my daily caregiving routine |
| `*`      | caregiver                                  | flag a task as urgent | make critical care items stand out from routine ones |
| `*`      | caregiver                                  | log a symptom observation for a patient | track changes in their health over time |
| `*`      | caregiver                                  | view a timeline of a patient's past symptom observations | identify patterns to raise with a doctor |
| `*`      | caregiver with many patients               | filter patients by a medical condition | see everyone affected by a particular condition at once |
| `*`      | caregiver with many patients               | sort patients by their next appointment or medication due | prioritize the patients who need attention soonest |
| `*`      | caregiver                                  | pin the contacts I reach most often to the top of the list | find them faster |
| `*`      | caregiver                                  | attach a photo of a document, such as a referral letter, to a patient's record | stop keeping track of the paper copy |
| `*`      | caregiver                                  | see when a contact's details were last updated | tell at a glance whether they may be out of date |
| `*`      | caregiver who travels for work             | give a family member temporary access to a patient's records | let them take over care while I am away |
| `*`      | expert user                                | use short aliases for common commands | type commands faster |
| `*`      | caregiver                                  | filter a patient's contacts by role (e.g., pharmacist) | find the right provider quickly when I am in a hurry |
| `*`      | caregiver                                  | search a patient's notes by keyword | find when an issue was first mentioned before a check-up |
| `*`      | caregiver                                  | group contacts under a custom tag (e.g., "heart & blood pressure") | see all providers involved in one area of care together |
| `*`      | caregiver returning from a break           | see what changed in a patient's records while I was away | catch up after another caregiver has taken over |
| `*`      | caregiver                                  | give a community nurse view-only access to a patient's records | avoid repeating the patient's background at every visit |
| `*`      | caregiver                                  | export all my data | move to another app without losing my records |
*{More to be added}*

### Use cases

(For all use cases below, the **System** is `MediConnect` and the **Actor** is the `caregiver`, unless specified otherwise)

**Use case: UC1 - Add a patient**

**System**: MediConnect<br>
**Actor**: Caregiver

**MSS**

1.  Caregiver requests to add a patient, providing the patient's name, NRIC, date of birth, phone number, and address.
2.  MediConnect adds the patient and shows the new patient's details.

    Use case ends.

**Extensions**

* 1a. A required detail is missing.

    * 1a1. MediConnect shows an error message with the correct command format.

      Use case ends.

* 1b. A given detail is invalid (e.g., the NRIC is not in a valid format, or the date of birth is in the future).

    * 1b1. MediConnect shows an error message describing the invalid detail.

      Use case ends.

* 1c. The same detail is given more than once (e.g., two names).

    * 1c1. MediConnect shows an error message naming the repeated detail.

      Use case ends.

* 1d. A patient with the same NRIC already exists.

    * 1d1. MediConnect shows an error message stating that the patient already exists.

      Use case ends.

**Use case: UC2 - Delete a patient**

**System**: MediConnect<br>
**Actor**: Caregiver

**MSS**

1.  Caregiver requests to list patients.
2.  MediConnect shows a list of patients.
3.  Caregiver requests to delete a specific patient in the list.
4.  MediConnect deletes the patient and shows the deleted patient's name and NRIC.

    Use case ends.

**Extensions**

* 1a. Caregiver searches for the patient by name instead.

    * 1a1. MediConnect shows the patients whose names match.

      Use case resumes at step 3.

* 2a. The list is empty.

  Use case ends.

* 3a. The given index is invalid.

    * 3a1. MediConnect shows an error message.

      Use case resumes at step 2.

**Use case: UC3 - Add care information to a patient**

**System**: MediConnect<br>
**Actor**: Caregiver

**MSS**

1.  Caregiver requests to list patients.
2.  MediConnect shows a list of patients.
3.  Caregiver requests to add care information (medications, allergens and/or care instructions) to a specific patient in the list.
4.  MediConnect adds the care information to the patient's record and shows the entries that were added.

    Use case ends.

**Extensions**

* 1a. Caregiver searches for the patient by name instead.

    * 1a1. MediConnect shows the patients whose names match.

      Use case resumes at step 3.

* 2a. The list is empty.

  Use case ends.

* 3a. The given index is invalid.

    * 3a1. MediConnect shows an error message.

      Use case resumes at step 2.

* 3b. No care information is given.

    * 3b1. MediConnect shows an error message.

      Use case resumes at step 2.

* 3c. One of the given entries is blank.

    * 3c1. MediConnect shows an error message and does not add any of the entries.

      Use case resumes at step 2.

* 3d. Some of the given entries already exist in the patient's record.

    * 3d1. MediConnect adds only the new entries and tells the caregiver which entries were not added again.

      Use case ends.

* 3e. All the given entries already exist in the patient's record.

    * 3e1. MediConnect shows an error message naming the entries, and adds nothing.

      Use case resumes at step 2.

**Use case: UC4 - Edit a medication of a patient**

**System**: MediConnect<br>
**Actor**: Caregiver

**MSS**

1.  Caregiver requests to list patients.
2.  MediConnect shows a list of patients.
3.  Caregiver requests to view a specific patient in the list.
4.  MediConnect shows the patient's details, with the medications numbered.
5.  Caregiver requests to edit a specific medication of the patient, giving a new description and/or new daily times.
6.  MediConnect updates the medication and shows its number, the patient's name and the updated medication.

    Use case ends.

**Extensions**

* 1a. Caregiver searches for the patient by name instead.

    * 1a1. MediConnect shows the patients whose names match.

      Use case resumes at step 3.

* 2a. The list is empty.

  Use case ends.

* 3a. The given index is invalid.

    * 3a1. MediConnect shows an error message.

      Use case resumes at step 2.

* 5a. The patient index is invalid.

    * 5a1. MediConnect shows an error message that the patient index is invalid.

      Use case resumes at step 2.

* 5b. The medication number is invalid.

    * 5b1. MediConnect shows an error message that the medication number is invalid.

      Use case resumes at step 4.

* 5c. An index is missing, or is not a positive integer.

    * 5c1. MediConnect shows an error message about the invalid command format, with the correct usage of the command.

      Use case resumes at step 4.

* 5d. Neither a new description nor a time is given.

    * 5d1. MediConnect shows an error message that at least a new description or new times must be given.

      Use case resumes at step 4.

* 5e. The new description is blank.

    * 5e1. MediConnect shows an error message that the description cannot be blank.

      Use case resumes at step 4.

* 5f. A given time is not a valid 24-hour time.

    * 5f1. MediConnect shows an error message that times must be in 24-hour format, with an example.

      Use case resumes at step 4.

* 5g. Caregiver requests to clear all daily times but also gives new times.

    * 5g1. MediConnect shows an error message that clearing all times cannot be combined with giving new times.

      Use case resumes at step 4.

* 5h. Another medication of the patient already has the new description.

    * 5h1. MediConnect shows an error message that the medication was not updated because the patient already has a medication with that description.

      Use case resumes at step 4.

* 5i. Caregiver requests to clear all daily times of the medication, without giving new times.

    * 5i1. MediConnect removes all the times of the medication and shows the updated medication.

      Use case ends.

**Use case: UC5 - Delete a medication of a patient**

**System**: MediConnect<br>
**Actor**: Caregiver

**MSS**

1.  Caregiver requests to list patients.
2.  MediConnect shows a list of patients.
3.  Caregiver requests to view a specific patient in the list.
4.  MediConnect shows the patient's details, with the medications numbered.
5.  Caregiver requests to delete a specific medication of the patient.
6.  MediConnect deletes the medication and shows its number, the patient's name and the deleted medication.

    Use case ends.

**Extensions**

* 1a. Caregiver searches for the patient by name instead.

    * 1a1. MediConnect shows the patients whose names match.

      Use case resumes at step 3.

* 2a. The list is empty.

  Use case ends.

* 3a. The given index is invalid.

    * 3a1. MediConnect shows an error message.

      Use case resumes at step 2.

* 5a. The patient index is invalid.

    * 5a1. MediConnect shows an error message that the patient index is invalid.

      Use case resumes at step 2.

* 5b. The medication number is invalid.

    * 5b1. MediConnect shows an error message that the medication number is invalid.

      Use case resumes at step 4.

* 5c. An index is missing, is not a positive integer, or more than two indexes are given.

    * 5c1. MediConnect shows an error message about the invalid command format, with the correct usage of the command.

      Use case resumes at step 4.

**Use case: UC6 - See the medications due today**

**System**: MediConnect<br>
**Actor**: Caregiver

**MSS**

1.  Caregiver requests to see the medications due today.
2.  MediConnect shows today's medication times, sorted by time, for all active patients.

    Use case ends.

**Extensions**

* 1a. Caregiver requests the medications due today for one patient only.

    * 1a1. MediConnect considers only that patient.

      Use case resumes at step 2.

* 1b. The given patient index is invalid.

    * 1b1. MediConnect shows an error message that the patient index is invalid.

      Use case ends.

* 1c. The given index is not a positive integer, or more than one index is given.

    * 1c1. MediConnect shows an error message about the invalid command format, with the correct usage of the command.

      Use case ends.

* 2a. A time is earlier than the current time.

    * 2a1. MediConnect marks that time as earlier today.

      Use case ends.

* 2b. No medication has a time, or the patient is archived.

    * 2b1. MediConnect shows a message that no medications are due today, naming the patient if only one patient was requested.

      Use case ends.

**Use case: UC7 - View a patient's full details**

**System**: MediConnect<br>
**Actor**: Caregiver

**MSS**

1.  Caregiver requests to list patients.
2.  MediConnect shows a list of patients.
3.  Caregiver requests to view a specific patient in the list.
4.  MediConnect shows the patient's details in the detail panel, with their medications, allergens, care instructions, contacts and appointments numbered, and a message with the patient's name.

    Use case ends.

**Extensions**

* 1a. Caregiver searches for the patient by name instead.

    * 1a1. MediConnect shows the patients whose names match.

      Use case resumes at step 3.

* 1b. The patient is archived.

    * 1b1. Caregiver requests to list archived patients instead.

    * 1b2. MediConnect shows a list of archived patients.

      Use case resumes at step 3.

* 2a. The list is empty.

  Use case ends.

* 3a. The given index is invalid.

    * 3a1. MediConnect shows an error message that the patient index is invalid.

      Use case resumes at step 2.

* 3b. The index is missing, or is not a positive integer.

    * 3b1. MediConnect shows an error message about the invalid command format, with the correct usage of the command.

      Use case resumes at step 2.

* 4a. The patient's details are changed later, e.g., care information is added.

    * 4a1. MediConnect updates the detail panel to show the changed details.

      Use case ends.

* 4b. The patient is deleted or archived later.

    * 4b1. MediConnect clears the detail panel.

      Use case ends.

**Use case: UC8 - Back up all patient data**

**System**: MediConnect<br>
**Actor**: Caregiver

**MSS**

1.  Caregiver requests to back up the patient data.
2.  MediConnect saves a copy of all patient data to a new file in `data/backups`, named after the current date and time, and shows the full path of the file.

    Use case ends.

**Extensions**

* 2a. A backup with the same name already exists, e.g., one made in the same second.

    * 2a1. MediConnect saves the backup with a number added to its name, e.g., `-1`, so that the earlier backup is kept, and shows the full path of the file.

      Use case ends.

* 2b. The backup cannot be saved, e.g., because the folder cannot be created.

    * 2b1. MediConnect shows an error message with the folder and the reason, and that the data has not been changed.

      Use case ends.

**Use case: UC9 - Restore patient data from a backup**

**Preconditions**: Caregiver has backed up the data before (UC8).

**System**: MediConnect<br>
**Actor**: Caregiver

**MSS**

1.  Caregiver requests to list the backups.
2.  MediConnect lists the backups in `data/backups`, newest first.
3.  Caregiver requests to restore a specific backup, giving its name.
4.  MediConnect checks the whole backup, then backs up the current data, and replaces all patient data with the data in the backup.
5.  MediConnect shows all active patients, how many patients were restored, and the full path of the backup of the data from before the restore.

    Use case ends.

**Extensions**

* 1a. Caregiver already knows the name of the backup.

  Use case resumes at step 3.

* 2a. There are no backups.

    * 2a1. MediConnect shows a message that no backups were found, and how to make one.

      Use case ends.

* 2b. There are more than 10 backups.

    * 2b1. MediConnect lists only the newest 10, followed by how many older ones there are.

      Use case resumes at step 3.

* 3a. A path is given instead of only the name of a file.

    * 3a1. MediConnect shows an error message asking for only the name of a backup file.

      Use case resumes at step 3.

* 3b. There is no backup with the given name.

    * 3b1. MediConnect shows an error message that no such backup was found, and that the data has not been changed.

      Use case resumes at step 3.

* 4a. The backup cannot be read, e.g., it is damaged, is not a MediConnect data file, or MediConnect has no permission to read it.

    * 4a1. MediConnect shows an error message that the backup could not be read, or that it has no permission to read the backup, and that the data has not been changed.

      Use case ends.

* 4b. The backup has invalid data, e.g., an invalid NRIC or two patients with the same NRIC.

    * 4b1. MediConnect shows an error message with details of the invalid data, and that the data has not been changed.

      Use case ends.

* 4c. The current data cannot be backed up.

    * 4c1. MediConnect shows an error message with the reason, and that nothing was restored.

      Use case ends.

* 5a. Some of the restored patients are archived.

    * 5a1. MediConnect also shows how many of them are archived, and that `listarchived` shows them.

      Use case ends.

* 5b. Caregiver wants the data from before the restore back.

    * 5b1. Caregiver restores the backup that MediConnect made in step 4.

      Use case resumes at step 4.

**Use case: UC10 - Export a patient's record**

**System**: MediConnect<br>
**Actor**: Caregiver

**MSS**

1.  Caregiver requests to list patients.
2.  MediConnect shows a list of patients.
3.  Caregiver requests to export the record of a specific patient in the list.
4.  MediConnect saves the patient's full record to a text file in `data/exports`, named after their NRIC, and shows the full path of the file.

    Use case ends.

**Extensions**

* 1a. Caregiver searches for the patient by name instead.

    * 1a1. MediConnect shows the patients whose names match.

      Use case resumes at step 3.

* 1b. The patient is archived.

    * 1b1. Caregiver requests to list archived patients instead.

    * 1b2. MediConnect shows a list of archived patients.

      Use case resumes at step 3.

* 2a. The list is empty.

  Use case ends.

* 3a. The given index is invalid.

    * 3a1. MediConnect shows an error message that the patient index is invalid.

      Use case resumes at step 2.

* 3b. The index is missing, or is not a positive integer.

    * 3b1. MediConnect shows an error message about the invalid command format, with the correct usage of the command.

      Use case resumes at step 2.

* 4a. The patient's record was exported before.

    * 4a1. MediConnect replaces the earlier file with the new record.

      Use case ends.

* 4b. The file cannot be saved, e.g., because the folder cannot be created.

    * 4b1. MediConnect shows an error message with the path of the file and the reason, and keeps any earlier file unchanged.

      Use case ends.

**Use case: UC11 - Edit a patient's details**

**System**: MediConnect<br>
**Actor**: Caregiver

**MSS**

1.  Caregiver requests to list patients.
2.  MediConnect shows a list of patients.
3.  Caregiver requests to edit one or more basic details of a specific patient in the list.
4.  MediConnect updates the patient and shows the patient's complete updated basic details.

    Use case ends.

**Extensions**

* 1a. Caregiver searches for the patient by name instead.

    * 1a1. MediConnect shows the patients whose names match.

      Use case resumes at step 3.

* 3a. The given patient index is invalid.

    * 3a1. MediConnect shows an error message.

      Use case resumes at step 2.

* 3b. No detail is given, or a given detail is invalid.

    * 3b1. MediConnect shows an error message and changes nothing.

      Use case resumes at step 2.

* 3c. The new NRIC is already used by another patient.

    * 3c1. MediConnect shows an error message and changes nothing.

      Use case resumes at step 2.

**Use case: UC12 - Archive and restore a patient**

**System**: MediConnect<br>
**Actor**: Caregiver

**MSS**

1.  Caregiver requests to list active patients.
2.  MediConnect shows the active patients.
3.  Caregiver requests to archive a specific patient in the list.
4.  MediConnect archives the patient and removes them from the active list.
5.  Caregiver requests to list archived patients.
6.  MediConnect shows the archived patients.
7.  Caregiver requests to restore the archived patient.
8.  MediConnect makes the patient active again and removes them from the archived list.

    Use case ends.

**Extensions**

* 2a. The active patient list is empty.

  Use case ends.

* 3a. The given patient index is invalid.

    * 3a1. MediConnect shows an error message.

      Use case resumes at step 2.

* 6a. The archived patient list is empty.

  Use case ends.

* 7a. The given patient index is invalid.

    * 7a1. MediConnect shows an error message.

      Use case resumes at step 6.

**Use case: UC13 - Update a patient's care entries**

**System**: MediConnect<br>
**Actor**: Caregiver

**MSS**

1.  Caregiver requests to view a patient.
2.  MediConnect shows the patient's numbered care information.
3.  Caregiver requests to replace a specific care instruction.
4.  MediConnect updates the care instruction and shows the new text.

    Use case ends.

**Extensions**

* 3a. Caregiver requests to delete a specific care instruction instead.

    * 3a1. MediConnect deletes the instruction and shows the removed text.

      Use case ends.

* 3b. Caregiver requests to delete a specific allergen instead.

    * 3b1. MediConnect deletes the allergen and shows the removed text.

      Use case ends.

* 3c. The patient index or care-entry number is invalid.

    * 3c1. MediConnect shows an error message and changes nothing.

      Use case resumes at step 2.

* 3d. The replacement care instruction is blank or duplicates another instruction.

    * 3d1. MediConnect shows an error message and changes nothing.

      Use case resumes at step 2.

**Use case: UC14 - Add an appointment**

**System**: MediConnect<br>
**Actor**: Caregiver

**MSS**

1.  Caregiver requests to list patients.
2.  MediConnect shows a list of patients.
3.  Caregiver requests to add an appointment to a specific patient in the list, giving its date, time and who it is with, and optionally a note.
4.  MediConnect adds the appointment, shows the patient in the detail panel with their appointments numbered, and shows the new appointment.

    Use case ends.

**Extensions**

* 1a. Caregiver searches for the patient by name instead.

    * 1a1. MediConnect shows the patients whose names match.

      Use case resumes at step 3.

* 2a. The list is empty.

  Use case ends.

* 3a. The given index is invalid.

    * 3a1. MediConnect shows an error message that the patient index is invalid.

      Use case resumes at step 2.

* 3b. The date, the time or who the appointment is with is missing, or the index is not a positive integer.

    * 3b1. MediConnect shows an error message about the invalid command format, with the correct usage of the command.

      Use case resumes at step 2.

* 3c. A given detail is invalid, e.g., a date that does not exist or is before 1900-01-01, or a blank name of who the appointment is with.

    * 3c1. MediConnect shows an error message describing the invalid detail.

      Use case resumes at step 2.

* 3d. The patient already has an appointment at the same date and time with the same person, ignoring letter case and extra spaces.

    * 3d1. MediConnect shows an error message that the appointment already exists, and adds nothing.

      Use case resumes at step 2.

* 4a. The appointment is in the past, e.g., because a past visit is being recorded.

    * 4a1. MediConnect also notes that the appointment is in the past.

      Use case ends.

* 4b. The appointment is more than 2 years ahead, e.g., because the year was mistyped.

    * 4b1. MediConnect also notes that the appointment is more than 2 years ahead.

      Use case ends.

* 4c. The patient has another appointment at the same date and time, with someone else.

    * 4c1. MediConnect also warns about the other appointment and who it is with.

      Use case ends.

**Use case: UC15 - Edit an appointment**

**System**: MediConnect<br>
**Actor**: Caregiver

**MSS**

1.  Caregiver views a patient's details (UC7).
2.  Caregiver requests to edit a specific appointment of the patient, giving its number in the detail panel and a new date, time, name of who it is with or note.
3.  MediConnect changes only the given details, numbers the patient's appointments again, and shows the edited appointment with its number after the edit.

    Use case ends.

**Extensions**

* 2a. No detail to change is given.

    * 2a1. MediConnect shows an error message that at least one detail must be given.

      Use case resumes at step 2.

* 2b. The patient index or the appointment number is invalid.

    * 2b1. MediConnect shows an error message that the index is invalid.

      Use case resumes at step 2.

* 2c. A given detail is invalid, e.g., a date before 1900-01-01.

    * 2c1. MediConnect shows an error message describing the invalid detail.

      Use case resumes at step 2.

* 2d. The edited appointment would be the same as another appointment of the patient.

    * 2d1. MediConnect shows an error message that the appointment already exists, and changes nothing.

      Use case resumes at step 2.

* 2e. The new note contains a word such as `w/o` (without) or `c/o` (care of).

    * 2e1. MediConnect keeps the word as part of the note.

      Use case resumes at step 3.

* 3a. The edited appointment is in the past, more than 2 years ahead, or at the same date and time as another appointment of the patient.

    * 3a1. MediConnect also shows the same note or warning as when adding the appointment (UC14).

      Use case ends.

**Use case: UC16 - Cancel an appointment**

**System**: MediConnect<br>
**Actor**: Caregiver

**MSS**

1.  Caregiver views a patient's details (UC7).
2.  Caregiver requests to cancel a specific appointment of the patient, giving its number in the detail panel.
3.  MediConnect removes the appointment, numbers the remaining appointments again, and shows the cancelled appointment.

    Use case ends.

**Extensions**

* 2a. The patient index or the appointment number is invalid.

    * 2a1. MediConnect shows an error message that the index is invalid.

      Use case resumes at step 2.

* 2b. The patient index or the appointment number is missing, or is not a positive integer.

    * 2b1. MediConnect shows an error message about the invalid command format, with the correct usage of the command.

      Use case resumes at step 2.

* 2c. An appointment has passed since the patient was shown.

    * 2c1. MediConnect still uses the numbers shown in the detail panel, so the appointment cancelled is the one with the given number in the panel.

      Use case resumes at step 3.

**Use case: UC17 - List appointments**

**System**: MediConnect<br>
**Actor**: Caregiver

**MSS**

1.  Caregiver requests to list the upcoming appointments.
2.  MediConnect lists the upcoming appointments of all active patients, soonest first, each with the patient's name and NRIC.

    Use case ends.

**Extensions**

* 1a. Caregiver requests every appointment of a specific patient in the displayed list instead, which can be an archived patient after listing archived patients.

    * 1a1. MediConnect lists the patient's appointments, upcoming ones first, then past ones, with the numbers that UC15 and UC16 use, and shows the patient in the detail panel.

      Use case ends.

* 1b. The given index is invalid.

    * 1b1. MediConnect shows an error message that the patient index is invalid.

      Use case ends.

* 2a. There are no upcoming appointments.

    * 2a1. MediConnect shows a message that there are no upcoming appointments.

      Use case ends.

*{More to be added}*

**Use case: UC18 - Add a patient's contact**

**System**: MediConnect<br>
**Actor**: Caregiver

**MSS**

1.  Caregiver views a patient's details (UC7).
2.  Caregiver requests to add a contact, giving a name, role and phone, and optionally a note.
3.  MediConnect adds the contact at the end of the patient's contact list and shows the added contact's details.

    Use case ends.

**Extensions**

* 2a. The patient index is missing or invalid.

    * 2a1. MediConnect shows an error message and changes nothing.

      Use case resumes at step 1.

* 2b. A required field is missing, a field is invalid, or the command format is invalid.

    * 2b1. MediConnect shows an error message describing the problem and changes nothing.

      Use case resumes at step 2.

* 2c. The patient already has a contact with the same name and phone under the duplicate rules for contacts.

    * 2c1. MediConnect shows a duplicate-contact error and changes nothing, even if the role or note differs.

      Use case resumes at step 2.

* 3a. The updated patient data cannot be saved.

    * 3a1. MediConnect undoes the addition and shows a save-failure message.

      Use case ends.

**Use case: UC19 - Edit a patient's contact**

**System**: MediConnect<br>
**Actor**: Caregiver

**MSS**

1.  Caregiver views a patient's details (UC7), with the contacts numbered.
2.  Caregiver requests to edit a specific contact, giving its number and one or more replacement fields.
3.  MediConnect changes only the supplied fields, keeps the contact's number, and shows its updated details.

    Use case ends.

**Extensions**

* 2a. The patient index or contact number is missing or invalid.

    * 2a1. MediConnect shows an error message and changes nothing.

      Use case resumes at step 1.

* 2b. No field to change is given, a supplied field is invalid, or the command format is invalid.

    * 2b1. MediConnect shows an error message describing the problem and changes nothing.

      Use case resumes at step 2.

* 2c. The edited contact would duplicate another contact of the patient.

    * 2c1. MediConnect shows a duplicate-contact error and changes nothing.

      Use case resumes at step 2.

* 2d. Caregiver supplies an empty note.

    * 2d1. MediConnect clears the contact's note. An omitted note would preserve the existing note.

      Use case resumes at step 3.

* 3a. The updated patient data cannot be saved.

    * 3a1. MediConnect undoes the edit and shows a save-failure message.

      Use case ends.

**Use case: UC20 - Delete a patient's contact**

**System**: MediConnect<br>
**Actor**: Caregiver

**MSS**

1.  Caregiver views a patient's details (UC7), with the contacts numbered.
2.  Caregiver requests to delete a specific contact, giving its number.
3.  MediConnect removes the contact, renumbers the remaining contacts, and shows the removed contact's details.

    Use case ends.

**Extensions**

* 2a. The patient index or contact number is missing or invalid, or the command format is invalid.

    * 2a1. MediConnect shows an error message and changes nothing.

      Use case resumes at step 1.

* 3a. The updated patient data cannot be saved.

    * 3a1. MediConnect restores the deleted contact and the previous numbering, and shows a save-failure message.

      Use case ends.

### Non-Functional Requirements

1. MediConnect must operate as a single-user application. Its data file must not be accessed by another user during regular operation.
2. MediConnect must work on Windows, Linux, and macOS computers with Java `25` installed.
3. MediConnect must be distributed as a single JAR file of no more than 100 MB and must not require an installer.
4. MediConnect must store its data locally in a human-editable JSON text file that can be edited in a standard text editor. If the edited file is invalid, MediConnect must start with no patients, keep a copy of the unreadable file, and show a warning with the copy's path and failure details, including the file entry position for patient validation errors. If the recovery copy cannot be created, it must instead show a startup error and exit without changing patient data.
5. MediConnect must not use a database management system.
6. MediConnect must not depend on a team-owned remote server for its essential features and must remain usable without an Internet connection.
7. MediConnect should complete common commands, including listing, finding, adding, editing, and deleting records, and `due`, and update the displayed results within one second when managing up to 100 patient records, each with up to 10 medications, on a computer with a 4-core CPU and 8 GB of RAM.
8. A user who types at least 60 words per minute should be able to add a patient, add a medication and delete a patient each in under 30 seconds using commands.
9. MediConnect's GUI should work without resolution-related inconvenience at resolutions of 1920x1080 or higher with 100% or 125% display scaling. All functions must remain accessible at resolutions of 1280x720 or higher with display scaling up to 150%.

### Glossary

* **Allergy**: A harmful reaction a patient has to a substance, such as a drug or a food. Each allergy is recorded by
  the name of the substance that causes it (the *allergen*), e.g., `Penicillin`.
* **Appointment**: A scheduled visit by a patient to a healthcare provider, recorded with its date, time, and provider.
  Notes on what was discussed can be added after the visit.
* **APPT_INDEX**: The number shown beside an appointment in the numbered appointment list of the patient's details,
  starting from 1. Upcoming appointments come first, then past ones. `editappt` and `cancelappt` use it, together with
  the patient's index, to refer to an appointment. It stays the same until the patient is shown again, even after an
  appointment passes.
* **Archive**: To remove a patient's profile from the active patient list while keeping all of its records for future
  reference. Unlike deleting, archiving does not remove any data.
* **Care handover**: The transfer of responsibility for a patient's care from one person to another, e.g., from the
  main caregiver to a relative while the caregiver is travelling.
* **Care information**: The care-related records in a patient's profile, i.e., allergies, medications, care
  instructions, appointments, and contacts. It does not include the patient's basic details.
* **Care instruction**: A routine care task that does not involve medication, e.g., "check blood sugar before
  breakfast".
* **Caregiver**: A person who uses MediConnect to coordinate a patient's care, typically a family member of the
  patient. A caregiver is not necessarily a healthcare professional.
* **CLI (Command Line Interface)**: A way of using an application by typing text commands rather than by clicking on
  elements with a mouse.
* **Community nurse**: A nurse who cares for patients through scheduled home visits.
* **Contact**: A person or organisation linked to a patient's profile, which is either a healthcare provider (e.g., a
  GP, specialist, or pharmacist) or an emergency contact.
* **Daily time**: A time of day, in 24-hour `HH:MM` format, at which a patient takes a medication every day, e.g.,
  `08:00`. A medication can have any number of daily times, or none.
* **Display scaling**: An operating system setting that enlarges text and other on-screen elements by a percentage
  (e.g., 125%) to make them easier to read.
* **Dose**: One daily time of one medication of one patient. `due` lists one line for each dose.
* **Emergency contact**: A person to notify in an emergency involving the patient, such as a family member. An
  emergency contact is not involved in the patient's medical treatment.
* **Expert user**: A user who is familiar with MediConnect's commands and prefers to complete tasks using as few
  commands as possible.
* **GUI (Graphical User Interface)**: The visual part of MediConnect, consisting of windows, lists, and panels that
  display information to the user.
* **Human-editable text file**: A data file stored in a plain-text format that a user can open and modify using a
  standard text editor.
* **Index**: The number shown beside a patient in the currently displayed list, starting from 1. Commands use it to
  refer to a specific patient. A patient's index can change when the displayed list changes, e.g., after a search or a
  deletion.
* **JAR file**: A Java Archive file. MediConnect is distributed as a single JAR file, which can be run on any computer
  with Java installed.
* **MED_INDEX**: The number shown beside a medication in the numbered medication list of the patient's details,
  starting from 1. `editmed` and `deletemed` use it, together with the patient's index, to refer to a medication. It
  can change when a medication before it is deleted.
* **Medication**: A drug prescribed to a patient, recorded with a description (e.g., `Metformin 500mg`) and its daily
  times (e.g., `08:00` and `20:00`).
* **MSS (Main Success Scenario)**: The sequence of steps in a use case that describes the most straightforward
  interaction, in which nothing goes wrong.
* **NRIC**: The identification number on a Singapore National Registration Identity Card, or a Foreign Identification
  Number (FIN), in the format of 1 letter, 7 digits, and 1 letter (e.g., `S1234567A`). MediConnect uses the NRIC to
  tell patients apart, since two patients can have the same name.
* **Patient**: A person whose care is coordinated using MediConnect, typically an elderly or chronically ill person.
* **Patient profile**: The record of a patient in MediConnect. It consists of the patient's basic details (name, NRIC,
  date of birth, phone number, and address) and their care information.
* **Sample data**: Example patient profiles that MediConnect displays when it is launched for the first time, so that
  new users can see what a complete record looks like.

--------------------------------------------------------------------------------------------------------------------

## **Appendix: Instructions for manual testing**

Given below are instructions to test the app manually.

<box type="info" seamless>

**Note:** These instructions only provide a starting point for testers to work on;
testers are expected to do more *exploratory* testing.
</box>

### Launch and shutdown

1. Initial launch

   1. Download `mediconnect.jar` and copy it into an empty folder.

   1. Open a terminal in that folder and run `java -jar mediconnect.jar`.<br>
      Expected: The GUI opens with a set of sample patients. The window size may not be optimal.

1. Saving window preferences

   1. Resize the window to an optimal size. Move the window to a different location. Close the window.

   1. Relaunch the app with `java -jar mediconnect.jar`.<br>
       Expected: The most recent window size and location are retained.

1. Closing the app

   1. Prerequisites: The app is running.

   1. Test case: Type `exit` in the command box and press Enter.<br>
      Expected: The app window closes.

   1. Test case: Close the window using the window's close button.<br>
      Expected: The app window closes. The window size and location are retained on the next launch.

### Viewing help

1. Opening the help window

   1. Test case: `help`<br>
      Expected: A help window opens in front of the main window. It lists every command with its format and a one-line description, under the headings Patients, Care information, Contacts, Appointments, Data and Other, below a link to the User Guide. The status message shows `Opened help window.`

   1. Test case: Click **Copy URL**, then paste into a browser's address bar.<br>
      Expected: The User Guide opens.

   1. Test case: `help add`<br>
      Expected: Same as `help`, because any text after `help` is ignored.

1. Typing while the help window is open

   1. Prerequisites: The help window is open.

   1. Test case: Type `list` and press Enter.<br>
      Expected: The text goes to the main window's command box, not the help window, and the command runs. The help window stays open in front of the main window.

   1. Test case: `help` again<br>
      Expected: No second help window opens. The status message shows `Opened help window.`

   1. Test case: Minimize the help window, then enter `help` again.<br>
      Expected: The help window opens again in front of the main window, and the next command typed goes to the
      main window's command box.

1. Closing the help window

   1. Prerequisites: The help window has just been opened with `help`, so the main window's command box has the focus.

   1. Test case: Type some text in the command box without pressing Enter, then press `Esc`.<br>
      Expected: The help window closes. The typed text stays in the command box.

   1. Test case: Press `F1`, or choose **Help** in the menu bar. Click the help window, then press `Esc`.<br>
      Expected: `F1` and the menu open the help window as `help` does, and `Esc` in the help window closes it.

### Command case sensitivity

1. Test cases: `LIST`, `List`, `Add n/John Tan ic/S1234567A dob/1950-04-12 p/91234567 a/Blk 1`<br>
   Expected: Each returns `Unknown command.` without changing patient data or the displayed list.

1. Test case: `list`<br>
   Expected: The lowercase command succeeds and shows all active patients.

1. Test case: `add N/John Tan ic/S1234567A dob/1950-04-12 p/91234567 a/Blk 1`<br>
   Expected: No patient is added. The command reports `Unrecognized parameter: N/. Please check the command format.`

### Entering a blank command

1. Test case: Leave the command box empty and press Enter.<br>
   Expected: Nothing happens.

1. Test case: Type three spaces in the command box and press Enter.<br>
   Expected: No command executes and no error appears. The spaces and previous result remain unchanged.

### Adding a patient

1. Prerequisites: No patient with NRIC `S1234567A` exists in the data.

1. Test case: `add n/John Tan ic/S1234567A dob/1950-04-12 p/91234567 a/21 Lorong 3, #05-10`<br>
   Expected: The patient is added. The status message begins `New patient added:` and ends
   `Address: 21 Lorong 3, #05-10`, without an extra semicolon after the address.

### Adding a patient with punctuation after a slash word

1. Test case: `add n/Ben s/o, Ng ic/S1111111B dob/1950-01-01 p/91111111 a/Blk 1`<br>
   Expected: No patient is added. The status message shows
   `Unrecognized word: 's/o,'. Write s/o as a separate word without punctuation.`

### Checking slash text and field boundaries

1. Prerequisites: Use sample data in a disposable app folder. Run `view 1`.

1. Test cases: `addcare 1 ci/Use cane and/or walker`, `addcare 1 med/Insulin 10 units/day`, and
   `addcare 1 al/N/A`.<br>
   Expected: Each command succeeds and its complete text appears in the appropriate care list.

1. Test case: `editmed 1 1 med/Amoxicillin 250 mg/ml t/08:00`.<br>
   Expected: The first medication has that description and the time `08:00`.

1. Test case: `addcontact 1 n/Clinic A/B r/GP nt/Ask clinic and/or GP p/123`.<br>
   Expected: The contact is added with its complete name and note, and phone `123`.

1. Test case: `addappt 1 d/2027-01-02 t/10:00 w/Dr Tan nt/dose 2 mg/ml`.<br>
   Expected: The appointment is added with note `dose 2 mg/ml`.

1. Test case: `add n/First Value Test ic/S1111111A dob/1950-04-12 p/91234567 a/x/123`.<br>
   Expected: The patient is added with address `x/123`. Change the NRIC to `S1111112A` and the address to
   `a/https://example.com`, then run again; the URL is also accepted.

1. Test cases: `edit 1 a/p/123`, `addcare 1 med/p/9`, `editcontact 1 1 nt/p/123`.<br>
   Expected: Each reports `Unrecognized parameter: p/. Please check the command format.` Nothing changes.

1. Test cases: `addcare 1 x/123 ci/Text`, `edit 1 n/and/or`, and `editmed 1 1 med/Text t/mg/ml`.<br>
   Expected: Unrecognized-parameter errors naming `x/`, `and/`, and `mg/`, respectively. Nothing changes.

1. Test cases: `addappt 1 d/2027-01-01 t/10:00 w/Dr Tan NT/bring scans`,
   `editmed 1 1 med/Aspirin T/08:00`, and `editcontact 1 1 nt/Nt/Call first`.<br>
   Expected: Unrecognized-parameter errors naming `NT/`, `T/`, and `Nt/`, respectively. Nothing changes.
   Change `NT/` to `nt/` and `T/` to `t/` in the first two commands and retry; the note or time is parsed as a field.
   For the contact command, use `editcontact 1 1 nt/Call first` instead of nesting two note prefixes.

1. Test case: `editcontact 1 1 nt/N/A, call first`.<br>
   Expected: The note becomes `N/A, call first`. `N/A` remains text despite the reserved `n/` prefix.

1. Test case: `edit 1 a/s/o,`.<br>
   Expected: `Unrecognized word: 's/o,'. Write s/o as a separate word without punctuation.` Nothing changes.

### Adding a patient with a blank address

1. Test case: `add n/Ben Ng ic/S1111111B dob/1950-01-01 p/91111111 a/`<br>
   Expected: No patient is added. The status message shows `Addresses cannot be blank.`

### Adding care information

1. Prerequisites: The sample data is loaded. Run `view 1` to show Alex Yeoh's existing care information.

1. Test case: `addcare 1 med/Paracetamol 500mg al/Latex ci/Turn every 2 hours`<br>
   Expected: All three entries are appended to Alex Yeoh's care information and appear in the detail panel. The status
   message names each added entry with its category.

1. Test case: `addcare 1 med/Paracetamol 500mg med/Vitamin D al/latex`<br>
   Expected: Only `Vitamin D` is added. The status message says that `Paracetamol 500mg` and `latex` already exist,
   ignoring letter case, while still reporting the newly added medication.

1. Test case: `addcare 1 med/Ibuprofen al/`<br>
   Expected: Nothing is added. The status message shows `Allergen entries cannot be blank.`

1. Test case: `addcare 999 med/Ibuprofen`<br>
   Expected: Nothing is added. The status message shows `The patient index provided is invalid.`

### Managing patient contacts

1. Canonically equivalent Unicode names

   1. Prerequisites: Use sample data. Copy the names below: `Café` uses U+00E9, while `Café` uses
      U+0065 followed by U+0301 for its last two code points. The two names should look the same.

   1. Test case: `addcontact 1 n/Café r/GP p/123`, followed by
      `addcontact 1 n/Café r/Guardian p/123`.<br>
      Expected: The first succeeds; the second reports the duplicate-contact error and adds nothing.

   1. Test case: `addcontact 1 n/Other clinic r/GP p/123`. Run `view 1` and note its contact number, then run
      `editcontact 1 CONTACT_INDEX n/Café`, replacing `CONTACT_INDEX` with that number.<br>
      Expected: The duplicate-contact error; the existing contacts and their names remain unchanged.

1. Prerequisites: The sample data is loaded. Run `view 1` and note the existing contacts and care information.

1. Test case: `addcontact 1 n/Mary Tan r/Guardian p/+44 20 7946 0958 nt/Call first`<br>
   Expected: A numbered contact appears in the panel, with its role, phone and note. Care information is unchanged.

1. Test case: Repeat the add with `n/MARY   TAN` and `p/+442079460958`.<br>
   Expected: `This contact (identified by name and phone) already exists.` Nothing is added.

1. Test case: `editcontact 1 2 nt/` (use the new contact's displayed number if it differs).<br>
   Expected: The note is cleared while the contact's other fields and number stay the same.

1. Test case: Exit and relaunch, then run `view 1`.<br>
   Expected: Contacts and their order are preserved, including the cleared note.

1. Test case: `deletecontact 1 2` (use the number of the added contact).<br>
   Expected: The removed contact is echoed, disappears from the panel, and remaining contacts are renumbered.

1. Test case: `editcontact 1 99 nt/note`, then `deletecontact 1 99`.<br>
   Expected: Both report `The contact index provided is invalid.` and change nothing.

1. Adding and editing contact fields

   1. Test cases: `addcontact 1 n/ r/GP p/123` and `addcontact 1 n/Review clinic r/GP p/12`.<br>
      Expected: The first reports the contact-name constraints; the second reports the phone constraints.
      Neither adds a contact.

   1. Test case: `addcontact 1 n/Review clinic r/GP p/123 nt/Call first`.<br>
      Expected: The contact is added. Run `view 1` and note its contact number as `CONTACT_INDEX`.

   1. Test case: `editcontact 1 CONTACT_INDEX r/Guardian p/456`, replacing `CONTACT_INDEX` with that number.<br>
      Expected: The role and phone change, while the name, note, contact number and other patient data are preserved.

   1. Test case: `editcontact 1 CONTACT_INDEX`, using the same number.<br>
      Expected: `Specify at least one contact field to edit.` The contact is unchanged.

   1. Test case: `addcontact 1 n/Other review clinic r/GP p/789`. Run `view 1` and note its number as `OTHER_INDEX`.
      Then run `editcontact 1 OTHER_INDEX n/Review clinic p/456`, replacing `OTHER_INDEX` with that number.<br>
      Expected: The second contact is added, but its edit is rejected as a duplicate of `Review clinic` despite
      their different roles and notes. Both contacts remain as they were before the edit.

1. Showing the patient affected by a contact command

   1. Prerequisites: At least two active patients are listed. Run `view 1`.

   1. Test case: `addcontact 2 n/Panel Check r/Guardian p/91234567`<br>
      Expected: The panel switches to patient 2. Scroll to Contacts and note the new contact's number.
      The displayed patient list is unchanged.

   1. Run `view 1`, then `editcontact 2 CONTACT_INDEX nt/Call first`, using the number just noted.<br>
      Expected: The panel switches to patient 2 and shows the updated note under Contacts.

   1. Run `view 1`, then `deletecontact 2 CONTACT_INDEX`, using the same number.<br>
      Expected: The panel switches to patient 2 and the added contact is gone. Remaining contacts are renumbered.

   1. Run `view 1`, then `editcontact 2 999 nt/Call first` and `deletecontact 2 999`, where 999 exceeds the
      number of contacts on patient 2.<br>
      Expected: Each command reports an invalid contact index and the panel continues to show patient 1.

1. Validating contact phone numbers

   1. Test cases: `addcontact 1 n/Short number r/Clinic p/123` and
      `addcontact 1 n/International number r/Clinic p/+123 456-789 012-345`<br>
      Expected: Both contacts are added; the second phone contains exactly 15 digits and retains its formatting.

   1. Test cases: `addcontact 1 n/Bad number r/Clinic p/+++123+++`, then the same command with
      `p/123+456` and `p/1234567890123456`<br>
      Expected: Each command shows the contact-phone constraints and adds no contact.

   1. Run `view 1` and note the contact number of `Short number`. Run
      `editcontact 1 CONTACT_INDEX p/+1234567890123456`, using that contact number.<br>
      Expected: The edit is rejected with the same constraints; the phone remains `123`.

   1. In a disposable app folder, close the app and change a contact phone in `data/mediconnect.json`
      to `+++123+++`, then relaunch.<br>
      Expected: The file is rejected, startup shows the recovery-copy path, and the original invalid data is preserved.

1. Contact text length limits

   1. Prerequisites: Prepare strings of 100 and 101 characters for names/roles, and 500 and 501 for notes.
      Replace `NAME`, `ROLE` and `NOTE` in the commands below with those strings, without the placeholder labels.

   1. Test case: `addcontact 1 n/NAME r/ROLE p/123 nt/NOTE`, using the 100/100/500-character strings.<br>
      Expected: The contact is added. Run `view 1` and note its contact number.

   1. Repeat the add with one field over its limit at a time, then try `editcontact 1 CONTACT_INDEX n/NAME`,
      `editcontact 1 CONTACT_INDEX r/ROLE` and `editcontact 1 CONTACT_INDEX nt/NOTE` with overlong values.<br>
      Expected: Each command reports the affected field's length constraint and leaves contacts unchanged.

1. Copied no-break spaces in contact fields

   1. Prerequisites: Use sample data. The gaps in `Whitespace Contact` and the blank values below are
      no-break spaces (U+00A0); copy and paste them when testing.

   1. Test case: `addcontact 1 n/Whitespace Contact r/Guardian p/91234567 nt/Call first`<br>
      Expected: The contact is added. Run `view 1` and note its contact number.

   1. Test case: `addcontact 1 n/Whitespace Contact r/Guardian p/91234567`<br>
      Expected: The duplicate-contact error; no second contact is added.

   1. Test cases: `addcontact 1 n/  r/Guardian p/91234567` and
      `addcontact 1 n/Whitespace Contact r/  p/91234567`<br>
      Expected: Blank-name and blank-role errors respectively, without adding contacts.

   1. Test case: `editcontact 1 CONTACT_INDEX nt/ `, using the noted contact number.<br>
      Expected: The note is cleared, as with an ordinary-space-only note. Other fields are unchanged.

1. Adding and editing contacts with slash words

   1. Test case: `addcontact 1 n/Tan s/o Lim r/Guardian p/91234567 nt/Call c/o daughter`<br>
      Expected: The contact is added with the name and note unchanged. Run `view 1` to check its contact number.

   1. Test case: `editcontact 1 CONTACT_INDEX n/Siti d/o Ahmad nt/Call C/O family`, replacing `CONTACT_INDEX`
      with the number from the previous step.<br>
      Expected: The contact is updated, preserving the spelling and letter case of both fields.

   1. Other accepted standalone words to try: `a/l`, `a/p`, `S/O`, `D/O`, `C/O`.<br>
      Try these in a name, role or note for both add and edit.

   1. Test case: `editcontact 1 CONTACT_INDEX nt/Call c/o, family`, using the same contact number.<br>
      Expected: No contact is changed. The message explains that `c/o` must be a separate word without punctuation.

### Listing active patients

1. Listing patients when there is data

   1. Prerequisites: The app has at least one patient, e.g. the sample data on first launch.

   1. Test case: `list`<br>
      Expected: All active patients are shown in the patient list. The status message shows
      `Listed all active patients.`

   1. Test case: `find John` followed by `list`<br>
      Expected: The filter is cleared and all active patients are shown again. The status message shows
      `Listed all active patients.`

1. Listing patients when there is no data

   1. Prerequisites: Clear all data using `clear confirm`; its automatic backup preserves the previous data.<br>
      Expected: The status message shows `All patients have been cleared.` followed by the backup path.

   1. Test case: `list`<br>
      Expected: The patient list is empty. The status message shows `No active patients found.`

### Clearing all patients

1. Prerequisites: Have active and archived patients, and run `view 1`.

1. Test cases: `clear`, `clear 1`, `clear CONFIRM`, `clear confirm extra`<br>
   Expected: `Invalid command format!` followed by the clearing warning and `Format: clear confirm`.
   All patients and the viewed patient remain unchanged, and no backup is created.

1. Test case: `clear confirm`<br>
   Expected: `All patients have been cleared.` followed by `Backup saved to: PATH`. The patient list is empty,
   `listarchived` reports no archived patients, and the detail panel shows its placeholder. Restarting the app still shows no patients.
   A new backup contains all previous active and archived patients. Note its file name for the recovery check below.

1. Test case: `clear confirm` again<br>
   Expected: The same success message and a new backup path. The empty address book is backed up without
   overwriting the earlier backup.

1. Test case: `restore FILENAME`, using the backup noted before the repeated clear<br>
   Expected: All original active and archived patients are restored. The patient list shows the active patients;
   `listarchived` shows the archived patients.

1. Simulate a backup failure in a disposable app folder by making `data/backups` a regular file, then run
   `clear confirm`.<br>
   Expected: A backup failure message. All patients and the viewed patient remain unchanged.

### Deleting a patient

1. Deleting a patient while all active patients are being shown

   1. Prerequisites: List all active patients using the `list` command, with multiple patients in the list.

   1. Test case: `delete 1`<br>
      Expected: The first patient is deleted from the list. The status message shows `Deleted patient: NAME; NRIC: NRIC` with the deleted patient's name and NRIC, e.g. `Deleted patient: Alex Yeoh; NRIC: S0000001A` for the sample data.

   1. Test case: `delete 0`<br>
      Expected: No patient is deleted. The status message shows `Invalid command format!` followed by the `delete` usage: `delete: Deletes the patient at the specified INDEX.`, `Parameters: INDEX (must be a positive integer)` and `Example: delete 3`.

   1. Test case: `delete x` where x is larger than the list size<br>
      Expected: No patient is deleted. The status message shows `The patient index provided is invalid.`

   1. Other incorrect delete commands to try: `delete`, `delete abc`, `delete -1`<br>
      Expected: Similar to `delete 0`.

1. Deleting a patient after finding patients

   1. Prerequisites: The sample data is loaded. Run `find Alex` so that only one patient is shown.

   1. Test case: `delete 1`<br>
      Expected: The patient found by `find Alex` is deleted, not the first patient of the full list. The status message shows `Deleted patient: Alex Yeoh; NRIC: S0000001A`.

   1. Test case: `delete 2`<br>
      Expected: No patient is deleted. The status message shows `The patient index provided is invalid.`, because the index refers to the filtered list.

### Finding patients

1. Finding patients by name

   1. Prerequisites: The sample data is loaded, with patients named `Alex Yeoh`, `Bernice Yu`, `Charlotte Oliveiro` and `David Li`.

   1. Test case: `find Alex`<br>
      Expected: Only `Alex Yeoh` is shown in the patient list. The status message shows `1 patient(s) listed!`

   1. Test case: `find aLeX`<br>
      Expected: Same as `find Alex`, because keywords are matched case-insensitively.

   1. Test case: `find alex david`<br>
      Expected: `Alex Yeoh` and `David Li` are shown. The status message shows `2 patient(s) listed!`

   1. Test case: `find Zebra`<br>
      Expected: The patient list is empty. The status message shows `No patients found matching the given keyword(s).`

   1. Test case: `find`<br>
      Expected: The patient list is unchanged. The status message shows `Please provide at least one keyword to search for.`

### Viewing a patient

1. Viewing a patient while all active patients are being shown

   1. Prerequisites: List all active patients using the `list` command, with multiple patients in the list.

   1. Test case: `view 1`<br>
      Expected: The detail panel shows all fields of the first patient, with `None recorded` under each empty care list. The status message shows `Showing patient:` followed by the patient's name.

   1. Test case: `view 2` on the sample data<br>
      Expected: The detail panel shows Bernice Yu's appointment as `1. 2026-10-02 10:00 (past) with Polyclinic nurse (Blood pressure normal)`.

   1. Test case: `view 0`<br>
      Expected: The detail panel is unchanged. The status message shows the invalid command format error.

   1. Other incorrect view commands to try: `view`, `view x`, `view 1 2`, `view y` (where y is larger than the list size)<br>
      Expected: The detail panel is unchanged. The status message shows an error.

1. Filtering out the patient being viewed

   1. Prerequisites: `list`, then `view 1`. The detail panel shows `List index: 1` under the name.

   1. Test case: `find Bernice`<br>
      Expected: The detail panel still shows the first patient, now with `Not in the displayed list` under the name. Running `list` shows `List index: 1` again.

1. Deleting the patient being viewed

   1. Prerequisites: `view 1` has been run.

   1. Test case: `delete 1`<br>
      Expected: The detail panel returns to the placeholder message.

### Saving data

1. Dealing with missing data files

   1. Prerequisites: The app is closed. Delete `data/mediconnect.json`.

   1. Launch the app.<br>
      Expected: The app starts with the sample patients. A new `data/mediconnect.json` containing them is created on launch.

1. Dealing with corrupted data files

   1. Prerequisites: The app is closed. In `data/mediconnect.json`, change the second patient's `phone` to `123`.

   1. Launch the app.<br>
      Expected: The app starts with no patients and shows a warning containing the recovery copy's full path and
      `Details: Patient entry 2:` followed by the phone-number constraints.
      The recovery copy contains the original invalid file unchanged.

   1. Run `list`, then close and relaunch the app.<br>
      Expected: `list` leaves the invalid data file unchanged. Relaunching shows the warning again and creates
      another recovery copy; both copies retain the original contents.

   1. Close the app. Repair a copy of the recovery file, replace `data/mediconnect.json` with it, and relaunch.<br>
      Expected: The repaired patients load normally without a recovery warning.

   1. In a disposable app folder, replace the data file with malformed JSON, such as `{"persons": [`, and launch.<br>
      Expected: The warning keeps the recovery path and asks you to check the JSON format and file permissions.
      It does not display raw parser exceptions or claim to identify a patient entry.

1. Starting when a missing data file cannot be created

   1. Prerequisites: Use a disposable folder containing the app JAR, with the app closed and no existing data.
      Create a regular file named `data` (without an extension) in that folder, so it blocks the data folder.

   1. Launch the app.<br>
      Expected: The sample patients appear, then a warning says they are not saved, names the full data-file path,
      gives a reason and asks you to check the parent folder and free space. The blocking file is unchanged.

   1. Close the app, move the blocking file elsewhere, and relaunch.<br>
      Expected: The app creates `data/mediconnect.json` with sample patients and shows no creation warning.

### Startup failures that prevent data recovery

1. Prerequisites: Use a disposable app folder. With the app closed, create a directory named
   `data/mediconnect.json` in place of the data file. Keep any real data elsewhere.

1. Launch MediConnect.<br>
   Expected: No main window opens. An error dialog titled `MediConnect could not start` gives the data-file
   path and says it is a folder instead of a file. Closing the dialog exits the app. The directory is unchanged.

1. Replace that directory with a valid data file and relaunch.<br>
   Expected: The main window opens normally with the saved patients.

1. Where file permissions can be controlled, deny read access to the test data file, then launch.<br>
   Expected: A startup error explains the read/copy permission problem. The patient data remains unchanged.
   Restore permissions before continuing. Do not run this test with an account that bypasses those permissions.

1. Alternatively, use malformed JSON in a readable test data file and deny write access to its containing folder.<br>
   Expected: Failure to create a recovery copy produces the same startup error instead of opening an empty model.
   Restore permissions and repair or replace the test file before relaunching.

### Backing up data

1. Backing up the current data

   1. Test case: `backup`<br>
      Expected: A new file named after the current date and time, e.g., `mediconnect-20261020-083015.json`, appears in `data/backups`, holding the same patients as `data/mediconnect.json`. The status message shows `Backup saved to:` followed by the full path of the file.

1. Backing up when the backup folder cannot be created

   1. Prerequisites: The app is closed. In the `data` folder, delete the `backups` folder if there is one, and create an empty file named `backups` (with no extension) in its place.

   1. Launch the app, then enter `backup`.<br>
      Expected: No backup is saved. The status message shows `Could not save the backup to` followed by the folder, says that there is a file named `backups` where that folder should be, and says that your data has not been changed.

### Restoring data

1. Restoring a backup

   1. Prerequisites: Enter `backup` and note the name of the file in the status message. Then delete a patient, e.g., with `delete 1`.

   1. Test case: `restore FILENAME`, where `FILENAME` is the name you noted<br>
      Expected: The deleted patient is back, and the list shows all active patients. The status message shows how many patients were restored, and the full path of an automatic backup of the data from just before the restore.

   1. Test case: `restore FILENAME`, where `FILENAME` is the name of the automatic backup from the previous test case<br>
      Expected: The data from before the previous restore is back, i.e., the patient is deleted again.

   1. Test case: `restore FILENAME`, where `FILENAME` is the name you noted first, without the `.json` at the end<br>
      Expected: The same as the first test case. The status message gives the name with `.json`.

1. Listing the backups

   1. Prerequisites: The test cases under "Restoring a backup" above have been run.

   1. Test case: `restore`<br>
      Expected: The data is unchanged. The status message lists the backups in `data/backups`, newest first: the automatic backups made by the three restores, then the backup you made.

1. Restoring a file that cannot be restored

   1. Test case: `restore nosuchfile.json`<br>
      Expected: The data is unchanged, and no automatic backup is saved. The status message says that no backup named `nosuchfile.json` was found in `data/backups`.

   1. Test case: `restore ../mediconnect.json`<br>
      Expected: The data is unchanged. The status message asks for only the name of a backup file, not a path.

   1. Prerequisites: Create a file `bad.json` in `data/backups` containing only the text `hello`.<br>
      Test case: `restore bad.json`<br>
      Expected: The data is unchanged. The status message says that the backup could not be read.

   1. Prerequisites: In `data/backups`, copy a backup to `invalid.json`, and change a patient's `nric` in it to `123`.<br>
      Test case: `restore invalid.json`<br>
      Expected: The data is unchanged. The status message says that the backup has invalid data, followed by details that start with the changed patient's position in the file, counting from 1, then give the NRIC format, e.g., `Details: Patient entry 2: NRIC should be ...` if you changed the second patient.

   1. Other incorrect restore commands to try: `restore .`, `restore data/backups/bad.json`<br>
      Expected: The data is unchanged. The status message shows an error.

### Exporting a patient's record

1. Exporting a patient while all active patients are being shown

   1. Prerequisites: List all active patients using the `list` command, with multiple patients in the list.

   1. Test case: `export 1`<br>
      Expected: A file named after the first patient's NRIC, e.g., `S0000001A.txt`, appears in `data/exports`. It has a title, the date and time of the export, the patient's details, and their numbered medications (with their daily times, if any), allergens, care instructions, contacts and appointments (upcoming ones first, then past ones marked `(past)`, decided at the time of the export), with `None recorded` for any empty list. The status message shows `Exported`, the patient's name and the full path of the file.

   1. Test case: `addcare 1 med/Paracetamol 500mg`, then `export 1` again<br>
      Expected: The same file is replaced, and now lists `Paracetamol 500mg` under Medications.

   1. Test case: `export 0`<br>
      Expected: No file is written. The status message shows the invalid command format error.

   1. Other incorrect export commands to try: `export`, `export x`, `export y` (where y is larger than the list size)<br>
      Expected: No file is written. The status message shows an error.

1. Exporting from a filtered list

   1. Prerequisites: `find Bernice` shows exactly one patient.

   1. Test case: `export 1`<br>
      Expected: The file for the patient shown is written, named after their NRIC, not the file for the first patient in the full list.

1. Exporting when the export folder cannot be created

   1. Prerequisites: The app is closed. In the `data` folder, delete the `exports` folder if there is one, and create an empty file named `exports` (with no extension) in its place.

   1. Launch the app, then enter `export 1`.<br>
      Expected: No file is written. The status message shows `Could not export`, the patient's name and the path of the file, and says that there is a file named `exports` where that folder should be.

### Managing appointments

1. Adding appointments

   1. Prerequisites: The sample data is loaded. Run `view 1` to show Alex Yeoh, who has two upcoming appointments.

   1. Test case: `addappt 1 d/2027-11-20 t/10:00 w/Dr Lim nt/Bring scans`<br>
      Expected: `New appointment for Alex Yeoh: 2027-11-20 10:00 with Dr Lim (Bring scans)`. The panel lists it as appointment 2, between the two earlier ones.

   1. Test case: `addappt 1 d/2027-11-20 t/10:00 w/dr  lim`<br>
      Expected: `This appointment already exists for this patient.` Nothing is added.

   1. Test case: `addappt 1 d/2027-11-20 t/10:00 w/Dr Ong`<br>
      Expected: The appointment is added, and the message ends with `Warning: Alex Yeoh already has an appointment at this date and time (with Dr Lim).`

   1. Test case: `addappt 1 d/2020-01-01 t/09:00 w/Dr Tan`<br>
      Expected: The appointment is added, and the message ends with `Note: this appointment is in the past.` The panel lists it last, marked `(past)` after its date and time.

   1. Test case: `addappt 1 d/2207-11-05 t/09:30 w/Dr Tan`<br>
      Expected: The appointment is added, and the message ends with `Note: this appointment is more than 2 years ahead.`

   1. Incorrect commands to try: `addappt 1 d/1899-12-31 t/09:30 w/Dr Tan`, `addappt 1 d/2027-02-30 t/09:30 w/Dr Tan`, `addappt 1 d/2027-11-20 t/25:00 w/Dr Tan`, `addappt 1 d/2027-11-20 t/10:00 w/`, `addappt 1 d/2027-11-20 t/10:00`, `addappt 9 d/2027-11-20 t/10:00 w/Dr Tan`<br>
      Expected: Nothing is added. The status message shows, in turn, the date rule (twice), the time rule, that who the appointment is with cannot be blank, the invalid command format, and that the patient index is invalid.

1. Editing appointments

   1. Prerequisites: The app is closed. Delete `data/mediconnect.json`, launch the app so that the sample data is loaded, then run `view 1`.

   1. Test case: `editappt 1 1 t/10:00`<br>
      Expected: `Edited appointment 1 of Alex Yeoh: 2027-11-05 10:00 with Dr Tan`. Only the time changes.

   1. Test case: `editappt 1 2 d/2027-11-01`<br>
      Expected: `Edited appointment 1 of Alex Yeoh: 2027-11-01 14:00 with Cardiology clinic (Bring BP log)`. The appointment now comes before the one with Dr Tan, so its number changes from 2 to 1 in the message and the panel.

   1. Test case: `editappt 1 1 nt/MRI w/o contrast`<br>
      Expected: The note becomes `MRI w/o contrast`, and the appointment is still with `Cardiology clinic`.

   1. Incorrect commands to try: `editappt 1 1`, `editappt 1 9 t/10:00`<br>
      Expected: Nothing is changed. The status message shows `At least one of d/, t/, w/ or nt/ must be provided.` and `The appointment index provided is invalid.` respectively.

1. Cancelling appointments

   1. Prerequisites: The app is closed. Delete `data/mediconnect.json`, launch the app so that the sample data is loaded, then run `view 1`.

   1. Test case: `cancelappt 1 2`<br>
      Expected: `Cancelled appointment 2 of Alex Yeoh: 2027-12-10 14:00 with Cardiology clinic (Bring BP log)`. The panel shows only the appointment with Dr Tan.

   1. Incorrect commands to try: `cancelappt 1 9`, `cancelappt 1`<br>
      Expected: Nothing is cancelled. The status message shows `The appointment index provided is invalid.` and the invalid command format respectively.

1. Listing appointments

   1. Prerequisites: The app is closed. Delete `data/mediconnect.json`, then launch the app so that the sample data is loaded.

   1. Test case: `listappt`<br>
      Expected: `Upcoming appointments:`, followed by `Alex Yeoh (S0000001A): 2027-11-05 09:30 with Dr Tan` and `Alex Yeoh (S0000001A): 2027-12-10 14:00 with Cardiology clinic (Bring BP log)`. Bernice Yu's past appointment is not listed. The patient list does not change.

   1. Test case: `listappt 2`<br>
      Expected: `Appointments for Bernice Yu:`, then `Upcoming: none`, then `Past:` and `1. 2026-10-02 10:00 with Polyclinic nurse (Blood pressure normal)`. The panel shows Bernice Yu.

   1. Test case: `listappt 3`<br>
      Expected: `Charlotte Oliveiro has no appointments.`

   1. Test case: `archive 2`, then `listarchived`, then `listappt 1`<br>
      Expected: Bernice Yu's appointment is listed as in the `listappt 2` test case above, although she is archived.

   1. Incorrect commands to try: `listappt 9`, `listappt 0`<br>
      Expected: The status message shows `The patient index provided is invalid.` and the invalid command format respectively.
