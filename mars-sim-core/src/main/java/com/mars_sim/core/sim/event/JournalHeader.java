package org.mars_sim.msp.core.sim.event;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public record JournalHeader(long seed, int tickMillis) {
  public static final byte[] MAGIC = "MSJR1".getBytes(StandardCharsets.US_ASCII);

  public byte[] toBytes() {
    ByteBuffer buf = ByteBuffer.allocate(5 + Long.BYTES + Integer.BYTES);
    buf.put(MAGIC).putLong(seed).putInt(tickMillis);
    return buf.array();
  }
}
