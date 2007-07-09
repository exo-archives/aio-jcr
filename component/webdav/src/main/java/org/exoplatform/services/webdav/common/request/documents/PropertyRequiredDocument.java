/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.services.webdav.common.request.documents;

import org.apache.commons.logging.Log;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.webdav.DavConst;
import org.exoplatform.services.webdav.common.property.DavProperty;
import org.exoplatform.services.webdav.common.property.factory.PropertyDefine;
import org.exoplatform.services.webdav.common.property.factory.PropertyFactory;
import org.exoplatform.services.webdav.common.util.DavUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Created by The eXo Platform SARL
 * Author : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
 * @version $Id: $
 */

public class PropertyRequiredDocument extends CommonPropDocument {
  
  private static Log log = ExoLogger.getLogger("jcr.PropertyRequiredDoc");
  
  protected String xmlName = DavConst.DavDocument.PROPFIND;
  
  @Override
  public boolean init(Document requestDocument, PropertyFactory propertyFactory) {
    super.init(requestDocument, propertyFactory);
    try {      
      Node propFind = DavUtil.getChildNode(requestDocument, xmlName);

      if (DavUtil.getChildNode(propFind, DavProperty.ALLPROP) != null) {
        isNeedSearchProperties = true;
        return true;
      }
      
      defines.clear();
      
      Node props = DavUtil.getChildNode(propFind, DavProperty.PROP);
      
      if (DavUtil.getChildNode(props, DavProperty.ALLPROP) != null) {        
        isNeedSearchProperties = true;
        return true;
      }      
      
      NodeList nodes = props.getChildNodes();
      for (int i = 0; i < nodes.getLength(); i++) {
        Node curNode = nodes.item(i);
                
        String nameSpace = curNode.getNamespaceURI();
        String localName = curNode.getLocalName();
        
        if (localName != null) {
          PropertyDefine define = propertyFactory.getDefine(nameSpace, localName);
          defines.add(define);
        }
        
      }

      Node propInclude = DavUtil.getChildNode(propFind, DavProperty.INCLUDE);
      if (propInclude != null) {
        log.info("Needed extended include property.");
      }

      isNeedSearchProperties = false;
      
      return true;
    } catch (Exception exc) {
      log.info("Can't fill document data. " + exc.getMessage());
      exc.printStackTrace();      
    }

    return false;    
  }  

}
