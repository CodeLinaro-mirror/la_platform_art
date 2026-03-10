/*
 * Copyright (C) 2026 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.ahat;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.android.ahat.heapdump.AhatClassObj;
import com.android.ahat.heapdump.AhatSnapshot;
import com.android.ahat.heapdump.HprofFormatException;
import com.android.ahat.heapdump.Parser;
import com.android.ahat.heapdump.Reachability;
import com.android.ahat.proguard.ProguardMap;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.ByteBuffer;
import org.junit.Test;

public class ParserTest {

  /**
   * Regression test for b/490200823 where AHAT stalls when reading an empty hprof.
   */
  @Test(expected = HprofFormatException.class)
  public void emptyHeapDump() throws IOException, HprofFormatException {
    // test empty ByteBuffer
    Parser.parseHeapDump(ByteBuffer.allocate(0), new ProguardMap());

    // test empty File
    File emptyFile = Files.createTempFile("empty", ".hprof").toFile();
    assertTrue(emptyFile.createNewFile());
    try {
      Parser.parseHeapDump(emptyFile, new ProguardMap());
    } finally {
      Files.deleteIfExists(emptyFile.toPath());
    }
  }

  @Test
  public void heapDumpAsByteBuffer() throws IOException, HprofFormatException {
    ByteBuffer hprof = TestDump.getResourceAsByteBuffer("test-dump.hprof");
    ProguardMap map = TestDump.getProguardMapFromResource("test-dump.map");
    AhatSnapshot snapshot = Parser.parseHeapDump(hprof, map);
    AhatClassObj main = TestDump.findClass(snapshot, "Main");
    assertNotNull(main);
  }

  @Test
  public void heapDumpAsFile() throws IOException, HprofFormatException {
    File hprof = TestDump.getResourceAsFile("test-dump.hprof");
    ProguardMap map = TestDump.getProguardMapFromResource("test-dump.map");

    try {
      // test default chunk size
      AhatSnapshot snapshot = Parser.parseHeapDump(hprof, map);
      AhatClassObj main = TestDump.findClass(snapshot, "Main");
      assertNotNull(main);

      // test small chunk size
      int chunkSize = 256;
      snapshot = Parser.parseHeapDump(hprof, map, chunkSize);
      main = TestDump.findClass(snapshot, "Main");
      assertNotNull(main);
    } finally {
      Files.deleteIfExists(hprof.toPath());
    }
  }
}
