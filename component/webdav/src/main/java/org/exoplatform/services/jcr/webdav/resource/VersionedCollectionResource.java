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

package org.exoplatform.services.jcr.webdav.resource;

import java.net.URI;

import javax.jcr.AccessDeniedException;
import javax.jcr.Node;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.xml.namespace.QName;

import org.exoplatform.common.util.HierarchicalProperty;
import org.exoplatform.services.jcr.webdav.xml.WebDavNamespaceContext;

/**
 * Created by The eXo Platform SARL .<br/> 
 * @author Gennady Azarenkov
 * @version $Id: $
 */

public class VersionedCollectionResource extends CollectionResource implements
		VersionedResource {

	public VersionedCollectionResource(URI identifier, Node node,
			WebDavNamespaceContext namespaceContext)
			throws IllegalResourceTypeException, RepositoryException {
		super(VERSIONED_COLLECTION, identifier, node, namespaceContext);
		if(!node.isNodeType("mix:versionable"))
			throw new IllegalResourceTypeException("Node type is not applicable for Versioned FILE resource "+node.getPath());
	}

	public VersionHistoryResource getVersionHistory()  throws RepositoryException, 
	IllegalResourceTypeException { 
		return new VersionHistoryResource(versionHistoryURI(), node.getVersionHistory(), this, namespaceContext);
	}

	protected final URI versionHistoryURI() {
		return URI.create(identifier.toASCIIString() + "?vh");
	}
	
  @Override
  public HierarchicalProperty getProperty(QName name) throws PathNotFoundException, AccessDeniedException, RepositoryException {    
    if (name.equals(ISVERSIONED)) {
      return new HierarchicalProperty(name, "1");
    } else if (name.equals(CHECKEDIN)) {
      if (node.isCheckedOut()) {
        throw new PathNotFoundException();
      }
      
      String checkedInHref = identifier.toASCIIString() + "?version=" + node.getBaseVersion().getName();    
      HierarchicalProperty checkedIn = new HierarchicalProperty(name);      
      checkedIn.addChild(new HierarchicalProperty(new QName("DAV:", "href"), checkedInHref));      
      return checkedIn;
    
    } else if (name.equals(CHECKEDOUT)) {
      if (!node.isCheckedOut()) {
        throw new PathNotFoundException();
      }     
      return new HierarchicalProperty(name);
    } else if (name.equals(VERSIONNAME)) {
      return new HierarchicalProperty(name, node.getBaseVersion().getName());
    }
    
    return super.getProperty(name);
  }	

}
