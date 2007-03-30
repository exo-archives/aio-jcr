/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.services.webdav.search;

import org.exoplatform.services.webdav.search.basicsearch.BasicSearch;

/**
 * Created by The eXo Platform SARL
 * Author : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
 * @version $Id: SearchConst.java 12004 2007-01-17 12:03:57Z geaz $
 */

public class SearchConst {

  public class SearchType {
    
    public static final String BASIC = "basicsearch";
    
    public static final String XPATH = "xpath";
    
    public static final String SQL = "sql";    
  }
  
  public static final String [][]SEARCH_TEMPLATES = {
    {SearchType.SQL, SQLSearch.class.getCanonicalName()},
    {SearchType.XPATH, XPathSearch.class.getCanonicalName()},
    {SearchType.BASIC, BasicSearch.class.getCanonicalName()}
  };
  
}
