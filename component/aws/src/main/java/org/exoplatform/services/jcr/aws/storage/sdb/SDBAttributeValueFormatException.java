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
package org.exoplatform.services.jcr.aws.storage.sdb;


/**
 * Created by The eXo Platform SAS.
 * 
 * <br/>Date: 08.10.2008
 * 
 * @author <a href="mailto:peter.nedonosko@exoplatform.com.ua">Peter Nedonosko</a>
 * @version $Id$
 */
public class SDBAttributeValueFormatException extends SDBRepositoryException {

  /**
   * SDBAttributeValueFormatException constructor.
   * 
   * @param message
   *          error description.
   */
  public SDBAttributeValueFormatException(String message) {
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
  public SDBAttributeValueFormatException(String message, Exception rootCause) {
    super(message, rootCause);
  }
}
