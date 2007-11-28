/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.services.jcr.core;

import java.io.IOException;
import java.io.InputStream;

import javax.jcr.InvalidSerializedDataException;
import javax.jcr.ItemExistsException;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.jcr.Workspace;
import javax.jcr.nodetype.ConstraintViolationException;

import org.exoplatform.services.ext.action.InvocationContext;

/**
 * @author <a href="mailto:Sergey.Kabashnyuk@gmail.com">Sergey Kabashnyuk</a>
 * @version $Id: $
 */
public interface ExtendedWorkspace extends Workspace {

  /**
   * Deserializes an XML document and adds the resulting item subtree as a child
   * of the node at parentAbsPath.
   * 
   * @param parentAbsPath the absolute path of the node below which the
   *          deserialized subtree is added.
   * @param in The <code>Inputstream</code> from which the XML to be
   *          deserilaized is read.
   * @param uuidBehavior a four-value flag that governs how incoming UUIDs are
   *          handled.
   * @param respectPropertyDefinitionsConstraints
   * @throws IOException
   * @throws PathNotFoundException
   * @throws ItemExistsException
   * @throws ConstraintViolationException
   * @throws InvalidSerializedDataException
   * @throws RepositoryException
   */
  @Deprecated
  void importXML(String parentAbsPath,
                 InputStream in,
                 int uuidBehavior,
                 boolean respectPropertyDefinitionsConstraints) throws IOException,
                                                               PathNotFoundException,
                                                               ItemExistsException,
                                                               ConstraintViolationException,
                                                               InvalidSerializedDataException,
                                                               RepositoryException;

  /**
   * Deserializes an XML document and adds the resulting item subtree as a child
   * of the node at parentAbsPath.
   * 
   * @param parentAbsPath the absolute path of the node below which the
   *          deserialized subtree is added.
   * @param in The <code>Inputstream</code> from which the XML to be
   *          deserilaized is read.
   * @param uuidBehavior a four-value flag that governs how incoming UUIDs are
   *          handled.
   * @param context
   * @throws IOException
   * @throws PathNotFoundException
   * @throws ItemExistsException
   * @throws ConstraintViolationException
   * @throws InvalidSerializedDataException
   * @throws RepositoryException
   */
  void importXML(String parentAbsPath, InputStream in, int uuidBehavior, InvocationContext context) throws IOException,
                                                                                                   PathNotFoundException,
                                                                                                   ItemExistsException,
                                                                                                   ConstraintViolationException,
                                                                                                   InvalidSerializedDataException,
                                                                                                   RepositoryException;

}
