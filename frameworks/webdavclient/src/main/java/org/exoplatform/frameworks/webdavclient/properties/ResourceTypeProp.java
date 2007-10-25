/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SAS          All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.frameworks.webdavclient.properties;

import org.exoplatform.frameworks.webdavclient.Const;
import org.exoplatform.frameworks.webdavclient.XmlUtil;
import org.w3c.dom.Node;

/**
 * Created by The eXo Platform SAS
 * Author : Vitaly Guly <gavrikvetal@gmail.com>
 * @version $Id: $
 */

public class ResourceTypeProp extends CommonProp {

  protected boolean isCollection = true;

  public ResourceTypeProp() {
    this.propertyName = Const.DavProp.RESOURCETYPE;
  }
  
  public boolean init(Node node) {
    if (status != Const.HttpStatus.OK) {
      return false;
    }

    Node collectionNode = XmlUtil.getChildNode(node, Const.DavProp.COLLECTION);
    if (collectionNode == null) {
      isCollection = false;
    }
    
    return true;
  }  
  
  public boolean isCollection() {
    return isCollection;
  }
  
}
