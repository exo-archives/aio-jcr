/*
 * Copyright (C) 2003-2007 eXo Platform SAS.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
 */
package org.exoplatform.services.jcr.impl.storage.jdbc;
/**
 * Created by The eXo Platform SAS.
 * @author Gennady Azarenkov
 * @version $Id: DBConstants.java 11907 2008-03-13 15:36:21Z ksm $
 */

public class DBConstants {
  //error constants
  protected String JCR_PK_ITEM;
  protected String JCR_FK_ITEM_PARENT;
  protected String JCR_IDX_ITEM_PARENT;
  protected String JCR_IDX_ITEM_PARENT_NAME;
  protected String JCR_IDX_ITEM_PARENT_ID;
  protected String JCR_PK_VALUE;
  protected String JCR_FK_VALUE_PROPERTY;
  protected String JCR_IDX_VALUE_PROPERTY;
  protected String JCR_PK_REF;
  protected String JCR_IDX_REF_PROPERTY;
  
  protected String FIND_ITEM_BY_ID;
  protected String FIND_ITEM_BY_PATH;
  protected String FIND_ITEM_BY_NAME;
  
  protected String FIND_CHILD_PROPERTY_BY_PATH;
  protected String FIND_PROPERTY_BY_NAME;
  
  protected String FIND_REFERENCES;
  
  protected String FIND_VALUES_BY_PROPERTYID;
  //protected String FIND_VALUESDATA_BY_PROPERTYID;
  
  protected String FIND_VALUE_BY_PROPERTYID_OREDERNUMB;
  
  protected String FIND_NODES_BY_PARENTID;
  protected String FIND_PROPERTIES_BY_PARENTID;
  
  protected String INSERT_NODE;
  protected String INSERT_PROPERTY;
  protected String INSERT_VALUE;
  protected String INSERT_REF;
  
  protected String RENAME_NODE;
  
  protected String UPDATE_NODE;
  protected String UPDATE_PROPERTY;
  
  protected String DELETE_ITEM;
  protected String DELETE_VALUE;
  protected String DELETE_REF;
  
  // ITEM table
  protected static final String COLUMN_ID = "ID";
  protected static final String COLUMN_PARENTID = "PARENT_ID";
  protected static final String COLUMN_NAME = "NAME";
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
  
  // Dialects
  public final static String DB_DIALECT_GENERIC = "Generic".intern();
  public final static String DB_DIALECT_ORACLE = "Oracle".intern();
  public final static String DB_DIALECT_ORACLEOCI = "Oracle-OCI".intern();
  public final static String DB_DIALECT_PGSQL = "PgSQL".intern();
  public final static String DB_DIALECT_MYSQL = "MySQL".intern();
  public final static String DB_DIALECT_MYSQL_UTF8 = "MySQL-UTF8".intern();
  public final static String DB_DIALECT_HSQLDB = "HSQLDB".intern();
  public final static String DB_DIALECT_DB2 = "DB2".intern();
  public final static String DB_DIALECT_DB2V8 = "DB2V8".intern();
  public final static String DB_DIALECT_MSSQL = "MSSQL".intern();
  public final static String DB_DIALECT_SYBASE = "Sybase".intern();
  public final static String DB_DIALECT_DERBY = "Derby".intern();
      
  public final static String[] DB_DIALECTS = {DB_DIALECT_GENERIC, DB_DIALECT_ORACLE, DB_DIALECT_ORACLEOCI, DB_DIALECT_PGSQL, 
    DB_DIALECT_MYSQL, DB_DIALECT_HSQLDB, DB_DIALECT_DB2, DB_DIALECT_DB2V8, DB_DIALECT_MSSQL, DB_DIALECT_SYBASE, DB_DIALECT_DERBY, 
    DB_DIALECT_MYSQL_UTF8};
  
}
