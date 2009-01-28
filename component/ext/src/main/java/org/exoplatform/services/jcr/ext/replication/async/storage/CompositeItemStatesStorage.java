/*
 * Copyright (C) 2003-2009 eXo Platform SAS.
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
package org.exoplatform.services.jcr.ext.replication.async.storage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.exoplatform.services.jcr.dataflow.ItemState;
import org.exoplatform.services.jcr.datamodel.NodeData;
import org.exoplatform.services.jcr.datamodel.QPathEntry;

/**
 * Created by The eXo Platform SAS.
 * 
 * <br/>Date: 28.01.2009
 * 
 * @author <a href="mailto:peter.nedonosko@exoplatform.com.ua">Peter Nedonosko</a>
 * @version $Id: CompositeItemStatesStorage.java 111 2008-11-11 11:11:11Z pnedonosko $
 */
public class CompositeItemStatesStorage<T extends ItemState> extends AbstractChangesStorage<T>
    implements EditableChangesStorage<T> {

  protected final Member                  member;

  protected final List<ChangesStorage<T>> storages = new ArrayList<ChangesStorage<T>>();

  protected EditableChangesStorage<T>     current;

  public CompositeItemStatesStorage(Member member) {
    this.member = member;
  }

  /**
   * {@inheritDoc}
   */
  public Member getMember() {
    return member;
  }

  private EditableChangesStorage<T> current() {
    if (current == null) {
      current = new EditableItemStatesStorage<T>(null, member); // TODO path null
      storages.add(current);
    }

    return current;
  }

  /**
   * {@inheritDoc}
   */
  public void add(T change) throws IOException {
    current().add(change);
  }

  /**
   * {@inheritDoc}
   */
  public void addAll(ChangesStorage<T> changes) throws IOException {
    // close current, don't use it anymore
    current = null;

    // add storage to the list
    storages.add(changes);

    // if (changes instanceof CompositeItemStatesStorage) {
    // // copy changes
    // } else if (changes instanceof EditableItemStatesStorage) {
    //      
    // } else {
    //      
    // }
  }

  // =========== ChangesStorage impl

  /**
   * {@inheritDoc}
   */
  public void delete() throws IOException {
    current = null;

    for (ChangesStorage<T> cs : storages)
      cs.delete();

    storages.clear();
  }

  /**
   * {@inheritDoc}
   */
  public Iterator<T> getChanges() throws IOException, ClassCastException, ClassNotFoundException {
    // TODO Auto-generated method stub
    return null;
  }

  /**
   * {@inheritDoc}
   */
  public ChangesFile[] getChangesFile() {
    // TODO Auto-generated method stub
    return null;
  }

  /**
   * {@inheritDoc}
   */
  public T getItemState(NodeData parentData, QPathEntry name) {
    // TODO Auto-generated method stub
    return null;
  }

  /**
   * {@inheritDoc}
   */
  public T getItemState(String itemIdentifier) {
    // TODO Auto-generated method stub
    return null;
  }

  /**
   * {@inheritDoc}
   */
  public int size() throws IOException, ClassCastException, ClassNotFoundException {
    // TODO Auto-generated method stub
    return 0;
  }

}
