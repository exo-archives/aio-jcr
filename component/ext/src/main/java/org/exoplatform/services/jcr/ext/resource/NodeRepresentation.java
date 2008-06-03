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

package org.exoplatform.services.jcr.ext.resource;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

/**
 * Created by The eXo Platform SAS        .
 * @author Gennady Azarenkov
 * @version $Id: $
 */

public interface NodeRepresentation {
  
  /**
   * @return Mimetype for this representation.
   */
  String getMediaType() throws RepositoryException;
  
  /**
   * @return the content length or -1 if content length unknown 
   * @throws RepositoryException
   */
  long getContentLenght() throws RepositoryException;
  
  /**
   * @return the content encoding or null if it unknown.
   */
  String getContentEncoding();

  /**
   * @return the stream.
   * @throws IOException
   * @throws RepositoryException
   */
  InputStream getInputStream() throws IOException, RepositoryException;
  
  /**
   * @return the collection of node properties name.
   */
  Collection <String> getPropertyNames () throws RepositoryException;
  
  /**
   * @param name the name of properties.
   * @return the properties with specified name.
   */
  String getProperty(String name) throws RepositoryException;
  
  /**
   * Get date of last modified, it useful for nt:file.
   * @return the date of last modified.
   * @throws RepositoryException
   */
  long getLastModified() throws RepositoryException;
  
  /**
   * @return the node.
   */
  Node getNode();
  
}

