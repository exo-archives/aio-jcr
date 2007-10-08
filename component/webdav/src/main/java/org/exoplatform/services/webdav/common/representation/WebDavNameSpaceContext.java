/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.services.webdav.common.representation;

import java.util.HashMap;
import java.util.Iterator;

import javax.jcr.NamespaceRegistry;
import javax.jcr.RepositoryException;
import javax.xml.namespace.NamespaceContext;

import org.apache.commons.logging.Log;
import org.exoplatform.services.jcr.core.ManageableRepository;
import org.exoplatform.services.log.ExoLogger;

/**
 * Created by The eXo Platform SARL
 * Author : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
 * @version $Id: $
 */

public class WebDavNameSpaceContext implements NamespaceContext {
  
  private static Log log = ExoLogger.getLogger("jcr.WebDavNameSpaceContext");

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
      
      log.info("Added NAMESPACE & PREFIX: ");
      log.info("NAMESPACE: " + uris[i]);
      log.info("PREFIX: " + prefix);
      
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
