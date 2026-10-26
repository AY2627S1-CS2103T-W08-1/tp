---
  layout: default.md
  title: "User Guide"
  pageNav: 3
---

# MediConnect User Guide

MediConnect is a **desktop application for managing patient records and care information, optimized for use through a Command Line Interface (CLI)** while retaining the benefits of a Graphical User Interface (GUI). If you type quickly, MediConnect can help you manage patient records faster than traditional GUI applications.

<!-- * Table of Contents -->
<page-nav-print />

--------------------------------------------------------------------------------------------------------------------

## Quick start

1. Ensure that Java `25` or later is installed on your computer.<br>
   **Mac users:** Ensure you have the precise JDK version prescribed [here](https://se-education.org/guides/tutorials/javaInstallationMac.html).

1. Download `mediconnect.jar` from the [Releases page](https://github.com/AY2627S1-CS2103T-W08-1/tp/releases).

1. Copy the file to an empty folder. This folder will be the _home folder_ for your MediConnect data.

1. Open a terminal, `cd` to that folder, and run `java -jar mediconnect.jar`.<br>
   A GUI similar to the one below should appear in a few seconds. On first launch, the patient list is filled with sample patients so you can try out the commands.<br>
   ![Ui](images/Ui.png)

1. Type a command in the command box and press Enter to execute it. For example, type **`help`** and press Enter to open the help window.<br>
   Some example commands you can try, in this order:

   * `list` : Lists all active patients.

   * `add n/John Tan ic/S1234567A dob/1990-05-14 p/91234567 a/Blk 123 Clementi Ave 3` : Adds a patient named `John Tan` to MediConnect.

   * `view 1` : Shows the full details of the 1st patient in the current list.

   * `find John` : Lists all active patients whose name contains `John`.

   * `delete 1` : Deletes the 1st patient shown in the current list. Right after `find John`, this deletes `John Tan`.

   * `exit` : Exits the app.

1. Refer to the [Features](#features) section below for details of each command.

--------------------------------------------------------------------------------------------------------------------

## Features

<box type="info" seamless>

**Notes about the command format:**<br>

* Pressing Enter when the command box is empty or contains only whitespace does nothing.
  The text in the command box and the previous result remain unchanged.

* Words in `UPPER_CASE` are the parameters to be supplied by the user.<br>
  For example, in `add n/NAME`, replace `NAME` with a value such as `John Doe`.

* Items in square brackets are optional.<br>

* Items followed by `...` can appear zero or more times.<br>

* Parameters can be in any order.<br>
  For example, if the command specifies `n/NAME p/PHONE_NUMBER`, `p/PHONE_NUMBER n/NAME` is also acceptable.

* Extraneous parameters for commands that take no parameters, such as `help`, `list`, `listarchived`, `exit`,
  `clear` and `backup`, are ignored.<br>
  For example, `help 123` is interpreted as `help`.

* If you are using a PDF version of this document, be careful when copying and pasting commands that span multiple lines as space characters surrounding line-breaks may be omitted when copied over to the application.
</box>

### Viewing help: `help`

Opens a help window that lists every command with its format and a one-line description, grouped as patients,
care information, contacts, appointments, data and other commands. The top of the window links to this User Guide;
**Copy URL** copies the link, to paste into a browser.

<img src="images/helpMessage.png" alt="help window" width="560" />

Format: `help`

* You can also open the help window with the `Help` menu or by pressing `F1`.
* Any text after `help` is ignored. For example, `help add` is interpreted as `help`.
* The help window stays in front of the main window, and you can keep typing commands in the main window while it
  is open. When you switch to another app, such as a browser, the help window goes behind that app's windows.
  Press `Esc` to close it.
* If the help window is already open, no second help window is opened.

The command returns `Opened help window.`


### Adding a patient: `add`

Adds a patient's basic details to MediConnect, with empty medication, allergen, and care instruction lists.

Format: `add n/NAME ic/NRIC dob/DOB p/PHONE a/ADDRESS`

* All five fields are required and can appear in any order.
* Each prefix must appear only once. Repeated prefixes are rejected, e.g.,
  `Multiple values specified for a single-valued field: n/`.
* Unrecognized letters followed by `/` at the start of a word, such as `x/123`, `med/Aspirin`, or `A/B`, are
  rejected. For example, `x/123` gives `Unrecognized parameter: x/. Please check the command format.`
* Standalone `s/o`, `d/o`, `c/o`, `a/l`, and `a/p` are allowed anywhere in the `add` arguments, ignoring letter case.
  Other field rules still apply. Keep them separate from other text or punctuation: `s/o,` is not allowed.
  For example, `s/o,` gives `Unrecognized word: 's/o,'. Write s/o as a separate word without punctuation.`
  Addresses can include numeric slash text such as `21/3` and URLs.
* `NRIC` must be 1 letter (S, T, F, G, or M), 7 digits, and 1 letter, e.g., `S1234567A`.
  It is not case-sensitive and is stored in uppercase.
* `DOB` must be a valid date in `YYYY-MM-DD` format, e.g., `1950-04-12`, from `1900-01-01` to today.
* Patients with the same NRIC, ignoring letter case, are rejected with
  `This patient (identified by NRIC) already exists in MediConnect.` Names alone do not identify duplicates.
  If the matching patient is archived, the message instead says
  `This NRIC belongs to an archived patient. Use listarchived and unarchive to restore them.`
* `NAME` must start with a letter and can only contain letters, spaces, `'`, `-`, `/` and `.`,
  e.g., `Tan Ah Kow s/o Lim`, `Muthu a/l Ramasamy`, or `Mary-Jane O'Brien`.
  Each `'`, `-` or `/` must be between two letters, and each `.` must come right after a letter.
  A curly apostrophe (`’`) is treated as `'`. Multiple spaces between words are reduced to one.
* `PHONE` must be 8 digits starting with 3, 6, 8 or 9, with an optional `+65` in front,
  e.g., `91234567` or `+6591234567`.
  It is stored without the `+65`.

Examples:

* `add n/John Tan ic/S1234567A dob/1950-04-12 p/91234567 a/21 Lorong 3, #05-10`
* `add n/Mary Lim ic/S7654321B dob/1945-11-02 p/98765432 a/Blk 12 Ang Mo Kio Ave 4`

The first example returns:
`New patient added: John Tan; NRIC: S1234567A; DOB: 1950-04-12; Phone: 91234567; Address: 21 Lorong 3, #05-10;`

If a required field is missing, the command is rejected with `Invalid command format!` followed by the usage and example.

### Field formats

Field   | Prefix | Format | Example
--------|--------|--------|--------
Name    | `n/`   | Starts with a letter. Only letters, spaces, `'`, `-`, `/` and `.`; `'`, `-` and `/` must be between letters, and `.` must come right after a letter. Multiple spaces between words are reduced to one. | `Mary-Jane O'Brien`
NRIC    | `ic/`  | 1 letter (S, T, F, G or M), 7 digits, 1 letter. Not case-sensitive; stored in uppercase. | `S1234567A`
DOB     | `dob/` | A valid date in `YYYY-MM-DD` format, from `1900-01-01` to today. | `1950-04-12`
Phone   | `p/`   | 8 digits with no spaces, starting with 3, 6, 8 or 9, with an optional `+65` in front. Stored without the `+65`. | `91234567`, `+6591234567`
Address | `a/`   | Cannot be blank. | `Blk 12 Ang Mo Kio Ave 4, #05-10`

Leading and trailing spaces in every field are ignored. A word that looks like an unknown prefix, such as `x/12`, is not allowed in any field (see the `add` command).

### Adding care information: `addcare`

Appends medications, allergens, or care instructions to the patient at the displayed index.
Existing care information is never overwritten.

Format: `addcare INDEX [med/MEDICATION]... [al/ALLERGEN]... [ci/CARE_INSTRUCTION]...`

* `INDEX` follows the same rules as `delete` and refers to the currently displayed patient list.
* At least one `med/`, `al/`, or `ci/` prefix is required. Each prefix can be repeated.
* Care entries are free text with leading and trailing whitespace removed and consecutive internal whitespace
  collapsed to one space. They cannot be blank. If any entry is blank, the whole command is rejected.
* A word that looks like an unrecognized prefix is rejected. For example, `p/91234567` returns
  `Unrecognized parameter: p/. Please check the command format.` Numeric slash text and URLs are allowed.
* Duplicate entries, ignoring letter case and whitespace differences, are not added again. Other unique entries in
  the same command are still added.
* A medication added with `med/` has no daily times yet. Use [`editmed`](#editing-a-medication-editmed) to set them.
* After a successful command, the detail panel shows the updated patient so the new entry numbers can be checked.

Examples:

* `addcare 1 med/Metformin 500mg twice daily`
* `addcare 2 al/Penicillin al/Shellfish`
* `addcare 1 ci/Check blood sugar before breakfast`

A successful command returns `Updated care info for NAME. Added: ITEMS.` Each item is labeled by category while
keeping the category and input order, e.g., `Medication: Aspirin, Allergen: Aspirin, Care instruction: Aspirin`.

Each skipped duplicate is named in the feedback. For example:

* An entry already stored for the patient returns
  `Medication "Metformin" was not added: it already exists for this patient.`
* An entry repeated within the same command returns
  `Medication "Aspirin" was not added: it was repeated in this command.`

The same wording is used for allergens and care instructions, with `Allergen` or `Care instruction` in place of
`Medication`. Unique entries in the command are still added. If every supplied entry is a duplicate, the command is
rejected without changing the patient.

Other errors are reported as follows:

* An index outside the displayed list returns `The patient index provided is invalid.`
* A command with no care prefix returns `Please specify at least one of med/, al/, or ci/ to add.`
* A blank value returns `Medication entries cannot be blank.`, `Allergen entries cannot be blank.`, or
  `Care instructions cannot be blank.` for the affected category. The whole command is rejected.
* A missing, zero, negative, non-numeric, or excessively large index returns `Invalid command format!`, followed by
  the command usage.

### Editing a medication: `editmed`

Changes the description and/or the daily times of one medication of a patient.

Format: `editmed INDEX MED_INDEX [med/DESCRIPTION] [t/TIME]...`

* `INDEX` is the number of the patient in the currently displayed patient list, as for `delete`. It must be a
  positive whole number (1, 2, 3, ...).
* `MED_INDEX` is the number of the medication in the numbered medication list that `view` shows for the patient
  (`1`, `2`, ...). It must be a positive whole number.
* `INDEX` and `MED_INDEX` must come first, in that order, before `med/` and `t/`. `med/` and `t/` can be in any order.
* At least one of `med/` or `t/` must be given.
* `med/DESCRIPTION` replaces the description. Leading and trailing whitespace is removed, and consecutive internal
  whitespace is collapsed to one space. It cannot be blank and can be given only once. A description that is the
  same as another medication of the same patient, ignoring letter case and whitespace differences, is rejected.
  Changing only the letter case of the medication's own description is allowed.
* `t/TIME` sets the daily times. `TIME` must be in `HH:MM` (24-hour), with two digits on each side of the colon,
  e.g., `08:30`. Leading and trailing spaces are ignored.
  * Giving `t/` **replaces all** the existing times of the medication, so the new times are the only ones left.
    Repeat `t/` for more than one time, e.g., `t/08:00 t/20:00`.
  * Times are shown in ascending order, and a time given more than once is kept once.
  * `t/` with nothing after it **clears** all the times. It cannot be combined with another `t/`: for example,
    `t/08:00 t/` is rejected.
* Without `t/`, the existing times stay as they are. Without `med/`, the description stays as it is.
* The patient's other details and other medications are not changed. If the patient is shown in the details panel,
  the panel updates.
* A word that looks like an unrecognized prefix is rejected, the same as in `addcare`. For example,
  `editmed 1 1 med/Aspirin T/08:00` returns `Unrecognized parameter: T/. Please check the command format.`
  Only `med/` and `t/`, in lowercase, are recognized. Numeric slash text and URLs are allowed.

Examples (each one run on the sample data, where patient 1 is Alex Yeoh with `1. Aspirin 100mg (08:00)` and
`2. Amlodipine 5mg (08:00, 20:00)`):

* `editmed 1 2 t/07:30 t/19:30` replaces both times of Amlodipine 5mg.<br>
  Returns `Updated medication 2 of Alex Yeoh: Amlodipine 5mg (07:30, 19:30)`
* `editmed 1 1 med/Aspirin 75mg` changes only the description and keeps the time.<br>
  Returns `Updated medication 1 of Alex Yeoh: Aspirin 75mg (08:00)`
* `editmed 1 1 med/Aspirin 75mg t/09:00` changes the description and replaces the time.<br>
  Returns `Updated medication 1 of Alex Yeoh: Aspirin 75mg (09:00)`
* `editmed 1 2 t/` removes all the times of Amlodipine 5mg.<br>
  Returns `Updated medication 2 of Alex Yeoh: Amlodipine 5mg`
* `editmed 1 1 t/20:00 t/08:00 t/20:00` sorts the times and keeps `20:00` once.<br>
  Returns `Updated medication 1 of Alex Yeoh: Aspirin 100mg (08:00, 20:00)`

A successful command returns `Updated medication MED_INDEX of PATIENT_NAME: MEDICATION`, where `MEDICATION` is the
description followed by the times in brackets. The brackets are left out when there are no times.
Running the same command again succeeds and changes nothing more.

A command that fails changes nothing. Errors are reported as follows:

* An `INDEX` outside the displayed list returns `The patient index provided is invalid.`
* A `MED_INDEX` larger than the number of medications of the patient, or any `MED_INDEX` for a patient with no
  medications, returns `The medication index provided is invalid.`
* A missing, zero, negative, non-numeric, or excessively large `INDEX` or `MED_INDEX`, or a number of indices other
  than two, returns `Invalid command format!`, followed by the command usage.
* A command with neither `med/` nor `t/` returns `At least one of med/ or t/ must be provided.`
* A blank `med/` returns `Medication description cannot be blank.`
* A time that is not valid, such as `24:00`, `08:60`, `8:30`, or `noon`, returns
  `Time must be in HH:MM (24-hour), e.g. 08:30.`
* A `t/` with nothing after it next to another `t/` (e.g., `t/08:00 t/`) returns
  `t/ with no time clears all times and cannot be combined with other times.`
* A description that already belongs to another medication of the patient returns
  `Medication "amlodipine 5mg" was not updated: it already exists for this patient.`
* More than one `med/` returns `Multiple values specified for a single-valued field: med/`
* A word that looks like an unrecognized prefix returns
  `Unrecognized parameter: PREFIX. Please check the command format.`, with the prefix that was found. For example,
  `editmed 1 1 x/foo` returns `Unrecognized parameter: x/. Please check the command format.` and
  `editmed 1 1 al/Latex` returns `Unrecognized parameter: al/. Please check the command format.`

### Editing a care instruction: `editcare`

Replaces one care instruction while leaving its position and all other patient data unchanged.

Format: `editcare INDEX CI_INDEX ci/NEW_TEXT`

* `INDEX` is the patient's index in the displayed list. `CI_INDEX` is the care instruction's number in the detail
  panel. Both must be positive integers.
* `NEW_TEXT` is free text with leading and trailing whitespace removed and consecutive internal whitespace collapsed
  to one space. It cannot be blank.
* An edit that would duplicate another care instruction, ignoring letter case and whitespace differences, is rejected.
  Editing an instruction to its current text is allowed.
* `ci/` must appear exactly once. Unrecognized prefixes are rejected.

Example: `editcare 1 1 ci/Check blood sugar after lunch`

For patient `Alex Yeoh`, this returns:
`Updated care instruction for Alex Yeoh: Check blood sugar after lunch`

### Deleting a care instruction: `deletecare`

Removes one numbered care instruction and reports the removed text.

Format: `deletecare INDEX CI_INDEX`

Example: `deletecare 1 1`

For patient `Alex Yeoh` in the sample data, deleting `Monitor blood pressure every morning` returns:
`Deleted care instruction for Alex Yeoh: Monitor blood pressure every morning`

After either command, the detail panel shows the updated patient immediately and the remaining instructions are
numbered from `1` again. `The patient index provided is invalid.` means `INDEX` is outside the displayed list.
`The care instruction index provided is invalid.` means `CI_INDEX` is past the end of that patient's instruction
list, including when the list is empty. Missing, zero, negative, nonnumeric or extra indices give the command's usage.
Blank replacement text returns `Care instructions cannot be blank.` A duplicate edit returns
`This care instruction already exists for this patient.` Validation failures leave patient data unchanged.

### Deleting an allergen: `deleteallergy`

Removes one allergen from the patient at `INDEX` in the displayed patient list.

Format: `deleteallergy INDEX ALLERGY_INDEX`

* `ALLERGY_INDEX` is the allergen number shown in that patient's detail panel. Both indices must be positive
  integers.
* The removed allergen is reported in the success message. Remaining allergens are renumbered immediately in the
  detail panel, which shows the updated patient. All other patient details are unchanged.

Example: `deleteallergy 1 1`

For patient `Alex Yeoh` in the sample data, allergen 1 is `Penicillin`, so this returns
`Deleted allergen for Alex Yeoh: Penicillin`.

An `INDEX` outside the displayed patient list returns `The patient index provided is invalid.` An
`ALLERGY_INDEX` past the end of the patient's allergens, including when none are recorded, returns
`The allergen index provided is invalid.` Missing, zero, negative, nonnumeric or extra indices return
`Invalid command format!` followed by the command usage. Validation failures leave patient data unchanged.

### Adding a contact: `addcontact`

Adds one contact to the patient at `INDEX` in the displayed patient list.

Format: `addcontact INDEX n/NAME r/ROLE p/PHONE [nt/NOTE]`

* Name, role and phone are required. Each prefix can appear only once; fields can appear in any order.
* Names are nonblank free text, so both people and organisations such as `Tan & Partners Clinic` are supported.
  Leading/trailing whitespace is removed and repeated spaces in a name become one.
* Role is nonblank free text. Examples include `GP`, `specialist`, `pharmacist`, `emergency` and `guardian`;
  other roles are allowed.
* Contact phones can contain digits, spaces, `+` and `-`, with at least three digits. They use different rules
  from patient phones so overseas and clinic numbers are supported. Formatting is preserved after trimming.
* The note is optional. An omitted or empty `nt/` creates a contact without a note.
  Surrounding spaces are removed from notes entered through commands.
* A contact with the same name (ignoring case and repeated spaces) and phone (ignoring spaces and hyphens)
  already on this patient is rejected. Role and note do not distinguish duplicates. `+` remains significant;
  country codes are not inferred. The same contact can belong to different patients.
* Unknown prefixes and repeated prefixes are rejected. Words starting with unrecognised letters followed by `/`,
  including standalone `s/o`, `d/o` and `c/o`, are rejected.
  Standalone `a/l` and `a/p` are allowed in free text, ignoring letter case.
  Text matching a recognised prefix after a space starts another field, even inside a note.
* Contacts appear in insertion order, numbered in the `view` panel. If that patient is already viewed,
  the panel updates immediately. Existing care information is preserved.

Example: `addcontact 1 n/Lim & Partners Clinic r/GP p/+65 6123-4567 nt/Call in the morning`

For patient `Alex Yeoh`, this returns:
`Added contact for Alex Yeoh: Lim & Partners Clinic (GP); Phone: +65 6123-4567; Note: Call in the morning`

Blank names/roles return `Contact name cannot be blank.` or `Contact role cannot be blank.`
An invalid phone returns `Contact phone must contain at least 3 digits and only digits, spaces, + and -.`
A duplicate returns `This contact (identified by name and phone) already exists.` No contact is added on failure.

### Editing a contact: `editcontact`

Format: `editcontact INDEX CONTACT_INDEX [n/NAME] [r/ROLE] [p/PHONE] [nt/NOTE]`

* `INDEX` is the patient's index in the displayed list; `CONTACT_INDEX` is its contact number in the detail panel.
  Both indices must be positive integers.
* Supply at least one field. Omitted fields keep their values; `nt/` with no value clears the note.
* The same field and duplicate rules as `addcontact` apply. An edit that creates a duplicate is rejected.
  Editing a contact to its existing values is allowed. Its list position stays the same.
* With no fields, the error is `Specify at least one contact field to edit.`

Example: `editcontact 1 1 nt/Call after lunch`

Success begins with `Updated contact for NAME:` followed by the updated contact's name, role, phone and any note.

### Deleting a contact: `deletecontact`

Format: `deletecontact INDEX CONTACT_INDEX`

Removes that contact and reports `Deleted contact for NAME:` followed by the removed contact's details.
Remaining contacts are renumbered. Other patient details are unchanged.

Example: `deletecontact 1 1` removes `Tan & Partners Clinic` from `Alex Yeoh` in the sample data and returns
`Deleted contact for Alex Yeoh: Tan & Partners Clinic (GP); Phone: +65 6123-4567; Note: Call in the morning`.

For all three contact commands, `INDEX` must refer to a patient in the displayed list; otherwise the error is
`The patient index provided is invalid.` A contact number past the end, including when there are no contacts,
returns `The contact index provided is invalid.` Missing, zero, negative, nonnumeric or extra indices give the
command's usage. Validation failures leave patient data unchanged.

### Adding an appointment: `addappt`

Adds an appointment to the patient at the displayed index.

Format: `addappt INDEX d/DATE t/TIME w/WITH [nt/NOTE]`

* `INDEX` follows the same rules as `delete` and refers to the currently displayed patient list.
* `DATE` must be a real date in `YYYY-MM-DD` format, e.g., `2026-11-05`.
* `TIME` must be in `HH:MM` 24-hour format, e.g., `09:30` or `14:00`.
* `WITH` is who the appointment is with, e.g., `Dr Tan` or `Cardiology clinic`. It cannot be blank. Leading and
  trailing spaces are ignored, and multiple spaces between words are reduced to one.
* `NOTE` is optional free text. `nt/` with nothing after it means no note.
* Text that looks like a prefix, such as `w/` or `c/o`, cannot be used inside `WITH` or `NOTE`. Standalone `a/l` and
  `a/p` are allowed, ignoring letter case.
* The patient's appointments are numbered in the detail panel of `view`, upcoming ones first, then past ones,
  which are marked `(past)`.
  After `addappt`, the detail panel shows the patient, so the new appointment and its number can be checked.

Examples:

* `addappt 3 d/2027-11-05 t/09:30 w/Dr Tan`
* `addappt 2 d/2027-12-10 t/14:00 w/Cardiology clinic nt/Bring BP log`

A successful command returns `New appointment for NAME: DATE TIME with WITH`, followed by ` (NOTE)` if there is a
note. For example, the second example returns
`New appointment for Bernice Yu: 2027-12-10 14:00 with Cardiology clinic (Bring BP log)`.

* An appointment in the past is still added, so past visits can be recorded. The message then ends with
  `Note: this appointment is in the past.`
* An appointment at the same date and time as another appointment of the patient, but with someone else, is still
  added. The message then ends with `Warning: NAME already has an appointment at this date and time (with WITH).`

Other errors are reported as follows:

* An appointment with the same date, time and `WITH` as one the patient already has, ignoring letter case and extra
  spaces, returns `This appointment already exists for this patient.` and is not added.
* An index outside the displayed list returns `The patient index provided is invalid.`
* An invalid date returns `Appointment date should be a real date in YYYY-MM-DD format, e.g. 2026-10-25.`
* An invalid time returns `Time must be in HH:MM (24-hour), e.g. 08:30.`
* A blank `WITH` returns `Who the appointment is with should not be blank.`
* A missing `d/`, `t/` or `w/`, or a missing or invalid index, returns `Invalid command format!`, followed by the
  command usage.

### Editing an appointment: `editappt`

Changes an appointment of the patient at the displayed index.

Format: `editappt INDEX APPT_INDEX [d/DATE] [t/TIME] [w/WITH] [nt/NOTE]`

* `INDEX` and `APPT_INDEX` follow the same rules as `cancelappt`.
* At least one of `d/`, `t/`, `w/` or `nt/` must be given. Only the fields given are changed.
* `DATE`, `TIME`, `WITH` and `NOTE` follow the same rules as `addappt`. `nt/` with nothing after it clears the note.
* The appointments are sorted again after the edit, so an appointment's number can change. The detail panel then
  shows the patient.

Examples:

* `view 1` followed by `editappt 1 2 t/10:00 nt/` moves the 2nd appointment of the 1st patient to 10:00 and clears
  its note.
* `editappt 1 1 d/2027-11-12 w/Dr Lim` changes the date and who the 1st appointment of the 1st patient is with.

A successful command returns `Edited appointment APPT_INDEX of NAME: APPOINTMENT`, with the updated appointment shown
as in the detail panel. `APPT_INDEX` is the appointment's number after the edit, which can differ from the one typed.
For example, the first example returns `Edited appointment 2 of Alex Yeoh: 2027-12-10 10:00 with Cardiology clinic`.

* As with `addappt`, an appointment moved into the past or to the same date and time as another appointment of the
  patient is still saved, and the message ends with the same note or warning.

Other errors are reported as follows:

* An edit that makes the appointment the same as another appointment of the patient returns
  `This appointment already exists for this patient.` and changes nothing.
* No `d/`, `t/`, `w/` or `nt/` returns `At least one of d/, t/, w/ or nt/ must be provided.`
* Index errors and invalid fields are reported as for `cancelappt` and `addappt`.

### Cancelling an appointment: `cancelappt`

Removes an appointment from the patient at the displayed index.

Format: `cancelappt INDEX APPT_INDEX`

* `INDEX` follows the same rules as `delete` and refers to the currently displayed patient list.
* `APPT_INDEX` is the appointment's number in the Appointments section of the patient's detail panel, where
  upcoming appointments come first, then past ones. The numbers stay as shown, even after an appointment passes,
  until the patient is shown again, e.g., with `view`.
* Both indexes **must be positive integers** 1, 2, 3, ...
* The appointment is removed, not kept as cancelled. The detail panel then shows the patient.
* Use `cancelappt` also to remove a past appointment that was entered by mistake.

Examples:

* `view 1` followed by `cancelappt 1 2` cancels the 2nd appointment of the 1st patient.

A successful command returns `Cancelled appointment APPT_INDEX of NAME: APPOINTMENT`, with the appointment shown as
in the detail panel. For example, the example above returns
`Cancelled appointment 2 of Alex Yeoh: 2027-12-10 14:00 with Cardiology clinic (Bring BP log)`.

Errors are reported as follows:

* An index outside the displayed list returns `The patient index provided is invalid.`
* An `APPT_INDEX` larger than the number of the patient's appointments returns
  `The appointment index provided is invalid.` So does any `APPT_INDEX` for a patient with no appointments.
* A missing, zero, negative, non-numeric or excessively large index (e.g., `99999999999`), or a number of values
  other than two (e.g., `cancelappt 1` or `cancelappt 1 2 x`), returns `Invalid command format!`, followed by the
  command usage.

### Listing appointments: `listappt`

Lists the upcoming appointments of all patients, or every appointment of one patient.

Format: `listappt [INDEX]`

* Without `INDEX`, lists the appointments of all active patients at or after the current date and time, soonest
  first. An appointment earlier today is past and is not listed. Archived patients are skipped, and every active
  patient is included, even after `find`.
* With `INDEX`, lists every appointment of the patient at `INDEX`, upcoming ones first, then past ones. Each
  appointment shows its `APPT_INDEX`, the number to use with `editappt` and `cancelappt`. The detail panel then
  shows the patient, with the same numbers.
* `INDEX` follows the same rules as `delete` and refers to the currently displayed patient list. An archived patient
  is not listed: run `unarchive` first to see their appointments.
* The patient list does not change.

Examples:

* `listappt` returns `Upcoming appointments:`, followed by one line per appointment with the patient's name, e.g.,
  `Alex Yeoh: 2027-11-05 09:30 with Dr Tan`.
* `listappt 2` returns `Appointments for Bernice Yu:`, then `Upcoming: none`, then `Past:` followed by
  `1. 2026-10-02 10:00 with Polyclinic nurse (Blood pressure normal)`.

Other messages are as follows:

* With no upcoming appointments, `listappt` returns `No upcoming appointments.`
* For a patient with no appointments, `listappt INDEX` returns `NAME has no appointments.`
* For an archived patient, e.g., after `listarchived`, `listappt INDEX` returns
  `This patient is archived. Unarchive them to list their appointments.`
* An index outside the displayed list returns `The patient index provided is invalid.`
* A zero, negative, or non-numeric index, or more than one index, returns `Invalid command format!`, followed by
  the command usage.

### Listing all active patients: `list`

Shows a list of all active patients in MediConnect. Archived patients are not shown.

Format: `list`

Any text after `list` is ignored. The command returns `Listed all active patients.` or, if there are none,
`No active patients found.`

### Archiving a patient: `archive`

Archives a patient without deleting their record.

Format: `archive INDEX`

* `INDEX` refers to the patient in the currently displayed list, including a filtered `find` result.
* The archived patient disappears from active patient lists. Their complete record is preserved.
* If that patient is shown in the detail panel, the panel is cleared after the patient is archived.

Example: `find Alex` followed by `archive 1` archives the first patient in those results.

A successful command returns `Archived patient: NAME; NRIC: NRIC`. An index outside the displayed list returns
`The patient index provided is invalid.` Attempting to archive an archived patient returns
`This patient is already archived.` Missing, zero, negative, nonnumeric or extra indices return
`Invalid command format!` followed by the command usage.

### Listing archived patients: `listarchived`

Shows only archived patients. The displayed indices are the indices used by `unarchive` and other commands that
operate on the currently displayed list. Active patients are not shown.

Archived patients can still be updated with patient-editing commands such as `edit`, `addcare`, `editcare`,
`deletecare`, and `deleteallergy`. These commands update the archived record without making it active again.
Use `unarchive` when the patient should return to the active list.

Format: `listarchived`

Any text after `listarchived` is ignored. The command returns `Listed all archived patients.` or, if there are none,
`No archived patients found.` Run `list` to return to the active patient list.

### Restoring an archived patient: `unarchive`

Returns an archived patient to the active patient list.

Format: `unarchive INDEX`

* `INDEX` refers to the patient in the currently displayed list. Normally, run `listarchived` first so that the
  index identifies an archived patient.
* The restored patient disappears from the archived list and becomes visible again after `list` or a matching
  `find` command.
* The patient's complete record is preserved.

Example with fresh sample data: `archive 1`, followed by `listarchived`, then `unarchive 1` archives and restores
`Alex Yeoh`.

A successful command returns `Unarchived patient: NAME; NRIC: NRIC`. An index outside the displayed list returns
`The patient index provided is invalid.` Attempting to unarchive an active patient returns
`This patient is already active.` Missing, zero, negative, nonnumeric or extra indices return
`Invalid command format!` followed by the command usage.

### Editing a patient: `edit`

Edits one or more basic details of a patient without changing their care information.

Format: `edit INDEX [n/NAME] [ic/NRIC] [dob/DOB] [p/PHONE] [a/ADDRESS]`

* `INDEX` refers to the patient in the currently displayed list, including a filtered `find` result.
* Supply at least one field. Omitted fields keep their existing values.
* Each supplied value follows the same validation and normalization rules as `add`.
* Medications and their times, allergens, care instructions, contacts, appointments, and archived state are
  preserved.
* An edit that gives a patient an NRIC already used by another patient is rejected.
* After a successful command, the detail panel shows the edited patient immediately.

Examples:
* `edit 1 p/91234567` changes only the first displayed patient's phone number.
* `edit 2 n/John Tan a/21 Lorong 3` changes the second displayed patient's name and address.

A successful command returns
`Edited patient: NAME; NRIC: NRIC; DOB: DOB; Phone: PHONE; Address: ADDRESS`. An index outside the displayed list returns
`The patient index provided is invalid.` Supplying no fields returns
`At least one field to edit must be provided.` A duplicate NRIC returns
`This patient (identified by NRIC) already exists in MediConnect.` If the matching patient is archived, it returns
`This NRIC belongs to an archived patient. Use listarchived and unarchive to restore them.`

### Locating patients by name: `find`

Finds active patients whose names contain any of the given keywords. Archived patients are not included.

Format: `find KEYWORD [MORE_KEYWORDS]...`

* The search is case-insensitive; for example, `alex` matches `Alex`.
* Keyword order does not matter; for example, `Alex David` matches `David Alex`.
* The search considers only names.
* Only full words match; for example, `Ale` does not match `Alex`.
* Patients matching at least one keyword are returned (an `OR` search); for example, `Alex David` returns `Alex Yeoh` and `David Li`.

Examples:
* `find Bernice` returns `Bernice Yu`
* `find alex david` returns `Alex Yeoh`, `David Li`<br>
  ![result for 'find alex david'](images/findAlexDavidResult.png)

### Deleting a patient: `delete`

Deletes the specified patient from MediConnect.

Format: `delete INDEX`

* Deletes the patient at the specified `INDEX`.
* The index refers to the index number shown in the displayed patient list.
* The index **must be a positive integer** 1, 2, 3, ...

Examples:
* `list` followed by `delete 2` deletes the 2nd patient in MediConnect.
* `find Alex` followed by `delete 1` deletes the 1st patient in the results of the `find` command.

### Deleting a medication: `deletemed`

Deletes one medication of a patient.

Format: `deletemed INDEX MED_INDEX`

* `INDEX` is the number of the patient in the currently displayed patient list, as for `delete`. It must be a
  positive whole number (1, 2, 3, ...).
* `MED_INDEX` is the number of the medication in the numbered medication list that `view` shows for the patient
  (`1`, `2`, ...). It must be a positive whole number.
* `INDEX` and `MED_INDEX` must both be given, in that order, and nothing else may follow them.
* The other medications, allergens, care instructions, and details of the patient are not changed. The
  medications after the deleted one move up by one number, e.g., deleting medication 1 makes the old medication 2
  the new medication 1. If the patient is shown in the details panel, the panel updates and renumbers its list.
  When the last medication is deleted, the panel shows `None recorded`.

Examples (each one run on the sample data, where patient 1 is Alex Yeoh with `1. Aspirin 100mg (08:00)` and
`2. Amlodipine 5mg (08:00, 20:00)`):

* `deletemed 1 2` deletes Amlodipine 5mg.<br>
  Returns `Deleted medication 2 of Alex Yeoh: Amlodipine 5mg (08:00, 20:00)`
* `deletemed 1 1` deletes Aspirin 100mg, so Amlodipine 5mg becomes medication 1.<br>
  Returns `Deleted medication 1 of Alex Yeoh: Aspirin 100mg (08:00)`

A successful command returns `Deleted medication MED_INDEX of PATIENT_NAME: MEDICATION`, where `MEDICATION` is the
description of the deleted medication followed by its times in brackets. The brackets are left out when it has no
times.

Running the same command again deletes the medication that has since moved into that number, not the same one
again. For example, `deletemed 1 1` run twice on Alex Yeoh deletes both medications.

A command that fails changes nothing. Errors are reported as follows:

* An `INDEX` outside the displayed list returns `The patient index provided is invalid.`
* A `MED_INDEX` larger than the number of medications of the patient, or any `MED_INDEX` for a patient with no
  medications, returns `The medication index provided is invalid.`
* A missing, zero, negative, non-numeric, or excessively large `INDEX` or `MED_INDEX`, or a number of values other
  than two (including extra words such as `deletemed 1 1 med/Aspirin`), returns `Invalid command format!`, followed
  by the command usage.

### Viewing a patient's full details: `view`

Shows all the details of the specified patient in the panel to the right of the patient list.

Format: `view INDEX`

* Shows the patient at the specified `INDEX`, which refers to the index number shown in the displayed patient list.
* The index **must be a positive integer** 1, 2, 3, ...
* The panel shows the patient's name, NRIC, date of birth, phone number, and address, followed by their
  medications, allergens, care instructions, contacts and appointments. Each list is numbered (`1.`, `2.`, ...), one
  entry per line. A medication with daily times shows them after its description, e.g., `Metformin 500mg (08:00, 20:00)`.
  Upcoming appointments are listed first, then past ones, each sorted by date, then time. Each past appointment is
  marked `(past)`, e.g., on the sample data, `view 2` shows
  `1. 2026-10-02 10:00 with Polyclinic nurse (Blood pressure normal) (past)`. Like the numbers, the marks stay as
  shown until the patient is shown again.
  An empty list shows `None recorded`.
* Before any patient is viewed, the panel shows `Enter 'view INDEX' to see a patient's full details here.`
* The panel keeps showing the patient after other commands, such as `list` or `find`, even if `find` hides the
  patient from the list. It updates when the patient's care information changes, e.g., after `addcare`, and goes back
  to the message above when the patient is deleted or all patients are cleared.
* Under the name, the panel shows the patient's index in the displayed patient list, e.g., `List index: 1`. This is the
  `INDEX` that commands such as `addcare` use for the patient. It updates when the list changes, e.g., after `find`. If
  the list does not show the patient, e.g., after `find` hides them or they are archived, it shows
  `Not in the displayed list`. To show the patient again before changing them, run `list` or `find`, or
  `listarchived` for an archived patient.
* When a different patient is viewed, the panel scrolls back to the top, so their name is shown first. When the same
  patient is shown again, e.g., after `addcare`, the panel stays where it was scrolled to.

Examples:

* `list` followed by `view 2` shows the details of the 2nd patient in the list.
* `find Bernice` followed by `view 1` shows the details of the 1st patient in the results of the `find` command.

A successful command returns `Showing patient: NAME`, e.g., `Showing patient: Bernice Yu`.

If `INDEX` is larger than the number of patients in the displayed list, the command is rejected with
`The patient index provided is invalid.` If `INDEX` is missing, is not a positive integer (e.g., `0`, `-1`, or `abc`),
is too large to be a valid number (e.g., `99999999999`), or is followed by extra words, the command is rejected with
`Invalid command format!` followed by the usage and example. In both cases, the panel keeps showing the patient it
showed before.

### Listing today's medication times: `due`

Lists the times at which medications are taken today, one line per dose, in the order they are due.

Format: `due [INDEX]`

* Without `INDEX`, `due` lists the doses of **all active** patients in MediConnect, even if `find` is hiding some of
  them from the patient list. Archived patients are not included.
* With `INDEX`, `due` lists only the doses of the patient at that index in the displayed patient list. `INDEX` is
  the number of the patient in the currently displayed patient list, as for `delete`. It must be a positive whole
  number (1, 2, 3, ...).
* Each line has the form `HH:MM PATIENT_NAME: DESCRIPTION`, e.g., `08:00 Alex Yeoh: Aspirin 100mg`. A medication
  with several times has one line for each time.
* Lines are sorted by time, earliest first, and then by patient name, ignoring letter case. Doses of the same
  patient at the same time are listed in the order of the patient's medications.
* A time that is earlier than the current time of your computer (in its time zone) is followed by
  ` (earlier today)`. A time in the current minute is not marked. Every time is a daily time, so the same times
  are listed each day.
* Medications without times are not listed.
* The result is shown in the result box. The patient list and the details panel are not changed, and no data is
  changed.

Examples (each one run on the sample data at 10:30, where patient 1 is Alex Yeoh with `1. Aspirin 100mg (08:00)` and
`2. Amlodipine 5mg (08:00, 20:00)`, and the other patients have no times):

* `due` returns<br>
  `08:00 Alex Yeoh: Aspirin 100mg (earlier today)`<br>
  `08:00 Alex Yeoh: Amlodipine 5mg (earlier today)`<br>
  `20:00 Alex Yeoh: Amlodipine 5mg`
* `due 1` returns the same three lines, because only Alex Yeoh has times.
* `due 2` returns `No medications are due today for Bernice Yu.`

Run before 08:00, the same `due` returns the three lines without ` (earlier today)`.

If there is nothing to list, `due` returns `No medications are due today.` and `due INDEX` returns
`No medications are due today for PATIENT_NAME.`

Errors are reported as follows:

* An `INDEX` outside the displayed list returns `The patient index provided is invalid.`
* A zero, negative, non-numeric, or excessively large `INDEX`, or anything after `due` other than a single
  number (e.g., `due 1 2`), returns `Invalid command format!`, followed by the command usage.

### Clearing all patients: `clear`

Clears all patients and their care information from MediConnect, and clears any patient details in the view panel.

Format: `clear`

Returns `All patients have been cleared.`, including when there are no patients to clear.

### Backing up data: `backup`

Saves a copy of all patient data to the `data/backups` folder, inside the folder you launch MediConnect from. MediConnect creates the folder if it does not exist yet.

Format: `backup`

* The backup is named after the date and time it was made, as `mediconnect-YYYYMMDD-HHMMSS.json` (e.g., `mediconnect-20261020-083015.json` for 20 Oct 2026, 8:30:15 am).
* If you make more than one backup within the same second, MediConnect adds `-1`, `-2` and so on to the name, so no backup is overwritten.
* Any text after `backup` is ignored. For example, `backup now` is interpreted as `backup`.
* On macOS and Linux, only your user account can open a backup, since it holds every patient's medical details.
* A backup is first written to a temporary file, and only gets its name when it is complete, so a backup that fails partway, e.g., because the disk is full, does not leave a damaged backup behind.

Returns `Backup saved to: ` followed by the full path of the backup file.

If the backup cannot be saved (e.g., the folder is read-only), MediConnect shows `Could not save the backup to ` followed by the folder and the reason, and your data is not changed.

<box type="warning" seamless>

**Caution:**
`backup` saves the patient data currently loaded in MediConnect. If MediConnect started with no patients because `data/mediconnect.json` could not be loaded, a backup made then has no patients. Your original data is in the recovery copy named in the startup warning. To recover it, see [Editing the data file](#editing-the-data-file).
</box>

<box type="tip" seamless>

**Tip:** Run `backup` before editing the data file by hand or before making many changes at once. To go back to a backup later, use `restore`.
</box>

### Restoring data from a backup: `restore`

Replaces all patient data with the data in a backup file from the `data/backups` folder.

Format: `restore [FILENAME]`

* `restore` without `FILENAME` lists the backups in `data/backups`, newest first, so you can copy the name you need. At most 10 are listed, followed by how many older ones there are. Your data is not changed.
* The list shows every `.json` file in `data/backups`, including ones that were not made by `backup`. The data in a file is checked only when you restore it, so a listed file can still be rejected, e.g., if it is damaged.
* `FILENAME` is the name of a file in `data/backups`, such as one made by `backup`. Give only the file name, not a path, e.g., `mediconnect-20261020-083015.json` rather than `data/backups/mediconnect-20261020-083015.json`.
* MediConnect checks the whole backup before changing anything. If the file is missing, cannot be read, or has invalid data, MediConnect says what is wrong and your data is not changed.
* Before replacing your data, MediConnect backs up your current data to `data/backups`, the same way `backup` does. If that backup cannot be saved, nothing is restored. To undo a restore, restore that backup.
* MediConnect never deletes backups, including the automatic ones. You can delete old files in `data/backups` by hand when you no longer need them.
* After a restore, the list shows all active restored patients. The view panel is cleared, unless the patient shown there is also in the backup with the same details.

Examples:
* `restore mediconnect-20261020-083015.json` replaces your data with the backup made at 8:30:15 am on 20 Oct 2026.

Returns, for example, `Restored 7 patients from mediconnect-20261020-083015.json. Your previous data was backed up to: ` followed by the full path of the automatic backup. If some restored patients are archived, the number is followed by, e.g., `(2 archived, shown by listarchived)`, since the list after a restore shows only active patients.

`restore` alone returns, for example, `Backups in ` followed by the full path of the folder, `, newest first:`, one backup name per line, and `Restore one with: restore FILENAME`. If there are none, it returns `No backups found in ` followed by the folder and `. Make one with backup.`

### Exporting a patient's record: `export`

Saves the full record of a patient to a text file in the `data/exports` folder, for printing or sharing.

Format: `export INDEX`

* Exports the patient at the specified `INDEX`.
* The index refers to the index number shown in the displayed patient list.
* The index **must be a positive integer** 1, 2, 3, ...
* The file is named after the patient's NRIC, e.g., `data/exports/S1234567A.txt`. Exporting the same patient again replaces that file. If the patient's NRIC has been changed since an earlier export, e.g., with `edit`, the new export gets a new file, and the earlier file is kept unchanged. Delete it by hand if it is no longer needed.
* The file has a title and the date and time of the export, then the patient's details, with `Status: Archived` after the address for an archived patient, and their numbered lists of medications (with their daily times, if any), allergens, care instructions, contacts and appointments. An empty list shows `None recorded`.
* Appointments are ordered and marked the same way as in the detail panel: upcoming ones first, then past ones marked `(past)`. Upcoming and past are decided at the time of the export, so an appointment that has passed since the panel last showed the patient is listed as past, and the numbers can differ from the panel's until the patient is shown again.
* MediConnect creates the `data/exports` folder if it does not exist yet. Exporting does not change your patient data.
* On macOS and Linux, only your user account can open the file, since it holds medical details.

Examples:
* `list` followed by `export 2` exports the 2nd active patient.
* `find Alex` followed by `export 1` exports the 1st patient in the results of the `find` command.
* `listarchived` followed by `export 1` exports the 1st archived patient.

Returns, for example, `Exported Alex Yeoh's record to: ` followed by the full path of the file.

The file for Alex Yeoh in the sample data looks like this:

```
MediConnect patient record
Exported on 2026-10-21 14:30

Alex Yeoh
NRIC: S0000001A
DOB: 1948-03-15
Phone: 87438807
Address: Blk 30 Geylang Street 29, #06-40

Medications
1. Aspirin 100mg (08:00)
2. Amlodipine 5mg (08:00, 20:00)

Allergens
1. Penicillin

Care Instructions
1. Monitor blood pressure every morning

Contacts
1. Tan & Partners Clinic (GP); Phone: +65 6123-4567; Note: Call in the morning

Appointments
1. 2027-11-05 09:30 with Dr Tan
2. 2027-12-10 14:00 with Cardiology clinic (Bring BP log)
```

### Exiting the program: `exit`

Exits the program.

Format: `exit`

### Saving the data

MediConnect automatically saves data after every command. You do not need to save manually.

### Editing the data file

MediConnect data is saved automatically as a JSON file `data/mediconnect.json`, inside the folder you launch MediConnect from. Advanced users are welcome to update data directly by editing that data file.

<box type="warning" seamless>

**Caution:**
If the data file cannot be loaded, MediConnect first saves an unchanged recovery copy beside it, named
`mediconnect-unreadable-<unique ID>.json`, then starts with no patients. A warning shows the recovery copy's full
path. Commands continue to save to `data/mediconnect.json`; the recovery copy is kept separately.
If the recovery copy cannot be created, startup stops so the original file is not overwritten.
To recover your data, close MediConnect, repair a copy of the recovery file and replace `data/mediconnect.json`
with the repaired file before restarting. Keep the recovery copy until you have checked the recovered patients.
We still recommend backing up the file before editing it.<br>
Furthermore, certain edits can cause MediConnect to behave in unexpected ways (e.g., if a value entered is outside of the acceptable range). Therefore, edit the data file only if you are confident that you can update it correctly.
</box>

--------------------------------------------------------------------------------------------------------------------

## FAQ

**Q**: Where is my data saved?<br>
**A**: In `data/mediconnect.json`, inside the folder you launch MediConnect from. If this file does not exist, MediConnect creates it with sample patients when it starts. After that, it saves to the file after every command.

**Q**: How do I transfer my data to another computer?<br>
**A**: Install the app on the other computer and overwrite the data file it creates with the data file from your previous MediConnect home folder.

--------------------------------------------------------------------------------------------------------------------

## Known issues

1. **When using multiple screens**, if you move the application to a secondary screen, and later switch to using only the primary screen, the GUI will open off-screen. The remedy is to delete the `preferences.json` file created by the application before running the application again.
2. **If you minimize the Help Window** and then run the `help` command (or use the `Help` menu, or the keyboard shortcut `F1`) again, the original Help Window will remain minimized, and no new Help Window will appear. The remedy is to manually restore the minimized Help Window.

--------------------------------------------------------------------------------------------------------------------

## Command summary

Action            | Format, Examples
------------------|------------------------------------------------------------------------------------------------------------------
**Add**           | `add n/NAME ic/NRIC dob/DOB p/PHONE a/ADDRESS` <br> e.g., `add n/John Tan ic/S1234567A dob/1950-04-12 p/91234567 a/21 Lorong 3, #05-10`
**Add appointment** | `addappt INDEX d/DATE t/TIME w/WITH [nt/NOTE]` <br> e.g., `addappt 3 d/2027-11-05 t/09:30 w/Dr Tan`
**Add care info** | `addcare INDEX [med/MEDICATION]... [al/ALLERGEN]... [ci/CARE_INSTRUCTION]...` <br> e.g., `addcare 1 med/Paracetamol 500mg al/Latex ci/Turn every 2 hours`
**Add contact** | `addcontact INDEX n/NAME r/ROLE p/PHONE [nt/NOTE]` <br> e.g., `addcontact 1 n/Dr Tan r/GP p/+65 6123-4567`
**Archive**       | `archive INDEX` <br> e.g., `archive 1`
**Backup**        | `backup`
**Cancel appointment** | `cancelappt INDEX APPT_INDEX` <br> e.g., `cancelappt 1 2`
**Clear**         | `clear`
**Delete**        | `delete INDEX` <br> e.g., `delete 3`
**Delete allergen** | `deleteallergy INDEX ALLERGY_INDEX` <br> e.g., `deleteallergy 1 1`
**Delete care instruction** | `deletecare INDEX CI_INDEX` <br> e.g., `deletecare 1 1`
**Delete contact** | `deletecontact INDEX CONTACT_INDEX` <br> e.g., `deletecontact 1 1`
**Delete medication** | `deletemed INDEX MED_INDEX` <br> e.g., `deletemed 1 2`
**Due**           | `due [INDEX]` <br> e.g., `due`, `due 1`
**Edit appointment** | `editappt INDEX APPT_INDEX [d/DATE] [t/TIME] [w/WITH] [nt/NOTE]` <br> e.g., `editappt 1 2 t/10:00 nt/`
**Edit care instruction** | `editcare INDEX CI_INDEX ci/NEW_TEXT` <br> e.g., `editcare 1 1 ci/Check blood sugar after lunch`
**Edit contact** | `editcontact INDEX CONTACT_INDEX [n/NAME] [r/ROLE] [p/PHONE] [nt/NOTE]` <br> e.g., `editcontact 1 1 nt/Call first`
**Edit medication** | `editmed INDEX MED_INDEX [med/DESCRIPTION] [t/TIME]...` <br> e.g., `editmed 1 2 med/Metformin 850mg t/08:00 t/20:00`
**Edit patient**  | `edit INDEX [n/NAME] [ic/NRIC] [dob/DOB] [p/PHONE] [a/ADDRESS]` <br> e.g., `edit 1 p/91234567`
**Exit**          | `exit`
**Export**        | `export INDEX` <br> e.g., `export 2`
**Find**          | `find KEYWORD [MORE_KEYWORDS]...` <br> e.g., `find Alex Bernice`
**Help**          | `help`
**List**          | `list`
**List appointments** | `listappt [INDEX]` <br> e.g., `listappt`, `listappt 2`
**List archived** | `listarchived`
**Restore**       | `restore [FILENAME]` <br> e.g., `restore`, `restore mediconnect-20261020-083015.json`
**Unarchive**     | `unarchive INDEX` <br> e.g., `unarchive 1`
**View**          | `view INDEX` <br> e.g., `view 2`
