/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.services.webdav.deltav.representation.property;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.version.Version;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.exoplatform.services.webdav.WebDavStatus;
import org.exoplatform.services.webdav.common.representation.HrefRepresentation;
import org.exoplatform.services.webdav.common.representation.property.WebDavPropertyRepresentation;

/**
 * Created by The eXo Platform SARL
 * Author : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
 * @version $Id: $
 */

public class CheckedInRepresentation extends WebDavPropertyRepresentation {
  
  public static final String TAGNAME = "checked-in";
  
  private String href;
  
  private HrefRepresentation hrefRepresentation;
  
  public CheckedInRepresentation(String href) {
    this.href = href;
  }

  public void read(Node node) {
    try {
      
      String hrefValue;
      
      if (node instanceof Version) {
        hrefValue = href + "?VERSIONID=" + node.getName();
      } else {
        if (node.isCheckedOut()) {
          return;
        }
        
        hrefValue = href + "?VERSIONID=" + node.getBaseVersion().getName();
      }
      
      status = WebDavStatus.OK;      
      hrefRepresentation = new HrefRepresentation(hrefValue);
    } catch (RepositoryException exc) {
    }
  }

  @Override
  public String getTagName() {
    return TAGNAME;
  }

  @Override
  protected void writeContent(XMLStreamWriter xmlWriter) throws XMLStreamException {
    hrefRepresentation.write(xmlWriter);
  }

}
