/**
* Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
* Please look at license.txt in info directory for more license detail.   *
*/

package org.exoplatform.services.webdav.deltav.property;

import java.util.ArrayList;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.version.Version;

import org.apache.commons.logging.Log;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.webdav.DavConst;
import org.exoplatform.services.webdav.WebDavStatus;
import org.exoplatform.services.webdav.common.property.DavProperty;
import org.exoplatform.services.webdav.common.property.dav.AbstractDAVProperty;
import org.exoplatform.services.webdav.common.resource.WebDavResource;
import org.exoplatform.services.webdav.common.response.Href;
import org.exoplatform.services.webdav.deltav.resource.VersionResource;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Created by The eXo Platform SARL
 * Author : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
 * @version $Id: SuccessorSetProp.java 12525 2007-02-02 12:26:47Z gavrikvetal $
 */

public class SuccessorSetProp extends AbstractDAVProperty {
  
  private static Log log = ExoLogger.getLogger("jcr.SuccessorSetProp");

  protected ArrayList<String> versionSuccessors = new ArrayList<String>();
  protected ArrayList<Href> versionHrefs = new ArrayList<Href>();
  
  public SuccessorSetProp() {
    super(DavProperty.SUCCESSORSET);
  }
  
  @Override
  protected boolean initialize(WebDavResource resource) throws RepositoryException {    
    if (!(resource instanceof VersionResource)) {
      return false;
    }
    
    Node node = ((VersionResource)resource).getNode();
    
    if (!(node instanceof Version)) {
      return false;
    }
    
    Version []successors = ((Version)node).getSuccessors();

    while (successors.length > 0) {
      
      //Href href = new Href();
      String href = ((VersionResource)resource).getHref();
      log.info(">>> resource href: [" + href + "]");
      
      versionSuccessors.add(resourceHref.getValue() + DavConst.DAV_VERSIONPREFIX + successors[0].getName());
      successors = successors[0].getSuccessors();
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
    
    for (int i = 0; i < versionSuccessors.size(); i++) {
      Element hrefEl = propertyElement.getOwnerDocument().createElement(DavConst.DAV_PREFIX + DavProperty.HREF);
      propertyElement.appendChild(hrefEl);
      hrefEl.setTextContent(versionSuccessors.get(i));
    }
    
    return propertyElement;
  }
  
}
