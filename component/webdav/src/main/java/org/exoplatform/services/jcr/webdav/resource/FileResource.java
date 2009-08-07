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

package org.exoplatform.services.jcr.webdav.resource;

import java.io.InputStream;
import java.net.URI;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Set;

import javax.jcr.AccessDeniedException;
import javax.jcr.Node;
import javax.jcr.PathNotFoundException;
import javax.jcr.Property;
import javax.jcr.PropertyIterator;
import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import javax.xml.namespace.QName;

import org.exoplatform.common.util.HierarchicalProperty;
import org.exoplatform.services.jcr.webdav.xml.WebDavNamespaceContext;

/**
 * Created by The eXo Platform SARL .<br/>
 * Resource containing JCR's nt:file/jcr:content underneath. Identified by nt:file's URI
 * jcr:content's jcr:data property contains file's payload
 * 
 * @author Gennady Azarenkov
 * @version $Id$
 */

public class FileResource extends GenericResource {

  protected final static Set<String> FILE_SKIP    = new HashSet<String>();
  static {
    FILE_SKIP.add("jcr:primaryType");
    FILE_SKIP.add("jcr:mixinTypes");
    FILE_SKIP.add("jcr:created");
  };

  protected final static Set<String> CONTENT_SKIP = new HashSet<String>();
  static {
    CONTENT_SKIP.add("jcr:primaryType");
    CONTENT_SKIP.add("jcr:mixinTypes");
    CONTENT_SKIP.add("jcr:data");
    CONTENT_SKIP.add("jcr:lastModified");
    CONTENT_SKIP.add("jcr:mimeType");
    CONTENT_SKIP.add("jcr:uuid");
  };

  protected final Node               node;

  public FileResource(final URI identifier, Node node, final WebDavNamespaceContext namespaceContext) throws IllegalResourceTypeException,
      RepositoryException {
    this(FILE, identifier, node, namespaceContext);
  }

  protected FileResource(final int type,
                         final URI identifier,
                         Node node,
                         final WebDavNamespaceContext namespaceContext) throws IllegalResourceTypeException,
      RepositoryException {
    super(type, identifier, namespaceContext);
    if (!ResourceUtil.isFile(node))
      throw new IllegalResourceTypeException("Node type is not applicable for FILE resource "
          + node.getPath());
    this.node = node;
  }

  /*
   * (non-Javadoc)
   * @see org.exoplatform.services.jcr.webdav.resource.GenericResource#getProperties(boolean)
   */
  public Set<HierarchicalProperty> getProperties(boolean namesOnly) throws PathNotFoundException,
                                                                   AccessDeniedException,
                                                                   RepositoryException {

    Set<HierarchicalProperty> props = super.getProperties(namesOnly);
    props.add(namesOnly ? new HierarchicalProperty(GETLASTMODIFIED) : getProperty(GETLASTMODIFIED));
    props.add(namesOnly
        ? new HierarchicalProperty(GETCONTENTLENGTH)
        : getProperty(GETCONTENTLENGTH));
    props.add(namesOnly ? new HierarchicalProperty(GETCONTENTTYPE) : getProperty(GETCONTENTTYPE));

    Set<QName> presents = new HashSet<QName>();

    PropertyIterator jcrProps = node.getProperties();
    while (jcrProps.hasNext()) {
      Property property = jcrProps.nextProperty();
      if (!FILE_SKIP.contains(property.getName())) {
        QName name = namespaceContext.createQName(property.getName());
        presents.add(name);
        props.add((namesOnly) ? new HierarchicalProperty(name) : getProperty(name));
      }
    }

    jcrProps = contentNode().getProperties();
    while (jcrProps.hasNext()) {
      Property property = jcrProps.nextProperty();
      if (!CONTENT_SKIP.contains(property.getName())) {
        QName name = namespaceContext.createQName(property.getName());

        if (presents.contains(name)) {
          continue;
        }

        props.add((namesOnly) ? new HierarchicalProperty(name) : getProperty(name));
      }
    }

    return props;
  }

  public HierarchicalProperty getProperty(QName name) throws PathNotFoundException,
                                                     AccessDeniedException,
                                                     RepositoryException {
    if (name.equals(DISPLAYNAME)) {
      return new HierarchicalProperty(name, node.getName());

    } else if (name.equals(CREATIONDATE)) {
      Calendar created = node.getProperty("jcr:created").getDate();

      HierarchicalProperty creationDate = new HierarchicalProperty(name, created, CREATION_PATTERN);
      creationDate.setAttribute("b:dt", "dateTime.tz");
      return creationDate;

    } else if (name.equals(CHILDCOUNT)) {
      return new HierarchicalProperty(name, "0");

    } else if (name.equals(GETCONTENTLENGTH)) {
      return new HierarchicalProperty(name, ""
          + node.getNode("jcr:content").getProperty("jcr:data").getLength());

    } else if (name.equals(GETCONTENTTYPE)) {
      return new HierarchicalProperty(name, node.getNode("jcr:content")
                                                .getProperty("jcr:mimeType")
                                                .getString());

    } else if (name.equals(GETLASTMODIFIED)) {
      Calendar modified = contentNode().getProperty("jcr:lastModified").getDate();
      HierarchicalProperty lastModified = new HierarchicalProperty(name,
                                                                   modified,
                                                                   MODIFICATION_PATTERN);
      lastModified.setAttribute("b:dt", "dateTime.rfc1123");
      return lastModified;

    } else if (name.equals(HASCHILDREN)) {
      return new HierarchicalProperty(name, "0");

    } else if (name.equals(ISCOLLECTION)) {
      return new HierarchicalProperty(name, "0");

    } else if (name.equals(ISFOLDER)) {
      return new HierarchicalProperty(name, "0");

    } else if (name.equals(ISROOT)) {
      return new HierarchicalProperty(name, "0");

    } else if (name.equals(PARENTNAME)) {
      return new HierarchicalProperty(name, node.getParent().getName());

    } else if (name.equals(RESOURCETYPE)) {
      return new HierarchicalProperty(name);

    } else if (name.equals(SUPPORTEDLOCK)) {
      if (!node.canAddMixin("mix:lockable")) {
        throw new PathNotFoundException();
      }
      return supportedLock();
    } else if (name.equals(LOCKDISCOVERY)) {
      if (node.isLocked()) {
        String token = node.getLock().getLockToken();
        String owner = node.getLock().getLockOwner();
        return lockDiscovery(token, owner, "86400");
      } else
        throw new PathNotFoundException();
    } else if (name.equals(ISVERSIONED)) {
      return new HierarchicalProperty(name, "0");

    } else if (name.equals(SUPPORTEDMETHODSET)) {
      return supportedMethodSet();

    } else {
      try {
        Property property = node.getProperty(WebDavNamespaceContext.createName(name));
        String propertyValue;
        if (property.getDefinition().isMultiple()) {

          if (property.getValues().length == 0) {
            throw new PathNotFoundException();
          }

          propertyValue = property.getValues()[0].getString();
        } else {
          propertyValue = property.getString();
        }
        return new HierarchicalProperty(name, propertyValue);
      } catch (PathNotFoundException e) {
        Property property = contentNode().getProperty(WebDavNamespaceContext.createName(name));
        String propertyValue;
        if (property.getDefinition().isMultiple()) {
          propertyValue = property.getValues()[0].getString();
        } else {
          propertyValue = property.getString();
        }
        return new HierarchicalProperty(name, propertyValue);
      }

    }
  }

  /*
   * (non-Javadoc)
   * @see org.exoplatform.services.jcr.webdav.resource.Resource#isCollection()
   */
  public boolean isCollection() {
    return false;
  }

  public String getContentAsText() throws RepositoryException {
    return contentNode().getProperty("jcr:data").getString();
  }

  public InputStream getContentAsStream() throws RepositoryException {
    return contentNode().getProperty("jcr:data").getStream();
  }

  public boolean isTextContent() {
    try {
      return contentNode().getProperty("jcr:data").getType() != PropertyType.BINARY;
    } catch (RepositoryException e) {
      e.printStackTrace();
      return false;
    }
  }

  public Node contentNode() throws RepositoryException {
    return node.getNode("jcr:content");
  }

}
