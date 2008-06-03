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
package org.exoplatform.services.jcr.impl;

import org.exoplatform.services.jcr.datamodel.IllegalPathException;
import org.exoplatform.services.jcr.datamodel.InternalQName;
import org.exoplatform.services.jcr.datamodel.QPath;

/**
 * Created by The eXo Platform SAS.
 *
 * @author <a href="mailto:geaz@users.sourceforge.net">Gennady Azarenkov</a>
 * @version $Id: Constants.java 13986 2008-05-08 10:48:43Z pnedonosko $
 */

public class Constants {

  // default namespace (empty uri)
  public static final String NS_EMPTY_PREFIX = "";

  public static final String NS_DEFAULT_URI = "";

  // reserved namespace for exo node types
  public static final String NS_EXO_PREFIX = "exo";

  public static final String NS_EXO_URI = "http://www.exoplatform.com/jcr/exo/1.0";

  // reserved namespace for items defined by built-in node types
  public static final String NS_JCR_PREFIX = "jcr";

  public static final String NS_JCR_URI = "http://www.jcp.org/jcr/1.0";

  // reserved namespace for built-in primary node types
  public static final String NS_NT_PREFIX = "nt";

  public static final String NS_NT_URI = "http://www.jcp.org/jcr/nt/1.0";

  // reserved namespace for built-in mixin node types
  public static final String NS_MIX_PREFIX = "mix";

  public static final String NS_MIX_URI = "http://www.jcp.org/jcr/mix/1.0";

  // reserved namespace used in the system view XML serialization format
  public static final String NS_SV_PREFIX = "sv";

  public static final String NS_SV_URI = "http://www.jcp.org/jcr/sv/1.0";

  // reserved namespaces that must not be redefined and should not be used
  public static final String NS_XML_PREFIX = "xml";

  public static final String NS_XML_URI = "http://www.w3.org/XML/1998/namespace";

  public static final String NS_XMLNS_PREFIX = "xmlns";

  public static final String NS_XMLNS_URI = "http://www.w3.org/2000/xmlns/";
  
  public static final String NS_XS_URI = "http://www.w3.org/2001/XMLSchema";
  
  public static final String NS_FN_URI = "http://www.w3.org/2004/10/xpath-functions";
  
  public static final InternalQName JCR_DEFAULT_NAME = new InternalQName(NS_DEFAULT_URI, "");
  
  /**
   * The special wildcard name used as the name of residual item definitions.
   */
  public static final InternalQName JCR_ANY_NAME = new InternalQName("", "*");

  
  public static final InternalQName JCR_PRIMARYTYPE = new InternalQName(NS_JCR_URI, "primaryType");
  
  public static final InternalQName JCR_SYSTEM = new InternalQName(NS_JCR_URI, "system");

  public static final InternalQName JCR_MIXINTYPES = new InternalQName(NS_JCR_URI, "mixinTypes");
  
  public static final InternalQName JCR_UUID = new InternalQName(NS_JCR_URI, "uuid");
  
  public static final InternalQName JCR_FROZENUUID = new InternalQName(NS_JCR_URI, "frozenUuid");
  
  public static final InternalQName JCR_FROZENNODE = new InternalQName(NS_JCR_URI, "frozenNode");
  
  public static final InternalQName JCR_PATH = new InternalQName(NS_JCR_URI, "path");
  
  public static final InternalQName JCR_VERSIONHISTORY = new InternalQName(NS_JCR_URI, "versionHistory");
  
  public static final InternalQName JCR_CHILDVERSIONHISTORY = new InternalQName(NS_JCR_URI, "childVersionHistory");
  
  public static final InternalQName JCR_BASEVERSION = new InternalQName(NS_JCR_URI, "baseVersion");
  
  public static final InternalQName JCR_ISCHECKEDOUT = new InternalQName(NS_JCR_URI, "isCheckedOut");
  
  public static final InternalQName JCR_VERSIONLABELS = new InternalQName(NS_JCR_URI, "versionLabels");
  
  public static final InternalQName JCR_VERSIONSTORAGE = new InternalQName(NS_JCR_URI, "versionStorage");
  
  public static final InternalQName JCR_VERSIONABLEUUID = new InternalQName(NS_JCR_URI, "versionableUuid");
  
  public static final InternalQName JCR_PREDECESSORS = new InternalQName(NS_JCR_URI, "predecessors");
  
  public static final InternalQName JCR_ROOTVERSION = new InternalQName(NS_JCR_URI, "rootVersion");
  
  public static final InternalQName JCR_CREATED = new InternalQName(NS_JCR_URI, "created");
  
  public static final InternalQName JCR_MIMETYPE = new InternalQName(NS_JCR_URI, "mimeType");
  
  public static final InternalQName JCR_CONTENT = new InternalQName(NS_JCR_URI, "content");

  public static final InternalQName JCR_XMLTEXT = new InternalQName(NS_JCR_URI, "xmltext");
  
  public static final InternalQName JCR_XMLCHARACTERS = new InternalQName(NS_JCR_URI, "xmlcharacters");
  
  public static final InternalQName JCR_SCORE = new InternalQName(NS_JCR_URI, "score");

  public static final InternalQName JCR_NODETYPENAME = new InternalQName(NS_JCR_URI, "nodeTypeName");
  
  public static final InternalQName JCR_ISMIXIN = new InternalQName(NS_JCR_URI, "isMixin");
  
  public static final InternalQName JCR_HASORDERABLECHILDNODES = new InternalQName(NS_JCR_URI, "hasOrderableChildNodes");
  
  public static final InternalQName JCR_PRIMARYITEMNAME = new InternalQName(NS_JCR_URI, "primaryItemName");
  
  public static final InternalQName JCR_SUPERTYPES = new InternalQName(NS_JCR_URI, "supertypes");

  public static final InternalQName JCR_PROPERTYDEFINITION = new InternalQName(NS_JCR_URI, "propertyDefinition");

  public static final InternalQName JCR_CHILDNODEDEFINITION = new InternalQName(NS_JCR_URI, "childNodeDefinition");

  public static final InternalQName JCR_NAME = new InternalQName(NS_JCR_URI, "name");
  
  public static final InternalQName JCR_AUTOCREATED = new InternalQName(NS_JCR_URI, "autoCreated");
  
  public static final InternalQName JCR_PROTECTED = new InternalQName(NS_JCR_URI, "protected");
  
  public static final InternalQName JCR_MULTIPLE = new InternalQName(NS_JCR_URI, "multiple");
  
  public static final InternalQName JCR_ONPARENTVERSION = new InternalQName(NS_JCR_URI, "onParentVersion");
  
  public static final InternalQName JCR_MANDATORY = new InternalQName(NS_JCR_URI, "mandatory");
  
  public static final InternalQName JCR_REQUIREDTYPE = new InternalQName(NS_JCR_URI, "requiredType");
  
  public static final InternalQName JCR_VALUECONSTRAINTS = new InternalQName(NS_JCR_URI, "valueConstraints");
  
  public static final InternalQName JCR_DEFAULTVALUES = new InternalQName(NS_JCR_URI, "defaultValues");
  
  public static final InternalQName JCR_REQUIREDPRIMARYTYPES = new InternalQName(NS_JCR_URI, "requiredPrimaryTypes");
  
  public static final InternalQName JCR_SAMENAMESIBLINGS = new InternalQName(NS_JCR_URI, "sameNameSiblings");
  
  public static final InternalQName JCR_DEFAULTPRIMNARYTYPE = new InternalQName(NS_JCR_URI, "defaultPrimaryType");
  
  public static final InternalQName JCR_MERGEFAILED = new InternalQName(NS_JCR_URI, "mergeFailed");
  
  public static final InternalQName JCR_LOCKOWNER = new InternalQName(NS_JCR_URI, "lockOwner");

  public static final InternalQName JCR_LOCKISDEEP = new InternalQName(NS_JCR_URI, "lockIsDeep");
  
  public static final InternalQName JCR_NODETYPES = new InternalQName(NS_JCR_URI, "nodetypes");
  
  public static final InternalQName JCR_FROZENPRIMARYTYPE = new InternalQName(NS_JCR_URI, "frozenPrimaryType");

  public static final InternalQName JCR_FROZENMIXINTYPES = new InternalQName(NS_JCR_URI, "frozenMixinTypes");

  public static final InternalQName JCR_SUCCESSORS = new InternalQName(NS_JCR_URI, "successors");
  
  public static final InternalQName JCR_LANGUAGE = new InternalQName(NS_JCR_URI, "language");
  
  public static final InternalQName JCR_STATEMENT = new InternalQName(NS_JCR_URI, "statement");

  public static final InternalQName JCR_DATA = new InternalQName(NS_JCR_URI, "data");
  
  public static final InternalQName JCR_LASTMODIFIED = new InternalQName(NS_JCR_URI, "lastModified");

  public static final InternalQName NT_BASE = new InternalQName(NS_NT_URI, "base");

  public static final InternalQName MIX_REFERENCEABLE = new InternalQName(NS_MIX_URI, "referenceable");
  
  public static final InternalQName MIX_VERSIONABLE = new InternalQName(NS_MIX_URI, "versionable");

  public static final InternalQName MIX_LOCKABLE = new InternalQName(NS_MIX_URI, "lockable");

  public static final InternalQName NT_VERSIONHISTORY = new InternalQName(NS_NT_URI, "versionHistory");
  
  public static final InternalQName NT_VERSIONLABELS = new InternalQName(NS_NT_URI, "versionLabels");
  
  public static final InternalQName NT_VERSION = new InternalQName(NS_NT_URI, "version");
  
  public static final InternalQName NT_FROZENNODE = new InternalQName(NS_NT_URI, "frozenNode");
  
  public static final InternalQName NT_UNSTRUCTURED = new InternalQName(NS_NT_URI, "unstructured");
  
  public static final InternalQName NT_RESOURCE = new InternalQName(NS_NT_URI, "resource");
  
  public static final InternalQName NT_NODETYPE = new InternalQName(NS_NT_URI, "nodeType");

  public static final InternalQName NT_PROPERTYDEFINITION = new InternalQName(NS_NT_URI, "propertyDefinition");
  
  public static final InternalQName NT_CHILDNODEDEFINITION = new InternalQName(NS_NT_URI, "childNodeDefinition");
  
  public static final InternalQName NT_HIERARCHYNODE = new InternalQName(NS_NT_URI, "hierarchyNode");

  public static final InternalQName NT_VERSIONEDCHILD = new InternalQName(NS_NT_URI, "versionedChild");
  
  public static final InternalQName NT_QUERY = new InternalQName(NS_NT_URI, "query");

  public static final InternalQName NT_FILE = new InternalQName(NS_NT_URI, "file");
  
  public static final InternalQName NT_FOLDER = new InternalQName(NS_NT_URI, "folder");

  public static final String ROOT_URI = "[]:1";
  
  //public static final String ROOT_UUID = "__eXo_jcr_root_uuid_____________";
  public static final String ROOT_PARENT_UUID = " ".intern(); // empty string
  
  public static final String ROOT_UUID =           "00exo0jcr0root0uuid0000000000000";
  
  public static final String SYSTEM_UUID =         "00exo0jcr0system0uuid00000000000";
  
  public static final String VERSIONSTORAGE_UUID = "00exo0jcr0version0storage0uuid00";
  
  public static final String NODETYPESROOT_UUID =  "00exo0jcr0node0types0root0uuid00";
  
  public static final String JCR_URI = "[http://www.jcp.org/jcr/1.0]";

  public static final String PRIMARY_TYPE_URI = "[http://www.jcp.org/jcr/1.0]primaryType";

  public static final String MIXIN_TYPE_URI = "[http://www.jcp.org/jcr/1.0]mixinTypes";

  public static final String ACCESS_TYPE_URI = "[http://www.exoplatform.com/jcr/exo/1.0]accessControllable";
  
  public static final String PRIVILEGABLE_TYPE_URI = "[http://www.exoplatform.com/jcr/exo/1.0]privilegeable";

//  public static final String NODE_OWNER_URI = "[http://www.exoplatform.com/jcr/exo/1.0]owner";
//
//  public static final String NODE_PERM_URI = "[http://www.exoplatform.com/jcr/exo/1.0]permissions";
  
  public static final String JCR_VERSION_STORAGE_URI = "[]:1[http://www.jcp.org/jcr/1.0]system:1[http://www.jcp.org/jcr/1.0]versionStorage:1";
  
  public static final String JCR_NODETYPES_URI = "[]:1[http://www.jcp.org/jcr/1.0]system:1[http://www.jcp.org/jcr/1.0]nodetypes:1";

  public static final String JCR_SYSTEM_URI = "[]:1[http://www.jcp.org/jcr/1.0]system:1";

  public static final InternalQName EXO_NAMESPACE = new InternalQName(NS_EXO_URI, "namespace");
  
  public static final InternalQName EXO_NAMESPACES = new InternalQName(NS_EXO_URI, "namespaces");
  
  public static final InternalQName EXO_URI_NAME = new InternalQName(NS_EXO_URI, "uri");
  
  public static final InternalQName EXO_PREFIX = new InternalQName(NS_EXO_URI, "prefix");
  
  public static final InternalQName EXO_VERSIONSTORAGE = new InternalQName(NS_EXO_URI, "versionStorage");
  
  public static final InternalQName EXO_ACCESS_CONTROLLABLE = new InternalQName(NS_EXO_URI, "accessControllable");

  public static final InternalQName EXO_OWNEABLE = new InternalQName(NS_EXO_URI, "owneable");

  public static final InternalQName EXO_PRIVILEGEABLE = new InternalQName(NS_EXO_URI, "privilegeable");

  public static final InternalQName EXO_OWNER = new InternalQName(NS_EXO_URI, "owner");

  public static final InternalQName EXO_PERMISSIONS = new InternalQName(NS_EXO_URI, "permissions");
  
  //-------- system view name constants
  /**
   * 
   */
  public static final String SV_NODE = "node";
  /**
   * 
   */
  public static final String SV_PROPERTY = "property";
  /**
   * 
   */
  public static final String SV_VALUE = "value";
  /**
   * 
   */
  public static final String SV_TYPE = "type";
  /**
   * 
   */
  public static final String SV_NAME = "name";
  
  /**
   * 
   */
  public static final String EXO_ID = "id";

  /**
   * 
   */
  public static final String EXO_MULTIVALUED = "multivalued";
  
  /**
   * sv:node
   */
  public static final InternalQName SV_NODE_NAME = new InternalQName(NS_SV_URI, SV_NODE);
  /**
   * sv:property
   */
  public static final InternalQName SV_PROPERTY_NAME = new InternalQName(NS_SV_URI, SV_PROPERTY);
  /**
   * sv:value
   */
  public static final InternalQName SV_VALUE_NAME = new InternalQName(NS_SV_URI, SV_VALUE);
  /**
   * sv:type
   */
  public static final InternalQName SV_TYPE_NAME = new InternalQName(NS_SV_URI, SV_TYPE);
  /**
   * sv:name
   */
  public static final InternalQName SV_NAME_NAME = new InternalQName(NS_SV_URI, SV_NAME);
  /**
   * exo:id
   */
  public static final InternalQName EXO_ID_NAME = new InternalQName(NS_EXO_URI, EXO_ID);
  
  public static QPath JCR_VERSION_STORAGE_PATH;
  
  public static QPath JCR_NODETYPES_PATH;
  
  public static QPath JCR_SYSTEM_PATH;
 
  public static QPath EXO_NAMESPACES_PATH;

  public static QPath ROOT_PATH;

  //** Formatting and validation constants
  /**
   * Chars in a UUID String.
   */
  public static int UUID_UNFORMATTED_LENGTH = 32;

  /**
   * Chars in a UUID String.
   */
  public static int UUID_FORMATTED_LENGTH = 32;
  

  
  static {
    
    try {
      JCR_SYSTEM_PATH = QPath.parse(JCR_SYSTEM_URI);
    } catch(IllegalPathException e) {
      e.printStackTrace();
      System.err.println("ERROR: Can't parse JCR_SYSTEM_URI for constant JCR_SYSTEM ("
          + JCR_SYSTEM_URI + "): " + e);
    }
    
    try {
      JCR_VERSION_STORAGE_PATH = QPath.parse(JCR_VERSION_STORAGE_URI);
    } catch(IllegalPathException e) {
      e.printStackTrace();
      System.err.println("ERROR: Can't parse JCR_VERSION_STORAGE_URI for constant JCR_VERSION_STORAGE_PATH ("
          + JCR_VERSION_STORAGE_URI + "): " + e);
    }
    
    try {
      JCR_NODETYPES_PATH = QPath.parse(JCR_NODETYPES_URI);
    } catch(IllegalPathException e) {
      e.printStackTrace();
      System.err.println("ERROR: Can't parse JCR_NODETYPES_URI for constant JCR_NODETYPES_PATH ("
          + JCR_NODETYPES_URI + "): " + e);
    }
    
    String nsUri = JCR_SYSTEM_URI + EXO_NAMESPACES.getAsString() + ":1";
    try {
      EXO_NAMESPACES_PATH = QPath.parse(nsUri);
    } catch(IllegalPathException e) {
      e.printStackTrace();
      System.err.println("ERROR: Can't parse EXO_NAMESPACES_URI for constant EXO_NAMESPACES (" + nsUri + "): " + e);
    }

    
    try {
      ROOT_PATH = QPath.parse(ROOT_URI);
    } catch(IllegalPathException e) {
      e.printStackTrace();
      System.err.println("ERROR: Can't parse ROOT_URI " + e);
    }

  }

  public static final String DEFAULT_ENCODING = "UTF-8";
  
}