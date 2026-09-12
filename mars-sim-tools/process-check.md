# ProcessCheck

ProcessCheck validates process definitions.
It scans the configured manufacture, food production, and salvage processes and flags any process where total output mass is greater than total input mass.
The report is grouped by process type.

## Output

- Standard output: grouped summary of failed processes by process type.
- Report file: grouped detailed item-by-item breakdown by process type.

The detailed report file is written as:

- `process-check-details.txt`

## Arguments

- `-help` : show usage help.
- `-configdir <dir>` : use an alternative simulation data directory.
- `-output <dir>` : directory where the detailed report file is written.
- `-type <name>` : scan only one process type. Use `manufacture`, `food`, `salvage`, or `all`.

If `-output` is not provided, the report is written to the current working directory.
If `-type` is not provided, all process types are scanned.

## Run

From the repository root:

```bash
mvn -pl mars-sim-tools -am -DskipTests exec:java \
  -Dexec.mainClass=com.mars_sim.tools.manufacture.ProcessCheck \
  -Dexec.args="-output ."
```

Example with custom config and output directories:

```bash
mvn -pl mars-sim-tools -am -DskipTests exec:java \
  -Dexec.mainClass=com.mars_sim.tools.manufacture.ProcessCheck \
  -Dexec.args="-configdir C:/path/to/sim-data -output C:/path/to/reports"
```

Example that scans only salvage processes:

```bash
mvn -pl mars-sim-tools -am -DskipTests exec:java \
  -Dexec.mainClass=com.mars_sim.tools.manufacture.ProcessCheck \
  -Dexec.args="-type salvage -output C:/path/to/reports"
```
