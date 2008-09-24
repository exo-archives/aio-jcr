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
package org.exoplatform.services.jcr.core;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import javax.jcr.InvalidSerializedDataException;
import javax.jcr.ItemExistsException;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.jcr.Workspace;
import javax.jcr.nodetype.ConstraintViolationException;

/**
 * @author <a href="mailto:Sergey.Kabashnyuk@gmail.com">Sergey Kabashnyuk</a>
 * @version $Id: ExtendedWorkspace.java 12649 2008-04-02 12:46:37Z ksm $
 */
public interface ExtendedWorkspace extends Workspace {

  /**
   * Deserializes an XML document and adds the resulting item subtree as a child of the node at
   * parentAbsPath.
   * 
   * @param parentAbsPath
   *          the absolute path of the node below which the deserialized subtree is added.
   * @param in
   *          The <code>Inputstream</code> from which the XML to be deserilaized is read.
   * @param uuidBehavior
   *          a four-value flag that governs how incoming UUIDs are handled.
   * @param context
   * @throws IOException
   * @throws PathNotFoundException
   * @throws ItemExistsException
   * @throws ConstraintViolationException
   * @throws InvalidSerializedDataException
   * @throws RepositoryException
   */
  void importXML(String parentAbsPath, InputStream in, int uuidBehavior, Map<String, Object> context) throws IOException,
                                                                                                     PathNotFoundException,
                                                                                                     ItemExistsException,
                                                                                                     ConstraintViolationException,
                                                                                                     InvalidSerializedDataException,
                                                                                                     RepositoryException;

}
