/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.services.webdav.search;

import java.util.Vector;

import org.w3c.dom.Node;

/**
 * Created by The eXo Platform SARL
 * Author : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
 * @version $Id: Search.java 12004 2007-01-17 12:03:57Z geaz $
 */

public interface Search {
  
  boolean init(Node node);
  
  String getQueryLanguage();
  
  String getQuery();
  
  Vector<String> getRequiredPropertyList();
  
}
