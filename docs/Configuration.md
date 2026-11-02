---
  layout: default.md
  title: "Configuration guide"
---

# Configuration guide

MediConnect has no configuration file. The following are fixed in the code:

* **Data and preferences files**: `MainApp` sets the patient data file to `data/mediconnect.json`
  (`ADDRESS_BOOK_FILE_PATH`) and the preferences file, which holds the window size and position, to
  `preferences.json` (`USER_PREFS_FILE_PATH`).
* **Other folders**: lock files go in `.mediconnect/locks` (`MainApp`) and backups in `data/backups` (`BackupCommand`).
* **Logging**: the level, file name, file size limit and number of files are constants in `LogsCenter`. See the
  [Logging guide](Logging.md).

All these paths are relative to the folder MediConnect is launched from. To change any of these settings, edit the
constants and rebuild MediConnect.
