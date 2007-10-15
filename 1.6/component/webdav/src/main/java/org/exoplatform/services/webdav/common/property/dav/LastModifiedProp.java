/**
* Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
* Please look at license.txt in info directory for more license detail.   *
*/

package org.exoplatform.services.webdav.common.property.dav;

import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.TimeZone;

import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.RepositoryException;

import org.apache.commons.logging.Log;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.webdav.DavConst;
import org.exoplatform.services.webdav.common.property.DavProperty;
import org.exoplatform.services.webdav.common.resource.AbstractNodeResource;
import org.exoplatform.services.webdav.common.resource.WebDavResource;
import org.exoplatform.services.webdav.common.response.DavStatus;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Created by The eXo Platform SARL
 * Author : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
 * @version $Id: LastModifiedProp.java 12525 2007-02-02 12:26:47Z gavrikvetal $
 */

public class LastModifiedProp extends AbstractDAVProperty {
  
  private static Log log = ExoLogger.getLogger("jcr.LastModifiedProp");
  
  protected String lastModified = "";
  
  public LastModifiedProp() {
    super(DavProperty.GETLASTMODIFIED);
  }

  @Override
  protected boolean initialize(WebDavResource resource) throws RepositoryException {
    
    //log.info("initialize...");
    
    if (!(resource instanceof AbstractNodeResource)) {
      return false;
    }

    if (resource.isCollection()) {
      return false;
    }

    //log.info("try get info value.............");
    
    Node node = getResourceNode((AbstractNodeResource)resource);
    
    Node contentNode = node.getNode(DavConst.NodeTypes.JCR_CONTENT);      
    
    Property lastModifiedProp = contentNode.getProperty(DavConst.NodeTypes.JCR_LASTMODIFIED);
  
    SimpleDateFormat dateFormat = new SimpleDateFormat(DavConst.DateFormat.MODIFICATION, Locale.ENGLISH);
    dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));      
    lastModified = dateFormat.format(lastModifiedProp.getDate().getTime());
    
    status = DavStatus.OK;
    
    return true;
  }
  
  @Override
  public void serialize(Document rootDoc, Element parentElement) {
    super.serialize(rootDoc, parentElement);
    if (status != DavStatus.OK) {
      return;
    }
    propertyElement.setTextContent(lastModified);
  }

}
