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

import javax.jcr.AccessDeniedException;
import javax.jcr.InvalidSerializedDataException;
import javax.jcr.ItemExistsException;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.jcr.Workspace;
import javax.jcr.lock.LockException;
import javax.jcr.nodetype.ConstraintViolationException;
import javax.jcr.version.VersionException;

/**
 * SAX content handler for importing XML data to a JCR
 * {@link Workspace Workspace}. This utility class can be used to implement the
 * {@link Workspace#getImportContentHandler(String, int) Workspace.getImportContentHandler(String, int)}
 * method in terms of the
 * {@link Workspace#importXML(String, java.io.InputStream, int) Workspace.importXML(String, InputStream, int)}
 * method.
 */
public class WorkspaceImportContentHandler extends ImportContentHandler {

  /** The repository workspace. */
  private Workspace workspace;

  /** The import content path. */
  private String    path;

  /** The UUID behaviour. */
  private int       uuidBehaviour;

  /**
   * Creates a SAX content handler for importing XML data to the given workspace
   * and path using the given UUID behaviour.
   * 
   * @param workspace repository workspace
   * @param path import content path
   * @param uuidBehaviour UUID behaviour
   * @throws RepositoryException
   * @throws LockException
   * @throws ConstraintViolationException
   * @throws VersionException
   */
  public WorkspaceImportContentHandler(Workspace workspace, String path, int uuidBehaviour)
      throws VersionException, ConstraintViolationException, LockException, RepositoryException {
    super(workspace.getSession(), path);
    this.workspace = workspace;
    this.path = path;
    this.uuidBehaviour = uuidBehaviour;
  }

  /**
   * Imports the serialized XML stream using the standard
   * {@link Workspace#importXML(String, java.io.InputStream, int) Workspace.importXML(String, InputStream, int)}
   * method. {@inheritDoc}
   * 
   * @throws RepositoryException
   * @throws IOException
   * @throws AccessDeniedException
   * @throws LockException
   * @throws InvalidSerializedDataException
   * @throws ConstraintViolationException
   * @throws ItemExistsException
   * @throws PathNotFoundException
   */
  @Override
  protected void importXML(byte[] xml) throws IOException, PathNotFoundException,
      ItemExistsException, ConstraintViolationException, InvalidSerializedDataException,
      RepositoryException {
    workspace.importXML(path, new ByteArrayInputStream(xml), uuidBehaviour);
  }

}
