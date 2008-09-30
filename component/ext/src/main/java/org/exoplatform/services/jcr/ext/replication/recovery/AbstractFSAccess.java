/*
 * Copyright (C) 2003-2008 eXo Platform SAS.
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
package org.exoplatform.services.jcr.ext.replication.recovery;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;

/**
 * Created by The eXo Platform SAS.
 * 
 * @author <a href="mailto:alex.reshetnyak@exoplatform.com.ua">Alex Reshetnyak</a>
 * @version $Id$
 */

public class AbstractFSAccess {
  public static final String DATA_DIR_NAME       = "data";

  public static final String PREFIX_REMOVED_DATA = "---";

  public static final String PREFIX_CHAR         = "-";

  public static final String REMOVED_SUFFIX      = ".remove";

  protected static final int BUFFER_1KB          = 1024;

  protected static final int BUFFER_8X           = 8;

  protected static final int BUFFER_20X          = 20;

  protected File getAsFile(InputStream is) throws IOException {
    byte[] buf = new byte[BUFFER_1KB * BUFFER_20X];

    File tempFile = File.createTempFile("" + System.currentTimeMillis(), "" + System.nanoTime());
    FileOutputStream fos = new FileOutputStream(tempFile);
    int len;

    while ((len = is.read(buf)) > 0)
      fos.write(buf, 0, len);

    fos.flush();
    fos.close();

    return tempFile;
  }

  protected File getAsFile(ObjectInputStream ois, long fileSize) throws IOException {
    int bufferSize = BUFFER_1KB * BUFFER_8X;
    byte[] buf = new byte[bufferSize];

    File tempFile = File.createTempFile("" + System.currentTimeMillis(), "" + System.nanoTime());
    FileOutputStream fos = new FileOutputStream(tempFile);
    long readBytes = fileSize;

    while (readBytes > 0) {
      if (readBytes >= bufferSize) {
        ois.readFully(buf);
        fos.write(buf);
      } else if (readBytes < bufferSize) {
        ois.readFully(buf, 0, (int) readBytes);
        fos.write(buf, 0, (int) readBytes);
      }
      readBytes -= bufferSize;
    }

    fos.flush();
    fos.close();

    return tempFile;
  }
}
