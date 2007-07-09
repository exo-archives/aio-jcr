/**
* Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
* Please look at license.txt in info directory for more license detail.   *
*/

package org.exoplatform.frameworks.webdavclient.documents;

import org.w3c.dom.Document;

/**
 * Created by The eXo Platform SARL
 * Author : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
 * @version $Id: $
 */

public class PropFindDoc implements DocumentApi {

  public boolean initFromDocument(Document document) {
    
    System.out.println("in init from document propfind");
    
    return false;    
    
  }
  
}
