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
package org.exoplatform.services.jcr.ext.replication.storage;

import java.io.Closeable;
import java.io.IOException;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Created by The eXo Platform SAS.
 * 
 * <br/>
 * Date: 03.02.2009
 * 
 * @author <a href="mailto:peter.nedonosko@exoplatform.com.ua">Peter Nedonosko</a>
 * @version $Id: ResourcesHolder.java 111 2008-11-11 11:11:11Z pnedonosko $
 */
public class ResourcesHolder {

  /**
   * The queue of Closeable resources.
   */
  private final Queue<Closeable> resources = new ConcurrentLinkedQueue<Closeable>();

  /**
   * Add <code>Closeable</code> resource to the holder.
   * 
   * @param closeable
   *          Closeable
   */
  public void add(Closeable closeable) {
    resources.add(closeable);
  }

  /**
   * Close <code>Closeable</code> resource and remove it from the holder.
   * 
   * @throws IOException
   *           if close error occurs
   */
  public void close() throws IOException {
    Closeable c = resources.poll();
    while (c != null) {
      c.close();
      c = resources.poll();
    }
  }

}
