/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.frameworks.davclient.properties;

import org.apache.commons.logging.Log;
import org.exoplatform.frameworks.davclient.Const;
import org.exoplatform.frameworks.davclient.Const.DavProp;
import org.exoplatform.services.log.ExoLogger;
import org.w3c.dom.Node;

/**
 * Created by The eXo Platform SARL
 * Author : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
 * @version $Id: $
 */

public class CurrentUserPrivilegeSet extends CommonProp {
  
  private static Log log = ExoLogger.getLogger("jcr.CurrentUserPrivilegeSet");

  public CurrentUserPrivilegeSet() {
    this.propertyName = Const.DavProp.CURRENT_USER_PRIVILEGE_SET;
  }
  
  public boolean init(Node node) {
    return false;
  }
  
}
