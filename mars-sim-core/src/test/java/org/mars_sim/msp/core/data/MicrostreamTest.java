package org.mars_sim.msp.core.data;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import one.microstream.storage.embedded.types.EmbeddedStorage;
import one.microstream.storage.embedded.types.EmbeddedStorageManager;

public class MicrostreamTest {

  @Test
  void test001() {
    final MicrostreamRoot value = new MicrostreamRoot();
    value.setValue("HelloWorld");
    final EmbeddedStorageManager storageManager = EmbeddedStorage.start();
    storageManager.setRoot(value);
    storageManager.storeRoot();
    storageManager.shutdown();
  }

  @Test
  void test002() {
    final EmbeddedStorageManager storageManager = EmbeddedStorage.start();
    final Object                 root           = storageManager.root();
    Assertions.assertTrue(root instanceof MicrostreamRoot);
    MicrostreamRoot helloWorld = (MicrostreamRoot) root;
    Assertions.assertEquals("HelloWorld", helloWorld.getValue());
    storageManager.shutdown();
  }
}
