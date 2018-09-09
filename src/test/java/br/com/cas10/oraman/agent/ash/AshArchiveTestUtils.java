package br.com.cas10.oraman.agent.ash;

import static com.google.common.base.Preconditions.checkState;
import static com.google.common.base.StandardSystemProperty.JAVA_IO_TMPDIR;

import br.com.cas10.oraman.OramanProperties;
import com.google.common.collect.ImmutableList;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

class AshArchiveTestUtils {

  static final Path ARCHIVE_PATH = Paths.get(JAVA_IO_TMPDIR.value(), "oraman");

  static void createArchiveDir() throws IOException {
    Files.createDirectories(ARCHIVE_PATH);
  }

  static void deleteArchiveDir() throws IOException {
    Files.deleteIfExists(ARCHIVE_PATH);
  }

  static OramanProperties newOramanProperties() {
    OramanProperties properties = new OramanProperties();
    properties.getArchive().setDir(ARCHIVE_PATH.toString());
    return properties;
  }

  static AshSnapshot newSnapshot(long timestamp) {
    return new AshSnapshot(timestamp, ImmutableList.of(), 10);
  }

  static void writeSnapshots(String dataFileName, long... timestamps) throws IOException {
    Path dataFilePath = ARCHIVE_PATH.resolve(dataFileName);
    checkState(Files.notExists(dataFilePath), "Already exists: %s", dataFilePath);

    try (OutputStream fos = Files.newOutputStream(dataFilePath);
        ObjectOutputStream oos = new ObjectOutputStream(fos)) {
      oos.writeInt(timestamps.length);
      for (Long timestamp : timestamps) {
        oos.writeObject(newSnapshot(timestamp));
      }
    }
  }
}
