package org.mars_sim.msp.core.sim.event;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

/** Very small binary journal format for deterministic replay. */
public final class JournalWriter implements Closeable, Flushable {
  private final DataOutputStream out;

  public JournalWriter(OutputStream os, JournalHeader header) throws IOException {
    this.out = new DataOutputStream(new BufferedOutputStream(os));
    out.write(header.toBytes());
  }

  /** Append one event. Format: [2B typeLen][type][8B tick][2B payloadLen][payload]. */
  public void append(SimEvent ev, byte[] payload) throws IOException {
    byte[] type = ev.type().getBytes(StandardCharsets.UTF_8);
    out.writeShort(type.length);
    out.write(type);
    out.writeLong(ev.tick());
    out.writeShort(payload.length);
    out.write(payload);
  }

  @Override public void flush() throws IOException { out.flush(); }
  @Override public void close() throws IOException { out.flush(); out.close(); }
}
