/*
 * Copyright (C) 2003-2009 eXo Platform SAS.
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
package org.exoplatform.services.jcr.ext.replication.async.merge;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;

import org.exoplatform.services.jcr.dataflow.ItemState;
import org.exoplatform.services.jcr.dataflow.ItemStateChangesLog;
import org.exoplatform.services.jcr.dataflow.TransactionChangesLog;
import org.exoplatform.services.jcr.ext.replication.async.storage.ChangesFile;
import org.exoplatform.services.jcr.ext.replication.async.storage.ChangesLogStorage;
import org.exoplatform.services.jcr.ext.replication.async.storage.Member;

/**
 * Created by The eXo Platform SAS.
 * 
 * <br/>Date: 08.01.2009
 * 
 * @author <a href="mailto:peter.nedonosko@exoplatform.com.ua">Peter Nedonosko</a>
 * @version $Id$
 */
public class TesterChangesStorage<T extends ItemState> extends ChangesLogStorage<T> {

  public TesterChangesStorage(Member member) {
    super(new ArrayList<ChangesFile>(), member);
  }

  public void addLog(ItemStateChangesLog log) throws IOException {
    File ch = File.createTempFile("test", "-" + this.storage.size());
    ch.deleteOnExit();

    ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(ch));
    TransactionChangesLog tlog = (TransactionChangesLog) log;
    out.writeObject(tlog);

    this.storage.add(new ChangesFile(ch, "", System.currentTimeMillis()));
  }
}
