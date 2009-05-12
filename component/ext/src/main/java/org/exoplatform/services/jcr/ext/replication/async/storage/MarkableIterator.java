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
package org.exoplatform.services.jcr.ext.replication.async.storage;

import java.io.IOException;
import java.util.Iterator;

/**
 * Created by The eXo Platform SAS.
 * 
 * @author <a href="mailto:anatoliy.bazko@exoplatform.com.ua">Anatoliy Bazko</a>
 * @version $Id: MarkableIterator.java 111 2008-11-11 11:11:11Z $
 */
public interface MarkableIterator<E> extends Iterator<E> {

  /**
   * Marks the current position.
   * 
   * @throws IOException
   *           If position was already marked.
   */
  void mark() throws IOException;

  /**
   * Repositions to the position at the time the <code>mark</code> method was last called and clears
   * current marked position.
   * 
   * @throws IOException
   *           if can't reposition
   */
  void reset() throws IOException;

}
