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

import org.exoplatform.applications.ooplugin.WebDavConstants;
import org.exoplatform.applications.ooplugin.client.CommonProp;
import org.exoplatform.common.http.HTTPStatus;
import org.w3c.dom.Node;

/**
 * Created by The eXo Platform SAS
 * Author : Vitaly Guly <gavrikvetal@gmail.com>
 * @version $Id: $
 */

public class LastModifiedProp extends CommonProp {

  protected String lastModified = "";

  public LastModifiedProp() {
    this.propertyName = WebDavConstants.WebDavProp.GETLASTMODIFIED;
  }
  
  public boolean init(Node node) {
    if (status != HTTPStatus.OK) {
      return false;
    }
    lastModified = node.getTextContent();
    return false;
  }    
  
  public String getLastModified() {
    return lastModified;
  }
  
}
