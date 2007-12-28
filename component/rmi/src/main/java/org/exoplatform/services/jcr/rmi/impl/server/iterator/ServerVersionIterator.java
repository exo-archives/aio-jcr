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

import javax.jcr.version.Version;
import javax.jcr.version.VersionIterator;

import org.exoplatform.services.jcr.rmi.api.remote.RemoteAdapterFactory;



/**
 * A ServerIterator for iterating versions.
 */
public class ServerVersionIterator extends ServerIterator {

    /**
     * Creates a ServerVersionIterator instance.
     *
     * @param iterator      local version iterator
     * @param factory       remote adapter factory
     * @param maxBufferSize maximum size of the element buffer
     * @throws RemoteException on RMI errors
     */
    public ServerVersionIterator(
            VersionIterator iterator, RemoteAdapterFactory factory,
            int maxBufferSize) throws RemoteException {
        super(iterator, factory, maxBufferSize);
    }

    /**
     * Creates and returns a remote adapter for the given version..
     *
     * @param object local object
     * @return remote adapter
     * @throws RemoteException on RMI errors
     * @see ServerIterator#getRemoteObject(Object)
     */
    protected Object getRemoteObject(Object object) throws RemoteException {
        return getFactory().getRemoteVersion((Version) object);
    }

}
