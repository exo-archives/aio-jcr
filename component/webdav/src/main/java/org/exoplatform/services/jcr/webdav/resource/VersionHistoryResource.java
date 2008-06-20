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
import java.util.HashSet;
import java.util.Set;

import javax.jcr.AccessDeniedException;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.jcr.version.Version;
import javax.jcr.version.VersionHistory;
import javax.jcr.version.VersionIterator;
import javax.xml.namespace.QName;

import org.exoplatform.common.util.HierarchicalProperty;
import org.exoplatform.services.jcr.webdav.xml.WebDavNamespaceContext;

/**
 * Created by The eXo Platform SARL .<br/> 
 * @author Gennady Azarenkov
 * @version $Id: $
 */

public class VersionHistoryResource extends GenericResource {
	
	protected final VersionHistory versionHistory;
	
	//protected final URI versionedResourceID;
	protected final VersionedResource versionedResource;
	
	public VersionHistoryResource(final URI identifier, VersionHistory versionHistory,
			//final URI versionedResourceID,
	    final VersionedResource versionedResource,
			final WebDavNamespaceContext namespaceContext) 
	throws IllegalResourceTypeException, RepositoryException {
		super(VERSION_HISTORY, identifier, namespaceContext);
		this.versionHistory = versionHistory;
		this.versionedResource = versionedResource;
	}

	public HierarchicalProperty getProperty(QName name) throws PathNotFoundException,
			AccessDeniedException, RepositoryException {
		// TODO Auto-generated method stub
		return null;
	}

	public final boolean isCollection() {
		return false;
	}

	public Set <VersionResource> getVersions() throws RepositoryException,
	IllegalResourceTypeException {
		Set <VersionResource> resources = new HashSet <VersionResource>();
		VersionIterator versions = versionHistory.getAllVersions();
		while(versions.hasNext()) {
			Version version = versions.nextVersion();
			if ("jcr:rootVersion".equals(version.getName())) {
			  
			  continue;
			}
			resources.add(new VersionResource(versionURI(version.getName()), versionedResource, version, namespaceContext));
		}
		return resources;
	}
	
	public VersionResource getVersion(String name) throws RepositoryException,
	IllegalResourceTypeException {
		return new VersionResource(versionURI(name), versionedResource, versionHistory.getVersion(name), namespaceContext);
	}

	protected final URI versionURI(String versionName) {
		return URI.create(versionedResource.getIdentifier().toASCIIString() + "?version=" + versionName);
	}
	
}
