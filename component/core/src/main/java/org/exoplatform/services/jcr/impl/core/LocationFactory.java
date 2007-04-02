/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.services.jcr.impl.core;

import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;

import org.exoplatform.services.jcr.core.NamespaceAccessor;
import org.exoplatform.services.jcr.datamodel.InternalQName;
import org.exoplatform.services.jcr.datamodel.QPath;
import org.exoplatform.services.jcr.datamodel.QPathEntry;

/**
 * Created by The eXo Platform SARL .<br>
 * Helper for creating namespace mapping dependent entities like JCR path, name,
 * uuid
 * 
 * @author Gennady Azarenkov
 * @version $Id: LocationFactory.java 12841 2007-02-16 08:58:38Z peterit $
 */

public class LocationFactory {

  private NamespaceAccessor namespaces;

  public LocationFactory(NamespaceAccessor namespaces) {
    this.namespaces = namespaces;
  }

  public JCRPath createRootLocation() throws RepositoryException {
    return parseNames(JCRPath.ROOT_PATH, true);
  }

  /**
   * Creates JCRPath from parent path and relPath
   * 
   * @param parentLoc
   *          parent path
   * @param relPath
   *          related path
   * @param setIndexIfNotDefined
   *          if neccessary to set index = 1 if not defined (usable for node's
   *          path only)
   * @return
   * @throws RepositoryException
   */
  public JCRPath createJCRPath(JCRPath parentLoc, String relPath)
      throws RepositoryException {

    JCRPath path = new JCRPath();
    for (int i = 0; i < parentLoc.getEntries().length; i++)
      path.addEntry(parentLoc.getEntries()[i]);

    JCRPath addPath = parseNames(relPath, false);
    for (int i = 0; i < addPath.getEntries().length; i++) {
      path.addEntry(addPath.getEntries()[i]);
    }
    return path;
  }

  /**
   * Parses absolute JCR path from string (JCR format /ns:name[index]/etc)
   * 
   * @param absPath
   * @return
   * @throws RepositoryException
   */
  public JCRPath parseAbsPath(String absPath) throws RepositoryException {
    return parseNames(absPath, true);
  }
  
  
  public JCRPath parseRelPath(String relPath) throws RepositoryException {
    return parseNames(relPath, false);
  }

  /**
   * creates abs(if convertable to abs path) or rel(otherwice) JCRPath
   * @param path
   * @return JCRPath
   * @throws RepositoryException
   */
  public JCRPath parseJCRPath(String path) throws RepositoryException {
    if(isAbsPathParseable(path))
      return parseAbsPath(path);
    else
      return parseRelPath(path);
  }
    
  /**
   * Creates JCRPath by internalQPath
   * 
   * @param qPath
   * @return
   * @throws RepositoryException
   */
  public JCRPath createJCRPath(QPath qPath) throws RepositoryException {

    JCRPath path = new JCRPath();
    for (int i = 0; i < qPath.getEntries().length; i++) {
      QPathEntry entry = qPath.getEntries()[i];
      String prefix = namespaces.getNamespacePrefixByURI(entry.getNamespace());
      path.addEntry(entry.getNamespace(), entry.getName(), prefix, entry
          .getIndex());
    }

    return path;
  }

  public JCRName createJCRName(InternalQName qname) throws RepositoryException {
    String prefix = namespaces.getNamespacePrefixByURI(qname.getNamespace());
    return new JCRName(qname.getNamespace(), qname.getName(), prefix);
  }

  /**
   * Parses absolute JCR name from string (JCR format ns:name[index])
   * 
   * @param name
   * @return
   * @throws RepositoryException
   */
  public JCRName parseJCRName(String name) throws PathNotFoundException,
      RepositoryException {
    JCRPath.PathElement entry = parsePathEntry(new JCRPath(), name);
    return new JCRName(entry.getNamespace(), entry.getName(), entry.getPrefix());
  }

  public JCRPath.PathElement[] createRelPath(QPathEntry[] relPath)
      throws RepositoryException {
    JCRPath path = new JCRPath();
    JCRPath.PathElement[] entries = new JCRPath.PathElement[relPath.length];
    for (int i = 0; i < relPath.length; i++) {
      String uri = namespaces
          .getNamespaceURIByPrefix(relPath[i].getNamespace());
      String prefix = namespaces.getNamespacePrefixByURI(uri);
      path.addEntry(uri, relPath[i].getName(), prefix, relPath[i].getIndex());
    }
    return path.getEntries();
  }

  private JCRPath.PathElement parsePathEntry(JCRPath path, String name)
      throws PathNotFoundException, RepositoryException {

    // should be reset here (if there is explicit index) or 
    // in JCRPath.Entry() (with index == 1)
    int index = -1;

    if (name == null)
      throw new RepositoryException("Name can not be null");
    int delim = name.indexOf(":");
    int endOfName = name.length();
    int indexStart = name.indexOf("[");
    if (indexStart > 0) {
      int indexEnd = name.indexOf("]");
      if (indexEnd <= indexStart)
        throw new RepositoryException("Invalid path entry " + name);
      index = Integer.parseInt(name.substring(indexStart + 1, indexEnd));
      endOfName = indexStart;
    }
    try {

      String prefix;
      if (delim <= 0) {
        prefix = "";
      } else {
        prefix = name.substring(0, delim);
      }

      path.addEntry(namespaces.getNamespaceURIByPrefix(prefix), name.substring(delim + 1, endOfName), prefix, index);
      return (JCRPath.PathElement) path.getName();

    } catch (Exception e) {
      throw new RepositoryException(e.getMessage(), e);
    }
  }

  private JCRPath parseNames(String path, boolean absolute)
      throws PathNotFoundException, RepositoryException {
    
    if (path == null)
      throw new RepositoryException("Illegal relPath " + path);

    //List list = new ArrayList();
    JCRPath jcrPath = new JCRPath();
    int start = 0;
    if (!absolute)
      start = -1;
    if (isAbsPathParseable(path)) {
        //path.startsWith("/")) {
      if (!absolute)
        throw new RepositoryException("Illegal relPath " + path);
      parsePathEntry(jcrPath, "");
    } else {
      if (absolute)
        throw new RepositoryException("Illegal absPath " + path);
    }

    int end = 0;
    while (end >= 0) {
      end = path.indexOf('/', start + 1);
      String qname = path.substring(start + 1, end == -1 ? path.length() : end);
      if (qname.length() == 0)
        return jcrPath;
      parsePathEntry(jcrPath, qname);
      start = end;
    }

    return jcrPath;
  }
  
  private static boolean isAbsPathParseable(String str){
    return str.startsWith("/");
  }
}