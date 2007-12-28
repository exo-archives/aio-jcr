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

package org.exoplatform.services.webdav.deltav.representation.versiontree;

import java.util.ArrayList;
import java.util.HashMap;

import javax.jcr.Item;
import javax.jcr.RepositoryException;

import org.exoplatform.services.webdav.WebDavService;
import org.exoplatform.services.webdav.common.representation.XmlResponseRepresentation;
import org.exoplatform.services.webdav.common.representation.read.PropFindRepresentationFactory;
import org.exoplatform.services.webdav.common.util.DavUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

/**
 * Created by The eXo Platform SAS
 * Author : Vitaly Guly <gavrikvetal@gmail.com>
 * @version $Id: $
 */

public class VersionTreeRepresentationFactory {
  
  public static final String XML_VERSIONTREE = "version-tree";
  
  public static final String XML_ALLPROP = "allprop";
  
  public static final String XML_PROP = "prop";
  
  public static XmlResponseRepresentation createResponseRepresentation(WebDavService webDavService, Document document, Item node, String href) throws RepositoryException {
    Node versionTree = DavUtil.getChildNode(document, XML_VERSIONTREE);
  
    Node allProp = DavUtil.getChildNode(versionTree, XML_ALLPROP);
    if (allProp != null) {
      return new AllPropVersionTreeResponseRepresentation(webDavService, href, (javax.jcr.Node)node);
    }
    
    
    Node props = DavUtil.getChildNode(versionTree, XML_PROP);
    
    HashMap<String, ArrayList<String>> properties = PropFindRepresentationFactory.getProperties(props);
    
    return new VersionTreeResponseRepresentation(webDavService, properties, href, (javax.jcr.Node)node);
  }

}
