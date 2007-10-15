/***************************************************************************
 * Copyright 2001-${year} The eXo Platform SARL      All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.services.jcr.rmi.api.client.iterator;

import javax.jcr.query.Row;
import javax.jcr.query.RowIterator;

import org.exoplatform.services.jcr.rmi.api.client.LocalAdapterFactory;
import org.exoplatform.services.jcr.rmi.api.remote.RemoteIterator;
import org.exoplatform.services.jcr.rmi.api.remote.RemoteRow;

/**
 * A ClientIterator for iterating remote rows.
 */
public class ClientRowIterator extends ClientIterator implements RowIterator {

  /**
   * Creates a ClientRowIterator instance.
   * 
   * @param iterator remote iterator
   * @param factory local adapter factory
   */
  public ClientRowIterator(RemoteIterator iterator, LocalAdapterFactory factory) {
    super(iterator, factory);
  }

  /**
   * Creates and returns a local adapter for the given remote row.
   * 
   * @param remote remote reference
   * @return local adapter
   * @see ClientIterator#getObject(Object)
   */
  protected Object getObject(Object remote) {
    return getFactory().getRow((RemoteRow) remote);
  }

  /**
   * Returns the next row in this iteration.
   * 
   * @return next row
   * @see RowIterator#nextRow()
   */
  public Row nextRow() {
    return (Row) next();
  }

}
