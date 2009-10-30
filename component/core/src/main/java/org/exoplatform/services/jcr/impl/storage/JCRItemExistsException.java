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
package org.exoplatform.services.jcr.impl.storage;

import javax.jcr.ItemExistsException;

/**
 * Created by The eXo Platform SAS. <br/>
 * Date: 29.10.2009
 * 
 * @author <a href="mailto:alex.reshetnyak@exoplatform.com.ua">Alex
 *         Reshetnyak</a>
 * @version $Id: JCRItemExistsException.java 452 2009-10-30 11:11:43Z
 *          areshetnyak $
 */
public class JCRItemExistsException extends ItemExistsException {
  /**
   * serialVersionUID - autogenerated.
   */
  private static final long serialVersionUID = -5201442796959443584L;

  /**
   * Context Item Id.
   */
  private final String      itemId;

  /**
   * Context Item state.
   */
  private final int         itemState;

  /**
   * JCRItemExistsException constructor.
   * 
   * @param message - context message
   */
  public JCRItemExistsException(String message) {
    super(message);
    this.itemId = null;
    this.itemState = -1;
  }

  /**
   * JCRItemExistsException constructor.
   * 
   * @param message - context message
   * @param itemId - context Item id
   */
  public JCRItemExistsException(String message, String itemId) {
    super(message);
    this.itemId = itemId;
    this.itemState = -1;
  }

  /**
   * JCRItemExistsException constructor.
   * 
   * @param message - context message
   * @param itemId - context Item id
   * @param itemState - context Item state
   */
  public JCRItemExistsException(String message, String itemId, int itemState) {
    super(message);
    this.itemId = itemId;
    this.itemState = itemState;
  }

  /**
   * JCRItemExistsException constructor.
   * 
   *@param message - context message
   * @param itemId - context Item id
   * @param e - cause error
   */
  public JCRItemExistsException(String message, String itemId, Throwable e) {
    super(message, e);
    this.itemId = itemId;
    this.itemState = -1;
  }

  /**
   * JCRItemExistsException constructor.
   * 
   * @param message - context message
   * @param itemId - context Item id
   * @param itemState - context Item state
   * @param e - cause error
   */
  public JCRItemExistsException(String message, String itemId, int itemState, Throwable e) {
    super(message, e);
    this.itemId = itemId;
    this.itemState = itemState;
  }

  /**
   * Get context Item Id.
   * 
   * @return String Item id
   */
  public String getIdentifier() {
    return itemId;
  }

  /**
   * Get context Item state.
   * 
   * @return int state
   */
  public int getState() {
    return itemState;
  }
}
