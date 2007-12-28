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

package org.exoplatform.services.webdav.common.representation;

import java.util.HashMap;
import java.util.Iterator;

import javax.jcr.NamespaceRegistry;
import javax.jcr.RepositoryException;
import javax.xml.namespace.NamespaceContext;

import org.exoplatform.services.jcr.core.ManageableRepository;

/**
 * Created by The eXo Platform SAS
 * Author : Vitaly Guly <gavrikvetal@gmail.com>
 * @version $Id: $
 */

public class WebDavNameSpaceContext implements NamespaceContext {
  
  /*
   * Key: NameSpace
   * Value: Prefix
   * 
   */
  private HashMap<String, String> prefixes = new HashMap<String, String>();    
  
  public WebDavNameSpaceContext(ManageableRepository repository) throws RepositoryException {
    prefixes.put("DAV:", "D");
    
    NamespaceRegistry repositoryRegistry = repository.getNamespaceRegistry();
    String []uris = repositoryRegistry.getURIs();
    for (int i = 0; i < uris.length; i++) {
      String prefix = repositoryRegistry.getPrefix(uris[i]);      
      prefixes.put(uris[i], prefix);
    }
    
  }

  public String getNamespaceURI(String prefix) {
    Iterator<String> keyIter = prefixes.keySet().iterator();
    while (keyIter.hasNext()) {
      String key = keyIter.next();
      String value = prefixes.get(key);
      
      if (value.equals(prefix)) {
        return key;
      }
    }
    
    return null;
  }

  public String getPrefix(String namespaceURI) {
    return prefixes.get(namespaceURI);
  }

  public Iterator getPrefixes(String namespaceURI) {
    return null;
  }  
  
}
