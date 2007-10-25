/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SAS          All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.frameworks.webdavclient.properties;

import org.exoplatform.frameworks.webdavclient.Const;
import org.w3c.dom.Node;

/**
 * Created by The eXo Platform SAS
 * Author : Vitaly Guly <gavrikvetal@gmail.com>
 * @version $Id: $
 */

public class ContentTypeProp extends CommonProp {

  protected String contentType = "";

  public ContentTypeProp() {
    this.propertyName = Const.DavProp.GETCONTENTTYPE;
  }
  
  public boolean init(Node node) {
    if (status != Const.HttpStatus.OK) {
      return false;
    }
    contentType = node.getTextContent();
    return true;
  }    
  
  public String getContentType() {
    return contentType;
  }
  
}
