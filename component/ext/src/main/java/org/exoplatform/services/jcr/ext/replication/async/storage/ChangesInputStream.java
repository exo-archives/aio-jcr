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
package org.exoplatform.services.jcr.ext.replication.async.storage;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.StreamCorruptedException;

/**
 * Created by The eXo Platform SAS.
 * 
 * <br/>Date: 03.02.2009
 * 
 * @author <a href="mailto:peter.nedonosko@exoplatform.com.ua">Peter Nedonosko</a>
 * @version $Id: ChangesInputStream.java 111 2008-11-11 11:11:11Z pnedonosko $
 */
public class ChangesInputStream extends ObjectInputStream {

  public ChangesInputStream(InputStream in) throws IOException {
    super(in);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected void readStreamHeader() throws IOException, StreamCorruptedException {
    // read nothing
  }

}
