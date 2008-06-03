/**
 * Copyright (C) 2003-2008 eXo Platform SAS.
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

package org.exoplatform.services.jcr.ext.resource.representation;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;

import javax.jcr.Node;
import javax.jcr.PropertyIterator;
import javax.jcr.RepositoryException;

import org.exoplatform.services.jcr.ext.resource.NodeRepresentation;

/**
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id: $
 */
public class Groovy2RestNodeRepresentation implements NodeRepresentation {
  
  private Node node;
  
  public Groovy2RestNodeRepresentation(Node node) {
    this.node = node;
  }

  /* (non-Javadoc)
   * @see org.exoplatform.services.jcr.ext.resource.NodeRepresentation#getContentEncoding()
   */
  public String getContentEncoding() {
    try { 
      return node.getProperty("jcr:encoding").getString();
    } catch (RepositoryException e) {
      return null;
    }
  }

  /* (non-Javadoc)
   * @see org.exoplatform.services.jcr.ext.resource.NodeRepresentation#getContentLenght()
   */
  public long getContentLenght() throws RepositoryException {
    return node.getProperty("jcr:data").getLength();
  }

  /* (non-Javadoc)
   * @see org.exoplatform.services.jcr.ext.resource.NodeRepresentation#getInputStream()
   */
  public InputStream getInputStream() throws IOException, RepositoryException {
    return node.getProperty("jcr:data").getStream();
  }

  /* (non-Javadoc)
   * @see org.exoplatform.services.jcr.ext.resource.NodeRepresentation#getLastModified()
   */
  public long getLastModified() throws RepositoryException {
    return node.getProperty("jcr:lastModified").getLong();
  }

  /* (non-Javadoc)
   * @see org.exoplatform.services.jcr.ext.resource.NodeRepresentation#getMediaType()
   */
  public String getMediaType() throws RepositoryException {
    return node.getProperty("jcr:mimeType").getString();
  }

  /* (non-Javadoc)
   * @see org.exoplatform.services.jcr.ext.resource.NodeRepresentation#getNode()
   */
  public Node getNode() {
    return node;
  }

  /* (non-Javadoc)
   * @see org.exoplatform.services.jcr.ext.resource.NodeRepresentation#getProperty(java.lang.String)
   */
  public String getProperty(String name) throws RepositoryException {
    return node.getProperty(name).getString();
  }

  /* (non-Javadoc)
   * @see org.exoplatform.services.jcr.ext.resource.NodeRepresentation#getPropertyNames()
   */
  public Collection<String> getPropertyNames() throws RepositoryException {
    PropertyIterator iter = node.getProperties();
    ArrayList<String> props = new ArrayList<String>();
    while (iter.hasNext())
      props.add(iter.nextProperty().getName());
    
    return props;
  }

}

