/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.services.jcr.impl.xml.importing;

import java.util.Map;

import javax.jcr.RepositoryException;

import org.exoplatform.services.jcr.impl.core.NodeImpl;
import org.exoplatform.services.jcr.impl.util.NodeTypeRecognizer;
import org.exoplatform.services.jcr.impl.xml.XmlSaveType;

/**
 * The main purpose of class is determinate of import document type
 * 
 * @author <a href="mailto:Sergey.Kabashnyuk@gmail.com">Sergey Kabashnyuk</a>
 * @version $Id: $
 */
public class NeutralImporter extends ImporterBase {

  private Importer contentImporter = null;

  public NeutralImporter(NodeImpl parent, int uuidBehavior, XmlSaveType saveType) {
    super(parent, uuidBehavior, saveType);
  }

  public void characters(char[] ch, int start, int length) throws RepositoryException {
    if (contentImporter == null) {
      throw new IllegalStateException("StartElement must be  call first");
    }
    contentImporter.characters(ch, start, length);

  }

  public void endElement(String uri, String localName, String qName) throws RepositoryException {
    if (contentImporter == null) {
      throw new IllegalStateException("StartElement must be call first");
    }
    contentImporter.endElement(uri, localName, qName);
  }

  @Override
  public void save() throws RepositoryException {
    if (contentImporter == null) {
      throw new IllegalStateException("StartElement must be call first");
    }
    contentImporter.save();
  }

  public void startElement(String namespaceURI,
      String localName,
      String name,
      Map<String, String> atts) throws RepositoryException {
    if (contentImporter == null) {
      switch (NodeTypeRecognizer.recognize(namespaceURI, name)) {
      case DOCVIEW:
        contentImporter = new DocumentViewImporter(parent, uuidBehavior, getSaveType());
        break;
      case SYSVIEW:
        contentImporter = new SystemViewImporter(parent, uuidBehavior, getSaveType());
        break;
      default:
        throw new IllegalStateException("No importer find");
      }
    }
    contentImporter.startElement(namespaceURI, localName, name, atts);
  }

}
