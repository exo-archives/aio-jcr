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

package org.exoplatform.applications.ooplugin.props;


import org.exoplatform.applications.ooplugin.dav.Const;
import org.w3c.dom.Node;

/**
 * Created by The eXo Platform SAS
 * Author : Vitaly Guly <gavrikvetal@gmail.com>
 * @version $Id: $
 */

public class CreatorDisplayNameProp extends CommonProp {

  protected String creatorDisplayName = "";
  
  public CreatorDisplayNameProp() {
    this.propertyName = Const.DavProp.CREATORDISPLAYNAME;
  }
  
  public boolean init(Node node) {
    if (status != Const.HttpStatus.OK) {
      return false;
    }
    creatorDisplayName = node.getTextContent();
    return true;
  }
  
  public void setCreatorDisplayName(String creatorDisplayName) {
    this.creatorDisplayName = creatorDisplayName;
  }
  
  public String getCreatorDisplayName() {
    return creatorDisplayName;
  }
  
  
}
