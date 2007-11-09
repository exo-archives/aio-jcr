/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SAS          All rights reserved.  *
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
 * Created by The eXo Platform SAS
 * Author : Vitaly Guly <gavrikvetal@gmail.com>
 * @version $Id: $
 */

public class GetLastModifiedRepresentation extends WebDavPropertyRepresentation {
  
  public static final String TAGNAME = "getlastmodified";
  
  private String lastModified = ""; 
  
  public void read(Node node) {
    try {      
      Node contentNode = node.getNode(DavConst.NodeTypes.JCR_CONTENT);      
      
      Property lastModifiedProp = contentNode.getProperty(DavConst.NodeTypes.JCR_LASTMODIFIED);
    
      SimpleDateFormat dateFormat = new SimpleDateFormat(DavConst.DateFormat.MODIFICATION, Locale.ENGLISH);
      dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));      
      lastModified = dateFormat.format(lastModifiedProp.getDate().getTime());
      
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
    xmlWriter.writeCharacters(lastModified);
  }  

}
