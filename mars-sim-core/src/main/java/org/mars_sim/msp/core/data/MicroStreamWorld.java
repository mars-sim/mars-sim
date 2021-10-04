package org.mars_sim.msp.core.data;

import java.nio.file.Paths;
import java.util.Date;

import org.mars_sim.msp.core.SimulationFiles;

import one.microstream.storage.embedded.types.EmbeddedStorage;
import one.microstream.storage.embedded.types.EmbeddedStorageManager;

public class MicroStreamWorld {
	
	public MicroStreamWorld() {		
	}
	
	public static void main(final String[] args) {
		MicroStreamWorld.setup();
	}
	
	public static void setup() {
		// Application-specific root instance
		final DataRoot root = new DataRoot();
		
		System.out.println("0." + root);
		// Initialize a storage manager ("the database") with the given directory and defaults for everything else.
//		final EmbeddedStorageManager storageManager = EmbeddedStorage.start(root, Paths.get("MicroStreamData"));
		final EmbeddedStorageManager storageManager = EmbeddedStorage.start(root, Paths.get(SimulationFiles.getSaveDir()));
		
		// print the root to show its loaded content (stored in the last execution).
		System.out.println("1." + root);

		// Set content data to the root element, including the time to visualize changes on the next execution.
		root.setContent("" + new Date());
		root.prepContent();
		
		// print the root to show its loaded content (stored in the last execution).
		System.out.println("2." + root);
		
		// Store the modified root and its content.
		storageManager.storeRoot();

		// print the root to show its loaded content (stored in the last execution).
		System.out.println("3." + root);
		
		// Shutdown is optional as the storage concept is inherently crash-safe
//		storageManager.shutdown();
//		System.exit(0);
	}
}
