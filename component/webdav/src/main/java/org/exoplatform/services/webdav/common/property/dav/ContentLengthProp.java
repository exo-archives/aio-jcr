/**
* Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
* Please look at license.txt in info directory for more license detail.   *
*/

package org.exoplatform.services.webdav.common.property.dav;

import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.RepositoryException;

import org.exoplatform.services.webdav.DavConst;
import org.exoplatform.services.webdav.common.property.DavProperty;
import org.exoplatform.services.webdav.common.resource.AbstractNodeResource;
import org.exoplatform.services.webdav.common.resource.DavResource;
import org.exoplatform.services.webdav.common.response.DavStatus;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Created by The eXo Platform SARL
 * Author : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
 * @version $Id: ContentLengthProp.java 12525 2007-02-02 12:26:47Z gavrikvetal $
 */

public class ContentLengthProp extends AbstractDAVProperty {
  
  protected long contentLength = 0;
  
  public ContentLengthProp() {
    super(DavProperty.GETCONTENTLENGTH);
  }

  @Override
  protected boolean initialize(DavResource resource) throws RepositoryException {
    if (!(resource instanceof AbstractNodeResource)) {
      return false;
    }
  
    Node node = ((AbstractNodeResource)resource).getNode();
    
    if (node.isNodeType(DavConst.NodeTypes.NT_VERSION)) {
      node = node.getNode(DavConst.NodeTypes.JCR_FROZENNODE);
    }

    Node dataNode = node.getNode(DavConst.NodeTypes.JCR_CONTENT);      
    Property dataProperty = dataNode.getProperty(DavConst.NodeTypes.JCR_DATA);      
    contentLength = dataProperty.getLength();
    status = DavStatus.OK;

    return true;
  }
  
  @Override
  public void serialize(Document rootDoc, Element parentElement) {
    super.serialize(rootDoc, parentElement);
    if (status != DavStatus.OK) {
      return;
    }
    propertyElement.setTextContent("" + contentLength);
  }
  
}
