/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SAS          All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.services.webdav.deltav.representation.property;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.version.Version;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.exoplatform.services.webdav.DavConst;
import org.exoplatform.services.webdav.WebDavStatus;
import org.exoplatform.services.webdav.common.representation.property.WebDavPropertyRepresentation;

/**
 * Created by The eXo Platform SAS
 * Author : Vitaly Guly <gavrikvetal@gmail.com>
 * @version $Id: $
 */

public class VersionNameRepresentation extends WebDavPropertyRepresentation {
  
  public static final String TAGNAME = "version-name";
  
  private String versionName = "";
  
  public void read(Node node) {
    try {
      if (node instanceof Version) {
        versionName = node.getName();
        status = WebDavStatus.OK;
        return;
      }
      
      if (node.isNodeType(DavConst.NodeTypes.MIX_VERSIONABLE)) {
        Version baseVersion = node.getBaseVersion();
        versionName = baseVersion.getName();
        status = WebDavStatus.OK;
        return;
      }
      
    } catch (RepositoryException exc) {
    }    
  }

  @Override
  public String getTagName() {
    return TAGNAME;
  }

  @Override
  protected void writeContent(XMLStreamWriter xmlWriter) throws XMLStreamException {
    xmlWriter.writeCharacters(versionName);
  }

}
