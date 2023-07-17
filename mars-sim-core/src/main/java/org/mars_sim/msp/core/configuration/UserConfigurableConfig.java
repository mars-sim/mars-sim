/*
 * Mars Simulation Project
 * UserConfigurableConfig.java
 * @date 2022-08-24
 * @author Barry Evans
 */
package org.mars_sim.msp.core.configuration;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.XMLConstants;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.apache.commons.io.FileUtils;
import org.jdom2.Attribute;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.Namespace;
import org.jdom2.input.SAXBuilder;
import org.jdom2.input.sax.XMLReaderJDOMFactory;
import org.jdom2.input.sax.XMLReaderSchemaFactory;
import org.jdom2.input.sax.XMLReaders;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.mars_sim.msp.core.SimulationConfig;
import org.mars_sim.msp.core.SimulationFiles;
import org.mars_sim.msp.core.tool.Conversion;
import org.xml.sax.SAXException;

/**
 * This is a manager class of a category of UserConfigurable class. It provides
 * a means to manage a static collection of them and read/write to file system
 */
public abstract class UserConfigurableConfig<T extends UserConfigurable> {

	/** default logger. */
	private static final Logger logger = Logger.getLogger(UserConfigurableConfig.class.getName());

	private final String BACKUP = ".bak";

	public final String XML_EXTENSION = ".xml";
	
	private static Namespace xsiNameSpace;

	/**
	 * Saves an attribute to a Element if it is defined.
	 * 
	 * @param node
	 * @param attrName
	 * @param value
	 */
	protected static void saveOptionalAttribute(Element node, String attrName, String value) {
		if (value != null) {
			node.setAttribute(new Attribute(attrName, value));
		}
	}

	private String itemPrefix;
	private Map<String, T> knownItems = new HashMap<>();
	private String xsdName;

	/**
	 * Constructs a config of a UserConfigurable subclass.
	 * 
	 * @param itemPrefix The prefix to add when saving to an item,
	 */
	protected UserConfigurableConfig(String itemPrefix) {
		this.itemPrefix = itemPrefix;
	}

	protected void setXSDName(String xsd) {
		this.xsdName = xsd;

		// Have to pull it out of bundle
		SimulationConfig.instance().getBundledXML(itemPrefix + "/" + xsdName);
	}

	/**
	 * Loads the user defined configuration items.
	 */
	protected void loadUserDefined() {
		// Scan saved items folder
		File configDir = new File(SimulationFiles.getUserConfigDir());
		String[] found = configDir.list();
		if (found != null) {
			for (String userFile : configDir.list()) {
				if (userFile.startsWith(itemPrefix) && userFile.endsWith(XML_EXTENSION)) {
					try {
						loadItem(userFile, false);
					} catch (Exception e) {
						logger.warning("Problem loading user defined item in " + userFile + ": " + e.getMessage());
					}
				}
			}
		}
	}

	/**
	 * Loads the predefined defaults that are bundled in the code base.
	 * 
	 * @param predefined The predefined items that are bundled with the release.
	 */
	protected void loadDefaults(String[] predefined) {
		// Load predefined
		for (String name : predefined) {
			// Put a null entry to load on demand later
			knownItems.put(name, null);
		}
	}

	/**
	 * Loads a configuration from external or bundled XML.
	 * 
	 * @param name
	 * @return
	 */
	protected void loadItem(String file, boolean predefined) {
		InputStream contents = getRawConfigContents(file, predefined);
		if (contents == null) {
			throw new IllegalStateException("Can not find " + file);
		}

		Document doc;
		try {
			SAXBuilder builder = null;
			if (xsdName != null) {
				// Should we load the XSD schema just once and have the Schema a field.
				SchemaFactory schemafac = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
				Schema schema = schemafac
						.newSchema(new File(SimulationFiles.getXMLDir() + File.separator + itemPrefix, xsdName));
				XMLReaderJDOMFactory factory = new XMLReaderSchemaFactory(schema);
				builder = new SAXBuilder(factory);
				builder.setProperty(XMLConstants.ACCESS_EXTERNAL_DTD, "");
				builder.setProperty(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
			} else {
				builder = new SAXBuilder(XMLReaders.NONVALIDATING);
				builder.setProperty(XMLConstants.ACCESS_EXTERNAL_DTD, "");
				builder.setProperty(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
			}
			doc = builder.build(contents);
		} catch (JDOMException | SAXException | IOException e) {
			logger.log(Level.SEVERE, "Cannot build document: " + e.getMessage());
			throw new IllegalStateException("Problem parsing " + file, e);
		}

		T result = parseItemXML(doc, predefined);
		knownItems.put(result.getName(), result);
	}

	/**
	 * Gets a item by its name.
	 * 
	 * @param name
	 * @return
	 */
	public T getItem(String name) {
		T found = knownItems.get(name);
		if (found == null) {
			// Then it's a load on demand entry
			String file = getItemFilename(name);
			logger.config("Loading a template on demand: '" + itemPrefix + " " + name + "'.");
			loadItem(file, true);

			found = knownItems.get(name);
		}
		return found;
	}

	/**
	 * Adds an items that has been explicitly created to the control list.
	 * 
	 * @param newItem
	 */
	protected void addItem(T newItem) {
		knownItems.put(newItem.getName(), newItem);
	}

	/**
	 * Deletes the item.
	 * 
	 * @param name
	 */
	public void deleteItem(String name) {
		knownItems.remove(name);

		String filename = getItemFilename(name);
		File oldFile = new File(SimulationFiles.getUserConfigDir(), filename);
		if (oldFile.delete()) {
			logger.config("Deleted file " + oldFile.getAbsolutePath());
		}
	}

	/**
	 * Gets the filename for an item based on its name.
	 * 
	 * @param crewName
	 * @return
	 */
	protected String getItemFilename(String name) {
		// Replace spaces
		return itemPrefix + '_' + name.toLowerCase().replace(' ', '_') + XML_EXTENSION;
	}

	/**
	 * Estimates the configurable name from the file name.
	 * 
	 * @param configFile
	 * @return
	 */
	protected String getEstimateName(String configFile) {
		return Conversion.capitalize(configFile.replace('_', ' ').substring(0,
				configFile.length() - XML_EXTENSION.length()));
	}

	/**
	 * Gets the Location and streams the contents of the required configuration
	 * item.
	 * 
	 * @param filename Name of the item to locate
	 * @param bundled  Is it bundled with the application
	 * @throws FileNotFoundException
	 */
	protected InputStream getRawConfigContents(String filename, boolean bundled) {
		String path = "";

		if (bundled) { // For bundled
			path = SimulationFiles.getXMLDir() + File.separator + itemPrefix;

			// Bundled XML files need to be copied out of the CONF sub folder.
			// Must use the '/' for paths in the classpath.
			SimulationConfig.instance().getBundledXML(itemPrefix + "/" + filename);
		} else { // for user
			path = SimulationFiles.getUserConfigDir();
		}

		File f = new File(path, filename);
		if (!f.exists()) {
			return null;
		}
		if (f.exists() && f.canRead()) {
			try {
				return new FileInputStream(f);
			} catch (FileNotFoundException e) {
				logger.warning("Problem reading file " + f.getAbsolutePath() + ":" + e);
			}
		}

		return null;
	}

	/**
	 * Saves the XML document for this item.
	 *
	 * @param item The details to save
	 */
	public void saveItem(T item) {
		String storagePath = SimulationFiles.getUserConfigDir();

		String filename = getItemFilename(item.getName());
		File itemFile = new File(storagePath, filename);

		// Create save directory if it doesn't exist.
		if (!itemFile.getParentFile().exists()) {
			itemFile.getParentFile().mkdirs();
			logger.config(itemFile.getParentFile().getAbsolutePath() + " created successfully.");
		}

		if (itemFile.exists()) {
			File itemBackup = new File(storagePath, filename + BACKUP);
			try {
				if (Files.deleteIfExists(itemBackup.toPath())) {
					// Delete old backup file
					logger.config("Old " + itemBackup.getName() + " deleted.");
				}
			} catch (IOException e) {
				logger.log(Level.SEVERE, "Cannot delete " + itemBackup.getName() + ": " + e.getMessage());
			}

			try {
				// Back up the previous version of the crew xml file
				FileUtils.moveFile(itemFile, itemBackup);
				logger.config(itemFile.getName() + " --> " + itemBackup.getName());
			} catch (IOException e1) {
				logger.log(Level.SEVERE, "Cannot move " + itemBackup.getName() + ": " + e1.getMessage());
			}

			try {
				if (Files.deleteIfExists(itemFile.toPath())) {
					logger.config("Old " + itemFile.getName() + " deleted.");
				}

			} catch (IOException e) {
				logger.log(Level.SEVERE, "Cannot delete " + itemBackup.getName() + ": " + e.getMessage());
			}
		}

		if (!itemFile.exists()) {
			Document outputDoc = createItemDoc(item);

			// If XSD then add attributes
			if (xsdName != null) {
				Element root = outputDoc.getRootElement();
				Namespace nameSpace = getXSINameSpace();

				root.addNamespaceDeclaration(nameSpace);
				root.setAttribute("noNamespaceSchemaLocation", xsdName, nameSpace);
			}

			XMLOutputter fmt = new XMLOutputter();
			fmt.setFormat(Format.getPrettyFormat());

			try (FileOutputStream stream = new FileOutputStream(itemFile);
					OutputStreamWriter writer = new OutputStreamWriter(stream, "UTF-8")) {
				fmt.output(outputDoc, writer);
				logger.config("New " + itemFile.getName() + " created and saved.");
				stream.close();
			} catch (Exception e) {
				logger.log(Level.SEVERE, "Cannot create " + itemFile.getName() + e.getMessage());
			}
		}

		// Update or register new crew
		knownItems.put(item.getName(), item);
	}

	private static Namespace getXSINameSpace() {
		if (xsiNameSpace == null) {
			xsiNameSpace = Namespace.getNamespace("xsi", "http://www.w3.org/2001/XMLSchema-instance");
		}
		return xsiNameSpace;
	}

	/**
	 * Gets the names of the known items.
	 * 
	 * @return Alphabetically sorted names.
	 */
	public List<String> getItemNames() {
		List<String> names = new ArrayList<>(knownItems.keySet());
		Collections.sort(names);
		return names;
	}

	/**
	 * Converts an Item into am XML representation.
	 * 
	 * @param item Item to parse.
	 * @see #parseXML(Document, boolean)
	 * @return
	 */
	protected abstract Document createItemDoc(T item);

	/**
	 * Parses an XML document to create an item instance.
	 * 
	 * @param doc        Document of details
	 * @param predefined Is this item predefined or user defined.
	 * @see #createItemDoc(UserConfigurable)
	 * @return
	 */
	abstract protected T parseItemXML(Document doc, boolean predefined);
}
