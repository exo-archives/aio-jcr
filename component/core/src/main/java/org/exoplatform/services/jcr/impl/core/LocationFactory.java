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

import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;

import org.apache.commons.logging.Log;

import org.exoplatform.services.jcr.core.NamespaceAccessor;
import org.exoplatform.services.jcr.datamodel.InternalQName;
import org.exoplatform.services.jcr.datamodel.QPath;
import org.exoplatform.services.jcr.datamodel.QPathEntry;
import org.exoplatform.services.jcr.impl.xml.XMLChar;
import org.exoplatform.services.log.ExoLogger;

/**
 * Created by The eXo Platform SAS.<br>
 * Helper for creating namespace mapping dependent entities like JCR path, name,
 * uuid
 * 
 * @author Gennady Azarenkov
 * @version $Id: LocationFactory.java 11907 2008-03-13 15:36:21Z ksm $
 */
public class LocationFactory {

  protected static Log      log = ExoLogger.getLogger("jcr.LocationFactory");

  private NamespaceAccessor namespaces;

  private boolean           warnIllegalChar;

  private char              illegalChar;

  public LocationFactory(NamespaceAccessor namespaces) {
    this.namespaces = namespaces;
  }

  public JCRPath createRootLocation() throws RepositoryException {
    return parseNames(JCRPath.ROOT_PATH, true);
  }

  /**
   * Creates JCRPath from parent path and relPath
   * 
   * @param parentLoc parent path
   * @param relPath related path
   * @param setIndexIfNotDefined if necessary to set index = 1 if not defined
   *          (usable for node's path only)
   * @return
   * @throws RepositoryException
   */
  public JCRPath createJCRPath(JCRPath parentLoc, String relPath) throws RepositoryException {

    JCRPath path = new JCRPath();
    for (int i = 0; i < parentLoc.getEntries().length; i++) {
      path.addEntry(parentLoc.getEntries()[i]);
    }

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
   * 
   * @param path
   * @return JCRPath
   * @throws RepositoryException
   */
  public JCRPath parseJCRPath(String path) throws RepositoryException {
    if (isAbsPathParseable(path)) {
      return parseAbsPath(path);
    } else {
      return parseRelPath(path);
    }
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
      path.addEntry(entry.getNamespace(), entry.getName(), prefix, entry.getIndex());
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
  public JCRName parseJCRName(String name) throws PathNotFoundException, RepositoryException {
    JCRPath.PathElement entry = parsePathEntry(new JCRPath(), name);

    return new JCRName(entry.getNamespace(), entry.getName(), entry.getPrefix());
  }

  public JCRPath.PathElement[] createRelPath(QPathEntry[] relPath) throws RepositoryException {
    JCRPath path = new JCRPath();
    // JCRPath.PathElement[] entries = new JCRPath.PathElement[relPath.length];
    for (QPathEntry element : relPath) {
      String uri = namespaces.getNamespaceURIByPrefix(element.getNamespace());
      String prefix = namespaces.getNamespacePrefixByURI(uri);
      path.addEntry(uri, element.getName(), prefix, element.getIndex());
    }
    return path.getEntries();
  }

  private JCRPath.PathElement parsePathEntry(JCRPath path, String name) throws PathNotFoundException,
                                                                       RepositoryException {

    // should be reset here (if there is explicit index) or
    // in JCRPath.Entry() (with index == 1)
    int index = -1;

    if (name == null) {
      throw new RepositoryException("Name can not be null");
    }

    int delim = name.indexOf(":");
    int endOfName = name.length();
    int indexStart = name.indexOf("[");
    if (indexStart > 0) {
      int indexEnd = name.indexOf("]");
      if ((indexEnd <= indexStart + 1) || (indexEnd != name.length() - 1)) {
        throw new RepositoryException("Invalid path entry " + name);
      }
      index = Integer.parseInt(name.substring(indexStart + 1, indexEnd));
      if (index <= 0) {
        throw new RepositoryException("Invalid path entry " + name);
      }
      endOfName = indexStart;
    }

    try {
      String prefix;
      if (delim <= 0) {
        prefix = "";
      } else {
        // prefix validation
        prefix = name.substring(0, delim);
        if (!XMLChar.isValidName(prefix)) {
          throw new RepositoryException("Illegal path entry " + name);
        }
      }

      // name validation
      String someName = name.substring(delim + 1, endOfName);
      int validName = isValidName(someName, !prefix.equals(""));
      if (validName < 0) {
        throw new RepositoryException("Illegal path entry " + name);
      } else if (validName == 0) {
        log.warn("Path entry " + name + " contain illegal char " + illegalChar);
      }

      path.addEntry(namespaces.getNamespaceURIByPrefix(prefix), someName, prefix, index);
      return (JCRPath.PathElement) path.getName();

    } catch (Exception e) {
      throw new RepositoryException(e.getMessage(), e);
    }
  }

  private JCRPath parseNames(String path, boolean absolute) throws PathNotFoundException,
                                                           RepositoryException {

    if (path == null) {
      throw new RepositoryException("Illegal relPath " + path);
    }

    JCRPath jcrPath = new JCRPath();
    int start = 0;
    if (!absolute) {
      start = -1;
    }
    if (isAbsPathParseable(path)) {
      if (!absolute) {
        throw new RepositoryException("Illegal relPath " + path);
      }
      jcrPath.addEntry(namespaces.getNamespaceURIByPrefix(""), "", "", -1);
    } else {
      if (absolute) {
        throw new RepositoryException("Illegal absPath " + path);
      }
    }

    int end = 0;
    while (end >= 0) {
      end = path.indexOf('/', start + 1);
      String qname = path.substring(start + 1, end == -1 ? path.length() : end);

      if (start + 1 != path.length()) {
        parsePathEntry(jcrPath, qname);
      } else {
        // jcrPath.addEntry(namespaces.getNamespaceURIByPrefix(""), "", "", -1);
        return jcrPath;
      }

      start = end;
    }

    return jcrPath;
  }

  private static boolean isAbsPathParseable(String str) {
    return str.startsWith("/");
  }

  // Some functions for JCRPath Validation
  private boolean isNonspace(char ch) {
    if (ch == '|') {
      illegalChar = ch;
      warnIllegalChar = true;
    }

    return !((ch == '\t') || (ch == '\n') || (ch == '\f') || (ch == '\r') || (ch == ' ')
        || (ch == '/') || (ch == ':') || (ch == '[') || (ch == ']') || (ch == '\'') || (ch == '\"') || (ch == '*'));
  }

  private boolean isSimpleString(String str) {
    char ch;

    for (int i = 0; i < str.length(); i++) {
      ch = str.charAt(i);
      if (!isNonspace(ch) && (ch != ' ')) {
        return false;
      }
    }

    return true;
  }

  private boolean isLocalName(String str) {
    int strLen = str.length();

    switch (strLen) {
    case 0:
      return false;
    case 1:
      char ch = str.charAt(0);
      return (isNonspace(ch) && (ch != '.'));
    case 2:
      char ch0 = str.charAt(0);
      char ch1 = str.charAt(1);
      return (((ch0 == '.') && (isNonspace(ch1) && (ch1 != '.')))
          || ((isNonspace(ch0) && (ch0 != '.')) && (ch1 == '.')) || ((isNonspace(ch0) && (ch0 != '.')) && (isNonspace(ch1) && (ch1 != '.'))));
    default:
      return isNonspace(str.charAt(0)) && isSimpleString(str.substring(1, strLen - 1))
          && isNonspace(str.charAt(strLen - 1));
    }
  }

  private boolean isSimpleName(String str) {
    int strLen = str.length();

    switch (strLen) {
    case 0:
      return false;
    case 1:
      return isNonspace(str.charAt(0));
    case 2:
      return isNonspace(str.charAt(0)) && isNonspace(str.charAt(1));
    default:
      return isNonspace(str.charAt(0)) && isSimpleString(str.substring(1, strLen - 1))
          && isNonspace(str.charAt(strLen - 1));
    }
  }

  private int isValidName(String str, boolean prefixed) {
    warnIllegalChar = false;

    boolean result = (prefixed ? isLocalName(str) : isSimpleName(str)
        || str.equals(JCRPath.THIS_RELPATH) || str.equals(JCRPath.PARENT_RELPATH)
        || str.equals("*"));

    if (!result) {
      return -1;
    } else if (warnIllegalChar) {
      return 0;
    } else {
      return 1;
    }
  }
}
