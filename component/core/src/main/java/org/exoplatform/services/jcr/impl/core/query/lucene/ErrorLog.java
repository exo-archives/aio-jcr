/*
 * Copyright (C) 2003-2007 eXo Platform SAS.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
 */

package org.exoplatform.services.jcr.impl.core.query.lucene;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.exoplatform.services.jcr.dataflow.ItemState;
import org.exoplatform.services.log.ExoLogger;

/**
 * All changes that must be in index but interrupted by IOException are here.
 * Created by The eXo Platform SAS Author : Sergey Karpenko
 * <sergey.karpenko@exoplatform.com.ua>
 * 
 * @version $Id: $
 */

public class ErrorLog {
  public static final int    FILE_SIZE = 10;                                // Kb

  /**
   * Logger instance for this class
   */
  private static final Log   log       = ExoLogger.getLogger(RedoLog.class);

  public static final String REMOVE    = "rem";

  public static final String ADD       = "add";

  /**
   * The log file
   */
  private final File         logFile;

  /**
   * Writer to the log file
   */
  // private Writer out;
  private FileChannel        out;

  public ErrorLog(File log) throws IOException {
    // set file size;
    if (!log.exists()) {
      log.getParentFile().mkdirs();
      log.createNewFile();

      out = new FileOutputStream(log).getChannel();
      out.position(1024 * FILE_SIZE - 1);
      out.write(ByteBuffer.wrap(new byte[] { 0 }));
      out.position(0);
      out.force(false);
    } else {
      out = new FileOutputStream(log, true).getChannel();
    }
    logFile = log;
  }

  /**
   * Appends an action to the log.
   * 
   * @param action the action to append.
   * @throws IOException if the node cannot be written to the redo log.
   */
  void append(String action, String uuid) throws IOException {
    initOut();
    out.write(ByteBuffer.wrap((action + " " + uuid + "\n").getBytes()));
  }

  /**
   * Flushes all pending writes to the underlying file.
   * 
   * @throws IOException if an error occurs while writing.
   */
  void flush() throws IOException {
    if (out != null) {
      out.force(false);
    }
  }

  /**
   * Clears the redo log.
   * 
   * @throws IOException if the redo log cannot be cleared.
   */
  void clear() throws IOException {
    if (out != null) {
      out.truncate(0);
      out.close();
      out = new FileOutputStream(logFile).getChannel();
      out.position(1024 * FILE_SIZE - 1);
      out.write(ByteBuffer.wrap(new byte[] { 0 }));
      out.position(0);
      out.force(false);
    }
  }

  /**
   * Initializes the {@link #out} stream if it is not yet set.
   * 
   * @throws IOException if an error occurs while creating the output stream.
   */
  private void initOut() throws IOException {
    if (out == null) {
      FileOutputStream os = new FileOutputStream(logFile, false);
      out = os.getChannel();
    }
  }

  /**
   * Reads the log file .
   * 
   * @throws IOException if an error occurs while reading from the log file.
   */
  public List<String> readList() throws IOException {
    InputStream in = new FileInputStream(logFile);
    try {
      List<String> list = new ArrayList<String>();
      BufferedReader reader = new BufferedReader(new InputStreamReader(in));
      String line;
      while ((line = reader.readLine()) != null) {
        if (!line.matches("\\x00++")) {
          list.add(line);
        }
      }
      return list;

    } finally {
      if (in != null) {
        try {
          in.close();
        } catch (IOException e) {
          log.warn("Exception while closing error log: " + e.toString());
        }
      }
    }
  }

  public void exctractNotifyList(Set<String> rem, Set<String> add) throws IOException {
    List<String> list = readList();

    Iterator<String> it = list.iterator();
    while (it.hasNext()) {
      String[] str = it.next().split(" ");
      if (str.length == 2) {
        if (str[0].equals(ADD)) {
          add.add(str[1]);
        } else if (str[0].equals(REMOVE)) {
          rem.add(str[1]);
        }
      }
    }
  }

  public void logNotifyList(Set<String> removed, Set<String> added) throws IOException {
    try {
      if (!removed.isEmpty()) {
        Iterator<String> rem = removed.iterator();
        while (rem.hasNext()) {
          append(ErrorLog.REMOVE, rem.next());
        }
      }
      if (!added.isEmpty()) {
        Iterator<String> add = added.iterator();
        while (add.hasNext()) {
          append(ErrorLog.ADD, add.next());
        }
      }
    } finally {
      flush();
    }
  }

}
