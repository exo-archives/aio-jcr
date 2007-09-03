/**
* Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
* Please look at license.txt in info directory for more license detail.   *
*/

package org.exoplatform.services.webdav.common.property.dav;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.exoplatform.services.webdav.DavConst;
import org.exoplatform.services.webdav.WebDavStatus;
import org.exoplatform.services.webdav.common.property.DavProperty;
import org.exoplatform.services.webdav.common.resource.AbstractNodeResource;
import org.exoplatform.services.webdav.common.resource.WebDavResource;
import org.w3c.dom.Element;

/**
 * Created by The eXo Platform SARL
 * Author : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
 * @version $Id: ContentTypeProp.java 12525 2007-02-02 12:26:47Z gavrikvetal $
 */

public class ContentTypeProp extends AbstractDAVProperty {

  protected String mimeType = "";
  
  public ContentTypeProp() {
    super(DavProperty.GETCONTENTTYPE);
  }

  @Override
  protected boolean initialize(WebDavResource resource) throws RepositoryException {
    if (!(resource instanceof AbstractNodeResource)) {
      return false;
    }

    Node node = getResourceNode(resource);

    Node contentNode = node.getNode(DavConst.NodeTypes.JCR_CONTENT);
    if (!contentNode.hasProperty(DavConst.NodeTypes.JCR_MIMETYPE)) {        
      return false;
    }      

    mimeType = contentNode.getProperty(DavConst.NodeTypes.JCR_MIMETYPE).getString();      
    status = WebDavStatus.OK;

    return true;
  }
  
  @Override
  public Element serialize(Element parentElement) {
    super.serialize(parentElement);
    if (status == WebDavStatus.OK) {
      propertyElement.setTextContent(mimeType);
    }
    return propertyElement;
  }
  
}
