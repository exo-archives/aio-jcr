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

package org.exoplatform.services.webdav.common.resource.resourcedata;

import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.RepositoryException;

import org.exoplatform.services.webdav.DavConst;

/**
 * Created by The eXo Platform SAS
 * Author : Vitaly Guly <gavrikvetal@gmail.com>
 * @version $Id: $
 */

public class JcrFileResourceData extends AbstractResourceData {

  public JcrFileResourceData(Node resourceNode) throws RepositoryException {
    iscollection = false;
    
    name = resourceNode.getName();
    
    Node contentNode = resourceNode.getNode(DavConst.NodeTypes.JCR_CONTENT);

    if (contentNode.hasProperty(DavConst.NodeTypes.JCR_LASTMODIFIED)) {
      lastModified = contentNode.getProperty(DavConst.NodeTypes.JCR_LASTMODIFIED).getString();
    }
    
    contentType = contentNode.getProperty(DavConst.NodeTypes.JCR_MIMETYPE).getString();
    Property dataProperty = contentNode.getProperty(DavConst.NodeTypes.JCR_DATA);
    
    resourceInputStream = dataProperty.getStream();
    resourceLenght = dataProperty.getLength();
  }
  
}
