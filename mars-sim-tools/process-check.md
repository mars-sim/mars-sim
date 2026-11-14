# ProcessCheck

ProcessCheck validates process definitions.
It scans the configured manufacture, food production, and salvage processes and flags any process where total output mass is acceptable compared to the total input mass. Acceptable is the input mass cannot be less than the output or more than 20%.
The report is grouped by process type.
The second check is reports how Resources are created either from a Food, Manufacturing or Resource Process.

## Output

- Standard output: grouped summary of failed processes by process type.
- Report file: grouped detailed item-by-item breakdown by process type.

The detailed report file is written as:

- `resource-check-details.txt` - Detailed report on how resources are created
- `process-check-details.txt` - Detailed report on input/output mass of processes

## Arguments

- `-help` : show usage help.
- `-configdir <dir>` : use an alternative simulation data directory.
- `-output <dir>` : directory where the detailed report file is written.
- `-create` : Check all the resources can be created by a process.
- `-mass <process name>` : Check the input/output mass of all process or a single named one.

If `-output` is not provided, the report is written to the current working directory.

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

Example that check only the process masses:

```bash
mvn -pl mars-sim-tools -am -DskipTests exec:java \
  -Dexec.mainClass=com.mars_sim.tools.manufacture.ProcessCheck \
  -Dexec.args="-mass -output C:/path/to/reports"
```
