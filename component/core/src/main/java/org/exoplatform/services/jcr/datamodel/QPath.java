/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.services.jcr.datamodel;

import java.util.ArrayList;
import java.util.List;

import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;

import org.apache.commons.logging.Log;
import org.exoplatform.services.log.ExoLogger;

/**
 * Created by The eXo Platform SARL .
 *
 * @author Gennady Azarenkov
 * @version $Id$
 */

public class QPath implements Comparable {

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
    this.hashCode = hash; // prime * hash + this.names.hashCode()
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
    //InternalQPath path = new InternalQPath();
    if (relativeDegree > getLength() || getLength() <= 1) {
      throw new IllegalPathException("Relative degree " + relativeDegree
          + " is more than depth for " + getAsString());
    }

    int entryCount = getLength() - relativeDegree;
    QPathEntry[] ancestorEntries = new QPathEntry[entryCount];
    for (int i = 0; i < entryCount; i++)
      ancestorEntries[i] = new QPathEntry(names[i].getNamespace(), names[i].getName(), names[i].getIndex());

    return new QPath(ancestorEntries);
  }

  public QPathEntry[] getRelPath(int relativeDegree) throws IllegalPathException {

    if (relativeDegree > getLength() || getLength() <= 1)
      throw new IllegalPathException("Relative degree " + relativeDegree
          + " is more than depth for " + getAsString());

    List<QPathEntry> entries = new ArrayList<QPathEntry>();

    // [PN] 12.02.07
    for (int i = names.length - relativeDegree; i < names.length ; i++)
      entries.add(names[i]);

    return entries.toArray(new QPathEntry[entries.size()]);
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
   * @return The general primogenitor of two ways.
   * @throws PathNotFoundException
   */
 public static QPath getPrimogenitorPath(QPath firstPath, QPath secondPath)
      throws PathNotFoundException {

    if (!firstPath.getEntries()[0].equals(secondPath.getEntries()[0])) {
      throw new PathNotFoundException("For the given ways there is no general primogenitor.");
    }

    List<QPathEntry> primoEntries = new ArrayList<QPathEntry>();
    for (int i = 0; i < firstPath.getEntries().length; i++) {
      if (firstPath.getEntries()[i].equals(secondPath.getEntries()[i])){
        primoEntries.add(firstPath.getEntries()[i]);
      } else {
        break;
      }
    }

    return new QPath(primoEntries.toArray(new QPathEntry[primoEntries.size()]));
  }
  /**
   * @return last name of this path
   */
  public InternalQName getName() {
    //InternalQName name = names[getLength() - 1];
    //return new InternalQName(name.getNamespace(), name.getName());

    // [PN] 07.02.07
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
  public int getLength() {
    return names.length;
  }

  public String getAsString() {

    if (stringName == null) {

      String str = "";
      for (int i = 0; i < getLength(); i++) {
        str += names[i].getAsString(true);
      }
      stringName = str.intern();
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

  public int compareTo(Object o) {
    if(o == this) {
      return 0;

    } else if(o instanceof QPath) {
      QPath anotherPath = (QPath)o;

      final String myString = getAsString();
      final String anotherString = anotherPath.getAsString();

      if (myString == anotherString)
        return 0;

      return myString.compareTo(anotherString);
    }

    return 0;
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
    //InternalQPath path = new InternalQPath();
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

      //path.addEntry(uri, localName, index);
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
    //path.addEntry(path.parseEntry(name));
    return path;
  }

  public static QPath makeChildPath(final QPath parent, final InternalQName name) {
    return makeChildPath(parent, name, 1);
  }

  public static QPath makeChildPath(final QPath parent,
      final InternalQName name, final int itemIndex) {

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
