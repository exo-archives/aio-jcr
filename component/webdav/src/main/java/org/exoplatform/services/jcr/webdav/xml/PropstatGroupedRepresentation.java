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

package org.exoplatform.services.jcr.webdav.xml;

import java.security.AccessControlException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.jcr.AccessDeniedException;
import javax.jcr.ItemNotFoundException;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.xml.namespace.QName;

import org.exoplatform.common.util.HierarchicalProperty;
import org.exoplatform.services.jcr.access.PermissionType;
import org.exoplatform.services.jcr.webdav.WebDavStatus;
import org.exoplatform.services.jcr.webdav.resource.Resource;
import org.exoplatform.services.jcr.webdav.util.PropertyConstants;

/**
 * Created by The eXo Platform SARL .<br/>
 * 
 * @author Gennady Azarenkov
 * @version $Id$
 */

public class PropstatGroupedRepresentation {

  protected final Map<String, Set<HierarchicalProperty>> propStats;

  protected Set<QName>                                   propNames = null;

  protected final boolean                                namesOnly;

  protected final Resource                               resource;
  
  protected final Session                                session;

  public PropstatGroupedRepresentation(final Resource resource,
                                       final Set<QName> propNames,
                                       boolean namesOnly,
                                       final Session session) throws RepositoryException {
    this.session = session;
    this.namesOnly = namesOnly;
    this.resource = resource;
    this.propStats = new HashMap<String, Set<HierarchicalProperty>>();

    this.propNames = propNames;

    if (propNames != null) {
      this.propNames = new HashSet();
      Iterator<QName> propertyNameIter = propNames.iterator();
      while (propertyNameIter.hasNext()) {
        QName property = propertyNameIter.next();
        this.propNames.add(property);
      }
    }
  }
  
  public PropstatGroupedRepresentation(final Resource resource,
                                       final Set<QName> propNames,
                                       boolean namesOnly) throws RepositoryException {
    this(resource, propNames, namesOnly, null);
  }

  public final Map<String, Set<HierarchicalProperty>> getPropStats() throws RepositoryException {
    String statname = WebDavStatus.getStatusDescription(WebDavStatus.OK);
    if (propNames == null) {
      propStats.put(statname, resource.getProperties(namesOnly));
    } else {

      for (QName propName : propNames) {
        HierarchicalProperty prop = new HierarchicalProperty(propName);
        try {

          if(propName.equals(PropertyConstants.IS_READ_ONLY) && session != null){
            if(isReadOnly()){
              prop.setValue("1");
            } else {
              prop.setValue("0");
            }            
            statname = WebDavStatus.getStatusDescription(WebDavStatus.OK);
          } else {
            prop = resource.getProperty(propName);
            statname = WebDavStatus.getStatusDescription(WebDavStatus.OK);
          }

        } catch (AccessDeniedException e) {
          statname = WebDavStatus.getStatusDescription(WebDavStatus.FORBIDDEN);
          e.printStackTrace();
        } catch (ItemNotFoundException e) {
          statname = WebDavStatus.getStatusDescription(WebDavStatus.NOT_FOUND);
          e.printStackTrace();

        } catch (PathNotFoundException e) {
          statname = WebDavStatus.getStatusDescription(WebDavStatus.NOT_FOUND);

        } catch (RepositoryException e) {
          statname = WebDavStatus.getStatusDescription(WebDavStatus.INTERNAL_SERVER_ERROR);
        }

        if (!propStats.containsKey(statname)) {
          propStats.put(statname, new HashSet<HierarchicalProperty>());
        }

        Set<HierarchicalProperty> propSet = propStats.get(statname);
        propSet.add(prop);
      }
    }
    return propStats;
  }
  
  private boolean isReadOnly() {
    
    String resourcePath = resource.getIdentifier().getPath();
    String workspace = session.getWorkspace().getName();
    String path = resourcePath.substring(resourcePath.indexOf(workspace) + workspace.length());
      
    try {
      session.checkPermission(path, PermissionType.READ);
    } catch (AccessControlException e1) {      
      return false;
    } catch (RepositoryException e1) {
      return false;
    }
    
    // Node must not have any permission except "read" so checking for any other permission 
    // must throw AccessControlException
      try{
        session.checkPermission(path, PermissionType.SET_PROPERTY);
        return false;
      } catch (AccessControlException e) {
        return true;
      } catch (RepositoryException e) {
        return false;
      }
  }

}
