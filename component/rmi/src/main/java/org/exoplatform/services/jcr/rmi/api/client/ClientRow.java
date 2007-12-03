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
package org.exoplatform.services.jcr.rmi.api.client;

import java.rmi.RemoteException;

import javax.jcr.RepositoryException;
import javax.jcr.Value;
import javax.jcr.query.Row;

import org.exoplatform.services.jcr.rmi.api.exceptions.RemoteRepositoryException;
import org.exoplatform.services.jcr.rmi.api.remote.RemoteRow;

/**
 * Local adapter for the JCR-RMI {@link RemoteRow RemoteRow} inteface. This
 * class makes a remote query row locally available using the JCR
 * {@link Row Row} interface.
 * 
 * @see javax.jcr.query.Row Row
 * @see org.exoplatform.services.jcr.rmi.api.remote.RemoteRow
 */
public class ClientRow implements Row {

  /** The remote query row. */
  private RemoteRow remote;

  /**
   * Creates a client adapter for the given remote query row.
   * 
   * @param remote remote query row
   */
  public ClientRow(RemoteRow remote) {
    this.remote = remote;
  }

  /** {@inheritDoc} */
  public Value[] getValues() throws RepositoryException {
    try {
      return remote.getValues();
    } catch (RemoteException ex) {
      throw new RemoteRepositoryException(ex);
    }
  }

  /** {@inheritDoc} */
  public Value getValue(String s) throws RepositoryException {
    try {
      return remote.getValue(s);
    } catch (RemoteException ex) {
      throw new RemoteRepositoryException(ex);
    }
  }
}
