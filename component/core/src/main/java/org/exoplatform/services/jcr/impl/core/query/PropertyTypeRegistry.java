/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.exoplatform.services.jcr.impl.core.query;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.jcr.PropertyType;

import org.apache.commons.logging.Log;

import org.exoplatform.services.jcr.core.nodetype.NodeTypeData;
import org.exoplatform.services.jcr.core.nodetype.NodeTypeDataManager;
import org.exoplatform.services.jcr.core.nodetype.PropertyDefinitionData;
import org.exoplatform.services.jcr.datamodel.InternalQName;
import org.exoplatform.services.jcr.impl.core.nodetype.NodeTypeManagerListener;
import org.exoplatform.services.log.ExoLogger;

/**
 * The <code>PropertyTypeRegistry</code> keeps track of registered node type
 * definitions and its property types. It provides a fast type lookup for a
 * given property name.
 */
public class PropertyTypeRegistry implements NodeTypeManagerListener {

  /** The logger instance for this class */
  private static final Log                        log         = ExoLogger.getLogger(PropertyTypeRegistry.class);

  /**
   * Empty <code>TypeMapping</code> array as return value if no type is found
   */
  private static final TypeMapping[]              EMPTY       = new TypeMapping[0];

  /** The NodeTypeRegistry */
  private final NodeTypeDataManager               nodeTypeDataManager;

  /** Property Name to TypeMapping[] mapping */
  private final Map<InternalQName, TypeMapping[]> typeMapping = new HashMap<InternalQName, TypeMapping[]>();

  /**
   * Creates a new <code>PropertyTypeRegistry</code> instance. This instance is
   * *not* registered as listener to the NodeTypeRegistry in the constructor!
   * 
   * @param reg the <code>NodeTypeRegistry</code> where to read the property
   *          type information.
   */
  public PropertyTypeRegistry(NodeTypeDataManager nodeTypeDataManager) {
    this.nodeTypeDataManager = nodeTypeDataManager;
    fillCache();
  }

  public NodeTypeDataManager getNodeTypeDataManager() {
    return nodeTypeDataManager;
  }

  /**
   * Returns an array of type mappings for a given property name
   * <code>propName</code>. If <code>propName</code> is not defined as a
   * property in any registered node type an empty array is returned.
   * 
   * @param propName the name of the property.
   * @return an array of <code>TypeMapping</code> instances.
   */
  public TypeMapping[] getPropertyTypes(InternalQName propName) {
    synchronized (typeMapping) {
      TypeMapping[] types = typeMapping.get(propName);
      if (types != null) {
        return types;
      } else {
        return EMPTY;
      }
    }
  }

  public void nodeTypeRegistered(InternalQName ntName) {
    NodeTypeData def = nodeTypeDataManager.findNodeType(ntName);
    PropertyDefinitionData[] propDefs = def.getDeclaredPropertyDefinitions();
    synchronized (typeMapping) {
      for (int i = 0; i < propDefs.length; i++) {
        int type = propDefs[i].getRequiredType();
        if (!propDefs[i].isResidualSet() && type != PropertyType.UNDEFINED) {
          InternalQName name = propDefs[i].getName();
          TypeMapping[] types = typeMapping.get(name);
          if (types == null) {
            types = new TypeMapping[1];
          } else {
            TypeMapping[] tmp = new TypeMapping[types.length + 1];
            System.arraycopy(types, 0, tmp, 0, types.length);
            types = tmp;
          }
          types[types.length - 1] = new TypeMapping(ntName, type, propDefs[i].isMultiple());
          typeMapping.put(name, types);
        }
      }
    }
  }

  public void nodeTypeReRegistered(InternalQName ntName) {
    nodeTypeUnregistered(ntName);
    nodeTypeRegistered(ntName);
  }

  public void nodeTypeUnregistered(InternalQName ntName) {
    // remove all TypeMapping instances refering to this ntName
    synchronized (typeMapping) {
      Map<InternalQName, TypeMapping[]> modified = new HashMap<InternalQName, TypeMapping[]>();
      for (Iterator<InternalQName> it = typeMapping.keySet().iterator(); it.hasNext();) {
        InternalQName propName = it.next();
        TypeMapping[] mapping = typeMapping.get(propName);
        List<TypeMapping> remove = null;
        for (int i = 0; i < mapping.length; i++) {
          if (mapping[i].ntName.equals(ntName)) {
            if (remove == null) {
              // not yet created
              remove = new ArrayList<TypeMapping>(mapping.length);
            }
            remove.add(mapping[i]);
          }
        }
        if (remove != null) {
          it.remove();
          if (mapping.length == remove.size()) {
            // all removed -> done
          } else {
            // only some removed
            List<TypeMapping> remaining = new ArrayList<TypeMapping>(Arrays.asList(mapping));
            remaining.removeAll(remove);
            modified.put(propName, remaining.toArray(new TypeMapping[remaining.size()]));
          }
        }
      }
      // finally re-add the modified mappings
      typeMapping.putAll(modified);
    }
  }

  /**
   * Initially fills the cache of this registry with property type definitions
   * from the {@link org.apache.jackrabbit.core.nodetype.NodeTypeRegistry}.
   */
  private void fillCache() {
    List<NodeTypeData> ntTypes = nodeTypeDataManager.getAllNodeTypes();
    for (NodeTypeData nodeTypeData : ntTypes) {
      nodeTypeRegistered(nodeTypeData.getName());
    }
  }

  public static class TypeMapping {

    /** The property type as an integer */
    public final int     type;

    /** The Name of the node type where this type mapping originated */
    final InternalQName  ntName;

    /** True if the property type is multi-valued */
    public final boolean isMultiValued;

    private TypeMapping(InternalQName ntName, int type, boolean isMultiValued) {
      this.type = type;
      this.ntName = ntName;
      this.isMultiValued = isMultiValued;
    }
  }
}
