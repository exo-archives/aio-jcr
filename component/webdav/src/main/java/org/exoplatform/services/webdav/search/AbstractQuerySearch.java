/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.services.webdav.search;

import java.util.Vector;

import org.apache.commons.logging.Log;
import org.exoplatform.services.log.ExoLogger;

/**
 * Created by The eXo Platform SARL
 * Author : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
 * @version $Id: AbstractQuerySearch.java 12004 2007-01-17 12:03:57Z geaz $
 */

public abstract class AbstractQuerySearch implements Search {
  
  private static Log log = ExoLogger.getLogger("jcr.AbstractQuerySearch");
  
  protected String query;

  public boolean init(org.w3c.dom.Node node) {
    query = node.getTextContent();
    
    log.info("Received query: [" + query + "]");
    
    return true;
  }
  
  public Vector<String> getRequiredPropertyList() {
    return new Vector<String>();
  }
  
  public abstract String getQueryLanguage();
  
  public String getQuery() {
    return query;
  }
  
}
