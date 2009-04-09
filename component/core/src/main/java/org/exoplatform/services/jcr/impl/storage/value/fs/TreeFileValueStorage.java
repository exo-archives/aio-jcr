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
package org.exoplatform.services.jcr.impl.storage.value.fs;

import java.io.File;
import java.io.IOException;

import org.exoplatform.services.jcr.impl.util.io.FileCleaner;
import org.exoplatform.services.jcr.storage.value.ValueIOChannel;

/**
 * Created by The eXo Platform SAS
 * 
 * @author <a href="mailto:peter.nedonosko@exoplatform.com.ua">Peter Nedonosko</a>
 * @version $Id$
 */
public class TreeFileValueStorage extends FileValueStorage {

  protected class TreeFileCleaner extends FileCleaner {
    @Override
    public synchronized void addFile(File file) {
      super.addFile(new TreeFile(file.getAbsolutePath(), cleaner, rootDir));
    }
  }

  public TreeFileValueStorage() {
    this.cleaner = new TreeFileCleaner(); // TODO use container cleaner
  }

  @Override
  public ValueIOChannel openIOChannel() throws IOException {
    return new TreeFileIOChannel(rootDir, cleaner, getId(), resources);
  }
}
