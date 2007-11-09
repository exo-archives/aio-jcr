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

public class CreatorDisplayNameProp extends CommonProp {

  protected String creatorDisplayName = "";
  
  public CreatorDisplayNameProp() {
    this.propertyName = Const.DavProp.CREATORDISPLAYNAME;
  }
  
  public boolean init(Node node) {
    if (status != Const.HttpStatus.OK) {
      return false;
    }
    creatorDisplayName = node.getTextContent();
    return true;
  }
  
  public void setCreatorDisplayName(String creatorDisplayName) {
    this.creatorDisplayName = creatorDisplayName;
  }
  
  public String getCreatorDisplayName() {
    return creatorDisplayName;
  }
  
  
}
