/***************************************************************************
 * Copyright 2001-${year} The eXo Platform SARL      All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
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
