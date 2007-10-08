/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.services.webdav.search.basicsearch;

import java.util.Vector;

import org.exoplatform.services.webdav.search.Search;
import org.exoplatform.services.webdav.search.SearchConst;
import org.w3c.dom.Node;

/**
 * Created by The eXo Platform SARL
 * Author : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
 * @version $Id: BasicSearch.java 12525 2007-02-02 12:26:47Z gavrikvetal $
 */

public class BasicSearch implements Search {

  private String query;

  public boolean init(Node node) {
    return false;
  }

  public String getQueryLanguage() {
    return SearchConst.SearchType.SQL;
  }

  public String getQuery() {
    return query;
  }

  public Vector<String> getRequiredPropertyList() {
    return new Vector<String>();
  }

}
