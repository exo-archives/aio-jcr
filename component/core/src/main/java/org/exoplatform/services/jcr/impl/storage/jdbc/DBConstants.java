/**
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 */

package org.exoplatform.services.jcr.impl.storage.jdbc;
/**
 * Created by The eXo Platform SARL        .
 * @author Gennady Azarenkov
 * @version $Id: DBConstants.java 12841 2007-02-16 08:58:38Z peterit $
 */

public class DBConstants {
  //error constants
  protected String JCR_FK_ITEM_PARENT;
  protected String JCR_FK_NODE_ITEM;
  protected String JCR_FK_PROPERTY_NODE;
  protected String JCR_FK_PROPERTY_ITEM;
  protected String JCR_FK_VALUE_PROPERTY;
  
  protected String JCR_PK_ITEM;
  
  protected String FIND_ITEM_BY_ID;
  protected String FIND_ITEM_BY_PATH;
  protected String FIND_ITEM_BY_NAME;
  
  protected String FIND_CHILD_PROPERTY_BY_PATH;
  protected String FIND_PROPERTY_BY_NAME;
  
  protected String FIND_REFERENCES;
  
  protected String FIND_VALUES_BY_PROPERTYID;
  
  protected String FIND_VALUE_BY_PROPERTYID_OREDERNUMB;
  
  protected String FIND_NODES_BY_PARENTID;
  protected String FIND_PROPERTIES_BY_PARENTID;
  
  // protected String INSERT_ITEM;
  protected String INSERT_NODE;
  protected String INSERT_PROPERTY;
  protected String INSERT_VALUE;
  protected String INSERT_REF;
  
  //protected String UPDATE_ITEM;
  //protected String UPDATE_ITEM_PATH;
  protected String UPDATE_NODE;
  protected String UPDATE_PROPERTY;
  //protected String UPDATE_VALUE;
  
  protected String DELETE_ITEM;
  //protected String DELETE_NODE;
  //protected String DELETE_PROPERTY;
  protected String DELETE_VALUE;
  protected String DELETE_REF;
  
  // ITEM table
  protected static final String COLUMN_ID = "ID";
  protected static final String COLUMN_PARENTID = "PARENT_ID";
  protected static final String COLUMN_NAME = "NAME";
  //protected static final String COLUMN_PATH = "PATH";
  protected static final String COLUMN_VERSION = "VERSION";
  protected static final String COLUMN_CLASS = "I_CLASS";
  protected static final String COLUMN_INDEX = "I_INDEX";
  
  // NODE table
  protected static final String COLUMN_NORDERNUM = "N_ORDER_NUM";
  
  // PROPERTY table
  protected static final String COLUMN_PTYPE = "P_TYPE";
  protected static final String COLUMN_PMULTIVALUED = "P_MULTIVALUED";

  // VALUE table
  protected static final String COLUMN_VDATA = "DATA";
  protected static final String COLUMN_VORDERNUM = "ORDER_NUM";  
  protected static final String COLUMN_VSTORAGE_DESC = "STORAGE_DESC";
}
