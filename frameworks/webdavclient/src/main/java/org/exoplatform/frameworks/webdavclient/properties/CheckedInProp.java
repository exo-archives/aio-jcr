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

public class CheckedInProp extends CommonProp {
  
  protected boolean checkedIn = false;
  private String href = ""; 

  public CheckedInProp() {
    this.propertyName = Const.DavProp.CHECKEDIN;
  }
  
  public boolean init(Node node) {
    if (status != Const.HttpStatus.OK) {
      return false;
    }
    
    Node hrefN = XmlUtil.getChildNode(node, Const.DavProp.HREF);
    if (hrefN == null) {
      return false;
    }
    
    href = hrefN.getTextContent();
    checkedIn = true;    
    
    return true;
  }
  
  public boolean isCheckedIn() {
    return checkedIn;
  }
  
  public String getHref() {
    return href;
  }
  
}
