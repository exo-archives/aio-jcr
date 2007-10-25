/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SAS          All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.services.webdav.deltav.representation.versiontree;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.version.Version;
import javax.jcr.version.VersionHistory;
import javax.jcr.version.VersionIterator;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.exoplatform.services.webdav.DavConst;
import org.exoplatform.services.webdav.WebDavService;
import org.exoplatform.services.webdav.common.representation.HrefRepresentation;
import org.exoplatform.services.webdav.common.representation.read.AllPropResponseRepresentation;

/**
 * Created by The eXo Platform SAS
 * Author : Vitaly Guly <gavrikvetal@gmail.com>
 * @version $Id: $
 */

public class AllPropVersionTreeResponseRepresentation extends AllPropResponseRepresentation {
  
  public AllPropVersionTreeResponseRepresentation(WebDavService webDavService, String href, Node node) throws RepositoryException {
    super(webDavService, href, node, 0); 
  }
  
  @Override
  protected void listRecursive(XMLStreamWriter xmlStreamWriter, Node curNode, int curDepth) throws XMLStreamException, RepositoryException {    
    href = defaultHref + curNode.getPath();
    
    VersionHistory versionHistory = curNode.getVersionHistory();
    VersionIterator versionIterator = versionHistory.getAllVersions();
    
    while (versionIterator.hasNext()) {
      Version version = versionIterator.nextVersion();
      
      if (DavConst.NodeTypes.JCR_ROOTVERSION.equals(version.getName())) {
        continue;
      }
      
      xmlStreamWriter.writeStartElement("D", XML_RESPONSE, "DAV:");
      String responseHref = href + "?VERSIONID=" + version.getName();
      new HrefRepresentation(responseHref).write(xmlStreamWriter);
      
      writeResponseContent(xmlStreamWriter, version);
      
      xmlStreamWriter.writeEndElement();
    }
    
  }  

}
