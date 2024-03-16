/*
 * Mars Simulation Project
 * HelpLibrary.java
 * @date 2024-03-16
 * @author Barry Evans
 */
package com.mars_sim.tools.helpgenerator;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;

import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import com.mars_sim.core.SimulationConfig;
import com.mars_sim.core.SimulationRuntime;
import com.mars_sim.core.Version;
import com.mars_sim.core.tool.ResourceCache;

/**
 * This class is responsible for creating a library of help documents on the local filesystem
 * The library consists of a static pages extracted fromt eh classpath and a set of generated files
 * from the SimulationConfig.
 */
public final class HelpLibrary {

    // The default starting page for the help
    public static final String STARTING_PAGE = "userguide.html";

    static final String VERSION_FILE = "generated_version.props";
    static final String GENERATED_DIR = "generated";

    private ResourceCache cache;

    /**
     * Create the libary of help files in a directory.
     * @param config The source of the configuration.
     * @param location Loction to create files.
     * @return The main entry poitn to the library.
     * @throws IOException 
     */
    public HelpLibrary(SimulationConfig config, File location)
                throws IOException {
        
        location.mkdirs();

        // Extract the static HTML files
        extractResourceFiles("/help", location);

        // Create generated files if version does not match 
        boolean generateHelp = true;
        var versionFile = new File(location, VERSION_FILE);
        if (versionFile.exists()) {
            // Load the version of the generated files
            try (FileInputStream source = new FileInputStream(versionFile)) {
                var generatedVersion = Version.fromStream(source);
                generateHelp = !generatedVersion.equals(SimulationRuntime.VERSION);
            }
        }

        if (generateHelp) {
            var gen = HelpGenerator.createHTMLInline(config);
            File output = new File(location, GENERATED_DIR);
            gen.generateAll(output);

            // Always upload the versino files
            try (FileOutputStream sink = new FileOutputStream(versionFile)) {
                SimulationRuntime.VERSION.store(sink);
            }
        }
    }

    /**
     * Extract the resource files from the classpath into the output directory.
     * This method has to handle the Classpath being physicla files, e.g. IDE and
     * classpath resoruces being in a JAR file, e.g. installed
     * @param resourcePath Find any resources that are under this path.
     * @param outputDir Destination directory.
     * @throws IOException
     */
    private void extractResourceFiles(String resourcePath, File outputDir)
                    throws  IOException {
        cache = new ResourceCache(outputDir, true);

        ClassLoader classLoader = HelpLibrary.class.getClassLoader();
        var resolver = new PathMatchingResourcePatternResolver(classLoader);

        var found = resolver.getResources("classpath*:" + resourcePath + "/**/*");
        for(var r : found) {
            var path = r.getURL().getPath();
            if ((path != null) && !path.endsWith("/")) {
                // Path will be schems:/../../../<inResourcePath>/a/b/c.f
                int idx = path.indexOf(resourcePath);
                var resourceName = path.substring(idx);
                var relativePath = resourceName.substring(resourcePath.length());
                
                cache.extractContent(resourceName, relativePath);
            }
        }
    }

    /**
     * Get the URI of a help page
     * @param name This is shortern name; if null then the default page is returned
     * @return
     */
    public URI getPage(String name) {
        if (name == null) {
            name = STARTING_PAGE;
        }
        var physicalHTML = new File(cache.getLocation(), name);
        return physicalHTML.toURI();
    }

    /**
     * Create the default help library
     * @param config
     * @return
     * @throws IOException
     */
    public static HelpLibrary createDefault(SimulationConfig config) throws IOException {
        File helpDir = new File(SimulationRuntime.getDataDir(), "help");
        return new HelpLibrary(config, helpDir);
    }
}
