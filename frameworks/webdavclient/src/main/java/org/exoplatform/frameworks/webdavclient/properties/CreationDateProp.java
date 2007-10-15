/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.frameworks.webdavclient.properties;

import org.exoplatform.frameworks.webdavclient.Const;
import org.w3c.dom.Node;

/**
 * Created by The eXo Platform SARL
 * Author : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
 * @version $Id: $
 */

public class CreationDateProp extends CommonProp {

  protected String creationDate = "";
  
  public CreationDateProp() {
    this.propertyName = Const.DavProp.CREATIONDATE;
  }
  
  public boolean init(Node node) {
    if (status != Const.HttpStatus.OK) {
      return false;
    }
    creationDate = node.getTextContent();
    return false;
  }    

  public String getCreationDate() {
    return creationDate;
  }
  
}
