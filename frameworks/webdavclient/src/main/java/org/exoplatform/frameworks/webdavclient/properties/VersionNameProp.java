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

public class VersionNameProp extends CommonProp {

  protected String versionName = "";

  public VersionNameProp() {
    this.propertyName = Const.DavProp.VERSIONNAME;
  }
  
  public boolean init(Node node) {
    if (status != Const.HttpStatus.OK) {
      return false;
    }
    versionName = node.getTextContent();
    return true;
  }
  
  public String getVersionName() {
    return versionName;
  }  
  
}
