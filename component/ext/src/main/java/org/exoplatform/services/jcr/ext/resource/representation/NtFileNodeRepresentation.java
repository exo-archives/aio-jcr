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
import javax.jcr.PathNotFoundException;
import javax.jcr.Property;
import javax.jcr.PropertyIterator;
import javax.jcr.RepositoryException;
import javax.jcr.UnsupportedRepositoryOperationException;

import org.exoplatform.common.util.HierarchicalProperty;
import org.exoplatform.services.jcr.core.ExtendedSession;
import org.exoplatform.services.jcr.ext.resource.NodeRepresentation;

/**
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id: $
 */
public class NtFileNodeRepresentation implements NodeRepresentation {

  private Node               node;

  private NodeRepresentation content;

  /**
   * @param node
   * @param content
   * @throws RepositoryException
   */
  public NtFileNodeRepresentation(Node node, NodeRepresentation content) throws RepositoryException {
    this.node = node;
    this.content = content;
    // content = node.getNode("jcr:content");
  }

  /*
   * (non-Javadoc)
   * @see org.exoplatform.services.jcr.ext.resource.NodeRepresentation#getContentEncoding()
   */
  public String getContentEncoding() {
    return null;
  }

  /*
   * (non-Javadoc)
   * @see org.exoplatform.services.jcr.ext.resource.NodeRepresentation#getContentLenght()
   */
  public long getContentLenght() throws RepositoryException {
    return content.getContentLenght();
  }

  /*
   * (non-Javadoc)
   * @see org.exoplatform.services.jcr.ext.resource.NodeRepresentation#getMediaType()
   */
  public String getMediaType() throws RepositoryException {
    return content.getMediaType();
  }

  /*
   * (non-Javadoc)
   * @see org.exoplatform.services.jcr.ext.resource.NodeRepresentation#getLastModified()
   */
  public long getLastModified() throws RepositoryException {
    return content.getLastModified();
  }

  /*
   * (non-Javadoc)
   * @see org.exoplatform.services.jcr.ext.resource.NodeRepresentation#getNode()
   */
  public Node getNode() {
    return node;
  }

  /*
   * (non-Javadoc)
   * @see org.exoplatform.services.jcr.ext.resource.NodeRepresentation#getProperty(java.lang.String)
   */
  public HierarchicalProperty getProperty(String name) throws RepositoryException {
    if ("jcr:primaryType".equals(name) || "jcr:mixinTypes".equals(name))
      return null;

    if (content == null)
      return null;

    if (content.getProperty(name) != null) {
      return content.getProperty(name);
    }

    try {
      String value = node.getProperty(name).getString();
      String ns = ((ExtendedSession) node.getSession()).getLocationFactory()
                                                       .parseJCRName(name)
                                                       .getNamespace();
      return new HierarchicalProperty(name, value, ns);
    } catch (PathNotFoundException e) {
      return null;
    }

  }

  /*
   * (non-Javadoc)
   * @see
   * org.exoplatform.services.jcr.ext.resource.NodeRepresentation#getProperties(java.lang.String)
   */
  public Collection<HierarchicalProperty> getProperties(String name) throws RepositoryException {
    ArrayList<HierarchicalProperty> props = new ArrayList<HierarchicalProperty>();
    if ("jcr:primaryType".equals(name) || "jcr:mixinTypes".equals(name))
      return null;

    if (content != null && content.getProperty(name) != null) {
      props.addAll(content.getProperties(name));
    }

    try {
      String ns = ((ExtendedSession) node.getSession()).getLocationFactory()
                                                       .parseJCRName(name)
                                                       .getNamespace();
      PropertyIterator iter = node.getProperties(name);
      while (iter.hasNext()) {
        Property prop = iter.nextProperty();
        props.add(new HierarchicalProperty(name, prop.getString(), ns));
      }
    } catch (PathNotFoundException e) {
      return null;
    }
    return props;
  }

  /*
   * (non-Javadoc)
   * @see org.exoplatform.services.jcr.ext.resource.NodeRepresentation#getPropertyNames()
   */
  public Collection<String> getPropertyNames() throws RepositoryException {
    // List <String> propnames = new ArrayList<String>();
    PropertyIterator iter = node.getProperties();
    ArrayList<String> props = new ArrayList<String>();
    while (iter.hasNext()) {
      String name = iter.nextProperty().getName();
      if (!"jcr:primaryType".equals(name) && !"jcr:mixinTypes".equals(name))
        props.add(name);
    }
    props.addAll(content.getPropertyNames());
    return props;
  }

  /*
   * (non-Javadoc)
   * @see org.exoplatform.services.jcr.ext.resource.NodeRepresentation#getInputStream()
   */
  public InputStream getInputStream() throws IOException, RepositoryException {
    return content.getInputStream();
  }

  /* (non-Javadoc)
   * @see org.exoplatform.services.jcr.ext.resource.NodeRepresentation#addProperties(java.lang.String, java.util.Collection)
   */
  public void addProperties(Collection<HierarchicalProperty> properties) throws UnsupportedRepositoryOperationException {
    content.addProperties(properties);

  }

  /* (non-Javadoc)
   * @see org.exoplatform.services.jcr.ext.resource.NodeRepresentation#addProperty(java.lang.String, org.exoplatform.common.util.HierarchicalProperty)
   */
  public void addProperty(HierarchicalProperty property) throws UnsupportedRepositoryOperationException {
    content.addProperty(property);

  }

  /* (non-Javadoc)
   * @see org.exoplatform.services.jcr.ext.resource.NodeRepresentation#removeProperty(java.lang.String)
   */
  public void removeProperty(String name) throws UnsupportedRepositoryOperationException {
    content.removeProperty(name);

  }
}
