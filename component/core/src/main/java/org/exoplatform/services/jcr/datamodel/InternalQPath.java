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
 * @version $Id: InternalQPath.java 13819 2007-03-27 13:44:07Z vaz $
 */

public class InternalQPath implements Comparable {

  protected static Log log = ExoLogger.getLogger("jcr.SessionDataManager");

  public static final String PREFIX_DELIMITER = ":";

  private final Entry[] names;
  private final int hashCode;
  private String stringName; // compile on demand

  public InternalQPath(Entry[] names) {
    this.names = names;

    final int prime = 31;
    int hash = 1;
    for (Entry entry: names) {
      hash = prime * hash + entry.hashCode();
      hash = prime * hash + entry.getIndex();
    }
    this.hashCode = hash; // prime * hash + this.names.hashCode()
  }

  public boolean isAbsolute()
  {
   if( names[0].getIndex() == 1
       && names[0].getName().length() == 0
       && names[0].getNamespace().length() == 0)
   return false;
   else return true;
  }

//  @Deprecated
//  public void addEntry(String namespace, String name, int index) {
//    addEntry(new Entry(namespace, name, index));
//  }
//
//  @Deprecated
//  private void addEntry(Entry entry) {
////    if (entry.getIndex() < 1)
////      entry.setIndex(1);
//    Entry[] newNames = new Entry[names.length + 1];
//    for (int i = 0; i < names.length; i++)
//      newNames[i] = names[i];
//    newNames[names.length] = entry;
//    names = newNames;
//  }

  /**
   * @return parent path
   * @throws PathNotFoundException
   *           if path could not have parent - i.e. root path
   */
  public InternalQPath makeParentPath() throws IllegalPathException {
    return makeAncestorPath(1);
  }

  /**
   * Makes ancestor path by relative degree (For ex relativeDegree == 1 means
   * parent path etc)
   *
   * @param relativeDegree
   * @return
   */
  public InternalQPath makeAncestorPath(int relativeDegree) throws IllegalPathException {
    //InternalQPath path = new InternalQPath();
    if (relativeDegree > getLength() || getLength() <= 1) {
      throw new IllegalPathException("Relative degree " + relativeDegree
          + " is more than depth for " + getAsString());
    }

    int entryCount = getLength() - relativeDegree;
    Entry[] ancestorEntries = new Entry[entryCount];
    for (int i = 0; i < entryCount; i++)
      ancestorEntries[i] = new Entry(names[i].getNamespace(), names[i].getName(), names[i].getIndex());

    return new InternalQPath(ancestorEntries);
  }

  public Entry[] getRelPath(int relativeDegree) throws IllegalPathException {

    if (relativeDegree > getLength() || getLength() <= 1)
      throw new IllegalPathException("Relative degree " + relativeDegree
          + " is more than depth for " + getAsString());

    List<Entry> entries = new ArrayList<Entry>();

    // [PN] 12.02.07
    for (int i = names.length - relativeDegree; i < names.length ; i++)
      entries.add(names[i]);

    return entries.toArray(new Entry[entries.size()]);
  }

  /**
   * @return array of its path's names
   */
  public Entry[] getEntries() {
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
  public boolean isDescendantOf(InternalQPath anotherPath, boolean childOnly) {
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
 public static InternalQPath getPrimogenitorPath(InternalQPath firstPath, InternalQPath secondPath)
      throws PathNotFoundException {

    if (!firstPath.getEntries()[0].equals(secondPath.getEntries()[0])) {
      throw new PathNotFoundException("For the given ways there is no general primogenitor.");
    }

    List<Entry> primoEntries = new ArrayList<Entry>();
    for (int i = 0; i < firstPath.getEntries().length; i++) {
      if (firstPath.getEntries()[i].equals(secondPath.getEntries()[i])){
        primoEntries.add(firstPath.getEntries()[i]);
      } else {
        break;
      }
    }

    return new InternalQPath(primoEntries.toArray(new Entry[primoEntries.size()]));
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
//      StringBuffer str = new StringBuffer(256);
//      for (int i = 0; i < names.length; i++) {
//        str.append(names[i].getAsString());
//        str.append(PREFIX_DELIMITER);
//        str.append(names[i].getIndex());
//      }
//
//      stringName = str.toString();

      String str = "";
      for (int i = 0; i < getLength(); i++) {
        str += names[i].getAsString() + PREFIX_DELIMITER + names[i].getIndex();
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

    if (!(o instanceof InternalQPath))
      return false;

    return hashCode == o.hashCode();
  }

  public int compareTo(Object o) {
    if(o == this) {
      return 0;

    } else if(o instanceof InternalQPath) {
      InternalQPath anotherPath = (InternalQPath)o;

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
  public static InternalQPath parse(String qPath) throws IllegalPathException {
    //InternalQPath path = new InternalQPath();
    if (qPath == null)
      throw new IllegalPathException("Bad internal path '" + qPath + "'");

    if (qPath.length() < 2 || !qPath.startsWith("[]"))
      throw new IllegalPathException("Bad internal path '" + qPath + "'");

    int uriStart = 0;
    List<Entry> entries = new ArrayList<Entry>();
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
      entries.add(new Entry(uri, localName, index));
    }
    return new InternalQPath(entries.toArray(new Entry[entries.size()]));
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
  public static InternalQPath makeChildPath(InternalQPath parent, String name)
      throws IllegalPathException {
//    InternalQPath path = new InternalQPath();
//    for (int i = 0; i < parent.getLength(); i++)
//      path.addEntry(parent.getEntries()[i]);
//    path.addEntry(path.parseEntry(name));
//    return path;

    Entry[] parentEntries = parent.getEntries();
    Entry[] names = new Entry[parentEntries.length + 1];
    int index = 0;
    for (Entry pname: parentEntries) {
      names[index++] = pname;
    }

    names[index] = parseEntry(name);
    InternalQPath path = new InternalQPath(names);
    //path.addEntry(path.parseEntry(name));
    return path;
  }

  public static InternalQPath makeChildPath(final InternalQPath parent, final InternalQName name) {
//    InternalQPath path = new InternalQPath();
//    for (int i = 0; i < parent.getLength(); i++)
//      path.addEntry(parent.getEntries()[i]);
//    path.addEntry(new Entry(name.getNamespace(), name.getName(), 1));
//    return path;

    return makeChildPath(parent, name, 1);
  }

  public static InternalQPath makeChildPath(final InternalQPath parent,
      final InternalQName name, final int itemIndex) {

//    InternalQPath path = new InternalQPath();
//    for (int i = 0; i < parent.getLength(); i++)
//      path.addEntry(parent.getEntries()[i]);
//    path.addEntry(new Entry(name.getNamespace(), name.getName(), index));
//    return path;

    Entry[] parentEntries = parent.getEntries();
    Entry[] names = new Entry[parentEntries.length + 1];
    int index = 0;
    for (Entry pname: parentEntries) {
      names[index++] = pname;
    }
    names[index] = new Entry(name.getNamespace(), name.getName(), itemIndex);

    InternalQPath path = new InternalQPath(names);
    return path;
  }

  public static InternalQPath makeChildPath(final InternalQPath parent, final Entry[] relEntries) {

//    for (int i = 0; i < parent.getLength(); i++)
//      path.addEntry(parent.getEntries()[i]);
//    path.addEntry(new Entry(name.getNamespace(), name.getName(), 1));

    final Entry[] parentEntries = parent.getEntries();
    final Entry[] names = new Entry[parentEntries.length + relEntries.length];
    int index = 0;
    for (Entry name: parentEntries) {
      names[index++] = name;
    }
    for (Entry name: relEntries) {
      names[index++] = name;
    }

    InternalQPath path = new InternalQPath(names);
    return path;
  }

  private static Entry parseEntry(final String entry) throws IllegalPathException {

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
      return new Entry(uri,
          localName.substring(0, ind),
          Integer.parseInt(localName.substring(ind + 1)));
    }

    return new Entry(uri, localName, 1);
  }

  public static class Entry extends InternalQName {

    private final int index;

    public Entry(String namespace, String name, int index) {
      super(namespace, name);
      this.index = index > 0 ? index : 1;
    }

    public int getIndex() {
      return index;
    }
  }

}
