/***************************************************************************
 * Copyright 2001-2003 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.services.jcr.impl.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Property;
import javax.jcr.PropertyIterator;
import javax.jcr.RangeIterator;
import javax.jcr.nodetype.NodeType;
import javax.jcr.nodetype.NodeTypeIterator;
import javax.jcr.observation.Event;
import javax.jcr.observation.EventIterator;
import javax.jcr.observation.EventListener;
import javax.jcr.observation.EventListenerIterator;
import javax.jcr.query.Row;
import javax.jcr.query.RowIterator;
import javax.jcr.version.Version;
import javax.jcr.version.VersionIterator;


 
/**
 * Created by The eXo Platform SARL        .
 *
 * @author <a href="mailto:geaz@users.sourceforge.net">Gennady Azarenkov</a>
 * @version $Id: EntityCollection.java 12841 2007-02-16 08:58:38Z peterit $
 */

public class EntityCollection
    implements NodeIterator, PropertyIterator, 
    NodeTypeIterator, EventIterator, EventListenerIterator, RowIterator, VersionIterator {

  private Iterator iter;
  private List list;
  private long pos;

  public EntityCollection(Collection col) {
    this(new ArrayList(col));
  }

  public EntityCollection(List list) {
    if (list == null)
      this.list = new ArrayList();
    else
      this.list = list;

    this.iter = list.iterator();
//    this.iter = list.listIterator();
    this.pos = 0;
  }

  public EntityCollection() {
    this.list = new ArrayList();
    this.iter = list.iterator();
//    this.iter = list.listIterator();
    this.pos = 0;
  }
  
  public void reverse() {
    Collections.reverse(list);
  }


  /**
   * @see NodeIterator#nextNode()
   */
  public Node nextNode() {
    pos++;
    return (Node) iter.next();
  }

  /**
   * @see RangeIterator#skip(int)
   */
  public void skip(long skipNum) {
    pos += skipNum;
    while (skipNum-- > 0) {
      iter.next();
    }
  }

  /**
   * Returns the number of elements in the iterator.
   * If this information is unavailable, returns -1.
   *
   * @return a long
   */
  public long getSize() {
    return list.size();
  }

  /**
   * Returns the current position within the iterator. The number
   * returned is the 0-based index of the next element in the iterator,
   * i.e. the one that will be returned on the subsequent <code>next</code> call.
   * <p/>
   * Note that this method does not check if there is a next element,
   * i.e. an empty iterator will always return 0.
   *
   * @return a long
   */
  public long getPosition() {
    return pos;
  }

//  /**
//   * @see ItemIterator#nextElement()
//   */
//  public Item nextItem() {
//    pos++;
//    return (Item) iter.next();
//  }

  /**
   * @see Iterator#hasNext()
   */
  public boolean hasNext() {
    return iter.hasNext();
  }

  /**
   * @see Iterator#next()
   */
  public Object next() {
    pos++;
    return iter.next();
  }

  /**
   * @see Iterator#remove()
   */
  public void remove() {
    iter.remove();
  }

  /**
   * @see PropertyIterator#nextProperty()
   */
  public Property nextProperty() {
    pos++;
    return (Property) iter.next();
  }

  /**
   * Returns the next <code>String</code> in the iteration.
   *
   * @return the next <code>String</code> in the iteration.
   * @throws java.util.NoSuchElementException
   *          if iteration has no more <code>String</code>s.
   */
  public String nextString() {
    pos++;
    return (String) iter.next();
  }


  /**
   *
   */
  public NodeType nextNodeType() {
    pos++;
    return (NodeType) iter.next();
  }


  /**
   *@see javax.jcr.query.RowIterator#nextRow()
   */

  public Row nextRow(){
      pos++;
      return (Row) iter.next();
  }


  /**
   *
   */
  public Event nextEvent() {
    pos++;
    return (Event) iter.next();
  }

  /**
   *
   */
  public EventListener nextEventListener() {
    pos++;
    return (EventListener) iter.next();
  }
  
  /**
  *
  */
 public Version nextVersion() {
   pos++;
   return (Version) iter.next();
 }
  

  /**
   * @see HitIterator#nextHit()
   */
//    public Hit nextHit() {
//        pos++;
//        return (Hit) iter.next();
//    }

  public void add(Object obj) {
    pos = 0;
    list.add(obj);
    iter = list.iterator();
  }

  public void addAll(Collection col) {
    pos = 0;
    list.addAll(col);
    iter = list.iterator();
  }

  public void remove(Object obj) {
    pos = 0;
    list.remove(obj);
    iter = list.iterator();
  }

  public long size() {
    return getSize();
  }

  public List getList() {
    return list;
  }

}
