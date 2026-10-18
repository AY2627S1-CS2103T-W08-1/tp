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

   * `list` : Lists all patients.

   * `add n/John Tan ic/S1234567A dob/1990-05-14 p/91234567 a/Blk 123 Clementi Ave 3` : Adds a patient named `John Tan` to MediConnect.

   * `view 1` : Shows the full details of the 1st patient in the current list.

   * `find John` : Lists all patients whose name contains `John`.

   * `delete 1` : Deletes the 1st patient shown in the current list. Right after `find John`, this deletes `John Tan`.

   * `exit` : Exits the app.

1. Refer to the [Features](#features) section below for details of each command.

--------------------------------------------------------------------------------------------------------------------

## Features

<box type="info" seamless>

**Notes about the command format:**<br>

* Words in `UPPER_CASE` are the parameters to be supplied by the user.<br>
  For example, in `add n/NAME`, replace `NAME` with a value such as `John Doe`.

* Items in square brackets are optional.<br>

* Items followed by `...` can appear zero or more times.<br>

* Parameters can be in any order.<br>
  For example, if the command specifies `n/NAME p/PHONE_NUMBER`, `p/PHONE_NUMBER n/NAME` is also acceptable.

* Extraneous parameters for commands that take no parameters, such as `help`, `list`, `exit`, and `clear`, are ignored.<br>
  For example, `help 123` is interpreted as `help`.

* If you are using a PDF version of this document, be careful when copying and pasting commands that span multiple lines as space characters surrounding line-breaks may be omitted when copied over to the application.
</box>

### Viewing help: `help`

Opens a help window that lists every command with its format and a one-line description.

![help message](images/helpMessage.png)

Format: `help`

* You can also open the help window with the `Help` menu or by pressing `F1`.
* Any text after `help` is ignored. For example, `help add` is interpreted as `help`.
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
* Standalone `s/o`, `d/o`, and `c/o` are allowed anywhere in the `add` arguments, ignoring letter case.
  Other field rules still apply. Keep them separate from other text or punctuation: `s/o,` is not allowed.
  Addresses can include numeric slash text such as `21/3` and URLs.
* `NRIC` must be 1 letter (S, T, F, G, or M), 7 digits, and 1 letter, e.g., `S1234567A`.
  It is not case-sensitive and is stored in uppercase.
* `DOB` must be a valid date in `YYYY-MM-DD` format, e.g., `1950-04-12`, and cannot be in the future.
* Patients with the same NRIC, ignoring letter case, are rejected with
  `This patient (identified by NRIC) already exists in MediConnect.` Names alone do not identify duplicates.
* `NAME` must start with a letter and can only contain letters, spaces, `'`, `-`, `/` and `.`,
  e.g., `Tan Ah Kow s/o Lim` or `Mary-Jane O'Brien`.
  Multiple spaces between words are reduced to one.
* `PHONE` must be 8 digits, with an optional `+65` in front, e.g., `91234567` or `+6591234567`.
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
Name    | `n/`   | Starts with a letter. Only letters, spaces, `'`, `-`, `/` and `.`. Multiple spaces between words are reduced to one. | `Mary-Jane O'Brien`
NRIC    | `ic/`  | 1 letter (S, T, F, G or M), 7 digits, 1 letter. Not case-sensitive; stored in uppercase. | `S1234567A`
DOB     | `dob/` | A valid date in `YYYY-MM-DD` format, not in the future. | `1950-04-12`
Phone   | `p/`   | 8 digits with no spaces, with an optional `+65` in front. Stored without the `+65`. | `91234567`, `+6591234567`
Address | `a/`   | Any value, but not blank. | `Blk 12 Ang Mo Kio Ave 4, #05-10`

Leading and trailing spaces in every field are ignored. A word that looks like an unknown prefix, such as `x/12`, is not allowed in any field (see the `add` command).

### Adding care information: `addcare`

Appends medications, allergens, or care instructions to the patient at the displayed index.
Existing care information is never overwritten.

Format: `addcare INDEX [med/MEDICATION]... [al/ALLERGEN]... [ci/CARE_INSTRUCTION]...`

* `INDEX` follows the same rules as `delete` and refers to the currently displayed patient list.
* At least one `med/`, `al/`, or `ci/` prefix is required. Each prefix can be repeated.
* Care entries are trimmed free text and cannot be blank. If any entry is blank, the whole command is rejected.
* Duplicate entries, ignoring letter case and surrounding spaces, are not added again. Other unique entries in the
  same command are still added.

Examples:

* `addcare 1 med/Metformin 500mg twice daily`
* `addcare 2 al/Penicillin al/Shellfish`
* `addcare 1 ci/Check blood sugar before breakfast`

A successful command returns `Updated care info for NAME. Added: ITEMS.`

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

### Adding an appointment: `addappt`

Adds an appointment to the patient at the displayed index.

Format: `addappt INDEX d/DATE t/TIME w/WITH [nt/NOTE]`

* `INDEX` follows the same rules as `delete` and refers to the currently displayed patient list.
* `DATE` must be a real date in `YYYY-MM-DD` format, e.g., `2026-11-05`.
* `TIME` must be in `HH:MM` 24-hour format, e.g., `09:30` or `14:00`.
* `WITH` is who the appointment is with, e.g., `Dr Tan` or `Cardiology clinic`. It cannot be blank. Leading and
  trailing spaces are ignored, and multiple spaces between words are reduced to one.
* `NOTE` is optional free text. `nt/` with nothing after it means no note.
* Text that looks like a prefix, such as `w/` or `c/o`, cannot be used inside `WITH` or `NOTE`.
* The patient's appointments are shown in the detail panel of `view`, sorted by date, then time.

Examples:

* `addappt 3 d/2026-11-05 t/09:30 w/Dr Tan`
* `addappt 2 d/2026-12-10 t/14:00 w/Cardiology clinic nt/Bring BP log`

A successful command returns `New appointment for NAME: DATE TIME with WITH`, followed by ` (NOTE)` if there is a
note. For example, the second example returns
`New appointment for Bernice Yu: 2026-12-10 14:00 with Cardiology clinic (Bring BP log)`.

* An appointment in the past is still added, so past visits can be recorded. The message then ends with
  `Note: this appointment is in the past.`
* An appointment at the same date and time as another appointment of the patient, but with someone else, is still
  added. The message then ends with `Warning: NAME already has an appointment at this date and time (with WITH).`

Other errors are reported as follows:

* An appointment with the same date, time and `WITH` as one the patient already has, ignoring letter case and extra
  spaces, returns `This appointment already exists for this patient.` and is not added.
* An index outside the displayed list returns `The patient index provided is invalid.`
* An invalid date returns `Appointment date should be a real date in YYYY-MM-DD format, e.g. 2026-10-25.`
* An invalid time returns `Appointment time should be in HH:MM 24-hour format, e.g. 14:30.`
* A blank `WITH` returns `Who the appointment is with should not be blank.`
* A missing `d/`, `t/` or `w/`, or a missing or invalid index, returns `Invalid command format!`, followed by the
  command usage.

### Cancelling an appointment: `cancelappt`

Removes an appointment from the patient at the displayed index.

Format: `cancelappt INDEX APPT_INDEX`

* `INDEX` follows the same rules as `delete` and refers to the currently displayed patient list.
* `APPT_INDEX` is the appointment's number in the Appointments section of the patient's detail panel, counting from
  1. Appointments are listed by date, then time.
* Both indexes **must be positive integers** 1, 2, 3, ...
* The appointment is removed, not kept as cancelled.

Examples:

* `view 1` followed by `cancelappt 1 2` cancels the 2nd appointment of the 1st patient.

A successful command returns `Cancelled appointment for NAME: APPOINTMENT`, with the appointment shown as in the
detail panel. For example, the example above returns
`Cancelled appointment for Alex Yeoh: 2026-12-10 14:00 with Cardiology clinic (Bring BP log)`.

Errors are reported as follows:

* An index outside the displayed list returns `The patient index provided is invalid.`
* An `APPT_INDEX` larger than the number of the patient's appointments returns
  `The appointment index provided is invalid.`
* A missing, zero, negative, or non-numeric index, or more than two indexes, returns `Invalid command format!`,
  followed by the command usage.

### Listing all patients: `list`

Shows a list of all patients in MediConnect.

Format: `list`

### Editing a patient: `edit`

Not implemented in the MVP. The command returns `Edit command is not implemented yet.` without changing patient data.

### Locating patients by name: `find`

Finds patients whose names contain any of the given keywords.

Format: `find KEYWORD [MORE_KEYWORDS]`

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

### Viewing a patient's full details: `view`

Shows all the details of the specified patient in the panel to the right of the patient list.

Format: `view INDEX`

* Shows the patient at the specified `INDEX`, which refers to the index number shown in the displayed patient list.
* The index **must be a positive integer** 1, 2, 3, ...
* The panel shows the patient's name, NRIC, date of birth, phone number, and address, followed by their
  medications, allergens, care instructions, and appointments (sorted by date, then time), one entry per line.
  An empty list shows `None recorded`.
* Before any patient is viewed, the panel shows `Enter 'view INDEX' to see a patient's full details here.`
* The panel keeps showing the patient after other commands, such as `list` or `find`, even if `find` hides the
  patient from the list. It updates when the patient's care information changes, e.g., after `addcare`, and goes back
  to the message above when the patient is deleted or all patients are cleared.

Examples:

* `list` followed by `view 2` shows the details of the 2nd patient in the list.
* `find Bernice` followed by `view 1` shows the details of the 1st patient in the results of the `find` command.

A successful command returns `Showing patient: NAME`, e.g., `Showing patient: Bernice Yu`.

If `INDEX` is larger than the number of patients in the displayed list, the command is rejected with
`The patient index provided is invalid.` If `INDEX` is missing, is not a positive integer (e.g., `0`, `-1`, or `abc`),
is too large to be a valid number (e.g., `99999999999`), or is followed by extra words, the command is rejected with
`Invalid command format!` followed by the usage and example. In both cases, the panel keeps showing the patient it
showed before.

### Clearing all patients: `clear`

Clears all patients and their care information from MediConnect, and clears any patient details in the view panel.

Format: `clear`

Returns `All patients have been cleared.`, including when there are no patients to clear.

### Exiting the program: `exit`

Exits the program.

Format: `exit`

### Saving the data

MediConnect automatically saves data after every command. You do not need to save manually.

### Editing the data file

MediConnect data is saved automatically as a JSON file `data/mediconnect.json`, inside the folder you launch MediConnect from. Advanced users are welcome to update data directly by editing that data file.

<box type="warning" seamless>

**Caution:**
If your changes make the data file invalid, MediConnect starts with no patients at the next run. The invalid file remains on disk until you run a command (MediConnect saves after every command). Still, we recommend backing up the file before editing it.<br>
Furthermore, certain edits can cause MediConnect to behave in unexpected ways (e.g., if a value entered is outside of the acceptable range). Therefore, edit the data file only if you are confident that you can update it correctly.
</box>

### Archiving data files `[coming in v2.0]`

_Details coming soon ..._

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
**Add appointment** | `addappt INDEX d/DATE t/TIME w/WITH [nt/NOTE]` <br> e.g., `addappt 3 d/2026-11-05 t/09:30 w/Dr Tan`
**Add care info** | `addcare INDEX [med/MEDICATION]... [al/ALLERGEN]... [ci/CARE_INSTRUCTION]...` <br> e.g., `addcare 1 med/Paracetamol 500mg al/Latex ci/Turn every 2 hours`
**Cancel appointment** | `cancelappt INDEX APPT_INDEX` <br> e.g., `cancelappt 1 2`
**Clear**         | `clear`
**Delete**        | `delete INDEX` <br> e.g., `delete 3`
**Exit**          | `exit`
**Find**          | `find KEYWORD [MORE_KEYWORDS]...` <br> e.g., `find Alex Bernice`
**Help**          | `help`
**List**          | `list`
**View**          | `view INDEX` <br> e.g., `view 2`
