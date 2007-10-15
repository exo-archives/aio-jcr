/***************************************************************************
 * Copyright 2001-${year} The eXo Platform SARL      All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.services.jcr.rmi.api.remote.iterator;

import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.NoSuchElementException;

import org.exoplatform.services.jcr.rmi.api.remote.RemoteIterator;

/**
 * A buffered remote iterator. Used to transfer a remote iterator reference
 * along with a buffer of the first few iterator elements in one network
 * transmission.
 */
public class BufferIterator implements RemoteIterator, Serializable {

    /** The element buffer. Set to <code>null</code> when the iterator ends. */
    private Object[] buffer;

    /** Cached size of the iterator. */
    private final long size;

    /** Remote iterator reference. */
    private final RemoteIterator remote;

    /**
     * Creates a new buffered remote iterator.
     *
     * @param buffer first elements in the iterator
     * @param size   total iterator size
     * @param remote reference to the remaining iterator
     */
    public BufferIterator(Object[] buffer, long size, RemoteIterator remote) {
        this.buffer = buffer;
        this.size = size;
        this.remote = remote;
    }

    /**
     * Returns the cached size of the iterator.
     *
     * @return iterator size, or <code>-1</code> if unknown
     * @see RemoteIterator#getSize()
     */
    public long getSize() {
        return size;
    }

    /**
     * Skips the given number of elements. First discards elements from the
     * element buffer and only then contacts the remote iterator.
     *
     * @param items number of items to skip
     * @throws IllegalArgumentException if <code>items</code> is negative
     * @throws NoSuchElementException if skipped past the last element
     * @throws RemoteException on RMI errors
     * @see RemoteIterator#skip(long)
     */
    public void skip(long items)
            throws IllegalArgumentException, NoSuchElementException,
            RemoteException {
        if (items < 0) {
            throw new IllegalArgumentException("Negative skip is not allowed");
        } else if (buffer == null && items > 0) {
            throw new NoSuchElementException("Skipped past the last element");
        } else if (items > buffer.length) {
            remote.skip(items - buffer.length);
            buffer = remote.nextObjects();
        } else {
            Object[] tmp = new Object[buffer.length - (int) items];
            System.arraycopy(buffer, (int) items, tmp, 0, tmp.length);
            buffer = tmp;
        }
    }

    /**
     * Returns the currently buffered elements and fills in the buffer
     * with next elements.
     *
     * @return buffered elements, or <code>null</code> if the iterator has ended
     * @throws RemoteException on RMI errors
     * @see RemoteIterator#nextObjects()
     */
    public Object[] nextObjects() throws RemoteException {
        if (buffer == null) {
            return null;
        } else {
            Object[] tmp = buffer;
            buffer = remote.nextObjects();
            return tmp;
        }
    }

}
