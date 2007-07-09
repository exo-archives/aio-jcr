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
