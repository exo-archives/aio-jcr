/**
* Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
* Please look at license.txt in info directory for more license detail.   *
*/

package org.exoplatform.frameworks.davclient.properties;

import org.exoplatform.frameworks.davclient.Const;
import org.exoplatform.frameworks.davclient.XmlUtil;
import org.w3c.dom.Node;

/**
 * Created by The eXo Platform SARL
 * Author : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
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
