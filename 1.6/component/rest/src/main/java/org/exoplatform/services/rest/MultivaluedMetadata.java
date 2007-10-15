/**
 * Copyright 2001-2007 The eXo Platform SAS         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 */

package org.exoplatform.services.rest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import java.util.List;

/**
 * Created by The eXo Platform SARL        .
 * @author Gennady Azarenkov
 * @version $Id: $
 */

public class MultivaluedMetadata extends HashMap<String, List<String>> {
  
  public void add(String key, String value) {
    List<String> vals = get(key);
    if(vals == null)
      vals = new ArrayList<String>();
    vals.add(value);
  }

  public void putSingle(String key, String value) {
    List<String> vals = new ArrayList<String>();
    vals.add(value);
    put(key, vals);
  }
  
  public String getFirst(String key) {
    List<String> vals = get(key);
    if(vals == null || vals.size() == 0)
      return null;
    return vals.get(0);
  }
  
  public HashMap<String, String> getAll() {
    HashMap<String, String> h = new HashMap<String, String> ();
    Set<String> keys = keySet();
    Iterator<String> ikeys = keys.iterator();
    while (ikeys.hasNext()) {
      String key = ikeys.next();
      List<String> value = get(key);
      if(value != null)
        h.put(key, convertToString(value));
    }  
    return h;
  }
  
  private String convertToString(List<String> list) {
    if(list.size() == 0)
      return null;
    StringBuffer sb = new StringBuffer();
    for(String t : list)
      sb.append(t + ",");
    return sb.deleteCharAt(sb.length() - 1).toString();
  }

}
