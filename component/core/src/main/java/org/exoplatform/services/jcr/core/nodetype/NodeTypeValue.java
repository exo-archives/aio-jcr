/**
 * Copyright 2001-2003 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 */

package org.exoplatform.services.jcr.core.nodetype;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by The eXo Platform SARL .<br/>
 * NodeType value object
 * @author <a href="mailto:gennady.azarenkov@exoplatform.com">Gennady Azarenkov</a>
 * @version $Id: NodeTypeValue.java 12843 2007-02-16 09:11:18Z peterit $
 */

public class NodeTypeValue {

  protected String name;

  protected boolean mixin;

  protected boolean orderableChild;

  protected String primaryItemName;

  protected List<String> declaredSupertypeNames;

  protected List<PropertyDefinitionValue> declaredPropertyDefinitionValues;

  protected List<NodeDefinitionValue> declaredChildNodeDefinitionValues;

  public NodeTypeValue() {
  }

  /**
   * @return Returns the declaredSupertypeNames.
   */
  public List<String> getDeclaredSupertypeNames() {
    return declaredSupertypeNames;
  }

  /**
   * @param declaredSupertypeNames
   *          The declaredSupertypeNames to set.
   */
  public void setDeclaredSupertypeNames(List<String> declaredSupertypeNames) {
    this.declaredSupertypeNames = declaredSupertypeNames;
  }

  /**
   * @return Returns the mixin.
   */
  public boolean isMixin() {
    return mixin;
  }

  /**
   * @param mixin
   *          The mixin to set.
   */
  public void setMixin(boolean mixin) {
    this.mixin = mixin;
  }

  /**
   * @return Returns the name.
   */
  public String getName() {
    return name;
  }

  /**
   * @param name
   *          The name to set.
   */
  public void setName(String name) {
    this.name = name;
  }

  /**
   * @return Returns the orderableChild.
   */
  public boolean isOrderableChild() {
    return orderableChild;
  }

  /**
   * @param orderableChild
   *          The orderableChild to set.
   */
  public void setOrderableChild(boolean orderableChild) {
    this.orderableChild = orderableChild;
  }

  /**
   * @return Returns the primaryItemName.
   */
  public String getPrimaryItemName() {
    return primaryItemName;
  }

  /**
   * @param primaryItemName
   *          The primaryItemName to set.
   */
  public void setPrimaryItemName(String primaryItemName) {
    this.primaryItemName = primaryItemName;
  }

  /**
   * @return Returns the declaredChildNodeDefinitionNames.
   */
  public List<NodeDefinitionValue> getDeclaredChildNodeDefinitionValues() {
    return declaredChildNodeDefinitionValues;
  }

  /**
   * @param declaredChildNodeDefinitionNames
   *          The declaredChildNodeDefinitionNames to set.
   */
  public void setDeclaredChildNodeDefinitionValues(
      List<NodeDefinitionValue> declaredChildNodeDefinitionValues) {
    this.declaredChildNodeDefinitionValues = declaredChildNodeDefinitionValues;
  }

  /**
   * @return Returns the declaredPropertyDefinitionNames.
   */
  public List<PropertyDefinitionValue> getDeclaredPropertyDefinitionValues() {
    return declaredPropertyDefinitionValues;
  }

  /**
   * @param declaredPropertyDefinitionNames
   *          The declaredPropertyDefinitionNames to set.
   */
  public void setDeclaredPropertyDefinitionValues(
      List<PropertyDefinitionValue> declaredPropertyDefinitionValues) {
    this.declaredPropertyDefinitionValues = declaredPropertyDefinitionValues;
  }

  /**
   * validateNodeType, method checks the value bean for each valid filed
   */
  public boolean validateNodeType() {

    boolean hasValidated = false;

    if (primaryItemName != null) {
      if (primaryItemName.length() <= 0) {
        primaryItemName = null;
        hasValidated = true;
      }
    }

    if (declaredSupertypeNames == null) {
      declaredSupertypeNames = new ArrayList<String>();
      hasValidated = true;
    } else {
      int prevSize = declaredSupertypeNames.size();
      fixStringsList(declaredSupertypeNames);
      hasValidated = prevSize != declaredSupertypeNames.size();
    }

    if (declaredPropertyDefinitionValues == null) {
      declaredPropertyDefinitionValues = new ArrayList<PropertyDefinitionValue>();
      hasValidated = true;
    } else {
      int prevSize = declaredPropertyDefinitionValues.size();
      fixPropertyDefinitionValuesList(declaredPropertyDefinitionValues);
      hasValidated = prevSize != declaredPropertyDefinitionValues.size();
    }

    if (declaredChildNodeDefinitionValues == null) {
      declaredChildNodeDefinitionValues = new ArrayList<NodeDefinitionValue>();
      hasValidated = true;
    } else {
      int prevSize = declaredChildNodeDefinitionValues.size();
      fixNodeDefinitionValuesList(declaredChildNodeDefinitionValues);
      hasValidated = prevSize != declaredChildNodeDefinitionValues.size();
    }

    return hasValidated;
  }

  private void fixStringsList(List<String> strList) {
    int i = 0;
    while (i < strList.size()) {
      if (strList.get(i) == null) {
        strList.remove(i);
      } else if (strList.get(i) != null) {
        String s = strList.get(i);
        s.trim();
        if (s.length() <= 0) {
          strList.remove(i);
        } else {
          i++;
        }
      }
    }
  }

  private void fixPropertyDefinitionValuesList(List<PropertyDefinitionValue> pdvList) {
    int i = 0;
    while (i < pdvList.size()) {
      if (pdvList.get(i) == null) {
        pdvList.remove(i);
      } else {
        PropertyDefinitionValue p = pdvList.get(i++);
        if (p.getValueConstraints() != null) {
          fixStringsList(p.getValueConstraints());
        }
        if (p.getDefaultValueStrings() != null) {
          fixStringsList(p.getDefaultValueStrings());
        }
      }
    }
  }

  private void fixNodeDefinitionValuesList(List<NodeDefinitionValue> ndvList) {
    int i = 0;
    while (i < ndvList.size()) {
      if (ndvList.get(i) == null) {
        ndvList.remove(i);
      } else {
        NodeDefinitionValue p = (NodeDefinitionValue) ndvList.get(i++);
        if (p.getRequiredNodeTypeNames() != null) {
          fixStringsList(p.getRequiredNodeTypeNames());
          if (p.getRequiredNodeTypeNames().size() == 0) {
            // Fixing field requiredNodeTypeNames according the specefication (6.7.14) for NodeDefinition
            List<String> defNotEmptyArray = new ArrayList<String>();
            defNotEmptyArray.add("nt:base");
            p.setRequiredNodeTypeNames(defNotEmptyArray);
          }
        }
        if (p.getDefaultNodeTypeName() != null) {          
          if (p.getDefaultNodeTypeName().length() <= 0) {
            p.setDefaultNodeTypeName(null);            
          }
        }
      }
    }
  }
  
}
