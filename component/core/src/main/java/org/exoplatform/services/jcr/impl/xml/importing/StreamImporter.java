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
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.Namespace;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import org.apache.commons.logging.Log;
import org.exoplatform.services.jcr.impl.core.NodeImpl;
import org.exoplatform.services.jcr.impl.xml.ImportRespectingSemantics;
import org.exoplatform.services.jcr.impl.xml.XmlSaveType;
import org.exoplatform.services.log.ExoLogger;

/**
 * @author <a href="mailto:Sergey.Kabashnyuk@gmail.com">Sergey Kabashnyuk</a>
 * @version $Id: $
 */
public class StreamImporter {

  protected static Log      log                  = ExoLogger.getLogger("jcr.StreamImporter");

  private NeutralImporter   importer;

  private boolean           namespacesRegistered = false;

  private final NodeImpl    parent;

  private final XmlSaveType saveType;

  private final int         uuidBehavior;

  private final boolean respectPropertyDefinitionsConstraints;

  public StreamImporter(XmlSaveType saveType,
                        NodeImpl parent,
                        int uuidBehavior,
                        boolean respectPropertyDefinitionsConstraints) {
    super();
    this.saveType = saveType;
    this.uuidBehavior = uuidBehavior;
    this.parent = parent;
    this.respectPropertyDefinitionsConstraints = respectPropertyDefinitionsConstraints;

  }

  public void importStream(InputStream stream) throws RepositoryException {
    
    XMLInputFactory factory = XMLInputFactory.newInstance();
    if (log.isDebugEnabled())
      log.debug("FACTORY: " + factory);
    
    // TODO create in constructor
    this.importer = new NeutralImporter(parent, uuidBehavior, saveType,respectPropertyDefinitionsConstraints);
    
    try {

      XMLEventReader reader = factory.createXMLEventReader(stream);
      
      if (log.isDebugEnabled())
        log.debug("Start event handling");
      while (reader.hasNext()) {
        XMLEvent event = reader.nextEvent();
         //log.info(event.toString());
        switch (event.getEventType()) {
        case XMLEvent.START_ELEMENT:
          StartElement element = event.asStartElement();
          //log.info(element.getLocation().getCharacterOffset());
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
        case XMLEvent.END_ELEMENT:
          EndElement endElement = event.asEndElement();
          importer.endElement(endElement.getName().getNamespaceURI(), endElement.getName()
              .getLocalPart(), endElement.getName().getPrefix() + ":"
              + endElement.getName().getLocalPart());
          break;
        case XMLEvent.PROCESSING_INSTRUCTION:
          break;
        case XMLEvent.CHARACTERS:
          String chars = event.asCharacters().getData();
          importer.characters(chars.toCharArray(), 0, chars.length());
          break;
        case XMLEvent.COMMENT:
          break;
        case XMLEvent.START_DOCUMENT:
          break;
        case XMLEvent.END_DOCUMENT:
          importer.save();
          break;
        case XMLEvent.ENTITY_REFERENCE:
          break;
        case XMLEvent.ATTRIBUTE:
          break;
        case XMLEvent.DTD:
          break;
        case XMLEvent.CDATA:
          break;
        case XMLEvent.SPACE:
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

  private void registerNamespaces(StartElement event) {
    Iterator<Namespace> iter = event.getNamespaces();
    while (iter.hasNext()) {
      Namespace namespace = iter.next();
      importer.registerNamespace(namespace.getPrefix(), namespace.getNamespaceURI());
    }
  }
}
