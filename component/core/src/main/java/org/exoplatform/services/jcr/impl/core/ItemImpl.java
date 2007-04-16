/**
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 */

package org.exoplatform.services.jcr.impl.core;

import java.util.ArrayList;
import java.util.List;

import javax.jcr.AccessDeniedException;
import javax.jcr.InvalidItemStateException;
import javax.jcr.Item;
import javax.jcr.ItemNotFoundException;
import javax.jcr.Node;
import javax.jcr.PathNotFoundException;
import javax.jcr.PropertyType;
import javax.jcr.ReferentialIntegrityException;
import javax.jcr.RepositoryException;
import javax.jcr.Value;
import javax.jcr.ValueFormatException;
import javax.jcr.lock.LockException;
import javax.jcr.nodetype.ConstraintViolationException;
import javax.jcr.nodetype.ItemDefinition;
import javax.jcr.nodetype.PropertyDefinition;
import javax.jcr.version.VersionException;

import org.apache.commons.logging.Log;
import org.exoplatform.services.jcr.access.AccessControlEntry;
import org.exoplatform.services.jcr.access.AccessControlList;
import org.exoplatform.services.jcr.access.AccessManager;
import org.exoplatform.services.jcr.access.PermissionType;
import org.exoplatform.services.jcr.core.ExtendedPropertyType;
import org.exoplatform.services.jcr.core.nodetype.PropertyDefinitions;
import org.exoplatform.services.jcr.core.value.ExtendedValue;
import org.exoplatform.services.jcr.dataflow.ItemState;
import org.exoplatform.services.jcr.datamodel.InternalQName;
import org.exoplatform.services.jcr.datamodel.QPath;
import org.exoplatform.services.jcr.datamodel.ItemData;
import org.exoplatform.services.jcr.datamodel.NodeData;
import org.exoplatform.services.jcr.datamodel.PropertyData;
import org.exoplatform.services.jcr.datamodel.QPathEntry;
import org.exoplatform.services.jcr.datamodel.Uuid;
import org.exoplatform.services.jcr.datamodel.ValueData;
import org.exoplatform.services.jcr.impl.Constants;
import org.exoplatform.services.jcr.impl.core.nodetype.NodeTypeManagerImpl;
import org.exoplatform.services.jcr.impl.core.value.BaseValue;
import org.exoplatform.services.jcr.impl.core.value.PathValue;
import org.exoplatform.services.jcr.impl.core.value.PermissionValue;
import org.exoplatform.services.jcr.impl.core.value.ValueConstraintsMatcher;
import org.exoplatform.services.jcr.impl.core.value.ValueFactoryImpl;
import org.exoplatform.services.jcr.impl.dataflow.TransientPropertyData;
import org.exoplatform.services.jcr.impl.dataflow.TransientValueData;
import org.exoplatform.services.jcr.util.UUIDGenerator;
import org.exoplatform.services.log.ExoLogger;

/**
 * Created by The eXo Platform SARL .
 * 
 * @author Gennady Azarenkov
 * @version $Id: ItemImpl.java 13679 2007-03-22 16:44:26Z vetal_ok $
 */

public abstract class ItemImpl implements Item {

  private static Log log = ExoLogger.getLogger("jcr.ItemImpl");

  protected final SessionImpl session;

  protected ItemData data;

  protected JCRPath location;

  protected SessionDataManager dataManager;

  protected LocationFactory locationFactory;

  protected ValueFactoryImpl valueFactory;

  protected int itemHashCode = 0; // [PN]
                                                                                  // 19.04.06

  ItemImpl(ItemData data, SessionImpl session) throws RepositoryException {

    this.session = session;
    this.data = data;
    this.location = session.getLocationFactory().createJCRPath(data.getQPath());

    this.dataManager = session.getTransientNodesManager();
    this.locationFactory = session.getLocationFactory();
    this.valueFactory = session.getValueFactory();

    itemHashCode = (session.getWorkspace().getName() + data.getUUID()).hashCode();
  }

  protected void invalidate() {
    this.data = null;
  }
  
  /**
   * Return a status of the item state. If the state is invalid the item can't be used anymore.
   * @return boolean flag, true if an item is usable in the session.
   */
  public boolean isValid() {
    return data != null;
  }
  
  /**
   * Checking if this item has valid item state, i.e. wasn't removed (and saved).
   *  
   * @return true or throws an InvalidItemStateException exception otherwise
   * @throws InvalidItemStateException
   */
  protected boolean checkValid() throws InvalidItemStateException {
    if (data == null) 
      throw new InvalidItemStateException("Invalid item state. Item was removed.");
      session.updateLastAccessTime();
    return true;
  } 

  public String getPath() {
    return getLocation().getAsString(false);
  }

  public String getName() {
    return getLocation().getName().getAsString();
  }

//  public Item getAncestor(int degree) throws ItemNotFoundException, AccessDeniedException, RepositoryException {
//    try {
//      // 6.2.8 If depth > n is specified then an ItemNotFoundException is
//      // thrown.
//      if (degree < 0)
//        throw new ItemNotFoundException("Workspace.getAncestor() ancestor's degree < 0.");
//      int n = getDepth() - degree;
//      // log.debug("Item.getAncestor(" + degree + ") depth " +
//      // getDepth()+" "+n);
//      if (n == 0)
//        return this;
//      else if (n < 0)
//        throw new ItemNotFoundException(
//            "Workspace.getAncestor() ancestor's degree > depth of this item.");
//      else {
//        JCRPath ancestorLoc = getLocation().makeAncestorPath(n);
//        // return findItemByPath(ancestorLoc);
//        return item(ancestorLoc);
//      }
//    } catch (PathNotFoundException e) {
//      throw new ItemNotFoundException(e.getMessage(), e);
//    }
//  }

  /*
   * (non-Javadoc)
   * 
   * @see javax.jcr.Item#getAncestor(int)
   */  
  public Item getAncestor(int degree) throws ItemNotFoundException, AccessDeniedException, RepositoryException {
    try {
      // 6.2.8 If depth > n is specified then an ItemNotFoundException is
      // thrown.
      if (degree < 0)
        throw new ItemNotFoundException("Can't get ancestor with ancestor's degree < 0.");
      
      final QPath myPath = getData().getQPath();
      int n = myPath.getDepth() - degree;
      // log.debug("Item.getAncestor(" + degree + ") depth " + getDepth()+" "+n);
      if (n == 0) {
        return this;
      } else if (n < 0) {
        throw new ItemNotFoundException("Can't get ancestor with ancestor's degree > depth of this item.");
      } else {
        //JCRPath ancestorLoc = getLocation().makeAncestorPath(n);
        // return findItemByPath(ancestorLoc);
        
        final QPath ancestorPath = myPath.makeAncestorPath(n);
        NodeData rootData = (NodeData) dataManager.getItemData(Constants.ROOT_UUID);
        return dataManager.getItem(rootData,ancestorPath,true);
      }
    } catch (PathNotFoundException e) {
      throw new ItemNotFoundException(e.getMessage(), e);
    }
  }  

  /*
   * (non-Javadoc)
   * 
   * @see javax.jcr.Item#getParent()
   */
  public Node getParent() throws ItemNotFoundException, AccessDeniedException, RepositoryException {
    
    checkValid();
    
    if (isRoot())
      throw new ItemNotFoundException("Root node does not have a parent");

    return parent();
  }

  /*
   * (non-Javadoc)
   * 
   * @see javax.jcr.Item#getSession()
   */
  public SessionImpl getSession() {
    return session;
  }

  /*
   * (non-Javadoc)
   * 
   * @see javax.jcr.Item#getDepth()
   */
  public int getDepth() {
    return getLocation().getDepth();
  }

  /*
   * (non-Javadoc)
   * 
   * @see javax.jcr.Item#isSame(javax.jcr.Item)
   */
  public boolean isSame(Item otherItem) {
    
    if (isValid()) {
      if (otherItem == null)
        return false;
  
      if (!this.getClass().equals(otherItem.getClass())) 
        return false;
  
      try {
        
        // UUID is not changed on ItemImpl
        return getInternalUUID().equals(((ItemImpl) otherItem).getInternalUUID());
      } catch (Exception e) {
        log.debug("Item.isSame() failed " + e);
        return false;
      }
    }

    return false;    
  }

  /*
   * (non-Javadoc)
   * 
   * @see javax.jcr.Item#isNew()
   */
  public boolean isNew() {
    if (isValid())
      return dataManager.isNew(getInternalUUID());
    
    // if was removed (is invalid by check), isn't new
    return false;
  }

  /*
   * (non-Javadoc)
   * 
   * @see javax.jcr.Item#isModified()
   */
  public boolean isModified() {
    if (isValid())
      //return dataManager.isModified(getInternalUUID());
      return dataManager.isModified(getData());

    // if was removed (is invalid by check), was modified
    return true;
  }

  /*
   * (non-Javadoc)
   * 
   * @see javax.jcr.Item#remove()
   */
  public void remove() throws RepositoryException, ConstraintViolationException, VersionException,LockException {

    checkValid();
    
    if (isRoot())
      throw new RepositoryException("Can't remove ROOT node.");

    // Check constraints
    ItemDefinition def;
    if (isNode())
      def = ((NodeImpl) this).getDefinition();
    else
      def = ((PropertyImpl) this).getDefinition();

    if (def.isMandatory() || def.isProtected())
      throw new ConstraintViolationException("Can't remove mandatory or protected item "
          + getPath());
    NodeImpl parentNode = parent();
    // Check if versionable ancestor is not checked-in
    if (!parentNode.isCheckedOut())
      throw new VersionException("Node " + parent().getPath()
          + " or its nearest ancestor is checked-in");

    // Check locking
    if (!parentNode.checkLocking())
      throw new LockException("Node " + parent().getPath() + " is locked ");

    
    //launch event
    session.getActionHandler().preRemoveItem(parentNode,this);
    // remove from datamanager
    dataManager.delete(data);

    
  }
      
  
  protected PropertyImpl doUpdateProperty(NodeImpl parentNode, InternalQName propertyName,
      Value propertyValue, boolean multiValue, int expectedType) throws ValueFormatException,
      VersionException, LockException, ConstraintViolationException, RepositoryException {
    
    Value[] val = null;
    if (propertyValue != null) {
      val = new Value[]{propertyValue};
    }
    return doUpdateProperty(parentNode, propertyName, val, multiValue, expectedType);
  }
  
  protected PropertyImpl doUpdateProperty(NodeImpl parentNode, InternalQName propertyName,
      Value[] propertyValues, boolean multiValue, int expectedType) throws ValueFormatException,
      VersionException, LockException, ConstraintViolationException, RepositoryException {
 
    QPath qpath = QPath.makeChildPath(parentNode.getInternalPath(), propertyName);
    int state;

    String uuid;
    int version;
    PropertyImpl oldProp = null;
    ItemImpl oldItem = dataManager.getItem(parentNode.nodeData(), new QPathEntry(propertyName,0), true);
    PropertyDefinition def = null;
    
    NodeTypeManagerImpl ntm = session.getWorkspace().getNodeTypeManager();
    NodeData parentData = (NodeData) parentNode.getData();
    if (oldItem == null || oldItem.isNode()) { // new prop
      uuid = UUIDGenerator.generate();
      version = -1;
      if (propertyValues == null){
        //new property null values; 
          TransientPropertyData nullData = new TransientPropertyData(qpath, uuid, version, PropertyType.UNDEFINED,
              parentNode.getInternalUUID(), multiValue);
          PropertyImpl nullProperty = new PropertyImpl(nullData,session);
          nullProperty.invalidate();
          return nullProperty; 
      }
      def = ntm.findPropertyDefinitions(propertyName, parentData.getPrimaryTypeName(),
          parentData.getMixinTypeNames()).getDefinition(multiValue);
      
      // TODO [PN] DEBUG
//      def = ntm.findPropertyDefinition(propertyName, parentData.getPrimaryTypeName(),null
//        /*parentData.getMixinTypeNames()*/);

      state = ItemState.ADDED;
    } else {
      oldProp = (PropertyImpl) oldItem;
      def = ntm.findPropertyDefinitions(propertyName, parentData.getPrimaryTypeName(),
          parentData.getMixinTypeNames()).getDefinition(oldProp.isMultiValued());
      
      // TODO [PN] DEBUG      
//      def = ntm.findPropertyDefinition(propertyName, parentData.getPrimaryTypeName(),
//          /*parentData.getMixinTypeNames()*/null);

      uuid = oldProp.getInternalUUID();
      version = oldProp.getData().getPersistedVersion();
      if (propertyValues == null)
        state = ItemState.DELETED;
      else {
        state = ItemState.UPDATED;
      }
    }
    if (def != null && def.isProtected())
      throw new ConstraintViolationException("Can not set protected property " + getPath());

    
    if (multiValue && (def == null || (oldProp!= null && !oldProp.isMultiValued()))){
      throw new ValueFormatException(
          "Can not assign multiple-values Value to a single-valued property " + getPath());
    }
    
    if(!multiValue && (def == null || (oldProp!= null && oldProp.isMultiValued()))){
      throw new ValueFormatException(
          "Can not assign single-value Value to a multiple-valued property " + getPath());
    }
    
 

    if (!parentNode.isCheckedOut())
      throw new VersionException("Node " + parentNode.getPath()
          + " or its nearest ancestor is checked-in");

    // Check locking
    if (!parentNode.checkLocking())
      throw new LockException("Node " + parentNode.getPath() + " is locked ");


    


    List<ValueData> valueDataList = new ArrayList<ValueData>();

    // cast to required type if neccessary
    int requiredType = def.getRequiredType();

    int propType = requiredType;
    // if list of values not null
    if (propertyValues != null) {
      // All Value objects in the array must be of the same type, otherwise a
      // ValueFormatException is thrown.
      if (propertyValues.length > 1) {
        if (propertyValues[0] != null) {
          int vType = propertyValues[0].getType();
          for (Value val : propertyValues) {
            if (val != null && vType != val.getType()) {
              throw new ValueFormatException(
                  "All Value objects in the array must be of the same type");
            }
          }
        }
      }

      // if value count >0 and original type not UNDEFINED
      if (propertyValues.length > 0 && requiredType == PropertyType.UNDEFINED) {
        // if type what we expected to be UNDEFINED
        // set destination type = type of values else type expectedType
        if (expectedType == PropertyType.UNDEFINED) {
          for (Value val : propertyValues) {
            if (val != null) {
              expectedType = val.getType();
              break;
            }
          }
        }
        propType = expectedType;
      }
      // fill datas and also remove null values and reorder values
      for (Value value : propertyValues) {
        if (value != null) {
          valueDataList.add(valueData(value, propType));
        } else {
          if (log.isDebugEnabled())
            log.debug("Set null value (" + getPath() + ", multivalued: " + multiValue + ")");
        }
      }
    }

    // Check value constraints
    checkValueConstraints(def, valueDataList, propType);

    TransientPropertyData newData = new TransientPropertyData(qpath, uuid, version, propType,
        parentNode.getInternalUUID(), multiValue);

    if (requiredType != PropertyType.UNDEFINED && expectedType != PropertyType.UNDEFINED
        && requiredType != expectedType) {
      throw new ConstraintViolationException(" the type parameter "
          + ExtendedPropertyType.nameFromValue(expectedType) + " and the "
          + "type of the property do not match required type"
          + ExtendedPropertyType.nameFromValue(requiredType));
    }

    PropertyImpl prop = null; 
    if (state != ItemState.DELETED) {
      newData.setValues(valueDataList);
      ItemState itemState = new ItemState(newData, state, true, qpath,false);
      prop = (PropertyImpl) dataManager.update(itemState, true);
      // launch event
      session.getActionHandler().postSetProperty(parentNode, prop, state);

    } else {
      if (def.isMandatory()) {
        throw new ConstraintViolationException(
            "Can not remove (by setting null value) mandatory property " + getPath());
      } 
      //launch event
      session.getActionHandler().preRemoveItem(parentNode,oldProp);
      dataManager.delete(newData);
      prop =  oldProp;
    }
    
    
    return prop;

  }

  /*
   * (non-Javadoc)
   * 
   * @see javax.jcr.Item#save()
   */
  public void save() throws ReferentialIntegrityException, AccessDeniedException, LockException,
      ConstraintViolationException, InvalidItemStateException, ReferentialIntegrityException,
      RepositoryException {

    checkValid();
    
    if (isNew())
      throw new RepositoryException("It is impossible to call save() on the newly added item "
          + getPath());

    NodeTypeManagerImpl ntManager = session.getWorkspace().getNodeTypeManager();

    if (isNode()) {
      // validate referential integrity
      QPath path = getInternalPath();
      List<ItemState> changes = dataManager.getChangesLog().getDescendantsChanges(path);
      
      // referenceable nodes - if a node is deleted and then added, 
      // referential integrity is unchanged ('move' usecase) 
      List<NodeData> refNodes = new ArrayList<NodeData>();
      
      for (ItemState changedItem : changes) {
        if (changedItem.isNode()) {
          NodeData refNode = (NodeData) changedItem.getData();
          
          // Check referential integrity (remove of mix:referenceable node)
          if (ntManager.isNodeType(Constants.MIX_REFERENCEABLE, 
                  refNode.getPrimaryTypeName(), 
                  refNode.getMixinTypeNames())) {
  
            // mix:referenceable
            if (changedItem.isDeleted())
              refNodes.add(refNode); // add to refs (delete - alway is first)
            else if (changedItem.isAdded())
              refNodes.remove(refNode); // remove from refs (add - always at the end)
          }
        }
      }
      
      // check ref changes
      for (NodeData refNode : refNodes) {
        List<PropertyData> nodeRefs = dataManager.getReferencesData(refNode.getUUID());
        for (PropertyData refProp : nodeRefs) {
          // if ref property is deleted in this session
          ItemState refState = dataManager.getChangesLog().getItemState(refProp.getUUID());
          if (refState != null && refState.isDeleted())
            continue;
          
          NodeData refParent = (NodeData) dataManager.getItemData(refProp.getParentUUID());
          AccessControlList acl = refParent.getACL();
          AccessManager am = session.getAccessManager();

          if (!am.hasPermission(acl, PermissionType.READ, session.getUserID())) {
            throw new AccessDeniedException("Can not delete node " + refNode.getQPath() + " ("
                + refNode.getUUID() + ")"
                + ". It is currently the target of a REFERENCE property and "
                + path.getAsString());
          }
          throw new ReferentialIntegrityException("Can not delete node " + refNode.getQPath() + " ("
              + refNode.getUUID() + ")" + ". It is currently the target of a REFERENCE property "
              + path.getAsString());
        }
      }
    }

    dataManager.commit(getInternalPath());
  }

  /*
   * (non-Javadoc)
   * 
   * @see javax.jcr.Item#refresh(boolean)
   */
  public void refresh(boolean keepChanges) throws InvalidItemStateException, RepositoryException {
    
    checkValid(); 
    
    if (keepChanges)
      dataManager.refresh(this.getData());
    else
      dataManager.rollback(this.getData());
  }

  // -------------------- Implementation ----------------------

  public ItemData getData() {
    return data;
  }

  public String getParentUUID() {
    return getData().getParentUUID();
  }

  public QPath getInternalPath() {
    return getData().getQPath();
  }

  public InternalQName getInternalName() {
    return getData().getQPath().getName();
  }
//  @Deprecated
//  protected ItemImpl item(JCRPath path) throws RepositoryException {
//    return dataManager.getItem(path.getInternalPath(), true);
//  }
//  @Deprecated
//  protected ItemImpl item(QPath path) throws RepositoryException {
//    return dataManager.getItem(path, true);
//  }

  protected ItemImpl item(String uuid) throws RepositoryException {
    return dataManager.getItemByUUID(uuid, true);
  }

  protected NodeImpl parent() throws RepositoryException {
    NodeImpl parent = (NodeImpl) item(getParentUUID());
    if (parent == null)
      throw new ItemNotFoundException("FATAL: Parent is null for "
          + getInternalPath().getAsString() + " parent UUID: " + getParentUUID());
    return parent;
  }

  public String getInternalUUID() {
    return data.getUUID();
  }

  public JCRPath getLocation() {
    return this.location;
  }

  public boolean isRoot() {
    return (getDepth() == 0);
  }

  abstract void loadData(ItemData data) throws RepositoryException;

  public boolean hasPermission(String action) throws RepositoryException {
    NodeData nData;
    if (isNode()) {

      nData = (NodeData) getData();
    } else {
      nData = (NodeData) dataManager.getItemData(data.getParentUUID());

      if (nData == null) {
        throw new RepositoryException("FATAL: parent not found for " + this.getPath());
      }
    }
    return session.getAccessManager().hasPermission(nData.getACL(), action, session.getUserID());
  }

  /**
   * @author [PN] 18.04.06
   * @see java.lang.Object#equals(java.lang.Object)
   */
  @Override
  public boolean equals(Object obj) {
    if (obj instanceof ItemImpl) {
      ItemImpl otherItem = (ItemImpl) obj;
      
      if (!otherItem.isValid() || !this.isValid())
        return false;
      
      try {
        return getInternalUUID().equals(otherItem.getInternalUUID());
      } catch (Exception e) {
        return false;
      }
    }
    return false;
  }

  /**
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {
    return itemHashCode;
  }

  private ValueData valueData(Value value, int type) throws RepositoryException,
      ValueFormatException {

    if (value == null)
      return null;

    switch (type) {
    case PropertyType.STRING:
      return new TransientValueData(value.getString());
    case PropertyType.BINARY:
      TransientValueData vd = null ;
      if (value instanceof BaseValue) {
        vd  = ((BaseValue)value).getInternalData().createTransientCopy() ;
      }else if (value instanceof ExtendedValue){
        vd  = ((BaseValue)getSession().getValueFactory().createValue(value.getStream())).getInternalData();
      } else {
        vd  = ((BaseValue)getSession().getValueFactory().createValue(value.getString(),PropertyType.BINARY)).getInternalData();
      }
      return vd;
    case PropertyType.BOOLEAN:
      return new TransientValueData(value.getBoolean());
    case PropertyType.LONG:
      return new TransientValueData(value.getLong());
    case PropertyType.DOUBLE:
      return new TransientValueData(value.getDouble());
    case PropertyType.DATE:
      return new TransientValueData(value.getDate());
    case PropertyType.PATH:
      TransientValueData tvd = null;
      if (value instanceof PathValue) {
        tvd  = ((PathValue)value).getInternalData().createTransientCopy() ;
      }else{
        QPath pathValue = locationFactory.parseJCRPath(value.getString()).getInternalPath();
        tvd = new TransientValueData(pathValue);
      }
      return tvd;
    case PropertyType.NAME:
      InternalQName nameValue = locationFactory.parseJCRName(value.getString()).getInternalName();
      return new TransientValueData(nameValue);
    case PropertyType.REFERENCE:
      Uuid uuid = new Uuid(value.getString());
      return new TransientValueData(uuid);
    case ExtendedPropertyType.PERMISSION:
      PermissionValue permValue = (PermissionValue) value;
      AccessControlEntry ace = new AccessControlEntry(permValue.getIdentity(), permValue
          .getPermission());
      return new TransientValueData(ace);
    default:
      throw new ValueFormatException("ValueFactory.convert() unknown or unconvertable type " + type);
    }
  }

  private void checkValueConstraints(PropertyDefinition def, List<ValueData> newValues, int type)
      throws ConstraintViolationException, RepositoryException {

    // [PN] 20.09.06
    ValueConstraintsMatcher constraints = new ValueConstraintsMatcher(def.getValueConstraints(), session);

    for (ValueData value : newValues) {
      if (!constraints.match(value, type)) {
        String strVal = null;
        try {
          strVal = ((TransientValueData) value).getString();
        } catch (Throwable e) {
          log.error("Error of value read: " + e.getMessage(), e);
        }
        throw new ConstraintViolationException("Can not set value '" + strVal + "' to " + getPath()
            + " due to value constraints ");
      }
    }
  }
}
