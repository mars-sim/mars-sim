# Application Directory Structure

After building with `mvn package -Pjpackage`, you'll find:

```
mars-sim-installer/target/
├── app-images/                    # Self-contained applications
│   ├── MarsSim/                   # Swing UI application
│   │   ├── bin/MarsSim           # Executable
│   │   └── lib/                   # Application and Java runtime
│   └── MarsSimConsole/            # Console application
│       ├── bin/MarsSimConsole    # Executable
│       └── lib/                   # Application and Java runtime
└── installers/                   # Platform-native installers (if supported)
    ├── marssim_*.deb             # Debian package (Linux)
    ├── marssim-*.rpm             # RPM package (Linux)
    ├── MarsSim-*.msi             # MSI installer (Windows)
    └── MarsSim-*.dmg             # DMG image (macOS)
```

## Size Information
- Each app-image is approximately 400-500MB (includes full Java runtime)
- Native installers compress the runtime and may be smaller
- The embedded Java runtime ensures compatibility across different systems