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
package org.exoplatform.services.jcr.rmi.api.xml;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import javax.jcr.InvalidSerializedDataException;
import javax.jcr.ItemExistsException;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.lock.LockException;
import javax.jcr.nodetype.ConstraintViolationException;
import javax.jcr.version.VersionException;

/**
 * SAX content handler for importing XML data to a JCR {@link Session Session}.
 * This utility class can be used to implement the
 * {@link Session#getImportContentHandler(String, int) Session.getImportContentHandler(String, int)}
 * method in terms of the
 * {@link Session#importXML(String, java.io.InputStream, int) Session.importXML(String, InputStream, int)}
 * method.
 */
public class SessionImportContentHandler extends ImportContentHandler {

  /** The repository session. */
  private Session session;

  /** The import content path. */
  private String  path;

  /** The uuid behaviour mode */
  private int     mode;

  /**
   * Creates a SAX content handler for importing XML data to the given session
   * and path.
   * 
   * @param session repository session
   * @param path import content path
   * @param uuidBehaviour UUID behaviour mode
   * @throws RepositoryException
   * @throws LockException
   * @throws ConstraintViolationException
   * @throws VersionException
   */
  public SessionImportContentHandler(Session session, String path, int uuidBehaviour)
      throws VersionException, ConstraintViolationException, LockException, RepositoryException {
    super(session, path);
    this.session = session;
    this.path = path;
    this.mode = uuidBehaviour;
  }

  /**
   * Imports the serialized XML stream using the standard
   * {@link Session#importXML(String, java.io.InputStream, int) Session.importXML(String, InputStream, int)}
   * method. {@inheritDoc}
   * 
   * @throws RepositoryException
   * @throws IOException
   * @throws LockException
   * @throws InvalidSerializedDataException
   * @throws VersionException
   * @throws ConstraintViolationException
   * @throws ItemExistsException
   * @throws PathNotFoundException
   */
  @Override
  protected void importXML(byte[] xml) throws PathNotFoundException, ItemExistsException,
      ConstraintViolationException, VersionException, InvalidSerializedDataException,
      LockException, IOException, RepositoryException {
    session.importXML(path, new ByteArrayInputStream(xml), mode);
  }

}
