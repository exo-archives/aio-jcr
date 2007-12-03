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
package org.exoplatform.services.jcr.rmi.impl.server.iterator;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.NoSuchElementException;

import javax.jcr.RangeIterator;

import org.exoplatform.services.jcr.rmi.api.remote.RemoteAdapterFactory;
import org.exoplatform.services.jcr.rmi.api.remote.RemoteIterator;
import org.exoplatform.services.jcr.rmi.impl.server.ServerObject;




/**
 * Remote adapter for the JCR {@link RangeIterator} interface. This
 * class makes a local iterator available as an RMI service using teh
 * {@link RemoteIterator} interface.
 */
public abstract class ServerIterator extends ServerObject
        implements RemoteIterator {

    /** The adapted local iterator. */
    private final RangeIterator iterator;

    /** The maximum number of elements to send per request. */
    private final int maxBufferSize;

    /**
     * The cached number of elements in the iterator, -1 if the iterator
     * size is unknown, or -2 if the size has not been retrieved from the
     * adapted local iterator. This variable is useful in cases when the
     * underlying iterator does not know its sizes (getSize() returns -1)
     * but we reach the end of the iterator in a nextObjects() call and
     * can thus determine the size of the iterator.
     */
    private long size;

    /**
     * Creates a remote adapter for the given local item.
     *
     * @param iterator      local iterator to be adapted
     * @param factory       remote adapter factory
     * @param maxBufferSize maximum buffer size
     * @throws RemoteException on RMI errors
     */
    public ServerIterator(
            RangeIterator iterator, RemoteAdapterFactory factory,
            int maxBufferSize) throws RemoteException {
        super(factory);
        this.iterator = iterator;
        this.maxBufferSize = maxBufferSize;
        this.size = -2;
    }

    /**
     * Returns the size of the iterator. The size is cached by invoking the
     * adapted local iterator when this method is first called or by
     * determining the size from an end-of-iterator condition in nextObjects().
     *
     * @return size of the iterator
     * @throws RemoteException on RMI errors
     * @see RemoteIterator#getSize()
     * @see RangeIterator#getSize()
     */
    public long getSize() throws RemoteException {
        if (size == -2) {
            size = iterator.getSize();
        }
        return size;
    }

    /**
     * Skips the given number of elements.
     *
     * @param items number of elements to skip
     * @throws NoSuchElementException if skipped past the last element
     * @throws RemoteException on RMI errors
     */
    public void skip(long items)
            throws NoSuchElementException, RemoteException {
        try {
            iterator.skip(items);
        } catch (NoSuchElementException e) {
            throw new NoSuchElementException(e.getMessage());
        }
    }

    /**
     * Returns a remote adapter for the given local object. This abstract
     * method is used by {@link #nextObjects(int)} to convert the local
     * objects to remote references to be sent to the client.
     * <p>
     * Subclasses should implement this method to use the remote adapter
     * factory to create remote adapters of the specific element type.
     *
     * @param object local object
     * @return remote adapter
     * @throws RemoteException on RMI errors
     */
    protected abstract Object getRemoteObject(Object object)
            throws RemoteException;

    /**
     * Returns an array of remote references to the next elements in this
     * iteration.
     *
     * @return array of remote references, or <code>null</code>
     * @throws RemoteException on RMI errors
     * @see RemoteIterator#nextObjects(int)
     * @see java.util.Iterator#next()
     */
    public Object[] nextObjects() throws RemoteException {
        if (!iterator.hasNext()) {
            return null;
        } else {
            ArrayList items = new ArrayList();
            while (items.size() < maxBufferSize && iterator.hasNext()) {
                items.add(getRemoteObject(iterator.next()));
            }
            if (!iterator.hasNext()) {
                size = iterator.getPosition();
            }
            return items.toArray();
        }
    }

}
