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

public class ContentLengthProp extends CommonProp {

  protected int contentLength = 0;

  public ContentLengthProp() {
    this.propertyName = Const.DavProp.GETCONTENTLENGTH;
  }
  
  public boolean init(Node node) {
    if (status != Const.HttpStatus.OK) {
      return false;
    }
    
    String contLen = node.getTextContent();
    contentLength = new Integer(contLen);
    return true;
  }    
  
  public long getContentLength() {
    return contentLength;
  }
  
}
