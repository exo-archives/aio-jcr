/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SAS          All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.frameworks.webdavclient.documents;

import org.w3c.dom.Document;

/**
 * Created by The eXo Platform SAS
 * Author : Vitaly Guly <gavrikvetal@gmail.com>
 * @version $Id: $
 */

public class PropFindDoc implements DocumentApi {

  public boolean initFromDocument(Document document) {
    
    System.out.println("in init from document propfind");
    
    return false;    
    
  }
  
}
