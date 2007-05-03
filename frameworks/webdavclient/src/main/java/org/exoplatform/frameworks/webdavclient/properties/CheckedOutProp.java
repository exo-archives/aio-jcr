/**
* Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
* Please look at license.txt in info directory for more license detail.   *
*/

package org.exoplatform.frameworks.webdavclient.properties;

import org.exoplatform.frameworks.webdavclient.Const;
import org.w3c.dom.Node;

/**
 * Created by The eXo Platform SARL
 * Author : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
 * @version $Id: $
 */

public class CheckedOutProp extends CommonProp {

  protected boolean checkedOut = false;

  public CheckedOutProp() {
    this.propertyName = Const.DavProp.CHECKEDOUT;
  }
  
  public boolean init(Node node) {
    if (status != Const.HttpStatus.OK) {
      return false;
    }
    checkedOut = true;
    return true;
  }
  
  public boolean isCheckedOut() {
    return checkedOut;
  }
  
}
