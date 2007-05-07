/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.services.webdav.common.property.factory;

import java.util.ArrayList;

import org.exoplatform.services.webdav.DavConst;

/**
 * Created by The eXo Platform SARL
 * Author : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
 * @version $Id: PropertyFactory.java 12635 2007-02-07 12:57:47Z gavrikvetal $
 */

public class PropertyFactory {
  
  private MappingTable mappingTable;
  private PropertyConfigTable configTable;
    
  public PropertyFactory(MappingTable mappingTable, PropertyConfigTable configTable) {
    this.mappingTable = mappingTable;
    this.configTable = configTable;
  }
    
  public PropertyDefine getDefine(String propertyNameSpace, String propertyName) {
    return new PropertyDefine(propertyNameSpace, propertyName, mappingTable, configTable);
  }
  
  public ArrayList<PropertyDefine> getDefines(String nodeTypeName) {    
    ArrayList<PropertyDefine> defines = new ArrayList<PropertyDefine>();
    
    ArrayList<String> includes = configTable.getIncludes(nodeTypeName);
    for (int i = 0; i < includes.size(); i++) {
      String curPropertyName = includes.get(i);

      int pos = curPropertyName.indexOf(":"); 
      
      if (pos > 0) {
        String propertyNameSpace = curPropertyName.substring(0, pos + 1);
        String propertyName = curPropertyName.substring(pos + 1);
        defines.add(getDefine(propertyNameSpace, propertyName));
      } else {
        defines.add(getDefine(DavConst.DAV_NAMESPACE, curPropertyName));
      }
            
    }
    
    return defines;
  }
  
}

//DAV:lastaccessed
//DAV:defaultdocument
//DAV:isstructureddocument
//DAV:getcontentlanguage
//DAV:contentclass
//DAV:isreadonly
//DAV:ishidden
//DAV:href
//DAV:name
