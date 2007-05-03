/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.frameworks.webdavclient.search;


/**
 * Created by The eXo Platform SARL
 * Author : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
 * @version $Id: $
 */

public class SearchConst {
  
  public static final String XPATH_NAMESPACE = "XPATH:";
  public static final String XPATH_PREFIX = "X:";
  
  public static final String SQL_NAMESPACE = "SQL:";
  public static final String SQL_PREFIX = "S:";
  
  //<exo:sql xmlns:exo="http://exoplatform.com/jcr"/>
  public static final String SQL_SUPPORT = "sql";
  
  //<exo:xpath xmlns:exo="http://exoplatform.com/jcr"/>
  public static final String XPATH_SUPPORT = "xpath";
  
  public static final String NOT_TAG = "not";
  public static final String AND_TAG = "and";
  public static final String OR_TAG = "or";
  public static final String LIKE_TAG = "like";
  public static final String EQ_TAG = "eq";
  
  public static final String LITERAL_TAG = "literal";

}
