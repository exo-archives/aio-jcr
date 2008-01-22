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
import java.util.Iterator;
import java.util.Set;

import javax.jcr.RepositoryException;
import javax.xml.namespace.QName;

import org.exoplatform.services.jcr.webdav.xml.WebDavNamespaceContext;

/**
 * Created by The eXo Platform SARL .<br/> 
 * Abstract WebDav Resource implementation
 * It is recommended to extend this class instead of implement Resource itself
 * @author Gennady Azarenkov
 * @version $Id: $
 */

public abstract class GenericResource implements Resource {

	protected final URI identifier;
	protected final int type;
	protected final WebDavNamespaceContext namespaceContext;
	
	protected static final Set<QName> PRESET_PROP = new HashSet<QName>();
	static {
	  PRESET_PROP.add(DISPLAYNAME);
	  PRESET_PROP.add(RESOURCETYPE);
	  PRESET_PROP.add(CREATIONDATE);
	}
	
	public GenericResource(final int type, final URI identifier, final WebDavNamespaceContext namespaceContext) {
		this.type = type;
		this.identifier = identifier;
		this.namespaceContext = namespaceContext;
	}
	
	/* (non-Javadoc)
	 * @see org.exoplatform.services.jcr.webdav.resource.Resource#getIdentifier()
	 */
	public final URI getIdentifier() {
		return identifier;
	}
	
	/* (non-Javadoc)
	 * @see org.exoplatform.services.jcr.webdav.resource.Resource#getType()
	 */
	public final int getType() {
		return type;
	}
	
	/* (non-Javadoc)
	 * @see org.exoplatform.services.jcr.webdav.resource.Resource#getProperties(boolean)
	 */
	public Set <HierarchicalProperty> getProperties(boolean namesOnly) throws RepositoryException {
		Set <HierarchicalProperty> props = new HashSet<HierarchicalProperty>();
		
		Iterator<QName> propIter = PRESET_PROP.iterator();
		while (propIter.hasNext()) {
		  QName propertyName = propIter.next();
		  
		  try {
		    props.add(namesOnly ? new HierarchicalProperty(propertyName) :
		      getProperty(propertyName));		    
		  } catch (Exception exc) {
//		    System.out.println("Unhandled exception. " + exc.getMessage());
//		    exc.printStackTrace();
		  }
		  
		}
		
		return props;
	}

	/* (non-Javadoc)
	 * @see org.exoplatform.services.jcr.webdav.resource.Resource#getNamespaceContext()
	 */
	public final WebDavNamespaceContext getNamespaceContext() {
		return namespaceContext;
	}

  public static HierarchicalProperty lockDiscovery(String token, String lockOwner, String timeOut) {    
    HierarchicalProperty lockDiscovery = new HierarchicalProperty(new QName("DAV:", "lockdiscovery")); 
    
    HierarchicalProperty activeLock = lockDiscovery.addChild(new HierarchicalProperty(new QName("DAV:", "activelock")));
    
    HierarchicalProperty lockType = activeLock.addChild(new HierarchicalProperty(new QName("DAV:", "locktype")));
      lockType.addChild(new HierarchicalProperty(new QName("DAV:", "write")));
      
    HierarchicalProperty lockScope = activeLock.addChild(new HierarchicalProperty(new QName("DAV:", "lockscope")));
      lockScope.addChild(new HierarchicalProperty(new QName("DAV:", "exclusive")));
      
    HierarchicalProperty depth = activeLock.addChild(new HierarchicalProperty(new QName("DAV:", "depth")));
      depth.setValue("Infinity");
      
    if (lockOwner != null) {
      HierarchicalProperty owner = activeLock.addChild(new HierarchicalProperty(new QName("DAV:", "owner")));
      owner.setValue(lockOwner);
    }
      
    HierarchicalProperty timeout = activeLock.addChild(new HierarchicalProperty(new QName("DAV:", "timeout")));
    timeout.setValue("Second-" + timeOut);
    //timeout.setValue("Second-" + Integer.MAX_VALUE);
    
    if (token != null) {
      HierarchicalProperty lockToken = activeLock.addChild(new HierarchicalProperty(new QName("DAV:", "locktoken")));
      HierarchicalProperty lockHref = lockToken.addChild(new HierarchicalProperty(new QName("DAV:", "href")));      
      lockHref.setValue(token);
    }
   
    return lockDiscovery;
  }
  
  protected HierarchicalProperty supportedLock() {
    HierarchicalProperty supportedLock = new HierarchicalProperty(new QName("DAV:", "supportedlock"));
    
    HierarchicalProperty lockEntry = new HierarchicalProperty(new QName("DAV:", "lockentry"));
    supportedLock.addChild(lockEntry);
    
    HierarchicalProperty lockScope = new HierarchicalProperty(new QName("DAV:", "lockscope"));
    lockScope.addChild(new HierarchicalProperty(new QName("DAV:", "exclusive")));
    lockEntry.addChild(lockScope);
    
    HierarchicalProperty lockType = new HierarchicalProperty(new QName("DAV:", "locktype"));
    lockType.addChild(new HierarchicalProperty(new QName("DAV:", "write")));
    lockEntry.addChild(lockType);
    
    return supportedLock;
  }  
  
  protected HierarchicalProperty supportedMethodSet() {
    HierarchicalProperty supportedMethodProp = new HierarchicalProperty(SUPPORTEDMETHODSET);

    supportedMethodProp.addChild(new HierarchicalProperty(new QName("DAV:", "supported-method"))).setAttribute("name", "PROPFIND");
    supportedMethodProp.addChild(new HierarchicalProperty(new QName("DAV:", "supported-method"))).setAttribute("name", "OPTIONS");
    supportedMethodProp.addChild(new HierarchicalProperty(new QName("DAV:", "supported-method"))).setAttribute("name", "DELETE");
    supportedMethodProp.addChild(new HierarchicalProperty(new QName("DAV:", "supported-method"))).setAttribute("name", "PROPPATCH");
    supportedMethodProp.addChild(new HierarchicalProperty(new QName("DAV:", "supported-method"))).setAttribute("name", "CHECKIN");
    supportedMethodProp.addChild(new HierarchicalProperty(new QName("DAV:", "supported-method"))).setAttribute("name", "CHECKOUT");
    supportedMethodProp.addChild(new HierarchicalProperty(new QName("DAV:", "supported-method"))).setAttribute("name", "REPORT");
    supportedMethodProp.addChild(new HierarchicalProperty(new QName("DAV:", "supported-method"))).setAttribute("name", "UNCHECKOUT");
    supportedMethodProp.addChild(new HierarchicalProperty(new QName("DAV:", "supported-method"))).setAttribute("name", "PUT");
    supportedMethodProp.addChild(new HierarchicalProperty(new QName("DAV:", "supported-method"))).setAttribute("name", "GET");
    supportedMethodProp.addChild(new HierarchicalProperty(new QName("DAV:", "supported-method"))).setAttribute("name", "HEAD");
    supportedMethodProp.addChild(new HierarchicalProperty(new QName("DAV:", "supported-method"))).setAttribute("name", "COPY");
    supportedMethodProp.addChild(new HierarchicalProperty(new QName("DAV:", "supported-method"))).setAttribute("name", "MOVE");
    supportedMethodProp.addChild(new HierarchicalProperty(new QName("DAV:", "supported-method"))).setAttribute("name", "VERSION-CONTROL");
    supportedMethodProp.addChild(new HierarchicalProperty(new QName("DAV:", "supported-method"))).setAttribute("name", "LABEL");
    supportedMethodProp.addChild(new HierarchicalProperty(new QName("DAV:", "supported-method"))).setAttribute("name", "LOCK");
    supportedMethodProp.addChild(new HierarchicalProperty(new QName("DAV:", "supported-method"))).setAttribute("name", "UNLOCK");
    
    return supportedMethodProp;

  }
}
