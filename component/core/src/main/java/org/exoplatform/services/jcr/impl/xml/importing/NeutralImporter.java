/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.services.jcr.impl.xml.importing;

import java.util.Map;

import javax.jcr.RepositoryException;

import org.exoplatform.services.ext.action.InvocationContext;
import org.exoplatform.services.jcr.impl.core.NodeImpl;
import org.exoplatform.services.jcr.impl.util.NodeTypeRecognizer;
import org.exoplatform.services.jcr.impl.xml.XmlSaveType;

/**
 * The main purpose of class is determinate of import document type
 * 
 * @author <a href="mailto:Sergey.Kabashnyuk@gmail.com">Sergey Kabashnyuk</a>
 * @version $Id: $
 */
public class NeutralImporter extends BaseXmlImporter {

  private ContentImporter       contentImporter = null;

  private final NodeImpl parent;

  public NeutralImporter(NodeImpl parent,
                         int uuidBehavior,
                         XmlSaveType saveType,
                         InvocationContext context) {
    super(parent, uuidBehavior, saveType, context);
    this.parent = parent;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.exoplatform.services.jcr.impl.xml.importing.Importer#characters(char[],
   *      int, int)
   */
  public void characters(char[] ch, int start, int length) throws RepositoryException {
    if (contentImporter == null) {
      throw new IllegalStateException("StartElement must be  call first");
    }
    contentImporter.characters(ch, start, length);

  }

  /*
   * (non-Javadoc)
   * 
   * @see org.exoplatform.services.jcr.impl.xml.importing.Importer#endElement(java.lang.String,
   *      java.lang.String, java.lang.String)
   */
  public void endElement(String uri, String localName, String qName) throws RepositoryException {
    if (contentImporter == null) {
      throw new IllegalStateException("StartElement must be call first");
    }
    contentImporter.endElement(uri, localName, qName);
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.exoplatform.services.jcr.impl.xml.importing.BaseXmlImporter#save()
   */
  @Override
  public void save() throws RepositoryException {
    if (contentImporter == null) {
      throw new IllegalStateException("StartElement must be call first");
    }
    contentImporter.save();
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.exoplatform.services.jcr.impl.xml.importing.Importer#startElement(java.lang.String,
   *      java.lang.String, java.lang.String, java.util.Map)
   */
  public void startElement(String namespaceURI,
                           String localName,
                           String name,
                           Map<String, String> atts) throws RepositoryException {
    if (contentImporter == null) {
      switch (NodeTypeRecognizer.recognize(namespaceURI, name)) {
      case DOCVIEW:
        contentImporter = new DocumentViewImporter(parent, uuidBehavior, getSaveType(), context);
        break;
      case SYSVIEW:
        contentImporter = new SystemViewImporter(parent, uuidBehavior, getSaveType(), context);
        break;
      default:
        throw new IllegalStateException("There was an error during ascertaining the "
            + "type of document. First element " + namespaceURI + ":" + name);
      }
    }
    contentImporter.startElement(namespaceURI, localName, name, atts);
  }

}
