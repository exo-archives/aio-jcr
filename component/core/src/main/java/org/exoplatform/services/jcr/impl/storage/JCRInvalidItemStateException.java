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

import javax.jcr.InvalidItemStateException;

/**
 * Created by The eXo Platform SAS Author : Peter Nedonosko peter.nedonosko@exoplatform.com.ua
 * 24.12.2007
 * 
 * @author <a href="mailto:peter.nedonosko@exoplatform.com.ua">Peter Nedonosko</a>
 * @version $Id: JCRInvalidItemStateException.java 11907 2008-03-13 15:36:21Z ksm $
 */
public class JCRInvalidItemStateException extends InvalidItemStateException {

  private static final long serialVersionUID = -1061472145637688125L;

  private final String      itemId;

  private final int         itemState;

  public JCRInvalidItemStateException(String message) {
    super(message);
    this.itemId = null;
    this.itemState = -1;
  }

  public JCRInvalidItemStateException(String message, String itemId) {
    super(message);
    this.itemId = itemId;
    this.itemState = -1;
  }

  public JCRInvalidItemStateException(String message, String itemId, int itemState) {
    super(message);
    this.itemId = itemId;
    this.itemState = itemState;
  }

  public JCRInvalidItemStateException(String message, String itemId, Throwable e) {
    super(message, e);
    this.itemId = itemId;
    this.itemState = -1;
  }

  public JCRInvalidItemStateException(String message, String itemId, int itemState, Throwable e) {
    super(message, e);
    this.itemId = itemId;
    this.itemState = itemState;
  }

  public String getIdentifier() {
    return itemId;
  }

  public int getState() {
    return itemState;
  }
}
