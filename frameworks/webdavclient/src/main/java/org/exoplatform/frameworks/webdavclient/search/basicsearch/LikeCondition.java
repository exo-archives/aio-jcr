/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SAS          All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.frameworks.webdavclient.search.basicsearch;

import org.exoplatform.frameworks.webdavclient.Const;
import org.exoplatform.frameworks.webdavclient.search.SearchConst;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Created by The eXo Platform SAS
 * Author : Vitaly Guly <gavrikvetal@gmail.com>
 * @version $Id: $
 */

public class LikeCondition implements BasicSearchCondition {

  public LikeCondition() {    
  }
  
  public Element toXml(Document xmlDocument) {    
    Element likeElement = xmlDocument.createElement(Const.Dav.PREFIX + SearchConst.LIKE_TAG);
    
    
    
    return likeElement; 
  }
  
}
