# VendRx

**Radio signal logging and transcription**

VendRx is a Java application for monitoring a radio audio input and automatically capturing individual transmissions.

The goal is to build a lightweight radio logging system capable of detecting activity, recording transmissions, storing metadata, and eventually producing searchable offline transcriptions.

> **Status:** Early development

## Current features

VendRx currently supports:

* Audio input device discovery
* Real-time RMS audio level monitoring
* Automatic signal detection
* Transmission state detection (`IDLE` / `RECORDING`)
* Configurable silence timeout
* 2-second audio pre-buffer to avoid clipping the beginning of transmissions
* Automatic WAV recording
* Transmission metadata generation
* SQLite metadata storage
* Persistent transmission history
* Display of recent transmissions at startup
* Separation of application code and runtime data

## How it works

```text
Radio / audio input
        |
        v
Audio capture
        |
        v
RMS signal detection
        |
        v
Transmission detector
   IDLE <-> RECORDING
        |
        v
2 s pre-buffer
        |
        v
WAV recorder
        |
        v
Transmission metadata
        |
        +------> WAV file
        |
        +------> SQLite database
```

Each detected transmission is saved as an individual WAV file along with metadata such as:

* Start time
* End time
* Duration
* File path
* Average RMS level
* Maximum RMS level

## Data storage

Runtime data is intentionally stored outside the Git repository.

On Windows, VendRx currently uses:

```text
C:\Users\<username>\VendRxData\
├── vendrx.db
└── recordings\
    ├── 2026-08-28_22-35-51.wav
    └── ...
```

This keeps recordings and databases separate from the source code and prevents generated data from being synchronized with GitHub.

## Project structure

```text
src/main/java/ca/vendrx/
├── Main.java
├── audio/
│   ├── AudioInputMonitor.java
│   ├── PreBuffer.java
│   ├── TransmissionDetector.java
│   └── TransmissionRecorder.java
├── config/
│   └── AppPaths.java
├── database/
│   └── TransmissionRepository.java
└── model/
    └── Transmission.java
```

## Requirements

* Java 25 or newer
* Apache Maven
* An audio input device
* A radio receiver or other audio source

VendRx uses the Xerial SQLite JDBC driver for local database access.

## Build

Clone the repository and compile it with Maven:

```bash
mvn compile
```

During development, the application can be launched from an IDE such as VS Code by running:

```text
ca.vendrx.Main
```

VendRx will list the available audio capture devices and prompt for the input to monitor.

## Example

```text
VendRx
Radio signal logging and transcription

Database: C:\Users\<username>\VendRxData\vendrx.db

Recent transmissions:

[1] 2026-08-28T22:35:51 | 5.260 s | RMS 0.0843

Audio inputs:

[0] Primary Sound Capture Driver
[1] USB Audio Device
[2] Microphone Array

Select input: 1
```

When a signal is detected:

```text
>>> Transmission started
Recording started

<<< Transmission ended

Saved: C:\Users\<username>\VendRxData\recordings\2026-08-28_22-35-51.wav
Transmission saved to database.
```

## Roadmap

VendRx is being developed incrementally from a command-line prototype into a desktop radio logging application.

### Near term

* Transmission history and browsing
* Display detailed transmission metadata
* Select and inspect recorded transmissions
* WAV playback
* Configurable signal detection threshold
* Configurable silence timeout and pre-buffer duration
* Improved error handling and application configuration

### Desktop interface

A graphical desktop interface is planned using JavaFX.

The interface should eventually provide:

* Live audio level monitoring
* Recording status and signal indication
* Audio input selection
* Transmission history
* Transmission metadata
* Integrated audio playback
* Application settings
* Search and filtering
* Recording and activity statistics

### Later development

Possible future features include:

* Frequency and channel metadata
* Manual notes and transmission tagging
* Import and export tools
* Audio waveform visualization
* Signal and activity statistics
* Offline speech-to-text transcription
* Searchable transcription storage


## Development

VendRx is being developed incrementally, with major features developed on separate Git branches before being merged into `main`.

## License

VendRx is released under the MIT License.
