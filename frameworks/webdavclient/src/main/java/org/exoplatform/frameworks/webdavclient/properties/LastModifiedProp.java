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

public class LastModifiedProp extends CommonProp {

  protected String lastModified = "";

  public LastModifiedProp() {
    this.propertyName = Const.DavProp.GETLASTMODIFIED;
  }
  
  public boolean init(Node node) {
    if (status != Const.HttpStatus.OK) {
      return false;
    }
    lastModified = node.getTextContent();
    return false;
  }    
  
  public String getLastModified() {
    return lastModified;
  }
  
}
