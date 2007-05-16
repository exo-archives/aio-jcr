/***************************************************************************
 * Copyright 2001-2003 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.services.jcr.impl.core;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.jcr.NamespaceException;
import javax.jcr.NamespaceRegistry;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.logging.Log;
import org.exoplatform.services.jcr.core.NamespaceAccessor;
import org.exoplatform.services.jcr.dataflow.DataManager;
import org.exoplatform.services.log.ExoLogger;

/**
 * Created by The eXo Platform SARL .
 * 
 * @author <a href="mailto:geaz@users.sourceforge.net">Gennady Azarenkov </a>
 * @version $Id: NamespaceRegistryImpl.java 12841 2007-02-16 08:58:38Z peterit $
 */

public class NamespaceRegistryImpl implements NamespaceRegistry, NamespaceAccessor {

  public static HashMap <String, String> DEF_NAMESPACES = new HashMap<String, String>();

  protected static Log log = ExoLogger.getLogger("jcr.NamespaceRegistryImpl");

  static {
    DEF_NAMESPACES.put("", "");
    DEF_NAMESPACES.put("jcr", "http://www.jcp.org/jcr/1.0");
    DEF_NAMESPACES.put("nt", "http://www.jcp.org/jcr/nt/1.0");
    DEF_NAMESPACES.put("mix", "http://www.jcp.org/jcr/mix/1.0");
    DEF_NAMESPACES.put("xml", "http://www.w3.org/XML/1998/namespace");
    DEF_NAMESPACES.put("sv", "http://www.jcp.org/jcr/sv/1.0");
    DEF_NAMESPACES.put("exo", "http://www.exoplatform.com/jcr/exo/1.0");
    DEF_NAMESPACES.put("xs", "http://www.w3.org/2001/XMLSchema");
    DEF_NAMESPACES.put("fn", "http://www.w3.org/2004/10/xpath-functions");
    //DEF_NAMESPACES.put("dc", "http://purl.org/dc/elements/1.1");
  }

  private HashMap <String, String> namespaces;
    
  private NamespaceDataPersister persister = null;

  private static final String[] protectedNamespaces = { "jcr", "nt", "mix", "xml",
      "sv", "exo" };

  public NamespaceRegistryImpl(DataManager dataManager, NamespaceDataPersister persister) throws RepositoryException {
    this.namespaces = DEF_NAMESPACES;
    this.persister = persister;
  }
   
  /**
   * for tests
   * @throws RepositoryException
   */
  public NamespaceRegistryImpl() throws RepositoryException {
    namespaces = DEF_NAMESPACES;
  }

  

  /* (non-Javadoc)
   * @see javax.jcr.NamespaceRegistry#getURI(java.lang.String)
   */
  public String getURI(String prefix) throws NamespaceException {
    String uri = namespaces.get(prefix);
    if (uri == null)
      throw new NamespaceException("Unknown Prefix " + prefix);
    return uri;
  }

  /* (non-Javadoc)
   * @see javax.jcr.NamespaceRegistry#registerNamespace(java.lang.String, java.lang.String)
   */
  public synchronized void registerNamespace(String prefix, String uri)
      throws NamespaceException, RepositoryException {
    

    validateNamespace(prefix, uri);
 
    Collection values = namespaces.values();
    if (values.contains(uri)) {
      
//      String key2Remove = null;
//      Set keys = namespaces.keySet();
//      for (Iterator iterator = keys.iterator(); iterator.hasNext();) {
//        String key = (String) iterator.next();
//        String value = (String) namespaces.get(key);
//        if (value.equals(uri)) {
//          key2Remove = key;
//          break;
//        }
//      }
//      namespaces.remove(key2Remove);
//      delete(key2Remove);
      
      throw new NamespaceException(
        "Re-registration is not supported as may cause integrity problems. (todo issue #46)");
    }
    
    persister.addNamespace(prefix, uri);
    persister.saveChanges();
    
    namespaces.put(new String(prefix), new String(uri));
  }
  
  public synchronized void validateNamespace(String prefix, String uri)
      throws NamespaceException, RepositoryException {
    
    if(prefix.indexOf(":") > 0)
      throw new RepositoryException(
      "Namespace prefix should not contain ':' " + prefix);

    if (ArrayUtils.contains(protectedNamespaces, prefix)) {
      if (uri == null)
        throw new NamespaceException("Can not remove built-in namespace");
      throw new NamespaceException("Can not change built-in namespace");
    }
    if (prefix.toLowerCase().startsWith("xml")) //&& namespaces.values().contains(uri) // TCK won't pass
      throw new NamespaceException(
          "Can not re-assign prefix that start with 'xml'");
    if (uri == null)
      throw new NamespaceException("Can not register NULL URI!");
  }  

  /* (non-Javadoc)
   * @see javax.jcr.NamespaceRegistry#unregisterNamespace(java.lang.String)
   */
  public synchronized void unregisterNamespace(String prefix) throws NamespaceException,
      RepositoryException {

    if (namespaces.get(prefix) == null)
      throw new NamespaceException("Prefix " + prefix + " is not registered");

    for (int i = 0; i < protectedNamespaces.length; i++)
      if (prefix.equals(protectedNamespaces[i]))
        throw new NamespaceException("Prefix " + prefix + " is not protected");
  
    throw new NamespaceException(
      "Unregistration is not supported as may cause integrity problems. (todo issue #46)");
  
    //persister.removeNamespace(prefix);
    //persister.saveChanges();
    
    //namespaces.remove(prefix);
  }

  /* (non-Javadoc)
   * @see javax.jcr.NamespaceRegistry#getPrefixes()
   */
  public String[] getPrefixes() {
    return (String[]) namespaces.keySet().toArray(
        new String[namespaces.keySet().size()]);
  }

  /* (non-Javadoc)
   * @see javax.jcr.NamespaceRegistry#getURIs()
   */
  public String[] getURIs() {
    return (String[]) namespaces.values()
        .toArray(new String[namespaces.size()]);
  }

  /* (non-Javadoc)
   * @see javax.jcr.NamespaceRegistry#getPrefix(java.lang.String)
   */
  public String getPrefix(String uri) throws NamespaceException,
      RepositoryException {
    String[] prefixes = getPrefixes();
    for (int i = 0; i < prefixes.length; i++) {
      if (getURI(prefixes[i]).equals(uri))
        return prefixes[i];
    }
    throw new NamespaceException("Prefix for " + uri + " not found");
  }


  public Map getURIMap() {
    return namespaces;
  }
  
  public void loadFromStorage() throws RepositoryException {

    try {
      namespaces.putAll(persister.loadNamespaces());
    } catch (PathNotFoundException e) {
      log.info("Namespaces storage (/jcr:system/exo:namespaces) is not accessible. Default namespaces only will be used. " + e);
      return;
    }
  }  

  ////////////////////// NamespaceAccessor
  
  /* (non-Javadoc)
   * @see org.exoplatform.services.jcr.core.NamespaceAccessor#getNamespaceURIByPrefix(java.lang.String)
   */
  public String getNamespaceURIByPrefix(String prefix)
      throws NamespaceException {
    return getURI(prefix);
  }

  /* (non-Javadoc)
   * @see org.exoplatform.services.jcr.core.NamespaceAccessor#getNamespacePrefixByURI(java.lang.String)
   */
  public String getNamespacePrefixByURI(String uri) throws NamespaceException,
      RepositoryException {
    return getPrefix(uri);
  }

  /* (non-Javadoc)
   * @see org.exoplatform.services.jcr.core.NamespaceAccessor#getAllNamespacePrefixes()
   */
  public String[] getAllNamespacePrefixes() {
    return getPrefixes();
  }
  
}