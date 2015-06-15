package com.jme3x.jfx.util.os;

import static java.lang.Double.parseDouble;
import static java.lang.Integer.parseInt;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

/**
 * Name resolver OS distribution.
 * 
 * @author Ronn
 */
public class OperatingSystemResolver {

	public static final String FILE_PROC_VERSION = "/proc/version";
	public static final String FILE_ETC_ISSUE = "/etc/issue";
	public static final String FILE_ETC = "/etc/";
	public static final String FILE_ETC_SYSTEM_RELEASE = "/etc/system-release";
	public static final String FILE_ETC_LSB_RELEASE = "/etc/lsb-release";

	public static final String PROP_PRETTY_NAME = "PRETTY_NAME";
	public static final String PROP_DISTRIB_CODENAME = "DISTRIB_CODENAME";
	public static final String PROP_DISTRIB_DESCRIPTION = "DISTRIB_DESCRIPTION";

	private static final String NAME = System.getProperty("os.name");
	private static final String VERSION = System.getProperty("os.version");
	private static final String ARCH = System.getProperty("os.arch");

	private static final Map<Double, String> MAC_OS_VERSION_MAPPING = new HashMap<Double, String>();
	private static final Map<Integer, String> DARWIN_VERSION_MAPPING = new HashMap<Integer, String>();

	private static final List<String> LINUX_VERSION_NAMES = new ArrayList<String>();

	static {
		MAC_OS_VERSION_MAPPING.put(10.0, "Puma");
		MAC_OS_VERSION_MAPPING.put(10.1, "Cheetah");
		MAC_OS_VERSION_MAPPING.put(10.2, "Jaguar");
		MAC_OS_VERSION_MAPPING.put(10.3, "Panther");
		MAC_OS_VERSION_MAPPING.put(10.4, "Tiger");
		MAC_OS_VERSION_MAPPING.put(10.5, "Leopard");
		MAC_OS_VERSION_MAPPING.put(10.6, "Snow Leopard");
		MAC_OS_VERSION_MAPPING.put(10.7, "Snow Lion");
		MAC_OS_VERSION_MAPPING.put(10.8, "Mountain Lion");
		MAC_OS_VERSION_MAPPING.put(10.9, "Mavericks");
		MAC_OS_VERSION_MAPPING.put(10.10, "Yosemite");

		DARWIN_VERSION_MAPPING.put(5, "Puma");
		DARWIN_VERSION_MAPPING.put(6, "Jaguar");
		DARWIN_VERSION_MAPPING.put(7, "Panther");
		DARWIN_VERSION_MAPPING.put(8, "Tiger");
		DARWIN_VERSION_MAPPING.put(9, "Leopard");
		DARWIN_VERSION_MAPPING.put(10, "Snow Leopard");
		DARWIN_VERSION_MAPPING.put(11, "Lion");
		DARWIN_VERSION_MAPPING.put(12, "Mountain Lion");
		DARWIN_VERSION_MAPPING.put(13, "Mavericks");
		DARWIN_VERSION_MAPPING.put(14, "Yosemite");

		LINUX_VERSION_NAMES.addAll(Arrays.asList("Linux", "SunOS"));
	}

	private String findFile(final File dir, final String postfix) {

		final File[] files = dir.listFiles((FilenameFilter) (directory, filename) -> filename.endsWith(postfix));

		if(files.length > 0) {
			return files[0].getAbsolutePath();
		}

		return null;
	}

	protected void resolve(final OperatingSystem system) {

		system.setName(NAME);
		system.setArch(ARCH);
		system.setVersion(VERSION);

		// Windows is quite easy to tackle with
		if(NAME.startsWith("Windows")) {
			system.setDistribution(NAME);
		}
		// Mac requires a bit of work, but at least it's consistent
		else if(NAME.startsWith("Mac")) {
			resolveMacOs(system);
		} else if(NAME.startsWith("Darwin")) {
			resolveDarwinOs(system);
		}
		// Try to detect other POSIX compliant platforms, now the fun begins
		else {
			for(final String name : LINUX_VERSION_NAMES) {
				if(NAME.startsWith(name)) {
					resolveLinuxOs(system);
				}
			}
		}
	}

	private void resolveDarwinOs(final OperatingSystem system) {

		final String[] versions = VERSION.split("\\.");

		system.setDistribution("OS X " + DARWIN_VERSION_MAPPING.get(parseInt(versions[0])) + " (" + VERSION + ")");
	}

	private void resolveLinuxOs(final OperatingSystem system) {

		// The most likely is to have a LSB compliant distro
		resolveNameFromLsbRelease(system);

		if(system.getDistribution() != null) {
			return;
		}

		// Generic Linux platform name
		resolveNameFromFile(system, FILE_ETC_SYSTEM_RELEASE);

		if(system.getDistribution() != null) {
			return;
		}

		final File dir = new File(FILE_ETC);

		if(dir.exists()) {

			// if generic 'system-release' file is not present, then try to find
			// another one
			resolveNameFromFile(system, findFile(dir, "-release"));

			if(system.getDistribution() != null) {
				return;
			}

			// if generic 'system-release' file is not present, then try to find
			// '_version'
			resolveNameFromFile(system, findFile(dir, "-_version"));

			if(system.getDistribution() != null) {
				return;
			}

			// try with /etc/issue file
			resolveNameFromFile(system, FILE_ETC_ISSUE);
		}

		if(system.getDistribution() != null) {
			return;
		}

		// if nothing found yet, looks for the version info
		final File fileVersion = new File(FILE_PROC_VERSION);

		if(fileVersion.exists()) {
			resolveNameFromFile(system, fileVersion.getAbsolutePath());
		}

		if(system.getDistribution() != null) {
			system.setDistribution(NAME);
		}
	}

	private void resolveMacOs(final OperatingSystem system) {

		final String[] versions = VERSION.split("\\.");

		final double version = parseDouble(versions[0] + "." + versions[1]);

		if(version < 10) {
			system.setDistribution("Mac OS " + VERSION);
		} else {
			system.setDistribution("OS X " + MAC_OS_VERSION_MAPPING.get(version) + " (" + VERSION + ")");
		}
	}

	private void resolveNameFromFile(final OperatingSystem system, final String filename) {

		if(filename == null) {
			return;
		}

		final File file = new File(filename);

		if(!file.exists()) {
			return;
		}

		String lastLine = null;

		try(Scanner scanner = new Scanner(file)) {

			int lineNb = 0;

			while(scanner.hasNextLine()) {

				final String line = scanner.nextLine();

				if(lineNb++ == 0) {
					lastLine = line;
				}

				if(line.startsWith(PROP_PRETTY_NAME)) {
					system.setDistribution(line.substring(13, line.length() - 1));
					break;
				}
			}

		} catch(final FileNotFoundException e) {
			e.printStackTrace();
		}

		if(system.getDistribution() == null) {
			system.setDistribution(lastLine);
		}
	}

	private void resolveNameFromLsbRelease(final OperatingSystem system) {

		final File file = new File(FILE_ETC_LSB_RELEASE);

		if(!file.exists()) {
			return;
		}

		String description = null;
		String codename = null;

		try(Scanner scanner = new Scanner(file)) {

			while(scanner.hasNextLine()) {

				final String line = scanner.nextLine();

				if(line.startsWith(PROP_DISTRIB_DESCRIPTION)) {
					description = line.replace(PROP_DISTRIB_DESCRIPTION + "=", "").replace("\"", "");
				} else if(line.startsWith(PROP_DISTRIB_CODENAME)) {
					codename = line.replace(PROP_DISTRIB_CODENAME + "=", "");
				}

				if(description != null && codename != null) {
					system.setDistribution(description + " (" + codename + ")");
					break;
				}
			}

		} catch(final FileNotFoundException e) {
			e.printStackTrace();
		}
	}
}
