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
package org.exoplatform.services.jcr.impl.storage.value.fs;

import java.io.IOException;

/**
 * Created by The eXo Platform SAS.
 * 
 * <br/>
 * Date: 03.04.2009
 * 
 * @author <a href="mailto:peter.nedonosko@exoplatform.com.ua">Peter Nedonosko</a>
 * @version $Id$
 */
public class FileLockException extends IOException {
  /**
   * serialVersionUID.
   */
  private static final long serialVersionUID = 5513012215532388738L;
  
  private final InterruptedException lockError;

  /**
   * FileLockException constructor.
   * 
   * @param s
   *          String message
   */
  public FileLockException(String s, InterruptedException lockError) {
    super(s);
    this.lockError = lockError;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Throwable getCause() {
    return lockError;
  }
}
