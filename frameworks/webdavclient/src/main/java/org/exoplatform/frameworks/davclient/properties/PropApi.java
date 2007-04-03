/**
* Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
* Please look at license.txt in info directory for more license detail.   *
*/

package org.exoplatform.frameworks.davclient.properties;

import org.w3c.dom.Node;

/**
 * Created by The eXo Platform SARL
 * Author : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
 * @version $Id: $
 */

public interface PropApi {
  
  public void setStatus(String httpStatus);
  
  public void setStatus(int status);
  
  public int getStatus();
  
  public String getName();  
  
  public String getValue();
  
  public boolean init(Node node);
  
}
