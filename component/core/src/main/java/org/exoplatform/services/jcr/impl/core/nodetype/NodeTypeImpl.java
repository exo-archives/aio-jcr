/**
 **************************************************************************
 * Copyright 2001-2003 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 */

package org.exoplatform.services.jcr.impl.core.nodetype;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.MalformedInputException;
import java.nio.charset.UnmappableCharacterException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import javax.jcr.Value;
import javax.jcr.nodetype.ConstraintViolationException;
import javax.jcr.nodetype.ItemDefinition;
import javax.jcr.nodetype.NoSuchNodeTypeException;
import javax.jcr.nodetype.NodeDefinition;
import javax.jcr.nodetype.NodeType;
import javax.jcr.nodetype.PropertyDefinition;

import org.exoplatform.commons.utils.ISO8601;
import org.exoplatform.services.jcr.core.nodetype.ExtendedItemDefinition;
import org.exoplatform.services.jcr.core.nodetype.ExtendedNodeType;
import org.exoplatform.services.jcr.core.nodetype.NodeDefinitionValue;
import org.exoplatform.services.jcr.core.nodetype.NodeTypeValue;
import org.exoplatform.services.jcr.core.nodetype.PropertyDefinitionValue;
import org.exoplatform.services.jcr.core.nodetype.PropertyDefinitions;
import org.exoplatform.services.jcr.datamodel.InternalQName;
import org.exoplatform.services.jcr.impl.Constants;
import org.exoplatform.services.jcr.impl.core.value.NameValue;

/**
 * Created by The eXo Platform SARL .
 * 
 * @author <a href="mailto:geaz@users.sourceforge.net">Gennady Azarenkov</a>
 * @version $Id: NodeTypeImpl.java 12841 2007-02-16 08:58:38Z peterit $
 */
public class NodeTypeImpl implements ExtendedNodeType {

	protected static final Value[] NULL_VALUES = { null };
	
	protected static final String[] EMPTY_CONSTRAINTS = new String[0];

	protected boolean mixin;

	protected boolean orderableChild;

	protected String primaryItemName;

	protected NodeType[] declaredSupertypes;

	protected PropertyDefinition[] declaredPropertyDefinitions;

	protected NodeDefinition[] declaredChildNodeDefinitions;

	protected NodeTypeManagerImpl manager;
  
  protected InternalQName qName;
  
	protected NodeTypeImpl(NodeTypeManagerImpl manager) {
		this.manager = manager;
	}

	NodeTypeImpl(NodeTypeManagerImpl manager,  
			NodeTypeValue value) throws NoSuchNodeTypeException,
			ConstraintViolationException, RepositoryException {
		this(manager);
    
    this.qName = manager.getLocationFactory().parseJCRName(value.getName()).getInternalName();
		this.mixin = value.isMixin();
		this.orderableChild = value.isOrderableChild();
		this.primaryItemName = value.getPrimaryItemName();

		List<String> supertypesList = value.getDeclaredSupertypeNames();
		this.declaredSupertypes = new NodeType[supertypesList.size()];
		for (int i = 0; i < supertypesList.size(); i++) {
			declaredSupertypes[i] = manager.getNodeType(supertypesList.get(i));
    }

		List declaredPropertyDefList = value.getDeclaredPropertyDefinitionValues();

		this.declaredPropertyDefinitions = new PropertyDefinition[declaredPropertyDefList.size()];
		for (int i = 0; i < declaredPropertyDefList.size(); i++) {
			PropertyDefinitionValue p = (PropertyDefinitionValue) declaredPropertyDefList.get(i);
			
			 //According the specification constraints can be null, indicationg
			 //that value constraint information is unavailable
			String[] constraints = null;
			if (p.getValueConstraints() != null) {
				
				 //Or an array interpreted as disjunctive set of constraints
				 //Empty array means that constraints is available and that no
				 //constaraints are placed
				 
				constraints = new String[p.getValueConstraints().size()];
				for (int j = 0; j < p.getValueConstraints().size(); j++) {
					constraints[j] = (String) p.getValueConstraints().get(j);
				}
			}
			
			 //According the specification defValues can be null, indicationg
			 //that has no fixed default value for property definition
			Value[] defValues = null;
			if (p.getDefaultValueStrings() != null) {
				// Or an array of values objects 
				defValues = new Value[p.getDefaultValueStrings().size()];
				for (int j = 0; j < p.getDefaultValueStrings().size(); j++) {
					defValues[j] = manager.getValueFactory().createValue((String) p
							.getDefaultValueStrings().get(j), p
							.getRequiredType());
				}
			}

      InternalQName pdName = manager.getLocationFactory().parseJCRName(p.getName()).getInternalName();
      declaredPropertyDefinitions[i] = new PropertyDefinitionImpl(p
          .getName(), this, p.getRequiredType(), constraints,
          defValues, p.isAutoCreate(), p.isMandatory(), p
              .getOnVersion(), p.isReadOnly(), p.isMultiple(), pdName);
		}

		List declaredChildNodeDefList = value.getDeclaredChildNodeDefinitionValues();
		this.declaredChildNodeDefinitions = new NodeDefinition[declaredChildNodeDefList.size()];
		for (int i = 0; i < declaredChildNodeDefList.size(); i++) {
			NodeDefinitionValue ndv = (NodeDefinitionValue) declaredChildNodeDefList.get(i);

			NodeType[] requiredTypes = new NodeType[ndv.getRequiredNodeTypeNames().size()];
			for (int j = 0; j < ndv.getRequiredNodeTypeNames().size(); j++) {
				requiredTypes[j] = manager.getNodeType((String) ndv.getRequiredNodeTypeNames().get(j));
			}

      //According the specification DefaultPrimaryType can be null,
			 //indicationg that DefaultPrimaryType is not specified
      
			NodeType nt = null;
      
			if (ndv.getDefaultNodeTypeName() != null) {
        
        if (ndv.getDefaultNodeTypeName().equals(getName())) {
          // requires himself
          nt = this;
        } else {
          nt = manager.getNodeType(ndv.getDefaultNodeTypeName());
        }
			}
      
      InternalQName ndName = manager.getLocationFactory().parseJCRName(ndv.getName()).getInternalName();
      declaredChildNodeDefinitions[i] = new NodeDefinitionImpl(
          ndv.getName(), this, requiredTypes, nt, ndv.isAutoCreate(), ndv
          .isMandatory(), ndv.getOnVersion(), ndv.isReadOnly(), ndv
          .isSameNameSiblings(), ndName);
		}
	}
  

	/** @see javax.jcr.nodetype.NodeType#getName */
	public String getName() {

    String n = null;
    try {
      n = manager.getLocationFactory().createJCRName(qName).getAsString();
    } catch (RepositoryException e) {
      // should never happen
      e.printStackTrace();
      throw new RuntimeException("TYPE NAME >>> "+n+" "+e);
    }
    return n;
    
    
	}

	/** @see javax.jcr.nodetype.NodeType#isMixin */
	public boolean isMixin() {
		return mixin;
	}

	/** @see javax.jcr.nodetype.NodeType#hasOrderableChildNodes */
	public boolean hasOrderableChildNodes() {
		return orderableChild;
	}

	/** @see javax.jcr.nodetype.NodeType#getPrimaryItemName */
	public String getPrimaryItemName() {
		return primaryItemName;
	}

	/** @see javax.jcr.nodetype.NodeType#getSupertypes */
	public NodeType[] getSupertypes() {
		ArrayList<NodeType> stypesList = new ArrayList<NodeType>();
		fillSupertypes(stypesList, this);
		if (stypesList.size() > 0) {
			NodeType[] supertypes = new NodeType[stypesList.size()];
			for (int i = 0; i < stypesList.size(); i++) {
				supertypes[i] = stypesList.get(i);
			}
			return supertypes;
		}
		return new NodeType[0];
	}

	/** @see javax.jcr.nodetype.NodeType#getDeclaredSupertypes */
	public NodeType[] getDeclaredSupertypes() {
		if (declaredSupertypes != null)
			return declaredSupertypes;
		else
			return new NodeType[0];

	}

	/** @see javax.jcr.nodetype.NodeType#getDeclaredSupertypes */
	public boolean isNodeType(String nodeTypeName) {
		NodeType superType = null;
		try {
			superType = manager.getNodeType(nodeTypeName);
      //superType = ((NodeTypeManagerImpl)manager).
      //  findNodeType(getQName(nodeTypeName));
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(" NodeTypeImpl.isNodeType (" + superType
					+ ") failed Reason:" + e);
		}
		return isSameOrSubType(superType, this);
	}

  public boolean isNodeType(InternalQName nodeTypeQName) {
    NodeType superType = null;
    try {
      superType = manager.getNodeType(nodeTypeQName);
    } catch (Exception e) {
      e.printStackTrace();
      throw new RuntimeException(" NodeTypeImpl.isNodeType (" + superType
          + ") failed Reason:" + e);
    }
    return isSameOrSubType(superType, this);
  }  

	/** @see javax.jcr.nodetype.NodeType#getPropertyDefs */
	public PropertyDefinition[] getPropertyDefinitions() {
	  
  Set<PropertyDefinition> propertyDefsList = new LinkedHashSet<PropertyDefinition>();
  if (declaredPropertyDefinitions != null) {
    for (int i = 0; i < declaredPropertyDefinitions.length; i++) {
      propertyDefsList.add(declaredPropertyDefinitions[i]);
    }
  }
  NodeType[] supertypes = getSupertypes();
  if (supertypes != null) {
    for (int i = 0; i < supertypes.length; i++) {
      if (supertypes[i].getDeclaredPropertyDefinitions() != null) {
        for (int j = 0; j < supertypes[i].getDeclaredPropertyDefinitions().length; j++) {
          if (!propertyDefsList.contains(supertypes[i].getDeclaredPropertyDefinitions()[j]))
            propertyDefsList.add(supertypes[i].getDeclaredPropertyDefinitions()[j]);
        }
      }
    }
  }
  if (propertyDefsList.size() > 0) {
    PropertyDefinition[] propertyDefs = new PropertyDefinition[propertyDefsList
        .size()];
    
    return propertyDefsList.toArray(propertyDefs);
  }
  return new PropertyDefinition[0];		
		
	}

	/** @see javax.jcr.nodetype.NodeType#getDeclaredPropertyDefs */
	public PropertyDefinition[] getDeclaredPropertyDefinitions() {
		if (declaredPropertyDefinitions != null)
			return declaredPropertyDefinitions;
		else
			return new PropertyDefinition[0];
	}

	/** @see javax.jcr.nodetype.NodeType#getChildNodeDefs */
	public NodeDefinition[] getChildNodeDefinitions() {
		ArrayList<NodeDefinition> nodeDefsList = new ArrayList<NodeDefinition>();
		if (declaredChildNodeDefinitions != null) {
			for (int i = 0; i < declaredChildNodeDefinitions.length; i++) {
				nodeDefsList.add(declaredChildNodeDefinitions[i]);
			}
		}
		NodeType[] supertypes = getSupertypes();
		if (supertypes != null) {
			for (int i = 0; i < supertypes.length; i++) {
				if (supertypes[i].getDeclaredChildNodeDefinitions() != null) {
					for (int j = 0; j < supertypes[i]
							.getDeclaredChildNodeDefinitions().length; j++) {
						if (!nodeDefsList.contains(supertypes[i]
								.getDeclaredChildNodeDefinitions()[j])) {
							nodeDefsList.add(supertypes[i]
									.getDeclaredChildNodeDefinitions()[j]);
						}
					}
				}
			}
		}
		if (nodeDefsList.size() > 0) {
			NodeDefinition[] childNodeDefs = new NodeDefinition[nodeDefsList
					.size()];
			for (int i = 0; i < nodeDefsList.size(); i++) {
				childNodeDefs[i] = nodeDefsList.get(i);
			}
			return childNodeDefs;
		}
		return new NodeDefinition[0];
	}

	/** @see javax.jcr.nodetype.NodeType#getDeclaredChildNodeDefs */
	public NodeDefinition[] getDeclaredChildNodeDefinitions() {
		if (declaredChildNodeDefinitions != null)
			return declaredChildNodeDefinitions;
		else
			return new NodeDefinition[0];
	}

	/** @see javax.jcr.nodetype.NodeType#canSetProperty */
	public boolean canSetProperty(String propertyName, Value value) {

		PropertyDefinition def = getPropertyDefinitions(propertyName)
				.getDefinition(false);
		if (def != null) {
			if (def.isProtected()) {
				return false;
			} else {
				int requiredType = def.getRequiredType();
				return canSetPropertyForType(requiredType, propertyName, value,
						def.getValueConstraints());
			}
		} else {
			return false;
		}
	}

	private boolean canSetPropertyForType(int requiredType,
			String propertyName, Value value, String[] constrains) {

		if (value == null) {
			return canRemoveItem(propertyName);
		} else if (requiredType == value.getType()) {
			return checkValueConstraints(constrains, value);
		} else if (requiredType == PropertyType.BINARY
				&& (value.getType() == PropertyType.STRING
						|| value.getType() == PropertyType.DATE
						|| value.getType() == PropertyType.LONG
						|| value.getType() == PropertyType.DOUBLE
						|| value.getType() == PropertyType.NAME
						|| value.getType() == PropertyType.PATH || value
						.getType() == PropertyType.BOOLEAN)) {
			return checkValueConstraints(constrains, value);
		} else if (requiredType == PropertyType.BOOLEAN) {
			if (value.getType() == PropertyType.STRING) {
				return checkValueConstraints(constrains, value);
			} else if (value.getType() == PropertyType.BINARY) {
				try {
					return isCharsetString(value.getString(),
							Constants.DEFAULT_ENCODING)
							&& checkValueConstraints(constrains, value);
				} catch (Exception e) {
					// Hm, this is not string and not UTF-8 too
					return false;
				}
			} else {
				return false;
			}
		} else if (requiredType == PropertyType.DATE) {
			String likeDataString = null;
			try {
				if (value.getType() == PropertyType.STRING) {
					likeDataString = value.getString();
				} else if (value.getType() == PropertyType.BINARY) {
					likeDataString = getCharsetString(value.getString(),
							Constants.DEFAULT_ENCODING);
					// likeDataString = value.getString(); // This line
					// can work too, replacing line below
				} else if (value.getType() == PropertyType.DOUBLE
						|| value.getType() == PropertyType.LONG) {
					return checkValueConstraints(constrains, value);
				} else {
					return false;
				}
				Calendar calDate = ISO8601.parse(likeDataString);
				return calDate != null
						&& checkValueConstraints(constrains, value);
			} catch (Exception e) {
				// Hm, this is not date format string
				return false;
			}
		} else if (requiredType == PropertyType.DOUBLE) {
			String likeDoubleString = null;
			try {
				if (value.getType() == PropertyType.STRING) {
					likeDoubleString = value.getString();
				} else if (value.getType() == PropertyType.BINARY) {
					likeDoubleString = getCharsetString(value.getString(),
							Constants.DEFAULT_ENCODING);
					// likeDataString = value.getString(); // This line
					// can work too, replacing line below
				} else if (value.getType() == PropertyType.LONG) {
					return checkValueConstraints(constrains, value);
				} else {
					return false;
				}
				Double doubleValue = new Double(likeDoubleString);
				return doubleValue != null
						&& checkValueConstraints(constrains, value);
			} catch (Exception e) {
				// Hm, this is not double formated string
				return false;
			}
		} else if (requiredType == PropertyType.LONG) {
			String likeLongString = null;
			try {
				if (value.getType() == PropertyType.STRING) {
					likeLongString = value.getString();
				} else if (value.getType() == PropertyType.BINARY) {
					likeLongString = getCharsetString(value.getString(),
							Constants.DEFAULT_ENCODING);
					// likeDataString = value.getString(); // This line
					// can work too, replacing line below
				} else if (value.getType() == PropertyType.DATE) {
					return true;
				} else if (value.getType() == PropertyType.DOUBLE) {
					return true;
				} else {
					return false;
				}
				Long longValue = new Long(likeLongString);
				return longValue != null
						&& checkValueConstraints(constrains, value);
			} catch (Exception e) {
				// Hm, this is not long formated string
				return false;
			}
		} else if (requiredType == PropertyType.NAME) {
			String likeNameString = null;
			try {
				if (value.getType() == PropertyType.STRING) {
					likeNameString = value.getString();
				} else if (value.getType() == PropertyType.BINARY) {
					likeNameString = getCharsetString(value.getString(),
							Constants.DEFAULT_ENCODING);
					// likeDataString = value.getString(); // This line
					// can work too, replacing line below
				} else if (value.getType() == PropertyType.PATH) {
					String pathString = value.getString();
					// PathValue pathv = (PathValue) value;
					String[] pathParts = pathString.split("\\/");
					if (pathString.startsWith("/")
							&& (pathParts.length > 1 || pathString.indexOf("[") > 0)) {
						// Path is not relative - absolute
						// FALSE if it is more than one element long
						// or has an index
						return false;
					} else if (!pathParts.equals("/") && pathParts.length == 1
							&& pathString.indexOf("[") < 0) {
						// Path is relative
						// TRUE if it is one element long
						// and has no index
						return checkValueConstraints(constrains, value);
					} else if (pathString.startsWith("/")
							&& pathString.lastIndexOf("/") < 1
							&& pathString.indexOf("[") < 0) {
						// System.err.println("abs path, one elem, no index: " +
						// pathString);
						return checkValueConstraints(constrains, value);
					} else {
						return false;
					}
				} else {
					return false;
				}
				try {
					//Value nameValue = getNameValue(likeNameString);
          Value nameValue = manager.getValueFactory().
            createValue(likeNameString, PropertyType.NAME);
					return nameValue != null
							&& checkValueConstraints(constrains, value);
				} catch (Exception e) {
					return false;
				}
			} catch (Exception e) {
				return false;
			}
		} else if (requiredType == PropertyType.STRING) {
			String likeStringString = null;
			try {
				if (value.getType() == PropertyType.BINARY) {
					likeStringString = getCharsetString(value.getString(),
							Constants.DEFAULT_ENCODING);
				} else if (value.getType() == PropertyType.DATE
						|| value.getType() == PropertyType.LONG
						|| value.getType() == PropertyType.BOOLEAN
						|| value.getType() == PropertyType.NAME
						|| value.getType() == PropertyType.PATH
						|| value.getType() == PropertyType.DOUBLE) {
					likeStringString = value.getString();
				} else {
					return false;
				}
				return likeStringString != null
						&& checkValueConstraints(constrains, value);
			} catch (Exception e) {
				return false;
			}
		} else if (requiredType == PropertyType.UNDEFINED) {
			return checkValueConstraints(constrains, value);
		} else {
			return false;
		}
	}

	private String getCharsetString(String source, String charSetName) {
		try {
			CharBuffer cb = CharBuffer.wrap(source.toCharArray());
			Charset cs = Charset.forName(charSetName);
			CharsetEncoder cse = cs.newEncoder();
			ByteBuffer encoded = cse.encode(cb);
			return new String(encoded.array()).trim(); // Trim is very
			// important!!!
		} catch (IllegalStateException e) {
			return null;
		} catch (MalformedInputException e) {
			return null;
		} catch (UnmappableCharacterException e) {
			return null;
		} catch (CharacterCodingException e) {
			return null;
		}
	}

	private boolean isCharsetString(String source, String charSetName) {
		try {
			String s = getCharsetString(source, charSetName);
			return s != null;
		} catch (Exception e) {
			return false;
		}
	}

	/** @see javax.jcr.nodetype.NodeType#canSetProperty */
	public boolean canSetProperty(String propertyName, Value[] values) {
		PropertyDefinition def = getPropertyDefinitions(propertyName)
				.getDefinition(true);
		if (def != null) {
			if (def.isProtected()) {
				return false;
			} else {
				if (values != null) {
					int requiredType = def.getRequiredType();
					if (values.length > 0) {
						int res = 0;
						for (int i = 0; i < values.length; i++) {
							try {
								if (canSetPropertyForType(requiredType,
										propertyName, values[i], def
												.getValueConstraints())) {
									res++;
								}
							} catch (Exception e) {
							}
						}
						return res == values.length;
					} else {
						return true;
					}
				} else {
					return canRemoveItem(propertyName);
				}
			}
		} else {
			return false;
		}
	}

	/** @see javax.jcr.nodetype.NodeType#canAddChildNode */
	public boolean canAddChildNode(String childNodeName) {
		NodeDefinition childNodeDef = getChildNodeDefinition(childNodeName);
		return !(childNodeDef == null || childNodeDef.isProtected() || childNodeDef
				.getDefaultPrimaryType() == null);
	}

	/** @see javax.jcr.nodetype.NodeType#canAddChildNode */
	public boolean canAddChildNode(String childNodeName, String nodeTypeName) {
    NodeDefinition childNodeDef = getChildNodeDefinition(childNodeName);
		return !(childNodeDef == null || childNodeDef.isProtected())
				&& isChildNodePrimaryTypeAllowed(nodeTypeName);
	}

	/** @see javax.jcr.nodetype.NodeType#canRemoveItem */
	public boolean canRemoveItem(String itemName) {
		PropertyDefinition propDef = getPropertyDefinitions(itemName)
				.getAnyDefinition();
		if (propDef != null)
			return !(propDef.isMandatory() || propDef.isProtected());

		NodeDefinition nodeDef = getChildNodeDefinition(itemName);
		if (nodeDef != null)
			return !(nodeDef.isMandatory() || nodeDef.isProtected());

		return false;
	}

	public PropertyDefinitions getPropertyDefinitions(String name) {
		PropertyDefinitions defs = new PropertyDefinitions();

		//PropertyDefinitionImpl propResidual = null;
		for (int i = 0; i < getPropertyDefinitions().length; i++) {
			PropertyDefinitionImpl propDef = (PropertyDefinitionImpl) getPropertyDefinitions()[i];
			//System.out.println(" >>>>>>> "+propDef.getName()+" "+propDef.isMultiple()+" "+name);

			if (propDef.getName().equals(name) || propDef.isResidualSet()) {
				defs.setDefinition(propDef);
			}
			/*
			 * if (propDef.getName().equals(name)) {
			 * defs.setDefinition(propDef); return defs; } else if
			 * (propDef.isResidualSet()) { propResidual = propDef; }
			 */
		}
		// defs.setDefinition(propResidual);
		return defs;
	}

	public NodeDefinition getChildNodeDefinition(String name) {
		NodeDefinition residual = null;
		for (int i = 0; i < getChildNodeDefinitions().length; i++) {
			NodeDefinition nodeDef = getChildNodeDefinitions()[i];
			if (nodeDef.getName().equals(name)) {
				return nodeDef;
			} else if (nodeDef.getName().equals(ExtendedItemDefinition.RESIDUAL_SET)) {
				residual = nodeDef;
			}
		}
		return residual;
	}

	public boolean equals(Object obj) {
		if ((obj instanceof ExtendedNodeType))
      return qName.equals(((ExtendedNodeType) obj).getQName());
    else
 			return false;
	}

	// //////////////////////////////////
  
	protected boolean checkValueConstraints(String[] constraints, Value value) {

		if (constraints != null && constraints.length > 0) {
			for (int i = 0; i < constraints.length; i++) {
				try {
					if (constraints[i].equals(value.getString())) {
						return true;
					}
				} catch (RepositoryException e) {
					System.err.println("Error! Can't get value's string value: " + e);
				}
			}
		} else {
			return true;
		}
		return false;
	}

	protected void fillSupertypes(List<NodeType> list, NodeType subtype) {
		if (subtype.getDeclaredSupertypes() != null) {
			for (int i = 0; i < subtype.getDeclaredSupertypes().length; i++) {
				list.add(subtype.getDeclaredSupertypes()[i]);
				fillSupertypes(list, subtype.getDeclaredSupertypes()[i]);
			}
		}
	}

	public static boolean isSameOrSubType(NodeType superType, NodeType subType) {
		if (superType.equals(subType))
			return true;
		else {
			NodeType[] superTypes = subType.getSupertypes();
			for (int j = 0; j < superTypes.length; j++) {
				NodeType testSuperType = superTypes[j];
				if (testSuperType.equals(superType))
					return true;
			}
		}
		return false;
	}

	public ArrayList<ItemDefinition> getManadatoryItemDefs() {

		ArrayList<ItemDefinition> itemDefs = new ArrayList<ItemDefinition>();
		for (int i = 0; i < getPropertyDefinitions().length; i++) {
			if (getPropertyDefinitions()[i].isMandatory())
				itemDefs.add(getPropertyDefinitions()[i]);
		}
		for (int i = 0; i < getChildNodeDefinitions().length; i++) {
			if (getChildNodeDefinitions()[i].isMandatory())
				itemDefs.add(getChildNodeDefinitions()[i]);
		}

		return itemDefs;

	}

  public boolean isChildNodePrimaryTypeAllowed(String typeName) {
    
    // [PN] 03.08.06 Fix chil nodes defs selection logic
    //NodeDefinition[] definitions = this.getDeclaredChildNodeDefinitions()
    NodeDefinition[] definitions = this.getChildNodeDefinitions();
    
    NodeType testType;
    try {
      testType = manager.getNodeType(typeName);
      //testType = (NodeTypeImpl)((NodeTypeManagerImpl)manager).
      //findNodeType(getQName(typeName));
    } catch (RepositoryException e) {
      throw new RuntimeException("Error " + e);
    }
    
    for (int i = 0; i < definitions.length; i++) {
      NodeType[] requiredTypes = definitions[i].getRequiredPrimaryTypes();
      for (int j = 0; j < requiredTypes.length; j++) {
        //System.out.println("Required : "+requiredTypes[j].getName()+" test "+testType.getName());
        if(((NodeType)requiredTypes[j]).equals(testType))
          return true;
        NodeType[] testSuperTypes = testType.getSupertypes();
        for(int k=0; k<testSuperTypes.length; k++) {
          //System.out.println("Required : "+requiredTypes[j].getName()+" test super "+testSuperTypes[k].getName());
          if(((NodeType)testSuperTypes[k]).equals((NodeType)requiredTypes[j]))
            return true;
        }
      }
    }
    return false;
  }
  


	/**
	 * @param mixin
	 *            The mixin to set.
	 */
	public void setMixin(boolean mixin) {
		this.mixin = mixin;
	}

	/**
	 * @param name
	 *            The name to set.
	 */
	public void setName(String name) throws RepositoryException {
		//this.name = name;
    this.qName = ((NameValue) manager.getValueFactory().createValue(name, PropertyType.NAME)).getQName();
	}

	/**
	 * @param orderableChild
	 *            The orderableChild to set.
	 */
	public void setOrderableChild(boolean orderableChild) {
		this.orderableChild = orderableChild;
	}

	/**
	 * @param primaryItemName
	 *            The primaryItemName to set.
	 */
	public void setPrimaryItemName(String primaryItemName) {
		this.primaryItemName = primaryItemName;
	}

	/**
	 * @param declaredNodeDefs
	 *            The declaredNodeDefs to set.
	 */
	public void setDeclaredNodeDefs(NodeDefinition[] declaredNodeDefs) {
		this.declaredChildNodeDefinitions = declaredNodeDefs;
	}

	/**
	 * @param declaredPropertyDefs
	 *            The declaredPropertyDefs to set.
	 */
	public void setDeclaredPropertyDefs(
			PropertyDefinition[] declaredPropertyDefs) {
		this.declaredPropertyDefinitions = declaredPropertyDefs;
	}

	/**
	 * @param declaredSupertypes
	 *            The declaredSupertypes to set.
	 */
	public void setDeclaredSupertypes(NodeType[] declaredSupertypes) {
		this.declaredSupertypes = declaredSupertypes;
	}
 
  public InternalQName getQName() {
    return qName;
  }
  
  ////////////////// NEW METHODS (since 1.2) //////////
  
  /* (non-Javadoc)
   * @see org.exoplatform.services.jcr.core.nodetype.ExtendedNodeType#getPropertyDefinitions(org.exoplatform.services.jcr.datamodel.InternalQName)
   */
  public PropertyDefinitions getPropertyDefinitions(InternalQName name) {
    PropertyDefinitions defs = new PropertyDefinitions();

    // PropertyDefinitionImpl propResidual = null;
    for (int i = 0; i < getPropertyDefinitions().length; i++) {
      PropertyDefinitionImpl propDef = (PropertyDefinitionImpl) getPropertyDefinitions()[i];
       //System.out.println(" ----------- "+propDef.getName()+" "+propDef.isMultiple()+" "+name);

      if (propDef.getQName().equals(name) || propDef.isResidualSet()) {
        defs.setDefinition(propDef);
      }
    }
    return defs;
  }

  /* (non-Javadoc)
   * @see org.exoplatform.services.jcr.core.nodetype.ExtendedNodeType#getChildNodeDefinition(org.exoplatform.services.jcr.datamodel.InternalQName)
   */
  public NodeDefinition getChildNodeDefinition(InternalQName name) {
    NodeDefinition residual = null;
    for (int i = 0; i < getChildNodeDefinitions().length; i++) {
      NodeDefinitionImpl nodeDef = (NodeDefinitionImpl) getChildNodeDefinitions()[i];
      //System.out.println(">>>> findChildNodeDefinition >> "+nodeDef.getQName().getAsString()+" "+name);

      if (nodeDef.getQName().equals(name)) {
        return nodeDef;
      } else if (nodeDef.getName().equals(ExtendedItemDefinition.RESIDUAL_SET)) {
        residual = nodeDef;
      }
    }
    return residual;
  }
}
