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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.jcr.AccessDeniedException;
import javax.jcr.ItemNotFoundException;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.xml.namespace.QName;

import org.exoplatform.common.http.HTTPStatus;
import org.exoplatform.common.util.HierarchicalProperty;
import org.exoplatform.services.jcr.webdav.WebDavConst;
import org.exoplatform.services.jcr.webdav.resource.Resource;

/**
 * Created by The eXo Platform SARL .<br/>
 * 
 * @author Gennady Azarenkov
 * @version $Id: $
 */

public class PropstatGroupedRepresentation {

  protected final Map<String, Set<HierarchicalProperty>> propStats;

  protected Set<QName>                                   propNames = null;

  protected final boolean                                namesOnly;

  protected final Resource                               resource;

  public PropstatGroupedRepresentation(final Resource resource,
                                       final Set<QName> propNames,
                                       boolean namesOnly) throws RepositoryException {
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

  public final Map<String, Set<HierarchicalProperty>> getPropStats() throws RepositoryException {
    String statname = WebDavConst.getStatusDescription(HTTPStatus.OK);
    if (propNames == null) {
      propStats.put(statname, resource.getProperties(namesOnly));
    } else {

      for (QName propName : propNames) {
        HierarchicalProperty prop = new HierarchicalProperty(propName);
        try {
          prop = resource.getProperty(propName);
          statname = WebDavConst.getStatusDescription(HTTPStatus.OK);

        } catch (AccessDeniedException e) {
          statname = WebDavConst.getStatusDescription(HTTPStatus.FORBIDDEN);
          e.printStackTrace();
        } catch (ItemNotFoundException e) {
          statname = WebDavConst.getStatusDescription(HTTPStatus.NOT_FOUND);
          e.printStackTrace();

        } catch (PathNotFoundException e) {
          statname = WebDavConst.getStatusDescription(HTTPStatus.NOT_FOUND);

        } catch (RepositoryException e) {
          statname = WebDavConst.getStatusDescription(HTTPStatus.INTERNAL_ERROR);
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

}
