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
package org.exoplatform.services.jcr.datamodel;

import java.util.ArrayList;
import java.util.List;

import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;

import org.apache.commons.logging.Log;
import org.exoplatform.commons.utils.QName;
import org.exoplatform.services.log.ExoLogger;

/**
 * Created by The eXo Platform SAS.
 *
 * @author Gennady Azarenkov
 * @version $Id$
 */

public class QPath implements Comparable<QPath> {

  protected static Log log = ExoLogger.getLogger("jcr.SessionDataManager");

  public static final String PREFIX_DELIMITER = ":";

  private final QPathEntry[] names;
  private final int hashCode;
  private String stringName; // compile on demand

  public QPath(QPathEntry[] names) {
    this.names = names;

    final int prime = 31;
    int hash = names.length > 0 ? 1 : super.hashCode();
    for (QPathEntry entry: names) {
      hash = prime * hash + entry.hashCode();
      hash = prime * hash + entry.getIndex();
    }
    this.hashCode = hash; 
  }

  public boolean isAbsolute() {
    if( names[0].getIndex() == 1
        && names[0].getName().length() == 0
        && names[0].getNamespace().length() == 0)
      return true;
    else 
      return false;
  }

  /**
   * @return parent path
   * @throws PathNotFoundException
   *           if path could not have parent - i.e. root path
   */
  public QPath makeParentPath() throws IllegalPathException {
    return makeAncestorPath(1);
  }

  /**
   * Makes ancestor path by relative degree (For ex relativeDegree == 1 means
   * parent path etc)
   *
   * @param relativeDegree
   * @return
   */
  public QPath makeAncestorPath(int relativeDegree) throws IllegalPathException {
    if (relativeDegree > getLength() || getLength() <= 1) {
      throw new IllegalPathException("Relative degree " + relativeDegree
          + " is more than depth for " + getAsString());
    }

    int entryCount = getLength() - relativeDegree;
    QPathEntry[] ancestorEntries = new QPathEntry[entryCount];
    for (int i = 0; i < entryCount; i++) {
      QPathEntry entry = names[i];
      ancestorEntries[i] = new QPathEntry(entry.getNamespace(), entry.getName(), entry.getIndex());
    }

    return new QPath(ancestorEntries);
  }

  public QPathEntry[] getRelPath(int relativeDegree) throws IllegalPathException {

    int len = getLength() - relativeDegree;
    if (len < 0)
      throw new IllegalPathException("Relative degree " + relativeDegree
          + " is more than depth for " + getAsString());

    QPathEntry[] relPath = new QPathEntry[relativeDegree]; 
    System.arraycopy(names, len, relPath, 0, relPath.length);
    
    return relPath;
  }

  /**
   * @return array of its path's names
   */
  public QPathEntry[] getEntries() {
    return names;
  }

  /**
   * @return depth of this path calculates as size of names array - 1. For ex
   *         root's depth=0 etc.
   */
  public int getDepth() {
    return getLength() - 1;
  }

  /**
   * @param anotherPath
   * @param childOnly
   *          if == true only direct children of the path will be taking in
   *          account
   * @return if this path is descendant of another one
   */
  public boolean isDescendantOf(QPath anotherPath, boolean childOnly) {
    int depthDiff = getDepth() - anotherPath.getDepth();
    if (depthDiff <= 0 || (childOnly && depthDiff != 1))
      return false;

    InternalQName[] anotherNames = anotherPath.getEntries();
    for (int i = 0; i < anotherNames.length; i++) {
      boolean result = anotherNames[i].equals(names[i]);
      if (!result)
        return false;
    }
    return true;
  }
  
  /**
   *
   * @param firstPath
   * @param secondPath
   * @return The common ancestor of two paths.
   * @throws PathNotFoundException
   */
  public static QPath getCommonAncestorPath(QPath firstPath, QPath secondPath)
      throws PathNotFoundException {

    if (!firstPath.getEntries()[0].equals(secondPath.getEntries()[0])) {
      throw new PathNotFoundException("For the given ways there is no common ancestor.");
    }

    List<QPathEntry> caEntries = new ArrayList<QPathEntry>();
    for (int i = 0; i < firstPath.getEntries().length; i++) {
      if (firstPath.getEntries()[i].equals(secondPath.getEntries()[i])){
        caEntries.add(firstPath.getEntries()[i]);
      } else {
        break;
      }
    }

    return new QPath(caEntries.toArray(new QPathEntry[caEntries.size()]));
  }
  
  /**
   * @return last name of this path
   */
  public InternalQName getName() {
    return names[getLength() - 1];
  }

  /**
   * @return index
   */
  public int getIndex() {
    return names[getLength() - 1].getIndex();
  }

  /**
   * @return length of names array
   */
  protected int getLength() {
    return names.length;
  }

  public String getAsString() {

    if (stringName == null) {

      String str = "";
      for (int i = 0; i < getLength(); i++) {
        str += names[i].getAsString(true);
      }
      stringName = str;
    }

    return stringName;
  }

  @Override
  public String toString() {
    return super.toString() + " (" + getAsString() + ")";
  }

  @Override
  public boolean equals(Object o) {
    if (o == this)
      return true;

    if (!(o instanceof QPath))
      return false;

    return hashCode == o.hashCode();
  }

  public int compareTo(QPath compare) {
    if (compare.equals(this))
      return 0;

    QPathEntry[] e1 = names;
    QPathEntry[] e2 = compare.getEntries();
    
    int len1 = e1.length;
    int len2 = e2.length;

    int k = 0;
    int lim = Math.min(len1, len2) ;
    while (k < lim) {
      
      QPathEntry c1 = e1[k];
      QPathEntry c2 = e2[k];
      
      if (!c1.isSame(c2)) {
        return c1.compareTo(c2);
      }
      k++;
    }
    return len1 - len2;
  }
  
  @Override
  public int hashCode() {
    return hashCode;
  }

  // Factory methods ---------------------------

  /**
   * Parses string and make internal path from it
   *
   * @param qPath
   * @return
   * @throws RepositoryException -
   *           if string is invalid
   */
  public static QPath parse(String qPath) throws IllegalPathException {
    if (qPath == null)
      throw new IllegalPathException("Bad internal path '" + qPath + "'");

    if (qPath.length() < 2 || !qPath.startsWith("[]"))
      throw new IllegalPathException("Bad internal path '" + qPath + "'");

    int uriStart = 0;
    List<QPathEntry> entries = new ArrayList<QPathEntry>();
    while (uriStart >= 0) {

      uriStart = qPath.indexOf("[", uriStart);

      int uriFinish = qPath.indexOf("]", uriStart);
      String uri = qPath.substring(uriStart + 1, uriFinish);

      int tmp = qPath.indexOf("[", uriFinish); // next token
      if (tmp == -1) {
        tmp = qPath.length();
        uriStart = -1;
      } else
        uriStart = tmp;

      String localName = qPath.substring(uriFinish + 1, tmp);
      int index = 0;
      int ind = localName.indexOf(PREFIX_DELIMITER);
      if (ind != -1) { // has index
        index = Integer.parseInt(localName.substring(ind + 1));
        localName = localName.substring(0, ind);
      } else {
        if (uriStart > -1)
          throw new IllegalPathException("Bad internal path '" + qPath
              + "' each intermediate name should have index");
      }

      entries.add(new QPathEntry(uri, localName, index));
    }
    return new QPath(entries.toArray(new QPathEntry[entries.size()]));
  }

  /**
   * Makes child path from existed path and child name. Assumed that parent path
   * belongs to node so it should have some index. If not sets index=1
   * automatically
   *
   * @param parent
   *          path
   * @param name
   *          child name
   * @return new InternalQPath
   */
  @Deprecated // [PN] 05.02.07
  public static QPath makeChildPath(QPath parent, String name)
      throws IllegalPathException {

    QPathEntry[] parentEntries = parent.getEntries();
    QPathEntry[] names = new QPathEntry[parentEntries.length + 1];
    int index = 0;
    for (QPathEntry pname: parentEntries) {
      names[index++] = pname;
    }

    names[index] = parseEntry(name);
    QPath path = new QPath(names);
    return path;
  }

  public static QPath makeChildPath(final QPath parent, final InternalQName name) {
    return makeChildPath(parent, name, 1);
  }

  public static QPath makeChildPath(final QPath parent,
      final QName name, final int itemIndex) {

    QPathEntry[] parentEntries = parent.getEntries();
    QPathEntry[] names = new QPathEntry[parentEntries.length + 1];
    int index = 0;
    for (QPathEntry pname: parentEntries) {
      names[index++] = pname;
    }
    names[index] = new QPathEntry(name.getNamespace(), name.getName(), itemIndex);

    QPath path = new QPath(names);
    return path;
  }

  public static QPath makeChildPath(final QPath parent, final QPathEntry[] relEntries) {

    final QPathEntry[] parentEntries = parent.getEntries();
    final QPathEntry[] names = new QPathEntry[parentEntries.length + relEntries.length];
    int index = 0;
    for (QPathEntry name: parentEntries) {
      names[index++] = name;
    }
    for (QPathEntry name: relEntries) {
      names[index++] = name;
    }

    QPath path = new QPath(names);
    return path;
  }

  private static QPathEntry parseEntry(final String entry) throws IllegalPathException {

    if (!entry.startsWith("["))
      throw new IllegalPathException("Invalid QPath Entry '" + entry
          + "' Should start of '['");
    final int uriStart = 0;
    final int uriFinish = entry.indexOf("]", uriStart);
    if (uriFinish == -1)
      throw new IllegalPathException("Invalid QPath Entry '" + entry
          + "' No closed ']'");
    final String uri = entry.substring(uriStart + 1, uriFinish);

    final String localName = entry.substring(uriFinish + 1, entry.length());

    final int ind = localName.indexOf(PREFIX_DELIMITER);
    if (ind > 1) {
      return new QPathEntry(uri,
          localName.substring(0, ind),
          Integer.parseInt(localName.substring(ind + 1)));
    }

    return new QPathEntry(uri, localName, 1);
  }

}
