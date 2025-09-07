package org.mars_sim.msp.core.sim.event;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

public final class JournalReader implements Closeable {
  private final DataInputStream in;

  public record Entry(String type, long tick, byte[] payload) {}

  public JournalReader(InputStream is) throws IOException {
    this.in = new DataInputStream(new BufferedInputStream(is));
    byte[] magic = in.readNBytes(5);
    if (magic.length != 5 || !"MSJR1".equals(new String(magic, StandardCharsets.US_ASCII))) {
      throw new IOException("Bad journal header");
    }
    // seed and tickMillis are currently not used here (they belong to launcher)
    long _seed = in.readLong();
    int _tickMillis = in.readInt();
  }

  public Optional<Entry> next() throws IOException {
    try {
      int typeLen = in.readUnsignedShort();
      String type = new String(in.readNBytes(typeLen), StandardCharsets.UTF_8);
      long tick = in.readLong();
      int payloadLen = in.readUnsignedShort();
      byte[] payload = in.readNBytes(payloadLen);
      return Optional.of(new Entry(type, tick, payload));
    } catch (EOFException eof) {
      return Optional.empty();
    }
  }

  @Override public void close() throws IOException { in.close(); }
}
