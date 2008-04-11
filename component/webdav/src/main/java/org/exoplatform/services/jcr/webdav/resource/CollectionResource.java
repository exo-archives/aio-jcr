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

import java.io.IOException;
import java.io.InputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.jcr.AccessDeniedException;
import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.PathNotFoundException;
import javax.jcr.Property;
import javax.jcr.PropertyIterator;
import javax.jcr.RepositoryException;
import javax.jcr.Value;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.apache.commons.logging.Log;
import org.exoplatform.services.jcr.impl.Constants;
import org.exoplatform.services.jcr.webdav.util.TextUtil;
import org.exoplatform.services.jcr.webdav.xml.WebDavNamespaceContext;
import org.exoplatform.services.log.ExoLogger;

/**
 * Created by The eXo Platform SARL .<br/> Other than
 * nt:file/jcr:content(nt:resource)
 * 
 * @author Gennady Azarenkov
 * @version $Id: $
 */

public class CollectionResource extends GenericResource {
  
  final String PREFIX       = "sv:";

  final String XML_NODE     = PREFIX + "node";

  final String XML_NAME     = PREFIX + "name";

  final String XML_PROPERTY = PREFIX + "property";

  final String XML_HREF     = "xlink:href";

  final String PREFIX_XMLNS = "xmlns:sv";

  final String PREFIX_LINK  = "http://www.jcp.org/jcr/sv/1.0";

  final String XLINK_XMLNS  = "xmlns:xlink";

  final String XLINK_LINK   = "http://www.w3.org/1999/xlink";
  
  private final static Log           LOGGER          = ExoLogger
                                                         .getLogger(CollectionResource.class);

  protected final static Set<String> COLLECTION_SKIP = new HashSet<String>();
  static {
    COLLECTION_SKIP.add("jcr:created");
    COLLECTION_SKIP.add("jcr:primaryType");
  };

  protected final Node               node;

  public CollectionResource(final URI identifier, Node node,
      final WebDavNamespaceContext namespaceContext) throws IllegalResourceTypeException,
      RepositoryException {
    this(COLLECTION, identifier, node, new WebDavNamespaceContext(node.getSession()));
  }

  protected CollectionResource(final int type, final URI identifier, Node node,
      final WebDavNamespaceContext namespaceContext) throws IllegalResourceTypeException,
      RepositoryException {
    super(type, identifier, new WebDavNamespaceContext(node.getSession()));
    if (ResourceUtil.isFile(node))
      throw new IllegalResourceTypeException("Node type is not applicable for COLLECTION resource "
          + node.getPath());
    this.node = node;
  }

  public Set<HierarchicalProperty> getProperties(boolean namesOnly) throws PathNotFoundException,
      AccessDeniedException, RepositoryException {
    Set<HierarchicalProperty> props = super.getProperties(namesOnly);

    PropertyIterator jcrProps = node.getProperties();
    while (jcrProps.hasNext()) {
      Property property = jcrProps.nextProperty();
      if (!COLLECTION_SKIP.contains(property.getName())) {
        QName name = namespaceContext.createQName(property.getName());

        try {
          props.add((namesOnly) ? new HierarchicalProperty(name) : getProperty(name));
        } catch (Exception exc) {
          // System.out.println("Unhandled exception. " + exc.getMessage());
          // exc.printStackTrace();
        }
      }
    }

    return props;
  }

  public HierarchicalProperty getProperty(QName name) throws PathNotFoundException,
      AccessDeniedException, RepositoryException {
    if (name.equals(DISPLAYNAME)) {
      return new HierarchicalProperty(name, node.getName());

    } else if (name.equals(CHILDCOUNT)) {
      return new HierarchicalProperty(name, "" + node.getNodes().getSize());

    } else if (name.equals(CREATIONDATE)) {
      if (node.isNodeType("nt:hierarchyNode")) {
        Calendar created = node.getProperty("jcr:created").getDate();
        HierarchicalProperty creationDate = new HierarchicalProperty(name, created,
            CREATION_PATTERN);
        creationDate.setAttribute("b:dt", "dateTime.tz");
        return creationDate;

      } else {
        throw new PathNotFoundException("Property not found " + CREATIONDATE);
      }

    } else if (name.equals(HASCHILDREN)) {
      if (node.getNodes().getSize() > 0) {
        return new HierarchicalProperty(name, "1");
      } else {
        return new HierarchicalProperty(name, "0");
      }

    } else if (name.equals(ISCOLLECTION)) {
      return new HierarchicalProperty(name, "1");

    } else if (name.equals(ISFOLDER)) {
      return new HierarchicalProperty(name, "1");

    } else if (name.equals(ISROOT)) {
      return new HierarchicalProperty(name, (node.getDepth() == 0) ? "1" : "0");

    } else if (name.equals(PARENTNAME)) {
      if (node.getDepth() == 0) {
        throw new PathNotFoundException();
      }
      return new HierarchicalProperty(name, node.getParent().getName());

    } else if (name.equals(RESOURCETYPE)) {
      HierarchicalProperty collectionProp = new HierarchicalProperty(
          new QName("DAV:", "collection"));
      HierarchicalProperty resourceType = new HierarchicalProperty(name);
      resourceType.addChild(collectionProp);
      return resourceType;

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
      }
      throw new PathNotFoundException();

    } else if (name.equals(ISVERSIONED)) {
      return new HierarchicalProperty(name, "0");

    } else if (name.equals(SUPPORTEDMETHODSET)) {
      return supportedMethodSet();

    } else if (name.equals(ORDERING_TYPE)) {
      if (node.getPrimaryNodeType().hasOrderableChildNodes()) {
        HierarchicalProperty orderingType = new HierarchicalProperty(name);

        // <D:href>DAV:custom</D:href>

        HierarchicalProperty orderHref = orderingType.addChild(new HierarchicalProperty(new QName(
            "DAV:", "href")));
        orderHref.setValue("DAV:custom");

        return orderingType;
      }
      throw new PathNotFoundException();

    } else {

      if ("DAV:".equals(name.getNamespaceURI())) {
        throw new PathNotFoundException();
      }

      Property property = node.getProperty(WebDavNamespaceContext.createName(name));

      if (property.getDefinition().isMultiple()) {
        Value[] values = property.getValues();
        return new HierarchicalProperty(name, values[0].getString());
      } else {
        return new HierarchicalProperty(name, property.getString());
      }

    }
  }

  public boolean isCollection() {
    return true;
  }

  public List<Resource> getResources() throws RepositoryException, IllegalResourceTypeException {
    NodeIterator children = node.getNodes();
    List<Resource> resources = new ArrayList<Resource>();
    while (children.hasNext()) {
      Node node = children.nextNode();

      if (ResourceUtil.isVersioned(node)) {
        if (ResourceUtil.isFile(node)) {
          resources
              .add(new VersionedFileResource(childURI(node.getName()), node, namespaceContext));
        } else {
          resources.add(new VersionedCollectionResource(childURI(node.getName()), node,
              namespaceContext));
        }
      } else {
        if (ResourceUtil.isFile(node)) {
          resources.add(new FileResource(childURI(node.getName()), node, namespaceContext));
        } else {
          resources.add(new CollectionResource(childURI(node.getName()), node, namespaceContext));
        }
      }

    }
    return resources;
  }

  protected final URI childURI(String childName) {
    String childURI = identifier.toASCIIString() + "/" + TextUtil.escape(childName, '%', true);
    // return URI.create(identifier.toASCIIString() + "/" + childName);
    return URI.create(childURI);
  }

  // make a xml representation of the collection and serialize it to srteam
  public InputStream getContentAsStream(final String rootHref) throws IOException {
    final PipedOutputStream po = new PipedOutputStream();
    final PipedInputStream pi = new PipedInputStream(po);
    new Thread() {
      @Override
      public void run() {
        try {
          XMLOutputFactory factory = XMLOutputFactory.newInstance();
          XMLStreamWriter writer = factory.createXMLStreamWriter(po, Constants.DEFAULT_ENCODING);

          writer.writeStartDocument(Constants.DEFAULT_ENCODING, "1.0");
          writer.writeStartElement(XML_NODE);
          writer.writeAttribute(PREFIX_XMLNS, PREFIX_LINK);
          writer.writeAttribute(XLINK_XMLNS, XLINK_LINK);
          String itemName = node.getName();
          writer.writeAttribute(XML_NAME, itemName);
          String itemPath = node.getPath();
          writer.writeAttribute(XML_HREF, rootHref + itemPath);
          // add properties
          for (PropertyIterator pi = node.getProperties(); pi.hasNext();) {
            Property curProperty = pi.nextProperty();
            writer.writeStartElement(XML_PROPERTY);
            writer.writeAttribute(XML_NAME, curProperty.getName());
            String propertyHref = rootHref + curProperty.getPath();
            writer.writeAttribute(XML_HREF, propertyHref);
            writer.writeEndElement();
          }
          // add subnodes
          for (NodeIterator ni = node.getNodes(); ni.hasNext();) {
            Node childNode = ni.nextNode();
            writer.writeStartElement(XML_NODE);
            writer.writeAttribute(XML_NAME, childNode.getName());
            String childNodeHref = rootHref + childNode.getPath();
            writer.writeAttribute(XML_HREF, childNodeHref);
            writer.writeEndElement();
          }
          writer.writeEndElement();
          writer.writeEndDocument();
        } catch (RepositoryException e) {
          LOGGER.error("Error has occured : ", e);
        } catch (XMLStreamException e) {
          LOGGER.error("Error has occured while xml processing : ", e);
        } finally {
          try {
            po.flush();
            po.close();
          } catch (IOException e) {
            e.printStackTrace();
          }
        }
      }
    }.start();
    return pi;
  }

}
