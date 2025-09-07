package org.mars_sim.msp.core.sim.hash;

import java.nio.ByteBuffer;

/** Minimal sink for building stable hashes from state snapshots. */
public interface HashSink {
  void putLong(long v);
  void putInt(int v);
  void putBytes(byte[] v);
  default void putString(String s) { putBytes(s.getBytes()); }
}
