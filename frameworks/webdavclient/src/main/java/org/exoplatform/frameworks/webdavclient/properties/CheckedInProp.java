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

package org.exoplatform.frameworks.webdavclient.properties;

import org.exoplatform.frameworks.webdavclient.Const;
import org.exoplatform.frameworks.webdavclient.XmlUtil;
import org.w3c.dom.Node;

/**
 * Created by The eXo Platform SAS
 * Author : Vitaly Guly <gavrikvetal@gmail.com>
 * @version $Id: $
 */

public class CheckedInProp extends CommonProp {
  
  protected boolean checkedIn = false;
  private String href = ""; 

  public CheckedInProp() {
    this.propertyName = Const.DavProp.CHECKEDIN;
  }
  
  public boolean init(Node node) {
    if (status != Const.HttpStatus.OK) {
      return false;
    }
    
    Node hrefN = XmlUtil.getChildNode(node, Const.DavProp.HREF);
    if (hrefN == null) {
      return false;
    }
    
    href = hrefN.getTextContent();
    checkedIn = true;    
    
    return true;
  }
  
  public boolean isCheckedIn() {
    return checkedIn;
  }
  
  public String getHref() {
    return href;
  }
  
}
