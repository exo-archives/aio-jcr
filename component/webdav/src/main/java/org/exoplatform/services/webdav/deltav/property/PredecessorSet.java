/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.services.webdav.deltav.property;

import java.util.ArrayList;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.version.Version;

import org.exoplatform.services.webdav.DavConst;
import org.exoplatform.services.webdav.WebDavStatus;
import org.exoplatform.services.webdav.common.property.DavProperty;
import org.exoplatform.services.webdav.common.property.dav.AbstractDAVProperty;
import org.exoplatform.services.webdav.common.resource.WebDavResource;
import org.exoplatform.services.webdav.deltav.resource.VersionResource;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Created by The eXo Platform SARL
 * Author : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
 * @version $Id: PredecessorSet.java 12525 2007-02-02 12:26:47Z gavrikvetal $
 */

public class PredecessorSet extends AbstractDAVProperty {
  
  private ArrayList<String> versionPredecessors = new ArrayList<String>();
  
  public PredecessorSet() {
    super(DavProperty.PREDECESSORSET);
  }
  
  @Override
  protected boolean initialize(WebDavResource resource) throws RepositoryException {    
    if (!(resource instanceof VersionResource)) {
      return false;
    }
    
    Node node = ((VersionResource)resource).getNode();    
    
    Version []predecessors = ((Version)node).getPredecessors();

    while (predecessors.length > 0) {
      if (DavConst.NodeTypes.JCR_ROOTVERSION.equals(predecessors[0].getName())) {
        break;
      }
      versionPredecessors.add(resourceHref.getValue() + DavConst.DAV_VERSIONPREFIX + predecessors[0].getName());
      predecessors = predecessors[0].getPredecessors();
    }
    
    status = WebDavStatus.OK;
    
    return true; 
  }
  
  @Override
  public Element serialize(Element parentElement) {
    super.serialize(parentElement);
    if (status != WebDavStatus.OK) {
      return propertyElement;
    }
    
    for (int i = 0; i < versionPredecessors.size(); i++) {
      Element hrefEl = parentElement.getOwnerDocument().createElement(DavConst.DAV_PREFIX + DavProperty.HREF);
      propertyElement.appendChild(hrefEl);
      hrefEl.setTextContent(versionPredecessors.get(i));
    }
    
    return propertyElement;
  }

}
