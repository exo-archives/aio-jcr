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
public class MultivaluedMetadata  {

  private HashMap<String, List<String>> data = new HashMap<String, List<String>>();
  
  public void put(String key, List<String> vals) {
    data.put(key.toLowerCase(), vals);
  }
  
  public void add(String key, String value) {
    List<String> vals = data.get(key);
    if(vals == null)
      vals = new ArrayList<String>();
    vals.add(value);
    put(key.toLowerCase(), vals);
  }

  public void putSingle(String key, String value) {
    List<String> vals = new ArrayList<String>();
    vals.add(value);
    put(key.toLowerCase(), vals);
  }
  
  public String getFirst(String key) {
    List<String> vals = data.get(key.toLowerCase());
    if(vals == null || vals.size() == 0)
      return null;
    return vals.get(0);
  }
  
  public HashMap<String, String> getAll() {
    HashMap<String, String> h = new HashMap<String, String> ();
    Set<String> keys = data.keySet();
    Iterator<String> ikeys = keys.iterator();
    while (ikeys.hasNext()) {
      String key = ikeys.next();
      List<String> value = data.get(key);
      if(value != null)
        h.put(key, convertToString(value));
    }  
    return h;
  }

  public String get(String key) {
    key = key.toLowerCase();
    List <String> vals = data.get(key);
    if(vals != null) {
      return convertToString(data.get(key));
    }
    return null;
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
