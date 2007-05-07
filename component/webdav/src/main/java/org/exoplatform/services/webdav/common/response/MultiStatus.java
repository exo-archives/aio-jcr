/**
* Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
* Please look at license.txt in info directory for more license detail.   *
*/

package org.exoplatform.services.webdav.common.response;

import java.util.ArrayList;

import org.exoplatform.services.webdav.DavConst;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Created by The eXo Platform SARL
 * Author : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
 * @version $Id: MultiStatus.java 12899 2007-02-20 15:13:30Z gavrikvetal $
 */

public class MultiStatus {
  
  private ArrayList<Response> responses = null;
  
  public MultiStatus(ArrayList<Response> responses) {
    this.responses = responses;
  }
  
  public Element toXml(Document rootDoc) {
    Element multistatusElement = rootDoc.createElementNS(DavConst.DAV_NAMESPACE, DavConst.DAV_PREFIX + DavConst.DavProperty.MULTISTATUS);
    rootDoc.appendChild(multistatusElement);

    for (Response curResponse : responses) {
      curResponse.toXml(rootDoc, multistatusElement);    
    }
    return multistatusElement;
  }
  
}
