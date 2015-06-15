package com.jme3x.jfx.util.os;

/**
 * @author Ronn
 */
public class OperatingSystem {

	/** name of the operating system */
	private String name;
	/** version of the operating system kernel */
	private String version;
	/** operating system architecture */
	private String arch;
	/** distribution name of the operating system */
	private String distribution;

	public OperatingSystem() {
		final OperatingSystemResolver resolver = new OperatingSystemResolver();
		resolver.resolve(this);
	}

	/**
	 * @return operating system architecture.
	 */
	public String getArch() {
		return arch;
	}

	/**
	 * @return distribution name of the operating system.
	 */
	public String getDistribution() {
		return distribution;
	}

	/**
	 * @return name of the operating system.
	 */
	public String getName() {
		return name;
	}

	/**
	 * @return version of the operating system kernel.
	 */
	public String getVersion() {
		return version;
	}

	/**
	 * @param arch operating system architecture.
	 */
	public void setArch(final String arch) {
		this.arch = arch;
	}

	/**
	 * @param platform distribution name of the operating system.
	 */
	public void setDistribution(final String platform) {
		this.distribution = platform;
	}

	/**
	 * @param name name of the operating system.
	 */
	public void setName(final String name) {
		this.name = name;
	}

	/**
	 * @param version version of the operating system kernel.
	 */
	public void setVersion(final String version) {
		this.version = version;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + " [name=" + name + ", version=" + version + ", arch=" + arch + ", distribution=" + distribution + "]";
	}
}
