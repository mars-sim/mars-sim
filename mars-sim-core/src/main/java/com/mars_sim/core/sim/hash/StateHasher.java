package org.mars_sim.msp.core.sim.hash;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/** Stable state hasher. TODO(mars-sim): connect to real world snapshot. */
public final class StateHasher {
  private final MessageDigest md;

  public StateHasher() {
    try { md = MessageDigest.getInstance("SHA-256"); }
    catch (NoSuchAlgorithmException e) { throw new RuntimeException(e); }
  }

  public void update(HashSink sinkLogic) {
    // In real integration, pass a lambda that writes state into the sink.
  }

  public byte[] digest() { return md.digest(); }
}
