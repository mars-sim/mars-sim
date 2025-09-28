# Mars Simulation Project - Native Installers

This directory contains the configuration for building native installers for the Mars Simulation Project using Java's `jpackage` tool.

## Features

- **Self-contained applications**: Each installer includes a complete Java runtime, eliminating the need for users to install Java separately
- **Two application variants**:
  - **MarsSim**: The full Swing UI application for interactive use
  - **MarsSimConsole**: The headless console application for server deployment and remote access
- **Cross-platform support**: Generates native installers for Linux, Windows, and macOS
- **Application icons**: Custom Mars-themed icons for better user experience

## Building Installers

### Prerequisites
- Java 21 or higher
- Maven 3.6 or higher

### Build Commands

```bash
# Build app-images (portable applications with embedded Java)
mvn clean package -Pjpackage -Dmaven.test.skip=true

# The built applications will be in:
# - mars-sim-installer/target/app-images/MarsSim/
# - mars-sim-installer/target/app-images/MarsSimConsole/
```

### Platform-specific Installers

The build also attempts to create platform-native installers:
- **Linux**: .deb and .rpm packages
- **Windows**: .msi and .exe installers  
- **macOS**: .dmg and .pkg installers

Note: Some installer formats may require additional system tools to be installed.

## Usage

### MarsSim (Swing UI)
```bash
./MarsSim/bin/MarsSim [options]
```

### MarsSimConsole (Headless)
```bash
./MarsSimConsole/bin/MarsSimConsole [options]
```

Both applications support the same command-line arguments as the original JAR files. Use `-help` to see available options.

## GitHub Actions Integration

The project includes automated workflows for building installers:
- `.github/workflows/build-installers.yml`: Multi-platform installer builder
- Updated release workflow includes installer generation

## Application Details

- **Memory allocation**: Applications are configured with 4GB heap size by default
- **Java options**: Includes optimizations for desktop applications
- **Logging**: Both applications include `--log` argument for file logging
- **Console application**: Includes `-remote` argument to enable SSH access