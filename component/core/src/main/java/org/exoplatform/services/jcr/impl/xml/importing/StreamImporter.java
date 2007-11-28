/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.services.jcr.impl.xml.importing;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.jcr.InvalidSerializedDataException;
import javax.jcr.RepositoryException;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.Namespace;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import org.apache.commons.logging.Log;
import org.exoplatform.services.ext.action.InvocationContext;
import org.exoplatform.services.jcr.impl.core.NodeImpl;
import org.exoplatform.services.jcr.impl.xml.XmlSaveType;
import org.exoplatform.services.log.ExoLogger;

/**
 * @author <a href="mailto:Sergey.Kabashnyuk@gmail.com">Sergey Kabashnyuk</a>
 * @version $Id: $
 */
public class StreamImporter implements RawDataImporter {
  /**
   * 
   */
  private final Log             log                  = ExoLogger.getLogger("jcr.StreamImporter");

  /**
   * 
   */
  private final ContentImporter importer;

  /**
   * 
   */
  private boolean               namespacesRegistered = false;

  /**
   * @param saveType
   * @param parent
   * @param uuidBehavior
   * @param respectPropertyDefinitionsConstraints
   */
  public StreamImporter(NodeImpl parent,
                        int uuidBehavior,
                        XmlSaveType saveType,
                        InvocationContext context) {
    super();
    this.importer = createContentImporter(parent, uuidBehavior, saveType, context);
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.exoplatform.services.jcr.impl.xml.importing.RawDataImporter#createContentImporter(org.exoplatform.services.jcr.impl.core.NodeImpl,
   *      int, org.exoplatform.services.jcr.impl.xml.XmlSaveType,
   *      org.exoplatform.services.ext.action.InvocationContext)
   */
  public ContentImporter createContentImporter(NodeImpl parent,
                                               int uuidBehavior,
                                               XmlSaveType saveType,
                                               InvocationContext context) {
    return new NeutralImporter(parent, uuidBehavior, saveType, context);

  }

  /**
   * @param stream
   * @throws RepositoryException
   */
  public void importStream(InputStream stream) throws RepositoryException {

    XMLInputFactory factory = XMLInputFactory.newInstance();
    if (log.isDebugEnabled())
      log.debug("FACTORY: " + factory);

    try {

      XMLEventReader reader = factory.createXMLEventReader(stream);

      if (log.isDebugEnabled())
        log.debug("Start event handling");
      while (reader.hasNext()) {
        XMLEvent event = reader.nextEvent();
        // log.info(event.toString());
        switch (event.getEventType()) {
        case XMLStreamConstants.START_ELEMENT:
          StartElement element = event.asStartElement();

          if (!namespacesRegistered) {
            namespacesRegistered = true;
            registerNamespaces(element);
          }
          Iterator attributes = element.getAttributes();
          Map<String, String> attr = new HashMap<String, String>();
          while (attributes.hasNext()) {
            Attribute attribute = (Attribute) attributes.next();
            attr.put(attribute.getName().getPrefix() + ":" + attribute.getName().getLocalPart(),
                     attribute.getValue());
          }
          QName name = element.getName();
          importer.startElement(name.getNamespaceURI(), name.getLocalPart(), name.getPrefix() + ":"
              + name.getLocalPart(), attr);
          break;
        case XMLStreamConstants.END_ELEMENT:
          EndElement endElement = event.asEndElement();
          importer.endElement(endElement.getName().getNamespaceURI(),
                              endElement.getName().getLocalPart(),
                              endElement.getName().getPrefix() + ":"
                                  + endElement.getName().getLocalPart());
          break;
        case XMLStreamConstants.PROCESSING_INSTRUCTION:
          break;
        case XMLStreamConstants.CHARACTERS:
          String chars = event.asCharacters().getData();
          importer.characters(chars.toCharArray(), 0, chars.length());
          break;
        case XMLStreamConstants.COMMENT:
          break;
        case XMLStreamConstants.START_DOCUMENT:
          break;
        case XMLStreamConstants.END_DOCUMENT:
          importer.save();
          break;
        case XMLStreamConstants.ENTITY_REFERENCE:
          break;
        case XMLStreamConstants.ATTRIBUTE:
          break;
        case XMLStreamConstants.DTD:
          break;
        case XMLStreamConstants.CDATA:
          break;
        case XMLStreamConstants.SPACE:
          break;
        default:
          break;
        }
      }
      if (log.isDebugEnabled())
        log.debug("Event handling finished");

    } catch (XMLStreamException e) {
      throw new InvalidSerializedDataException("ImportXML failed", e);
    }
  }

  /**
   * @param event
   */
  private void registerNamespaces(StartElement event) {
    Iterator<Namespace> iter = event.getNamespaces();
    while (iter.hasNext()) {
      Namespace namespace = iter.next();
      importer.registerNamespace(namespace.getPrefix(), namespace.getNamespaceURI());
    }
  }
}
