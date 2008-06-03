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
package org.exoplatform.services.jcr.impl.core;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.jcr.NamespaceException;
import javax.jcr.NamespaceRegistry;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;

import org.apache.commons.logging.Log;
import org.exoplatform.services.jcr.core.NamespaceAccessor;
import org.exoplatform.services.log.ExoLogger;

/**
 * Created by The eXo Platform SAS.
 * 
 * @author <a href="mailto:geaz@users.sourceforge.net">Gennady Azarenkov </a>
 * @version $Id: NamespaceRegistryImpl.java 11907 2008-03-13 15:36:21Z ksm $
 */

public class NamespaceRegistryImpl implements NamespaceRegistry, NamespaceAccessor {

  public static final Map<String, String> DEF_NAMESPACES       = new HashMap<String, String>();

  public static final Map<String, String> DEF_PREFIXES         = new HashMap<String, String>();

  private final static Set<String>                      PROTECTED_NAMESPACES = new HashSet<String>();

  protected final static Log                            log                  = ExoLogger
                                                                                 .getLogger("jcr.NamespaceRegistryImpl");

  static {

    DEF_NAMESPACES.put("", "");
    DEF_NAMESPACES.put("jcr", "http://www.jcp.org/jcr/1.0");
    DEF_NAMESPACES.put("nt", "http://www.jcp.org/jcr/nt/1.0");
    DEF_NAMESPACES.put("mix", "http://www.jcp.org/jcr/mix/1.0");
    DEF_NAMESPACES.put("xml", "http://www.w3.org/XML/1998/namespace");
    DEF_NAMESPACES.put("sv", "http://www.jcp.org/jcr/sv/1.0");
    DEF_NAMESPACES.put("exo", "http://www.exoplatform.com/jcr/exo/1.0");
    DEF_NAMESPACES.put("xs", "http://www.w3.org/2001/XMLSchema");
    DEF_NAMESPACES.put("fn", "http://www.w3.org/2005/xpath-functions");
    DEF_NAMESPACES.put("fn_old", "http://www.w3.org/2004/10/xpath-functions");
    

    DEF_PREFIXES.put("", "");
    DEF_PREFIXES.put("http://www.jcp.org/jcr/1.0", "jcr");
    DEF_PREFIXES.put("http://www.jcp.org/jcr/nt/1.0", "nt");
    DEF_PREFIXES.put("http://www.jcp.org/jcr/mix/1.0", "mix");
    DEF_PREFIXES.put("http://www.w3.org/XML/1998/namespace", "mix");
    DEF_PREFIXES.put("http://www.jcp.org/jcr/sv/1.0", "sv");
    DEF_PREFIXES.put("http://www.exoplatform.com/jcr/exo/1.0", "exo");
    DEF_PREFIXES.put("http://www.w3.org/2001/XMLSchema", "xs");
    DEF_PREFIXES.put("http://www.w3.org/2005/xpath-functions", "fn");
    DEF_PREFIXES.put("http://www.w3.org/2004/10/xpath-functions", "fn_old");
    

    PROTECTED_NAMESPACES.add("jcr");
    PROTECTED_NAMESPACES.add("nt");
    PROTECTED_NAMESPACES.add("mix");
    PROTECTED_NAMESPACES.add("xml");
    PROTECTED_NAMESPACES.add("sv");
    PROTECTED_NAMESPACES.add("exo");

  }

  private Map<String, String>             namespaces;

  private NamespaceDataPersister                        persister;

  private Map<String, String>             prefixes;

  /**
   * for tests
   */
  public NamespaceRegistryImpl() {
    this.namespaces = DEF_NAMESPACES;
    this.prefixes = DEF_PREFIXES;
  }

  public NamespaceRegistryImpl(NamespaceDataPersister persister) {

    this.namespaces = new HashMap<String, String>(DEF_NAMESPACES);
    this.prefixes = new HashMap<String, String>(DEF_PREFIXES);
    this.persister = persister;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.exoplatform.services.jcr.core.NamespaceAccessor#getAllNamespacePrefixes()
   */
  public String[] getAllNamespacePrefixes() {
    return getPrefixes();
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.exoplatform.services.jcr.core.NamespaceAccessor#getNamespacePrefixByURI(java.lang.String)
   */
  public String getNamespacePrefixByURI(String uri) throws NamespaceException, RepositoryException {
    return getPrefix(uri);
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.exoplatform.services.jcr.core.NamespaceAccessor#getNamespaceURIByPrefix(java.lang.String)
   */
  public String getNamespaceURIByPrefix(String prefix) throws NamespaceException {
    return getURI(prefix);
  }

  /*
   * (non-Javadoc)
   * 
   * @see javax.jcr.NamespaceRegistry#getPrefix(java.lang.String)
   */
  public String getPrefix(String uri) throws NamespaceException {
    String prefix = prefixes.get(uri);
    if (prefix != null) {
      return prefix;
    }
    throw new NamespaceException("Prefix for " + uri + " not found");
  }

  /*
   * (non-Javadoc)
   * 
   * @see javax.jcr.NamespaceRegistry#getPrefixes()
   */
  public String[] getPrefixes() {
    return namespaces.keySet().toArray(new String[namespaces.keySet().size()]);
  }

  /*
   * (non-Javadoc)
   * 
   * @see javax.jcr.NamespaceRegistry#getURI(java.lang.String)
   */
  public String getURI(String prefix) throws NamespaceException {
    String uri = namespaces.get(prefix);
    if (uri == null) {
      throw new NamespaceException("Unknown Prefix " + prefix);
    }
    return uri;
  }

  /*
   * (non-Javadoc)
   * 
   * @see javax.jcr.NamespaceRegistry#getURIs()
   */
  public String[] getURIs() {
    return namespaces.values().toArray(new String[namespaces.size()]);
  }

  public boolean isDefaultNamespace(String uri){
    return DEF_PREFIXES.containsKey(uri);
  }
  public boolean isDefaultPrefix(String prefix){
    return DEF_NAMESPACES.containsKey(prefix);
  }
  
  public boolean isPrefixMaped(String prefix) {
    return namespaces.containsKey(prefix);
  }

  public boolean isUriRegistered(String uri) {
    return prefixes.containsKey(uri);
  }

  public void loadFromStorage() throws RepositoryException {

    try {
      // namespaces.putAll(persister.loadNamespaces());
      persister.loadNamespaces(namespaces, prefixes);
    } catch (PathNotFoundException e) {
      log.info("Namespaces storage (/jcr:system/exo:namespaces) is not accessible."
          + " Default namespaces only will be used. " + e);
      return;
    }
  }

  // //////////////////// NamespaceAccessor

  /*
   * (non-Javadoc)
   * 
   * @see javax.jcr.NamespaceRegistry#registerNamespace(java.lang.String,
   *      java.lang.String)
   */
  public synchronized void registerNamespace(String prefix, String uri) throws NamespaceException,
      RepositoryException {

    validateNamespace(prefix, uri);

    if (namespaces.containsKey(prefix) || prefixes.containsKey(uri)) {
      throw new NamespaceException("Re-registration is not supported as may cause"
          + " integrity problems. (todo issue #46)");
    }

    persister.addNamespace(prefix, uri);
    persister.saveChanges();

    String newPrefix = new String(prefix);
    String newUri = new String(uri);

    namespaces.put(newPrefix, newUri);
    prefixes.put(newUri, newPrefix);
  }

  /*
   * (non-Javadoc)
   * 
   * @see javax.jcr.NamespaceRegistry#unregisterNamespace(java.lang.String)
   */
  public void unregisterNamespace(String prefix) throws NamespaceException, RepositoryException {

    if (namespaces.get(prefix) == null) {
      throw new NamespaceException("Prefix " + prefix + " is not registered");
    }

    if (PROTECTED_NAMESPACES.contains(prefix)) {
      throw new NamespaceException("Prefix " + prefix + " is protected");
    }

    throw new NamespaceException("Unregistration is not supported as"
        + " may cause integrity problems. (todo issue #46)");
  }

  public void validateNamespace(String prefix, String uri) throws NamespaceException,
      RepositoryException {

    if (prefix.indexOf(":") > 0) {
      throw new RepositoryException("Namespace prefix should not contain ':' " + prefix);
    }

    if (PROTECTED_NAMESPACES.contains(prefix)) {
      if (uri == null) {
        throw new NamespaceException("Can not remove built-in namespace");
      }
      throw new NamespaceException("Can not change built-in namespace");
    }
    if (prefix.toLowerCase().startsWith("xml")) {
      throw new NamespaceException("Can not re-assign prefix that start with 'xml'");
    }
    if (uri == null) {
      throw new NamespaceException("Can not register NULL URI!");
    }
  }

}