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

import org.exoplatform.common.util.HierarchicalProperty;
import org.exoplatform.services.jcr.ext.resource.NodeRepresentation;

/**
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id: $
 */
public class NtResourceNodeRepresentation implements NodeRepresentation {

  protected Node node;
  
  public NtResourceNodeRepresentation(Node node) throws RepositoryException {
    this.node = node;
  }

  /* (non-Javadoc)
   * @see org.exoplatform.services.jcr.ext.resource.NodeRepresentation#getContentEncoding()
   */
  public String getContentEncoding() {
    return null;
  }

  /* (non-Javadoc)
   * @see org.exoplatform.services.jcr.ext.resource.NodeRepresentation#getContentLenght()
   */
  public long getContentLenght() throws RepositoryException {
    return node.getProperty("jcr:data").getLength();
  }

  /* (non-Javadoc)
   * @see org.exoplatform.services.jcr.ext.resource.NodeRepresentation#getMediaType()
   */
  public String getMediaType() throws RepositoryException {
    return node.getProperty("jcr:mimeType").getString();
  }

  /* (non-Javadoc)
   * @see org.exoplatform.services.jcr.ext.resource.NodeRepresentation#getLastModified()
   */
  public long getLastModified() throws RepositoryException {
    return node.getProperty("jcr:lastModified").getLong();
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
  public HierarchicalProperty getProperty(String name) throws RepositoryException {
    if ("jcr:primaryType".equals(name) || "jcr:mixinTypes".equals(name) ||
        "jcr:data".equals(name) || "jcr:uuid".equals(name))
      return null;

    String value = node.getProperty(name).getString();
    return new HierarchicalProperty(name, value);
  }

  /* (non-Javadoc)
   * @see org.exoplatform.services.jcr.ext.resource.NodeRepresentation#getPropertyNames()
   */
  public Collection<String> getPropertyNames() throws RepositoryException {
    PropertyIterator iter = node.getProperties();
    ArrayList<String> props = new ArrayList<String>();
    while (iter.hasNext()) {
      String name = iter.nextProperty().getName();
      if (!"jcr:primaryType".equals(name) && !"jcr:mixinTypes".equals(name)
          && !"jcr:data".equals(name) && !"jcr:uuid".equals(name))
        props.add(name);
    }
    return props;
  }

  /* (non-Javadoc)
   * @see org.exoplatform.services.jcr.ext.resource.NodeRepresentation#getInputStream()
   */
  public InputStream getInputStream() throws IOException, RepositoryException {
    return node.getProperty("jcr:data").getStream();
  }

}

