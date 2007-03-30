/***************************************************************************
 * Copyright 2001-2005 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.services.jcr.impl.xml;

import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.Value;

import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

/**
 * Created by The eXo Platform SARL        .
 *
 * @author <a href="mailto:alex.kravchuk@gmail.com">Alexander Kravchuk</a>
 * @version $Id: ExporterSysView.java 12841 2007-02-16 08:58:38Z peterit $
 */

public class ExporterSysView extends ExporterBase {

   private final String SV_NAMESPACE_URI;

   public ExporterSysView(Session session) throws RepositoryException {
       super(session);
       SV_NAMESPACE_URI = session.getNamespaceURI("sv");
   }

      protected void startNode(Node node) throws RepositoryException,
            SAXException {
       //set name of node as sv:name attribute
       AttributesImpl atts = new AttributesImpl();
       atts.addAttribute(SV_NAMESPACE_URI,"name","sv:name",
                         "CDATA",getItemQName(node));
       contentHandler.startElement(SV_NAMESPACE_URI,"node","sv:node",atts);
       
//       contentHandler.startElement(SV_NAMESPACE_URI,"node","",atts);
   }

   protected void endNode(Node node) throws RepositoryException,
           SAXException {
       contentHandler.endElement(SV_NAMESPACE_URI,"node","sv:node");
//           contentHandler.endElement(SV_NAMESPACE_URI,"node","");
   }


   protected void startProperty(Property property) throws RepositoryException,
           SAXException{
        //set name and type of property
        AttributesImpl atts = new AttributesImpl();
        atts.addAttribute(SV_NAMESPACE_URI,"name","sv:name",
             "CDATA", property.getName());
        atts.addAttribute(SV_NAMESPACE_URI,"type","sv:type",
             "CDATA", getPropertyType(property));
        contentHandler.startElement(SV_NAMESPACE_URI,"property","sv:property",atts);
   }

   protected void endProperty(Property property) throws RepositoryException,
           SAXException{
       contentHandler.endElement(SV_NAMESPACE_URI,"property","sv:property");
   }

   protected void exportValue(Value value) throws RepositoryException,
           SAXException {

       contentHandler.startElement(SV_NAMESPACE_URI, "value", "sv:value", new AttributesImpl());

       char[] charValue = getValueAsString(value).toCharArray();
       
       contentHandler.characters(charValue, 0, charValue.length);
       
       //System.out.println("exportValue val " +getValueAsString(value));
       contentHandler.endElement(SV_NAMESPACE_URI, "value", "sv:value");
   }

}
