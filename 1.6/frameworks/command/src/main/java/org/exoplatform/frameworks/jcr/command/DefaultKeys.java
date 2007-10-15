/**
 * Copyright 2001-2003 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 */

package org.exoplatform.frameworks.jcr.command;
/**
 * Created by The eXo Platform SARL        .
 * @author <a href="mailto:gennady.azarenkov@exoplatform.com">Gennady Azarenkov</a>
 * @version $Id: DefaultKeys.java 5800 2006-05-28 18:03:31Z geaz $
 */

public interface DefaultKeys {
  
  //public static final String COMMAND = "command";
  
  public static final String PATH = "path";
  public static final String WORKSPACE = "workspace";
  public static final String NODE_TYPE = "nodeType";
  public static final String RESULT = "result";
  public static final String CURRENT_NODE = "currentNode";
  public static final String NAME = "name";
  public static final String PROPERTY_TYPE = "propertyType";
  public static final String VALUES = "values";
  public static final String MULTI_VALUED = "multiValued";
  
  public static final String[] JCR_KEYS = {
    PATH, WORKSPACE, NODE_TYPE, RESULT, CURRENT_NODE
  };

}
