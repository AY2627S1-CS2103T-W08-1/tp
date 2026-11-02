---
  layout: default.md
  title: "Logging guide"
---

# Logging guide

* The project uses the `java.util.logging` package for logging.
* `LogsCenter` manages logging levels and destinations.
* A class obtains a `Logger` with `LogsCenter.getLogger(Class)`, which logs messages at the configured level.
* Log messages are written to the console and to `mediconnect.log.0` in the folder MediConnect is launched from.
  Java's `FileHandler` adds the number to the file name `mediconnect.log`. When the file reaches 5 MiB, it is renamed
  `mediconnect.log.1` and a new `mediconnect.log.0` is started. At most five files (`.0` to `.4`) are kept, and the
  oldest is deleted. If another MediConnect in the same folder is using the file, Java adds a further number, e.g.,
  `mediconnect.log.0.1`.
* The level, file name, size limit and number of files are the constants `LOG_LEVEL`, `LOG_FILE`,
  `MAX_FILE_SIZE_IN_BYTES` and `MAX_FILE_COUNT` in the `LogsCenter` class. Edit them and rebuild to change the
  logging.
* **When choosing a level for a log message**, follow the conventions given in [_[se-edu/guides] Java: Logging conventions_](https://se-education.org/guides/conventions/java/logging.html).
