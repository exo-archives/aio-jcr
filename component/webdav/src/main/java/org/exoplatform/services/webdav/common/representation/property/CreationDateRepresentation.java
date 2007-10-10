/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.services.webdav.common.representation.property;

import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.TimeZone;

import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.RepositoryException;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.exoplatform.services.webdav.DavConst;
import org.exoplatform.services.webdav.WebDavStatus;

/**
 * Created by The eXo Platform SARL
 * Author : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
 * @version $Id: $
 */

public class CreationDateRepresentation extends WebDavPropertyRepresentation {
  
  public static final String TAGNAME = "creationdate";
  
  protected String creationDate = "";
  
  public void read(Node node) {
    try {
      if (node.isNodeType(DavConst.NodeTypes.NT_VERSION)) {
        node = node.getNode(DavConst.NodeTypes.JCR_FROZENNODE);
      }
      
      Property createdProp = node.getProperty(DavConst.NodeTypes.JCR_CREATED);
      
      SimpleDateFormat dateFormat = new SimpleDateFormat(DavConst.DateFormat.CREATION, Locale.ENGLISH);
      dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));      
      creationDate = dateFormat.format(createdProp.getDate().getTime());
      
      status = WebDavStatus.OK;
    } catch (RepositoryException exc) {
    }
  }

  @Override
  public String getTagName() {
    return TAGNAME;
  }

  @Override
  protected void writeContent(XMLStreamWriter xmlWriter) throws XMLStreamException {
    xmlWriter.writeCharacters(creationDate);
  }

}
