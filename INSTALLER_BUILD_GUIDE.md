# Lab Management System - Installer Build Guide

This guide explains how to build cross-platform installers (Windows EXE, macOS DMG, Linux DEB) for the Lab Management System JavaFX frontend.

## Prerequisites

- **Java 21 or higher** installed and in PATH
- **Maven 3.6+** installed
- **jpackage** tool (included with Java 16+)
- Operating system matching your target installer (easiest approach)

### Verify jpackage is available:
```powershell
# Windows
jpackage --version

# Mac/Linux
jpackage --version
```

---

## Quick Build Commands

### **Windows - Create EXE Installer**

```powershell
cd "c:\Users\user\Desktop\Student Ta\frontend"

# Build JAR + create Windows EXE installer
mvn clean package -P build-windows-exe
```

**Output location:** `target/installers/Lab Management System-1.0.0.exe`

### **macOS - Create DMG Installer**

```bash
cd ~/Desktop/Student\ Ta/frontend

# Build JAR + create macOS DMG installer
mvn clean package -P build-mac-dmg
```

**Output location:** `target/installers/Lab Management System-1.0.0.dmg`

### **Linux - Create DEB Installer**

```bash
cd ~/Desktop/Student\ Ta/frontend

# Build JAR + create Linux DEB installer
mvn clean package -P build-linux-deb
```

**Output location:** `target/installers/Lab Management System-1.0.0.deb`

---

## Detailed Build Steps

### **Step 1: Build the JAR (All Platforms)**

```powershell
cd "c:\Users\user\Desktop\Student Ta\frontend"
mvn clean package -DskipTests
```

This creates:
- `target/LabManagementSystem.jar` (regular JAR)
- `target/LabManagementSystem-executable.jar` (shaded JAR with all dependencies)

### **Step 2: Create Platform-Specific Installer**

#### **For Windows EXE:**
```powershell
mvn package -P build-windows-exe -DskipTests
```

**Features:**
- Standalone executable (no Java installation required)
- Per-user installation support
- Directory chooser for installation location
- GUI installer

#### **For macOS DMG:**
```bash
mvn package -P build-mac-dmg -DskipTests
```

**Features:**
- Native .app bundle
- DMG installer for easy distribution
- Supports both Intel and Apple Silicon (ARM64)
- Code signing disabled (set `--mac-sign false` - enable if you have a certificate)

#### **For Linux DEB:**
```bash
mvn package -P build-linux-deb -DskipTests
```

**Features:**
- Debian package format
- Works on Ubuntu, Debian, and derivatives
- Desktop launcher integration
- System dependency management

---

## Configuration Options

The `pom.xml` includes these configurable options in jpackage arguments:

| Option | Default | Description |
|--------|---------|-------------|
| `--java-options` | `-Xmx2g` | Memory allocation (2GB max heap) |
| `--app-version` | `1.0.0` | Application version from pom.xml |
| `--vendor` | `Lab Management System` | Vendor name in installer |
| `--win-console` | `false` | Show console window on Windows |
| `--win-dir-chooser` | `true` | Allow user to choose install directory |

### Customize Memory:

Edit `frontend/pom.xml` and change the `--java-options` argument:

```xml
<argument>-Xmx4g</argument>  <!-- Increase to 4GB -->
```

---

## Manual jpackage Commands (Advanced)

If you prefer to run jpackage directly:

### **Windows EXE:**
```powershell
$javaHome = (Get-Command java).Source | Split-Path -Parent | Split-Path -Parent
$jpackage = "$javaHome\bin\jpackage.exe"

& $jpackage `
  --input "frontend\target" `
  --name "Lab Management System" `
  --main-jar "LabManagementSystem-executable.jar" `
  --main-class "com.lms.ui.LabManagementApplication" `
  --type exe `
  --dest "frontend\target\installers" `
  --vendor "Lab Management System" `
  --app-version "1.0.0" `
  --java-options "-Xmx2g" `
  --win-console false `
  --win-per-user-install true `
  --win-dir-chooser true
```

### **macOS DMG:**
```bash
jpackage \
  --input frontend/target \
  --name "Lab Management System" \
  --main-jar LabManagementSystem-executable.jar \
  --main-class com.lms.ui.LabManagementApplication \
  --type dmg \
  --dest frontend/target/installers \
  --vendor "Lab Management System" \
  --app-version 1.0.0 \
  --java-options "-Xmx2g" \
  --mac-sign false
```

### **Linux DEB:**
```bash
jpackage \
  --input frontend/target \
  --name "Lab Management System" \
  --main-jar LabManagementSystem-executable.jar \
  --main-class com.lms.ui.LabManagementApplication \
  --type deb \
  --dest frontend/target/installers \
  --vendor "Lab Management System" \
  --app-version 1.0.0 \
  --java-options "-Xmx2g"
```

---

## Troubleshooting

### **"jpackage not found"**
- Ensure Java 16+ is installed: `java -version`
- Add Java bin to PATH: `echo $JAVA_HOME/bin`
- Restart terminal after PATH changes

### **"LabManagementSystem-executable.jar not found"**
- Run `mvn clean package -DskipTests` first to create the JAR

### **Windows: "WiX Toolset not found"**
- Install WiX Toolset 3.0+ from: https://github.com/wixtoolset/wix3/releases
- Add WiX bin directory to PATH
- Or use alternative: `--type msi` (MSI installer instead of EXE)

### **macOS: "xcrun not found"**
- Install Xcode Command Line Tools: `xcode-select --install`

### **macOS: Code signing issues**
- To disable signing: Keep `--mac-sign false` in pom.xml
- To enable signing: Replace with `--mac-sign true --mac-signing-key-user-name "your certificate name"`

### **Out of memory during build**
- Increase Maven heap: `export MAVEN_OPTS="-Xmx4g"` (Linux/Mac) or `set MAVEN_OPTS=-Xmx4g` (Windows)

---

## Distribution

After building, installers are located in `frontend/target/installers/`:

### **Windows Users:**
- Double-click the `.exe` file
- Follow the installer wizard
- Application launches after installation

### **macOS Users:**
- Double-click the `.dmg` file
- Drag app to Applications folder
- Launch from Applications folder

### **Linux Users:**
```bash
# Install DEB package
sudo dpkg -i "Lab Management System-1.0.0.deb"

# Launch application
Lab\ Management\ System
```

---

## Alternative: Distribute as JAR

If you prefer not to create platform-specific installers, users can run the JAR directly:

```bash
java -jar LabManagementSystem-executable.jar
```

**Requirements:** Java 21 installed on user's system

**Advantage:** Single file works on Windows, macOS (Intel & Apple Silicon), and Linux

---

## Building for Multiple Platforms

To build for all platforms, run on each respective OS:

```powershell
# Windows
mvn clean package -P build-windows-exe

# Mac
mvn clean package -P build-mac-dmg

# Linux  
mvn clean package -P build-linux-deb
```

Or create a CI/CD pipeline (GitHub Actions, GitLab CI, etc.) to automate cross-platform builds.

---

## Next Steps

1. Test the installer on your target OS
2. Create GitHub Releases with platform-specific installers
3. Document system requirements (Java 21 no longer required after jpackage)
4. Update user documentation with installation instructions

---

## Additional Resources

- [Java jpackage Documentation](https://docs.oracle.com/en/java/javase/21/docs/specs/man/jpackage.html)
- [JavaFX Deployment Guide](https://openjfx.io/openjfx-docs/)
- [WiX Toolset (for Windows MSI)](https://github.com/wixtoolset/wix3)

