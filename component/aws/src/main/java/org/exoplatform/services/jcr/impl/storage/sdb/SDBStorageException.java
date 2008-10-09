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
package org.exoplatform.services.jcr.impl.storage.sdb;

import com.amazonaws.sdb.AmazonSimpleDBException;

/**
 * Created by The eXo Platform SAS.
 * 
 * <br/>Date: 03.10.2008
 * 
 * @author <a href="mailto:peter.nedonosko@exoplatform.com.ua">Peter Nedonosko</a>
 * @version $Id: SDBRepositoryException.java 111 2008-11-11 11:11:11Z pnedonosko $
 */
public class SDBStorageException extends SDBRepositoryException {

  /**
   * SDBRepositoryException constructor.
   * 
   * @param message
   *          error description.
   */
  public SDBStorageException(String message) {
    super(message);
  }
  
  /**
   * SDBRepositoryException constructor.
   * 
   * @param message
   *          error description.
   * @param rootCause
   *          AmazonSimpleDBException
   */
  public SDBStorageException(String message, AmazonSimpleDBException rootCause) {
    super(message, rootCause);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String getMessage() {
    final AmazonSimpleDBException sdbErr = (AmazonSimpleDBException) super.rootCause;
    return super.getMessage() + ". Response Status Code: " + sdbErr.getStatusCode()
        + "; Error Code: " + sdbErr.getErrorCode() + "; Error Type: " + sdbErr.getErrorType()
        + "; Request ID: " + sdbErr.getRequestId(); // + "; XML: " + sdbErr.getXML()
  }
}
