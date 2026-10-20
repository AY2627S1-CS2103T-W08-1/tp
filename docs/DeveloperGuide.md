---
  layout: default.md
  title: "Developer Guide"
  pageNav: 3
---

# AB-3 Developer Guide

<!-- * Table of Contents -->
<page-nav-print />

--------------------------------------------------------------------------------------------------------------------

## **Acknowledgements**

* _{List the sources of reused or adapted ideas, code, documentation, and third-party libraries here, with links to the originals.}_

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

**`Main`** (consisting of classes [`Main`](https://github.com/se-edu/addressbook-level3/tree/master/src/main/java/seedu/address/Main.java) and [`MainApp`](https://github.com/se-edu/addressbook-level3/tree/master/src/main/java/seedu/address/MainApp.java)) is in charge of the app launch and shut down.
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
* `PatientDetailPanel` shows the full details of the patient being viewed, including their medications, allergens, and care instructions. It observes `Logic#getViewedPatient()` and redraws itself whenever that value changes, showing a placeholder message when no patient is being viewed. See the [view patient feature](#view-patient-feature) for how the viewed patient is set and cleared.

The `UI` component uses the JavaFX UI framework. The layouts of these UI parts are defined in matching `.fxml` files in `src/main/resources/view`. For example, [`MainWindow.fxml`](https://github.com/AY2627S1-CS2103T-W08-1/tp/tree/master/src/main/resources/view/MainWindow.fxml) specifies the layout of [`MainWindow`](https://github.com/AY2627S1-CS2103T-W08-1/tp/tree/master/src/main/java/seedu/address/ui/MainWindow.java).

The `UI` component,

* executes user commands using the `Logic` component.
* listens for changes to `Model` data so that the UI can be updated with the modified data.
* keeps a reference to the `Logic` component, because the `UI` relies on the `Logic` to execute commands.
* depends on some classes in the `Model` component because it displays `Patient` objects from the model.

### Logic component

**API** : [`Logic.java`](https://github.com/se-edu/addressbook-level3/tree/master/src/main/java/seedu/address/logic/Logic.java)

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

Here are the other classes in `Logic` (omitted from the class diagram above) that are used for parsing a user command:

<puml src="diagrams/ParserClasses.puml" width="600"/>

How the parsing works:
* When called upon to parse a user command, the `AddressBookParser` class creates an `XYZCommandParser` (`XYZ` is a placeholder for the specific command name, e.g., `AddCommandParser`). The parser uses the other classes shown above to parse the user command and create an `XYZCommand` object (e.g., `AddCommand`). The `AddressBookParser` returns that object as a `Command` object.
* All `XYZCommandParser` classes, such as `AddCommandParser` and `DeleteCommandParser`, implement the `Parser` interface so they can be treated similarly where appropriate, for example during testing.

### Model component
**API** : [`Model.java`](https://github.com/AY2627S1-CS2103T-W08-1/tp/blob/master/src/main/java/seedu/address/model/Model.java)

<puml src="diagrams/ModelClassDiagram.puml" width="450" />


The `Model` component,

* stores the patient data, i.e., all `Patient` objects (which are contained in a `UniquePatientList` object).
* stores the `Patient` objects selected by the current filter, such as search results, in a separate _filtered_ list. It exposes this list as an unmodifiable `ObservableList<Patient>` that the UI can observe and bind to, so the UI updates when the list changes.
* stores the patient being viewed, if any. It exposes this patient as an `ObservableValue<Patient>` that the UI observes, so the detail panel updates when the viewed patient changes.
* stores a `UserPrefs` object that represents the user’s preferences (currently, just the GUI settings). This is exposed to the outside as a `ReadOnlyUserPrefs` object.
* does not depend on any of the other three components (as the `Model` represents data entities of the domain, they should make sense on their own without depending on other components)


<box type="info" seamless>

**Patient records:** Each `Patient` contains `Name`, `Nric`, `Dob`, `Phone` and `Address` values, plus a
`List<Medication>` medication list, `List<String>` allergen and care instruction lists, and a `List<Contact>`
contact list.
These lists default to empty and are defensively copied with `List.copyOf`, so callers cannot modify them through
the getters. Updating a patient creates a replacement `Patient`, which the model stores using `setPatient`.

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

Each patient is saved as a `JsonAdaptedPatient`, which holds the name, NRIC, date of birth, phone number and address
as strings, medications as a list of `JsonAdaptedMedication` objects, allergens and care instructions as lists of
strings, and contacts as a list of `JsonAdaptedContact` objects. Each contact stores its name, role, phone and note.
The list of patients is saved under the JSON key `persons`, kept from AB3. When the file is read,
`JsonAdaptedPatient#toModelType()` validates patient
fields and converts contacts using their model constraints. If any patient or contact is invalid, two patients share
an NRIC, or a patient has duplicate contacts, the whole file is rejected.

On a `DataLoadingException`, `MainApp` copies the unreadable file beside the original as
`mediconnect-unreadable-<UUID>.json` before creating an empty model. The copy uses no replacement option, so an
existing recovery file is never overwritten. The UI shows its absolute path in a startup warning, and later commands
save normally to the original data path. If preservation fails, the exception stops initialization before commands
can overwrite the file. JSON validation stays in storage; startup recovery stays in `MainApp`.

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
1. `PatientDetailPanel` listens to `Logic#getViewedPatient()` and redraws itself whenever the value changes. It shows a placeholder message when the value is `null`, and `None recorded` for an empty medication, allergen, or care instruction list.

`ModelManager` keeps the viewed patient consistent with the address book:

* `setPatient(target, editedPatient)` replaces the viewed patient with `editedPatient` if `target` is being viewed, so the panel shows changes such as newly added care information.
* `deletePatient(target)` clears the viewed patient if `target` is being viewed.
* `setAddressBook(...)`, which the `clear` command uses, clears the viewed patient if it is not in the new data.

Filtering the patient list (e.g., with `find`) does not change the viewed patient.

#### Design considerations

**Aspect: Where the viewed patient is stored**

* **Alternative 1 (current choice):** In the `Model`, as an observable value.
  * Pros: Commands that change patients can keep the panel up to date without knowing about the UI. The behavior can be unit tested in `ModelManagerTest`.
  * Cons: Adds a small amount of UI-related state to the `Model`.
* **Alternative 2:** Return the patient in `CommandResult` and let `MainWindow` update the panel.
  * Pros: The `Model` stays unchanged.
  * Cons: The panel would show stale data after the patient is edited or deleted by another command.

### Edit patient feature

The `edit INDEX [n/NAME] [ic/NRIC] [dob/DOB] [p/PHONE] [a/ADDRESS]` command replaces one or more core details
of a patient in the displayed list while preserving all care-related data and state.

#### Implementation

The sequence diagram below shows the successful execution of `edit 1 p/91234567`. Error paths are omitted for
clarity.

<puml src="diagrams/EditSequenceDiagram.puml" alt="Interactions for the `edit` Command" />

1. `EditCommandParser` parses a positive patient index and the supplied fields. It requires at least one field,
   rejects repeated or unknown prefixes, and uses the same field parsers as `add`.
1. `EditCommand#execute()` resolves the index against the filtered patient list and creates an immutable replacement
   using the supplied values and the existing values for omitted fields.
1. The replacement preserves medications and their times, allergens, care instructions, contacts, and archived
   state. A changed NRIC is rejected if another patient already has it.
1. The command calls `Model#setPatient(patient, editedPatient)`. If the patient is currently being viewed,
   `ModelManager#setPatient()` updates `viewedPatient`, and the `PatientDetailPanel` listener redraws the panel.

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

### Contact management

`Contact` is immutable and contains a name, role, phone and optional note (stored as an empty string when absent).
Names are nonblank free text because contacts can be people or organisations, such as `Tan & Partners Clinic`.
They deliberately do not use patient `Name` constraints. Contact phones likewise have their own rules so clinic
and international numbers work without weakening patient-phone validation.

`Patient` keeps an immutable contact list in insertion order. `withContacts` replaces only that list; `addcare`
copies it when rebuilding a patient. Contact identity compares names ignoring case and collapsed spaces, and
phones ignoring spaces and hyphens, while retaining `+`. The model rejects duplicate identities within a patient.

The three contact parsers use the shared prefixes in `CliSyntax`. Add and edit reject unknown and repeated
prefixes without slash-word exceptions. Notes from commands use `trim()`; stored notes are
preserved when loading. Edit uses `Optional<String>` to distinguish
an omitted note (preserve) from a present empty note (clear). Commands use displayed patient indices and
contact indices from the detail panel. They validate indices and duplicates before calling `Model#setPatient`.
The existing viewed-patient listener refreshes that panel automatically.

`JsonAdaptedContact` saves the four contact fields. Missing contact lists load as empty, and missing notes load as
empty strings. Null entries, invalid fields and duplicate identities reject the data file with a reason.
`PatientDetailPanel` numbers contacts in insertion order and shows `None recorded` for an empty list.

The diagram focuses on parsing and updating the patient; `LogicManager` performs its usual save after execution.

<puml src="diagrams/AddContactSequenceDiagram.puml" alt="Adding a contact to a patient" />

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

_{more aspects and alternatives to be added}_

### \[Proposed\] Data archiving

_{Explain here how the data archiving feature will be implemented}_


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
* needs to track patient profiles, conditions, allergies, medications, appointments, and care instructions
* currently relies on fragmented sources such as messaging threads, paper notes, and memory
* needs quick access to accurate information during daily care, handovers, appointments, and emergencies
* is comfortable with everyday applications but may not be technically proficient
* prefers a local desktop application with a fast, keyboard-first workflow

**Value proposition**: MediConnect centralizes patient profiles, care contacts, medical notes, appointment history,
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
| `* *`    | caregiver                                  | view a patient's full medication schedule at once | spot overlaps or issues easily |
| `* *`    | caregiver                                  | add an appointment for a patient | keep track of upcoming visits |
| `* *`    | caregiver                                  | see a list of upcoming appointments across all patients | plan my schedule |
| `* *`    | caregiver                                  | record notes after an appointment | remember what was discussed for future reference |
| `* *`    | caregiver                                  | view a patient's appointment history | see their past visits at a glance |
| `* *`    | caregiver                                  | see a daily summary of everything due today across all patients | check all my responsibilities in one place |
| `* *`    | caregiver                                  | back up all my data to a local file | avoid losing information if something goes wrong |
| `* *`    | caregiver                                  | restore my data from a local backup | recover my records if data is lost |
| `* *`    | caregiver                                  | export a single patient's information to a text file | print it or hand it over during a hospital visit |
| `* *`    | caregiver                                  | edit a medication's dosage or frequency | keep the schedule correct when the doctor changes a prescription |
| `* *`    | caregiver                                  | remove a medication that is no longer prescribed | stop it from appearing in the patient's schedule |
| `* *`    | caregiver                                  | remove an allergy that was recorded by mistake | keep the patient's allergy information accurate |
| `* *`    | caregiver                                  | edit or remove a care instruction | stop following routines that are no longer recommended |
| `* *`    | caregiver                                  | edit or cancel an appointment | keep the schedule accurate when a visit is rescheduled |
| `* *`    | new user                                   | see sample patient data when I first open the app | understand what a complete record looks like before entering real data |
| `* *`    | new user                                   | clear all sample data at once | start fresh with my own patients || `*`      | caregiver                                  | mark a medication dose as taken | know that it has been given |
| `*`      | caregiver                                  | set a reminder for an upcoming medication dose | be alerted before it is due |
| `*`      | caregiver                                  | check for potential conflicts before adding a new medication | reduce the risk of harmful drug interactions |
| `*`      | caregiver                                  | set a reminder for an appointment | remember to bring the patient to it |
| `*`      | caregiver                                  | mark a care instruction as completed for the day | keep track of my daily caregiving routine |
| `*`      | caregiver                                  | flag a task as urgent | make critical care items stand out from routine ones |
| `*`      | caregiver                                  | log a symptom observation for a patient | track changes in their health over time |
| `*`      | caregiver                                  | view a timeline of a patient's past symptom observations | identify patterns to raise with a doctor |
| `*`      | caregiver with many patients               | filter patients by a medical condition | see everyone affected by a particular condition at once |
| `*`      | caregiver with many patients               | sort patients by their next appointment or medication due | prioritise the patients who need attention soonest |
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

1.  Caregiver requests to list patients
2.  MediConnect shows a list of patients
3.  Caregiver requests to add care information (medications, allergens and/or care instructions) to a specific patient in the list
4.  MediConnect adds the care information to the patient's record and shows the entries that were added

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

*{More to be added}*

### Non-Functional Requirements

1. MediConnect must operate as a single-user application. Its data file must not be accessed by another user during regular operation.
2. MediConnect must work on Windows, Linux, and macOS computers with Java `25` installed.
3. MediConnect must be distributed as a single JAR file of no more than 100 MB and must not require an installer.
4. MediConnect must store its data locally in a human-editable text file, with at least the same level of manual-editing support as AddressBook Level 3.
5. MediConnect must not use a database management system.
6. MediConnect must not depend on a team-owned remote server for its essential features and must remain usable without an Internet connection.
7. MediConnect should complete common commands, including listing, finding, adding, editing, and deleting records, and update the displayed results within one second when managing up to 100 patient records on a typical modern computer.
8. A user with above-average typing speed for regular English text should be able to perform common record-management tasks faster using commands than using mouse interactions.
9. MediConnect's GUI should work without resolution-related inconvenience at resolutions of 1920x1080 or higher with 100% or 125% display scaling. All functions must remain accessible at resolutions of 1280x720 or higher with display scaling up to 150%.

### Glossary

* **Allergy**: A harmful reaction a patient has to a substance, such as a drug or a food. Each allergy is recorded by
  the name of the substance that causes it (the *allergen*), e.g., `Penicillin`.
* **Appointment**: A scheduled visit by a patient to a healthcare provider, recorded with its date, time, and provider.
  Notes on what was discussed can be added after the visit.
* **Archive**: To remove a patient's profile from the active patient list while keeping all of its records for future
  reference. Unlike deleting, archiving does not remove any data.
* **Care handover**: The transfer of responsibility for a patient's care from one person to another, e.g., from the
  main caregiver to a relative while the caregiver is travelling.
* **Care information**: The care-related records in a patient's profile, i.e., medical conditions, allergies,
  medications, care instructions, appointments, contacts, and medical notes. It does not include the patient's basic
  details.
* **Care instruction**: A routine care task that does not involve medication, e.g., "check blood sugar before
  breakfast".
* **Caregiver**: A person who uses MediConnect to coordinate a patient's care, typically a family member of the
  patient. A caregiver is not necessarily a healthcare professional.
* **CLI (Command Line Interface)**: A way of using an application by typing text commands rather than by clicking on
  elements with a mouse.
* **Community nurse**: A nurse who cares for patients through scheduled home visits.
* **Contact**: A person or organisation linked to a patient's profile, which is either a healthcare provider (e.g., a
  GP, specialist, or pharmacist) or an emergency contact.
* **Display scaling**: An operating system setting that enlarges text and other on-screen elements by a percentage
  (e.g., 125%) to make them easier to read.
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
* **Medical condition**: A long-term illness or diagnosis that a patient has, e.g., diabetes or hypertension.
* **Medical note**: A free-text note about a patient's health or treatment, e.g., advice given by a doctor during an
  appointment.
* **Medication**: A drug prescribed to a patient, recorded with its dosage (the amount per dose, e.g., `500mg`) and
  frequency (how often it is taken, e.g., `twice daily`).
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

### Entering a blank command

1. Test case: Leave the command box empty and press Enter.<br>
   Expected: Nothing happens.

1. Test case: Type three spaces in the command box and press Enter.<br>
   Expected: No command executes and no error appears. The spaces and previous result remain unchanged.

### Adding a patient with punctuation after a slash word

1. Test case: `add n/Ben s/o, Ng ic/S1111111B dob/1950-01-01 p/91111111 a/Blk 1`<br>
   Expected: No patient is added. The status message shows
   `Unrecognized word: 's/o,'. Write s/o as a separate word without punctuation.`

### Adding a patient with a blank address

1. Test case: `add n/Ben Ng ic/S1111111B dob/1950-01-01 p/91111111 a/`<br>
   Expected: No patient is added. The status message shows `Addresses cannot be blank.`

### Managing patient contacts

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

   1. Prerequisites: Clear all data using the `clear` command.<br>
      Expected: The status message shows `All patients have been cleared.`

   1. Test case: `list`<br>
      Expected: The patient list is empty. The status message shows `No active patients found.`

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

   1. Test case: `view 0`<br>
      Expected: The detail panel is unchanged. The status message shows the invalid command format error.

   1. Other incorrect view commands to try: `view`, `view x`, `view 1 2`, `view y` (where y is larger than the list size)<br>
      Expected: The detail panel is unchanged. The status message shows an error.

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

   1. Prerequisites: The app is closed. In `data/mediconnect.json`, change a patient's `nric` to `123`.

   1. Launch the app.<br>
      Expected: The app starts with no patients and shows a warning containing the recovery copy's full path.
      The recovery copy contains the original invalid file unchanged.

   1. Run `list`, then close and relaunch the app.<br>
      Expected: The active data file contains an empty patient list, and the recovery copy is still unchanged.

   1. Close the app. Repair a copy of the recovery file, replace `data/mediconnect.json` with it, and relaunch.<br>
      Expected: The repaired patients load normally without a recovery warning.
