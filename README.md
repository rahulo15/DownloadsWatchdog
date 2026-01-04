# 🐕 The Downloads Watchdog

> A lightweight, background Java utility that keeps your file system organized automatically.

**The Problem:** Your Downloads folder is a chaotic mess of PDFs, installers, images, and ZIPs.  
**The Solution:** A "set it and forget it" Java program that watches specific folders and instantly sorts new files into categorized sub-directories.

## 🚀 Key Features

* **⚡ Zero-Latency Sorting:** Uses Java's `WatchService` API (Event-Driven) to detect file changes instantly without heavy CPU polling.
* **🛡️ Multi-Drive Support:** Capable of monitoring folders across different local drives (C:, D:, External Volumes) simultaneously.
* **🧠 Intelligent Conflict Resolution:** Never overwrites files. If `Resume.pdf` exists, it automatically renames the new file to `Resume (1).pdf`.
* **🧹 Startup Cleanup:** Performs a full scan and sort of existing files immediately upon launch.
* **📂 Broad Format Support:** Automatically categorizes:
    * **Images:** .jpg, .png, .svg, .webp, etc.
    * **Documents:** .pdf, .docx, .txt, .md
    * **Code:** .java, .py, .js, .json
    * **Archives:** .zip, .rar, .7z
    * **Installers:** .exe, .msi, .dmg

## 🛠️ Technical Highlights

This project demonstrates core Java software engineering concepts:
* **Java NIO (New I/O):** For efficient file system manipulation.
* **Concurrency:** Handling blocking event listeners while managing file operations.
* **Robust Error Handling:** Manages "Overflow" events and file locks (race conditions) during active downloads.
* **Modular Design:** Clean separation between the "Watch" logic and the "Sort" logic.

## 📦 Installation & Usage

### Prerequisites
* Java Development Kit (JDK) 8 or higher.
* IntelliJ IDEA (Recommended) or any Java IDE.

### Running the Project

1.  **Clone the repository:**
2.  **Open in IntelliJ IDEA.**
3.  **Configure your folders:**
    Open `Main.java` and edit the `foldersToWatch` list to point to your specific directories:
    ```java
    foldersToWatch.add(Paths.get("C:\\Users\\You\\Downloads"));
    foldersToWatch.add(Paths.get("D:\\Work\\Files"));
    ```
4.  **Run `Main.java`.**
    The console will show:
    ```text
    --- 🧹 Starting Initial Cleanup ---
    ✅ Moved: vacation.jpg (in Downloads)
    --- 👀 Watchdog is watching 2 folders ---
    ```

### Building a Standalone JAR
To run this in the background without an IDE:
1.  Build the artifact in IntelliJ (Project Structure -> Artifacts).
2.  Run via command line:
    ```bash
    java -jar DownloadsWatchdog.jar
    ```
3.  *(Optional)* Add the JAR shortcut to your Windows Startup folder to run on boot.

## 🤝 Contributing
Feel free to fork this project and add support for:
* A configuration file (JSON/Properties) to avoid hardcoding paths.
* A GUI system tray icon to pause/resume the watchdog.

## 📄 License
This project is open-source and available under the MIT License.
