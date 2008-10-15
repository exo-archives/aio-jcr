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
package org.exoplatform.services.jcr.impl.storage.sdb;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.jcr.InvalidItemStateException;
import javax.jcr.ItemExistsException;
import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;

import org.apache.commons.logging.Log;
import org.apache.ws.commons.util.Base64;
import org.apache.ws.commons.util.Base64.DecodingException;
import org.exoplatform.services.jcr.access.AccessControlEntry;
import org.exoplatform.services.jcr.access.AccessControlList;
import org.exoplatform.services.jcr.dataflow.ItemState;
import org.exoplatform.services.jcr.dataflow.persistent.PersistedNodeData;
import org.exoplatform.services.jcr.dataflow.persistent.PersistedPropertyData;
import org.exoplatform.services.jcr.datamodel.IllegalACLException;
import org.exoplatform.services.jcr.datamodel.IllegalNameException;
import org.exoplatform.services.jcr.datamodel.InternalQName;
import org.exoplatform.services.jcr.datamodel.ItemData;
import org.exoplatform.services.jcr.datamodel.NodeData;
import org.exoplatform.services.jcr.datamodel.PropertyData;
import org.exoplatform.services.jcr.datamodel.QPath;
import org.exoplatform.services.jcr.datamodel.QPathEntry;
import org.exoplatform.services.jcr.datamodel.ValueData;
import org.exoplatform.services.jcr.impl.Constants;
import org.exoplatform.services.jcr.impl.dataflow.persistent.ByteArrayPersistedValueData;
import org.exoplatform.services.jcr.impl.storage.JCRInvalidItemStateException;
import org.exoplatform.services.jcr.storage.WorkspaceStorageConnection;
import org.exoplatform.services.jcr.storage.value.ValueIOChannel;
import org.exoplatform.services.jcr.storage.value.ValueStoragePluginProvider;
import org.exoplatform.services.log.ExoLogger;

import com.amazonaws.sdb.AmazonSimpleDB;
import com.amazonaws.sdb.AmazonSimpleDBClient;
import com.amazonaws.sdb.AmazonSimpleDBConfig;
import com.amazonaws.sdb.AmazonSimpleDBException;
import com.amazonaws.sdb.model.Attribute;
import com.amazonaws.sdb.model.CreateDomainRequest;
import com.amazonaws.sdb.model.CreateDomainResponse;
import com.amazonaws.sdb.model.DeleteAttributesRequest;
import com.amazonaws.sdb.model.DeleteAttributesResponse;
import com.amazonaws.sdb.model.DeleteDomainRequest;
import com.amazonaws.sdb.model.DeleteDomainResponse;
import com.amazonaws.sdb.model.GetAttributesRequest;
import com.amazonaws.sdb.model.GetAttributesResponse;
import com.amazonaws.sdb.model.GetAttributesResult;
import com.amazonaws.sdb.model.Item;
import com.amazonaws.sdb.model.ListDomainsRequest;
import com.amazonaws.sdb.model.ListDomainsResponse;
import com.amazonaws.sdb.model.ListDomainsResult;
import com.amazonaws.sdb.model.PutAttributesRequest;
import com.amazonaws.sdb.model.PutAttributesResponse;
import com.amazonaws.sdb.model.QueryWithAttributesRequest;
import com.amazonaws.sdb.model.QueryWithAttributesResponse;
import com.amazonaws.sdb.model.QueryWithAttributesResult;
import com.amazonaws.sdb.model.ReplaceableAttribute;

/**
 * Created by The eXo Platform SAS.
 * 
 * <br/>Date: 30.09.2008
 * 
 * @author <a href="mailto:peter.nedonosko@exoplatform.com.ua">Peter Nedonosko</a>
 * @version $Id$
 */
public class SDBWorkspaceStorageConnection implements WorkspaceStorageConnection, SDBConstants {

  /**
   * Connection logger.
   */
  protected static final Log                 LOG                       = ExoLogger.getLogger("jcr.SDBWorkspaceStorageConnection");

  /**
   * SimpleDB Operation timeout (5sec).
   */
  protected static final int                 SDB_OPERATION_TIMEOUT     = 5000;

  /**
   * Item Delete operation constant. Should be INTERNED.
   */
  protected static final String              ITEM_DELETE               = "delete".intern();

  /**
   * Item Update operation constant. Should be INTERNED.
   */
  protected static final String              ITEM_UPDATE               = "update".intern();

  /**
   * Get Item by ID query.
   */
  protected static final String              QUERY_GET_ITEM_BY_ID      = "['" + ID + "' = '%s']";

  /**
   * Get Item by parent ID and name query.
   */
  protected static final String              QUERY_GET_ITEM_BY_NAME    = "['"
                                                                           + PID
                                                                           + "' = '%s'] intersection ['"
                                                                           + NAME + "' = '%s']";

  /**
   * Get Node child Nodes by parent ID query.
   */
  protected static final String              QUERY_GET_CHILDNODES      = "['"
                                                                           + PID
                                                                           + "' = '%s'] intersection ['"
                                                                           + ICLASS + "' = '"
                                                                           + NODE_ICLASS + "']";

  /**
   * Get Node Properties by parent ID query.
   */
  protected static final String              QUERY_GET_CHILDPROPERTIES = "['"
                                                                           + PID
                                                                           + "' = '%s'] intersection ['"
                                                                           + ICLASS + "' = '"
                                                                           + PROPERTY_ICLASS + "']";

  /**
   * Get REFERENCE Properties by Node ID query.
   */
  protected static final String              QUERY_GET_REFERENCES      = "['" + ICLASS + "' = '"
                                                                           + PROPERTY_ICLASS
                                                                           + "'] intersection ['"
                                                                           // + PTYPE + "' = '"
                                                                           // + PROPERTY_ICLASS
                                                                           // +
                                                                           // "']  intersection ['"
                                                                           + DATA + "' = '%s']";

  /**
   * SimpleDB service.
   */
  protected final AmazonSimpleDB             sdbService;

  /**
   * SimpleDB domain name.
   */
  protected final String                     domainName;

  /**
   * Changes applied before the last commit (to be used for rollback on fail).
   */
  protected final List<WriteOperation>       changes;

  /**
   * Changed nodes Identifiers. Uses for 'parent not found' validation.
   */
  protected final Set<String>                addedNodes;

  /**
   * Maximim buffer size (see configuration).
   */
  protected final int                        maxBufferSize;

  /**
   * External Value Storages provider.
   */
  protected final ValueStoragePluginProvider valueStorageProvider;

  /**
   * Node IData descriptor.
   */
  public class NodeIData {

    /**
     * Node persistent version.
     */
    private int                       version;

    /**
     * Node orderNumber.
     */
    private int                       orderNumber;

    /**
     * Node primaryType.
     */
    private InternalQName             primaryType;

    /**
     * Node mixinTypes. Empty if no mixins.
     */
    private final List<InternalQName> mixinTypes = new ArrayList<InternalQName>();

    /**
     * Node ACL.
     */
    private AccessControlList         acl;

    /**
     * @return the version
     */
    int getVersion() {
      return version;
    }

    /**
     * @return the orderNumber
     */
    int getOrderNumber() {
      return orderNumber;
    }

    /**
     * @return the primaryType
     */
    InternalQName getPrimaryType() {
      return primaryType;
    }

    /**
     * @return the mixinTypes
     */
    List<InternalQName> getMixinTypes() {
      return mixinTypes;
    }

    /**
     * addMixinType.
     * 
     * @param mixin
     *          - new mixin type
     */
    void addMixinType(InternalQName mixin) {
      mixinTypes.add(mixin);
    }

    /**
     * @return the acl
     */
    AccessControlList getACL() {
      return acl;
    }

    /**
     * @param version
     *          the version to set
     */
    void setVersion(int version) {
      this.version = version;
    }

    /**
     * @param orderNumber
     *          the orderNumber to set
     */
    void setOrderNumber(int orderNumber) {
      this.orderNumber = orderNumber;
    }

    /**
     * @param primaryType
     *          the primaryType to set
     */
    void setPrimaryType(InternalQName primaryType) {
      this.primaryType = primaryType;
    }

    /**
     * @param acl
     *          the acl to set
     */
    void setAcl(AccessControlList acl) {
      this.acl = acl;
    }
  }

  /**
   * Property IData descriptor.
   */
  public class PropertyIData {

    /**
     * Property persistent version.
     */
    private int     version;

    /**
     * Property type.
     */
    private int     ptype;

    /**
     * Property Value multivalued status.
     */
    private boolean multivalued;

    /**
     * Property Value storageKey (if in External Value Storage).
     */
    private String  storageKey;

    /**
     * @return the version
     */
    int getVersion() {
      return version;
    }

    /**
     * @return the ptype
     */
    int getType() {
      return ptype;
    }

    /**
     * @return the multivalued
     */
    boolean isMultivalued() {
      return multivalued;
    }

    /**
     * @param version
     *          the version to set
     */
    void setVersion(int version) {
      this.version = version;
    }

    /**
     * @param ptype
     *          the ptype to set
     */
    void setPtype(int ptype) {
      this.ptype = ptype;
    }

    /**
     * @param multivalued
     *          the multivalued to set
     */
    void setMultivalued(boolean multivalued) {
      this.multivalued = multivalued;
    }

    /**
     * @return the storageKey
     */
    String getStorageKey() {
      return storageKey;
    }

    /**
     * @param storageKey
     *          the storageKey to set
     */
    void setStorageKey(String storageKey) {
      this.storageKey = storageKey;
    }
  }

  /**
   * Base operation for SimpleDB storage modification.
   */
  abstract class WriteOperation {

    /**
     * Processed flag.
     */
    private boolean processed = false;

    /**
     * Rollback write operation.
     * 
     * @return SimpleDB responce for the operation or error
     * @throws RepositoryException
     *           in case of SimpleDB service error
     */
    abstract Object rollback() throws RepositoryException;

    /**
     * 
     * Execute write operation.
     * 
     * @return SimpleDB responce for the operation or error
     * @throws RepositoryException
     *           in case of Repository error
     */
    abstract Object execute() throws RepositoryException;

    /**
     * Get operation Item path.
     * 
     * @return QPath, the Item path
     */
    abstract QPath getPath();

    /**
     * Mark the operation processed.
     * 
     */
    void markProcessed() {
      this.processed = true;
    }

    /**
     * Return processed state.
     * 
     * @return boolean, state
     */
    boolean isProcessed() {
      return this.processed;
    }
  }

  /**
   * Add Node operation (PutAttribute).
   */
  class AddNodeOperation extends WriteOperation {

    /**
     * JCR Node.
     */
    final NodeData node;

    /**
     * AddNodeOperation constructor.
     * 
     * @param node
     *          - Node to be added
     */
    AddNodeOperation(NodeData node) {
      this.node = node;
    }

    /**
     * {@inheritDoc}
     */
    QPath getPath() {
      return node.getQPath();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    Object execute() throws RepositoryException {
      // validate
      validateItemAdd(node);

      final List<ReplaceableAttribute> list = new ArrayList<ReplaceableAttribute>();

      list.add(new ReplaceableAttribute(ID, node.getIdentifier(), false));
      list.add(new ReplaceableAttribute(PID, node.getParentIdentifier(), false));
      list.add(new ReplaceableAttribute(NAME,
                                        node.getQPath().getEntries()[node.getQPath().getEntries().length - 1].getAsString(true),
                                        false));
      list.add(new ReplaceableAttribute(ICLASS, NODE_ICLASS, false));
      // list.add(new ReplaceableAttribute(VERSION, String.valueOf(node.getPersistedVersion()),
      // false));
      // list.add(new ReplaceableAttribute(ORDERNUM, String.valueOf(node.getOrderNumber()), false));
      list.add(new ReplaceableAttribute(IDATA, formatIData(node), false));

      try {
        PutAttributesResponse resp = createReplaceItem(sdbService,
                                                       domainName,
                                                       node.getIdentifier(),
                                                       list);
        addedNodes.add(node.getIdentifier());
        return resp;
      } catch (AmazonSimpleDBException e) {
        throw new SDBStorageException("(add) Node " + node.getQPath().getAsString() + " "
            + node.getIdentifier() + " add fails " + e, e);
      }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    Object rollback() throws RepositoryException {
      try {
        return deleteItem(sdbService, domainName, node.getIdentifier());
      } catch (AmazonSimpleDBException e) {
        throw new SDBStorageException("(add) Node " + node.getQPath().getAsString() + " "
            + node.getIdentifier() + " rollback fails " + e, e);
      }
    }

  }

  /**
   * Add Property operation (PutAttribute).
   */
  class AddPropertyOperation extends WriteOperation {

    /**
     * JCR Property.
     */
    final PropertyData property;

    /**
     * AddPropertyOperation constructor.
     * 
     * @param property
     *          - Property to be added
     */
    AddPropertyOperation(PropertyData property) {
      this.property = property;
    }

    /**
     * {@inheritDoc}
     */
    QPath getPath() {
      return property.getQPath();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    Object execute() throws RepositoryException {

      // validate
      validateItemAdd(property);

      // process Values firts,
      // if some Values matches VS filters they will be stored there.
      try {
        String[] values = addValues(property);

        final List<ReplaceableAttribute> list = new ArrayList<ReplaceableAttribute>();

        list.add(new ReplaceableAttribute(ID, property.getIdentifier(), false));
        list.add(new ReplaceableAttribute(PID, property.getParentIdentifier(), false));
        list.add(new ReplaceableAttribute(NAME,
                                          property.getQPath().getEntries()[property.getQPath()
                                                                                   .getEntries().length - 1].getAsString(true),
                                          false));
        list.add(new ReplaceableAttribute(ICLASS, PROPERTY_ICLASS, false));
        // list.add(new ReplaceableAttribute(VERSION,
        // String.valueOf(property.getPersistedVersion()),
        // false));
        // list.add(new ReplaceableAttribute(PTYPE, String.valueOf(property.getType()), false));
        // list.add(new ReplaceableAttribute(MULTIVALUED,
        // String.valueOf(property.isMultiValued()),
        // false));
        list.add(new ReplaceableAttribute(IDATA, formatIData(property), false));

        // add Values to SimpleDB
        // TODO think about this too
        // Attributes are uniquely identified in an item by their name/value combination.
        // For example, a single item can have the attributes { "first_name", "first_value" } and {
        // "first_name", second_value" }.
        // However, it cannot have two attribute instances where both the Attribute.X.Name and
        // Attribute.X.Value are the same.
        for (String value : values)
          list.add(new ReplaceableAttribute(DATA, value, false));

        try {
          return createReplaceItem(sdbService, domainName, property.getIdentifier(), list);
        } catch (AmazonSimpleDBException e) {
          throw new SDBStorageException("(add) Property " + property.getQPath().getAsString() + " "
              + property.getIdentifier() + " add fails " + e, e);
        }
      } catch (IOException e) {
        throw new SDBRepositoryException("(add) Property " + property.getQPath().getAsString()
            + " " + property.getIdentifier() + " add fails with I/O error " + e, e);
      }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    Object rollback() throws RepositoryException {
      try {
        return deleteItem(sdbService, domainName, property.getIdentifier());
      } catch (AmazonSimpleDBException e) {
        throw new SDBStorageException("(add) Property " + property.getQPath().getAsString() + " "
            + property.getIdentifier() + " rollback fails " + e, e);
      }
    }
  }

  /**
   * Update Node operation (PutAttribute).
   */
  class UpdateNodeOperation extends WriteOperation {

    /**
     * JCR Node.
     */
    final NodeData node;

    /**
     * UpdateNodeOperation constructor.
     * 
     * @param node
     *          - Node to be updated
     */
    UpdateNodeOperation(NodeData node) {
      this.node = node;
    }

    /**
     * {@inheritDoc}
     */
    QPath getPath() {
      return node.getQPath();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    Object execute() throws RepositoryException {

      // validate
      validateItemChange(node, ITEM_UPDATE);

      final List<ReplaceableAttribute> list = new ArrayList<ReplaceableAttribute>();

      // list.add(new ReplaceableAttribute(ID, node.getIdentifier(), true));
      list.add(new ReplaceableAttribute(PID, node.getParentIdentifier(), true));
      list.add(new ReplaceableAttribute(NAME,
                                        node.getQPath().getEntries()[node.getQPath().getEntries().length - 1].getAsString(true),
                                        true));
      // list.add(new ReplaceableAttribute(ICLASS, NODE_ICLASS, true));

      // list.add(new ReplaceableAttribute(VERSION, String.valueOf(node.getPersistedVersion()),
      // true));
      // list.add(new ReplaceableAttribute(ORDERNUM, String.valueOf(node.getOrderNumber()), true));
      list.add(new ReplaceableAttribute(IDATA, formatIData(node), true));

      try {
        return createReplaceItem(sdbService, domainName, node.getIdentifier(), list);
      } catch (AmazonSimpleDBException e) {
        throw new SDBStorageException("(update) Node " + node.getQPath().getAsString() + " "
            + node.getIdentifier() + " update fails " + e, e);
      }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    Object rollback() {
      // delete
      // and back previous
      // TODO ..but now rollback isn't provided

      LOG.warn("UPDATE rollback isn't provided");

      return null;
    }
  }

  /**
   * Update Property operation (PutAttribute).
   */
  class UpdatePropertyOperation extends WriteOperation {

    /**
     * JCR Property.
     */
    final PropertyData property;

    /**
     * UpdatePropertyOperation constructor.
     * 
     * @param property
     *          - Property to be updated
     */
    UpdatePropertyOperation(PropertyData property) {
      this.property = property;
    }

    /**
     * {@inheritDoc}
     */
    QPath getPath() {
      return property.getQPath();
    }

    /**
     * {@inheritDoc}
     * 
     * @throws IOException
     */
    @Override
    Object execute() throws RepositoryException {

      // validate
      validateItemChange(property, ITEM_UPDATE);

      // process Values firts,
      // if some Values matches VS filters they will be stored there.
      try {
        String[] values = addValues(property);

        final List<ReplaceableAttribute> list = new ArrayList<ReplaceableAttribute>();

        // list.add(new ReplaceableAttribute(ID, property.getIdentifier(), true));
        list.add(new ReplaceableAttribute(PID, property.getParentIdentifier(), true));
        list.add(new ReplaceableAttribute(NAME,
                                          property.getQPath().getEntries()[property.getQPath()
                                                                                   .getEntries().length - 1].getAsString(true),
                                          true));
        // list.add(new ReplaceableAttribute(ICLASS, NODE_ICLASS, true));

        // list.add(new ReplaceableAttribute(VERSION,
        // String.valueOf(property.getPersistedVersion()),
        // true));
        // list.add(new ReplaceableAttribute(PTYPE, String.valueOf(property.getType()), true));
        // list.add(new ReplaceableAttribute(MULTIVALUED, String.valueOf(property.isMultiValued()),
        // true));
        list.add(new ReplaceableAttribute(IDATA, formatIData(property), true));

        // add Values to SimpleDB
        // TODO think about this too
        // Attributes are uniquely identified in an item by their name/value combination.
        // For example, a single item can have the attributes { "first_name", "first_value" } and {
        // "first_name", second_value" }.
        // However, it cannot have two attribute instances where both the Attribute.X.Name and
        // Attribute.X.Value are the same.
        for (String value : values)
          list.add(new ReplaceableAttribute(DATA, value, true));

        try {
          return createReplaceItem(sdbService, domainName, property.getIdentifier(), list);
        } catch (AmazonSimpleDBException e) {
          throw new SDBStorageException("(update) Property " + property.getQPath().getAsString()
              + " " + property.getIdentifier() + " update fails " + e, e);
        }
      } catch (IOException e) {
        throw new SDBRepositoryException("(update) Property " + property.getQPath().getAsString()
            + " " + property.getIdentifier() + " add fails with I/O error " + e, e);
      }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    Object rollback() {
      // delete
      // and back previous
      // TODO ..but now rollback isn't provided

      LOG.warn("UPDATE rollback isn't provided");

      return null;
    }
  }

  /**
   * Delete Node operation (DeleteAttributes).
   */
  class DeleteNodeOperation extends WriteOperation {

    /**
     * JCR Node.
     */
    final NodeData node;

    /**
     * DeleteNodeOperation constructor.
     * 
     * @param node
     *          - Node to be deleted
     */
    DeleteNodeOperation(NodeData node) {
      this.node = node;
    }

    /**
     * {@inheritDoc}
     */
    QPath getPath() {
      return node.getQPath();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    Object execute() throws RepositoryException {

      // validate
      validateItemChange(node, ITEM_DELETE);

      // update existsing Item ID to deleted 'D'
      final List<ReplaceableAttribute> list = new ArrayList<ReplaceableAttribute>();
      list.add(new ReplaceableAttribute(ID, ITEM_DELETED_ID, true));

      try {
        return createReplaceItem(sdbService, domainName, node.getIdentifier(), list);
      } catch (AmazonSimpleDBException e) {
        throw new SDBStorageException("(delete) Node " + node.getQPath().getAsString() + " "
            + node.getIdentifier() + " delete fails " + e, e);
      }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    Object rollback() throws RepositoryException {

      // back Item ID to the actual
      final List<ReplaceableAttribute> list = new ArrayList<ReplaceableAttribute>();
      list.add(new ReplaceableAttribute(ID, node.getIdentifier(), false));

      try {
        return createReplaceItem(sdbService, domainName, node.getIdentifier(), list);
      } catch (AmazonSimpleDBException e) {
        throw new SDBStorageException("(delete) Node " + node.getQPath().getAsString() + " "
            + node.getIdentifier() + " delete rollback fails " + e, e);
      }
    }

  }

  /**
   * Delete Property operation (DeleteAttributes).
   */
  class DeletePropertyOperation extends WriteOperation {

    /**
     * JCR Property.
     */
    final PropertyData property;

    /**
     * DeletePropertyOperation constructor.
     * 
     * @param property
     *          - Property to be deleted
     */
    DeletePropertyOperation(PropertyData property) {
      this.property = property;
    }

    /**
     * {@inheritDoc}
     */
    QPath getPath() {
      return property.getQPath();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    Object execute() throws RepositoryException {

      // validate
      validateItemChange(property, ITEM_DELETE);

      // update existsing Item ID to deleted 'D'
      final List<ReplaceableAttribute> list = new ArrayList<ReplaceableAttribute>();
      list.add(new ReplaceableAttribute(ID, ITEM_DELETED_ID, true));

      try {
        return createReplaceItem(sdbService, domainName, property.getIdentifier(), list);
      } catch (AmazonSimpleDBException e) {
        throw new SDBStorageException("(delete) Property " + property.getQPath().getAsString()
            + " " + property.getIdentifier() + " delete fails " + e, e);
      }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    Object rollback() throws RepositoryException {

      // back Item ID to the actual
      final List<ReplaceableAttribute> list = new ArrayList<ReplaceableAttribute>();
      list.add(new ReplaceableAttribute(ID, property.getIdentifier(), false));

      try {
        return createReplaceItem(sdbService, domainName, property.getIdentifier(), list);
      } catch (AmazonSimpleDBException e) {
        throw new SDBStorageException("(delete) Property " + property.getQPath().getAsString()
            + " " + property.getIdentifier() + " delete rollback fails " + e, e);
      }
    }
  }

  /**
   * NodeData comparator.
   * 
   */
  class NodeDataComparator implements Comparator<NodeData> {

    /**
     * {@inheritDoc}
     */
    public int compare(NodeData o1, NodeData o2) {
      return o1.getOrderNumber() - o2.getOrderNumber();
    }
  }

  /**
   * NOT USED.<br/> PropertyData comparator. Order allways will be following
   * 
   * <pre>
   * 1. jcr:primaryType
   * 2. jcr:mixinTypes
   * 3. jcr:uuid
   * ...
   * N. other properties unsorted
   * </pre>
   */
  class PropertyDataComparator implements Comparator<PropertyData> {

    /**
     * {@inheritDoc}
     */
    public int compare(PropertyData o1, PropertyData o2) {
      if (Constants.JCR_PRIMARYTYPE.equals(o1.getQPath().getName()))
        return Integer.MIN_VALUE;
      else if (Constants.JCR_MIXINTYPES.equals(o1.getQPath().getName()))
        return Integer.MIN_VALUE + 1;
      else if (Constants.JCR_UUID.equals(o1.getQPath().getName()))
        return Integer.MIN_VALUE + 2;
      else
        return 0;
    }
  }

  /**
   * ValueData comparator.
   * 
   */
  class ValueDataComparator implements Comparator<ValueData> {

    /**
     * {@inheritDoc}
     */
    public int compare(ValueData o1, ValueData o2) {
      return o1.getOrderNumber() - o2.getOrderNumber();
    }
  }

  /**
   * SDBWorkspaceStorageConnection constructor.
   * 
   * @param accessKey
   *          - Amazon access key
   * @param secretKey
   *          - Amazon secret key
   * @param domainName
   *          - SimpleDb domain name
   * @param maxBufferSize
   *          - maximum size of Value stored in
   * @param valueStorageProvider
   *          - External Value Storages provider
   * @throws RepositoryException
   *           - if storage error occurs
   */
  public SDBWorkspaceStorageConnection(String accessKey,
                                       String secretKey,
                                       String domainName,
                                       int maxBufferSize,
                                       ValueStoragePluginProvider valueStorageProvider) throws RepositoryException {
    super();

    AmazonSimpleDBConfig config = new AmazonSimpleDBConfig();
    config.setSignatureVersion("0");

    this.sdbService = new AmazonSimpleDBClient(accessKey, secretKey, config);

    this.domainName = domainName;

    this.maxBufferSize = maxBufferSize;

    this.valueStorageProvider = valueStorageProvider;

    this.changes = new ArrayList<WriteOperation>();
    this.addedNodes = new HashSet<String>();
  }

  /**
   * Init SimpleDB storage. <br/> Check if domain exists. Will create one new if there is not. Write
   * version value in special row.
   * 
   * <br/> If current storage (domain) contains version row (already initialized) will check if
   * container name matches to the given.
   * 
   * <br/> If the given and stored container names are not same the WARNING will be printed.
   * 
   * @param containerName
   *          - Workspace container name
   * @param version
   *          - version
   * @throws RepositoryException
   *           - if storage error occurs.
   * @return String with storage version
   */
  String initStorage(String containerName, String version) throws RepositoryException {
    try {
      List<String> domains = getDomainsList();
      if (!domains.contains(domainName)) {
        // create
        createDomain(sdbService, domainName);

        // wait for SDB sync
        int iter = 5;
        boolean notInitilizer = true;
        do {
          try {
            Thread.sleep(SDB_OPERATION_TIMEOUT);
          } catch (InterruptedException e) {
            LOG.debug("Init storage sleep error " + e, e);
          }
          domains = getDomainsList();
          iter--;
        } while (notInitilizer = !domains.contains(domainName) && iter > 0);

        if (notInitilizer)
          LOG.warn("SimpleDB domain '" + domainName + "' created but still not available.");
      } else {
        // read version
        String userContainer = null;
        String userVersion = null;
        GetAttributesResponse resp = readItem(sdbService, domainName, STORAGE_VERSION_ID);
        if (resp.isSetGetAttributesResult()) {
          GetAttributesResult res = resp.getGetAttributesResult();
          List<Attribute> attributeList = res.getAttribute();
          for (Attribute attr : attributeList) {
            if (attr.getName().equals(STORAGE_VERSION)) {
              if (attr.isSetValue()) {
                userVersion = attr.getValue();
              } else
                throw new SDBRepositoryException("FATAL Storage Version Item attribute "
                    + STORAGE_VERSION + " doesn't contains information.");
            } else if (attr.getName().equals(STORAGE_CONTAINER_NAME)) {
              if (attr.isSetValue()) {
                userContainer = attr.getValue();
              } else
                throw new SDBRepositoryException("FATAL Storage Version Item attribute "
                    + STORAGE_CONTAINER_NAME + " doesn't contains information.");
            }
          }
        } else
          throw new SDBRepositoryException("FATAL Storage domain (" + domainName
              + ") exists but Version Item not found " + STORAGE_VERSION_ID + ".");

        // just return current version,
        // container will decide what to do.
        if (userContainer != null && userVersion != null) {
          if (!containerName.equals(userContainer)) {
            // warn, domain in use by anoother container
            LOG.warn("Storage in use by another Workspace container '" + userContainer
                + "'. User container name and current should be same. User storage version is "
                + userVersion + ".");
          }

          return userVersion;
        }
      }
    } catch (AmazonSimpleDBException e) {
      throw new SDBRepositoryException("Can not create SDB domain " + this.domainName, e);
    }

    // add version record

    final List<ReplaceableAttribute> list = new ArrayList<ReplaceableAttribute>();

    list.add(new ReplaceableAttribute(STORAGE_VERSION, version, false));
    list.add(new ReplaceableAttribute(STORAGE_CONTAINER_NAME, containerName, false));

    try {
      createReplaceItem(sdbService, domainName, STORAGE_VERSION_ID, list);
      return version;
    } catch (AmazonSimpleDBException e) {
      throw new SDBStorageException("(init) Storage initialization fails " + e, e);
    }

  }

  /**
   * Run cleanup procedure (used from container).
   * 
   */
  void runCleanup() {
    final List<String> names = new ArrayList<String>();

    try {
      String nextToken = null;

      do {
        QueryWithAttributesResponse resp = queryItemAttrByIDWithToken(sdbService,
                                                             domainName,
                                                             ITEM_DELETED_ID,
                                                             nextToken,
                                                             ICLASS);

        if (resp.isSetQueryWithAttributesResult()) {
          QueryWithAttributesResult res = resp.getQueryWithAttributesResult();
          nextToken = res.getNextToken();
          for (Item item : res.getItem()) {
            names.add(item.getName());
          }
        }
      } while (nextToken != null);
    } catch (AmazonSimpleDBException e) {
      LOG.error("(cleaner) Error of deleted Items request " + e, e);
    }

    for (String name : names) {
      try {
        deleteItem(sdbService, domainName, name);
      } catch (AmazonSimpleDBException e) {
        LOG.error("(cleaner) Item " + name + " delete error " + e, e);
      }
    }
  }

  /**
   * Return list of domain names. If no one domains found then an empty list will be returned.
   * 
   * @return list of names
   * @throws AmazonSimpleDBException
   *           in case of SDB error
   */
  protected List<String> getDomainsList() throws AmazonSimpleDBException {
    String nextToken = "";

    List<String> names = new ArrayList<String>();
    ListDomainsResult result = getDomains(sdbService, nextToken, 10);
    while (nextToken != null) {
      nextToken = null;

      if (result != null) {
        List<String> domainNamesList = result.getDomainName();
        names.addAll(domainNamesList);

        nextToken = result.getNextToken();
      }
    }

    return names;
  }

  /**
   * Return domain list request.
   * 
   * @param service
   *          SimpleDB service instance
   * @param nextToken
   *          SimpleDB token
   * @param maxDomains
   *          max domains
   * @return ListDomainsResult
   * @throws AmazonSimpleDBException
   *           in case of SDB error
   */
  protected ListDomainsResult getDomains(final AmazonSimpleDB service,
                                         final String nextToken,
                                         int maxDomains) throws AmazonSimpleDBException {
    ListDomainsRequest request = new ListDomainsRequest(maxDomains, nextToken);
    ListDomainsResponse response = service.listDomains(request);

    if (response.isSetListDomainsResult()) {
      return response.getListDomainsResult();
    } else
      return null;
  }

  /**
   * Execute create domain request.
   * 
   * @param service
   *          SimpleDB service
   * @param domainName
   *          targeted domain name
   * @return CreateDomainResponse
   * @throws AmazonSimpleDBException
   *           in case of SDB error
   */
  protected CreateDomainResponse createDomain(final AmazonSimpleDB service, final String domainName) throws AmazonSimpleDBException {
    CreateDomainRequest request = new CreateDomainRequest(domainName);
    return service.createDomain(request);
  }

  /**
   * Execute delete domain request.
   * 
   * @param service
   *          SimpleDB service
   * @param domainName
   *          targeted domain name
   * @return DeleteDomainResponse
   * @throws AmazonSimpleDBException
   *           in case of SDB error
   */
  protected DeleteDomainResponse deleteDomain(final AmazonSimpleDB service, final String domainName) throws AmazonSimpleDBException {
    DeleteDomainRequest request = new DeleteDomainRequest(domainName);

    return service.deleteDomain(request);
  }

  /**
   * Execute create item request. If item already exists it will be replaced.
   * 
   * @param service
   *          SimpleDB service
   * @param domainName
   *          targeted domain name
   * @param itemName
   *          SimpleDB item name
   * @param list
   *          ReplaceableAttribute list
   * @return PutAttributesResponse
   * @throws AmazonSimpleDBException
   *           in case of SDB error
   */
  protected PutAttributesResponse createReplaceItem(final AmazonSimpleDB service,
                                                    final String domainName,
                                                    final String itemName,
                                                    final List<ReplaceableAttribute> list) throws AmazonSimpleDBException {
    PutAttributesRequest request = new PutAttributesRequest().withDomainName(domainName)
                                                             .withItemName(itemName);
    request.setAttribute(list);
    return service.putAttributes(request);
  }

  /**
   * Execute read item request.
   * 
   * @param service
   *          SimpleDB service
   * @param domainName
   *          targeted domain name
   * @param itemName
   *          SimpleDB item name
   * @return GetAttributesResponse
   * @throws AmazonSimpleDBException
   *           in case of SDB error
   */
  protected GetAttributesResponse readItem(final AmazonSimpleDB service,
                                           final String domainName,
                                           final String itemName) throws AmazonSimpleDBException {
    GetAttributesRequest request = new GetAttributesRequest().withDomainName(domainName)
                                                             .withItemName(itemName);

    return service.getAttributes(request);
  }

  /**
   * Execute delete item request.
   * 
   * @param service
   *          SimpleDB service
   * @param domainName
   *          targeted domain name
   * @param itemName
   *          SimpleDB item name
   * @return GetAttributesResponse
   * @throws AmazonSimpleDBException
   *           in case of SDB error
   */
  protected DeleteAttributesResponse deleteItem(final AmazonSimpleDB service,
                                                final String domainName,
                                                final String itemName) throws AmazonSimpleDBException {
    DeleteAttributesRequest request = new DeleteAttributesRequest().withDomainName(domainName)
                                                                   .withItemName(itemName);
    return service.deleteAttributes(request);
  }

  /**
   * Query item attributes by ID (QueryWithAttributes).
   * 
   * @param service
   *          SimpleDB service
   * @param domainName
   *          targeted domain name
   * @param itemId
   *          JCR Item Id
   * @param nextToken
   *          SDB next token
   * @param attributes
   *          SimpleDB item attributes for responce. If <code>null</code> all attributes will be
   *          returned
   * @return QueryWithAttributesResponse
   * @throws AmazonSimpleDBException
   *           in case of SDB error
   */
  protected QueryWithAttributesResponse queryItemAttrByIDWithToken(final AmazonSimpleDB service,
                                                          final String domainName,
                                                          final String itemId,
                                                          final String nextToken,
                                                          final String... attributes) throws AmazonSimpleDBException {

    String query = String.format(QUERY_GET_ITEM_BY_ID, itemId);
    QueryWithAttributesRequest request = new QueryWithAttributesRequest().withDomainName(domainName)
                                                                         .withQueryExpression(query)
                                                                         .withNextToken(nextToken);

    if (attributes != null)
      request.withAttributeName(attributes);

    return service.queryWithAttributes(request);
  }

  /**
   * Query item attributes by ID (QueryWithAttributes).
   * 
   * @param service
   *          SimpleDB service
   * @param domainName
   *          targeted domain name
   * @param itemId
   *          JCR Item Id
   * @param attributes
   *          SimpleDB item attributes for responce. If <code>null</code> all attributes will be
   *          returned
   * @return QueryWithAttributesResponse
   * @throws AmazonSimpleDBException
   *           in case of SDB error
   */
  protected QueryWithAttributesResponse queryItemAttrByID(final AmazonSimpleDB service,
                                                          final String domainName,
                                                          final String itemId,
                                                          final String... attributes) throws AmazonSimpleDBException {

    String query = String.format(QUERY_GET_ITEM_BY_ID, itemId);
    QueryWithAttributesRequest request = new QueryWithAttributesRequest().withDomainName(domainName)
                                                                         .withQueryExpression(query);

    if (attributes != null)
      request.withAttributeName(attributes);

    return service.queryWithAttributes(request);
  }

  /**
   * Query item attributes by parent ID and name (QueryWithAttributes).
   * 
   * @param service
   *          SimpleDB service
   * @param domainName
   *          targeted domain name
   * @param parentId
   *          JCR Item parent Id
   * @param name
   *          JCR Item name
   * @param attributes
   *          SimpleDB item attributes for responce. If <code>null</code> all attributes will be
   *          returned
   * @return QueryWithAttributesResponse
   * @throws AmazonSimpleDBException
   *           in case of SDB error
   */
  protected QueryWithAttributesResponse queryItemAttrByName(final AmazonSimpleDB service,
                                                            final String domainName,
                                                            final String parentId,
                                                            final String name,
                                                            final String... attributes) throws AmazonSimpleDBException {

    String query = String.format(QUERY_GET_ITEM_BY_NAME, parentId, name);
    QueryWithAttributesRequest request = new QueryWithAttributesRequest().withDomainName(domainName)
                                                                         .withQueryExpression(query);

    if (attributes != null)
      request.withAttributeName(attributes);

    return service.queryWithAttributes(request);
  }

  /**
   * Query Node child Nodes by ID (QueryWithAttributes).
   * 
   * @param service
   *          SimpleDB service
   * @param domainName
   *          targeted domain name
   * @param itemId
   *          JCR Item Id (parent of child Nodes)
   * @param attributes
   *          SimpleDB item attributes for responce
   * @return QueryWithAttributesResponse
   * @throws AmazonSimpleDBException
   *           in case of SDB error
   */
  protected QueryWithAttributesResponse queryChildNodesAttr(final AmazonSimpleDB service,
                                                            final String domainName,
                                                            final String itemId,
                                                            final String... attributes) throws AmazonSimpleDBException {

    String query = String.format(QUERY_GET_CHILDNODES, itemId);
    QueryWithAttributesRequest request = new QueryWithAttributesRequest().withDomainName(domainName)
                                                                         .withQueryExpression(query)
                                                                         .withAttributeName(attributes);

    return service.queryWithAttributes(request);
  }

  /**
   * Query Node properties by ID (QueryWithAttributes).
   * 
   * @param service
   *          SimpleDB service
   * @param domainName
   *          targeted domain name
   * @param itemId
   *          JCR Item Id (parent of Properties)
   * @param attributes
   *          SimpleDB item attributes for responce
   * @return QueryWithAttributesResponse
   * @throws AmazonSimpleDBException
   *           in case of SDB error
   */
  protected QueryWithAttributesResponse queryChildPropertiesAttr(final AmazonSimpleDB service,
                                                                 final String domainName,
                                                                 final String itemId,
                                                                 final String... attributes) throws AmazonSimpleDBException {

    String query = String.format(QUERY_GET_CHILDPROPERTIES, itemId);
    QueryWithAttributesRequest request = new QueryWithAttributesRequest().withDomainName(domainName)
                                                                         .withQueryExpression(query)
                                                                         .withAttributeName(attributes);

    return service.queryWithAttributes(request);
  }

  /**
   * Query Node references properties by node ID (QueryWithAttributes).
   * 
   * <br/>NOTE: REFERENCE Properties SHOULD be stored in SimpleDB storage (not in External Values
   * Storage).
   * 
   * @param service
   *          SimpleDB service
   * @param domainName
   *          targeted domain name
   * @param nodeId
   *          JCR Node Id (target of REFERENCE Properties)
   * @param attributes
   *          SimpleDB item attributes for responce
   * @return QueryWithAttributesResponse
   * @throws AmazonSimpleDBException
   *           in case of SDB error
   */
  protected QueryWithAttributesResponse queryReferencesAttr(final AmazonSimpleDB service,
                                                            final String domainName,
                                                            final String nodeId,
                                                            final String... attributes) throws AmazonSimpleDBException {

    String query = String.format(QUERY_GET_REFERENCES, nodeId);
    QueryWithAttributesRequest request = new QueryWithAttributesRequest().withDomainName(domainName)
                                                                         .withQueryExpression(query)
                                                                         .withAttributeName(attributes);

    return service.queryWithAttributes(request);
  }

  /**
   * Format Node IData.
   * 
   * @param node
   *          - NodeData
   * @return String with IData
   */
  protected String formatIData(NodeData node) {

    StringBuilder idata = new StringBuilder();

    // idata.append(IDATA_VERSION);
    idata.append(node.getPersistedVersion());
    idata.append(IDATA_DELIMITER);

    // idata.append(IDATA_ORDERNUMBER);
    idata.append(node.getOrderNumber());
    idata.append(IDATA_DELIMITER);

    // idata.append(IDATA_PRIMARYTYPE);
    idata.append(node.getPrimaryTypeName().getAsString());

    boolean isPrivilegeable = false;
    boolean isOwneable = false;
    for (InternalQName mixin : node.getMixinTypeNames()) {
      idata.append(IDATA_DELIMITER);
      idata.append(IDATA_MIXINTYPE);
      idata.append(mixin.getAsString());

      if (Constants.EXO_PRIVILEGEABLE.equals(mixin))
        isPrivilegeable = true;
      else if (Constants.EXO_OWNEABLE.equals(mixin))
        isOwneable = true;
    }

    AccessControlList acl = node.getACL();
    if (acl != null) {
      final boolean root = Constants.ROOT_PATH.equals(node.getQPath()); // equals by hash code
      if (isPrivilegeable || root)
        for (AccessControlEntry ace : node.getACL().getPermissionEntries()) {
          idata.append(IDATA_DELIMITER);
          idata.append(IDATA_ACL_PERMISSION);
          idata.append(ace.getAsString());
        }

      if (isOwneable || root) {
        idata.append(IDATA_DELIMITER);
        idata.append(IDATA_ACL_OWNER);
        idata.append(node.getACL().getOwner());
      }
    }

    return idata.toString();
  }

  /**
   * Format Property IData.
   * 
   * @param property
   *          - PropertyData
   * @return String with IData
   */
  protected String formatIData(PropertyData property) {
    StringBuilder idata = new StringBuilder();

    // idata.append(IDATA_VERSION);
    idata.append(property.getPersistedVersion());
    idata.append(IDATA_DELIMITER);

    // idata.append(IDATA_PTYPE);
    idata.append(property.getType());
    idata.append(IDATA_DELIMITER);

    // idata.append(IDATA_MULTIVALUED);
    idata.append(property.isMultiValued());

    return idata.toString();
  }

  /**
   * Parse Node IData.
   * 
   * @param field
   *          IData content
   * @param parentACL
   *          - parent ACL
   * @return NodeIData
   * @throws IllegalNameException
   *           if QName stored in SimpleDB is wrong
   * @throws IllegalACLException
   *           - if ACL stored in SimpleDB is wrong
   * @throws SDBValueNumberFormatException
   *           - if numeric values stored in SimpleDB is wrong
   */
  protected NodeIData parseNodeIData(String field, AccessControlList parentACL) throws IllegalNameException,
                                                                               IllegalACLException,
                                                                               SDBValueNumberFormatException {

    String[] fs = field.split(IDATA_DELIMITER_REGEXP);

    NodeIData idata = new NodeIData();

    List<AccessControlEntry> aclPermissions = null;
    String aclOwner = null;

    for (int i = 0; i < fs.length; i++) {
      String s = fs[i];
      if (i == 0) {
        // version
        try {
          idata.setVersion(Integer.valueOf(s));
        } catch (final NumberFormatException e) {
          throw new SDBValueNumberFormatException("Node persisted version contains wrong value '"
              + s + "'. Error " + e, e);
        }
      } else if (i == 1) {
        // orderNumber
        try {
          idata.setOrderNumber(Integer.valueOf(s));
        } catch (final NumberFormatException e) {
          throw new SDBValueNumberFormatException("Node order number contains wrong value '" + s
              + "'. Error " + e, e);
        }
      } else if (i == 2) {
        // primaryType
        try {
          idata.setPrimaryType(InternalQName.parse(s));
        } catch (IllegalNameException e) {
          throw new IllegalNameException("Node jcr:primaryType contains wrong value '" + s
              + "'. Error " + e, e);
        }
      } else {
        // parse for mixins and ACL
        String value = s.substring(2);
        if (s.startsWith(IDATA_MIXINTYPE)) {
          // mixin
          try {
            idata.addMixinType(InternalQName.parse(value));
          } catch (IllegalNameException e) {
            throw new IllegalNameException("Node jcr:mixinTypes contains wrong value '" + value
                + "'. Error " + e, e);
          }
        } else if (s.startsWith(IDATA_ACL_PERMISSION)) {
          // ACL permission
          String[] aclp = value.split(AccessControlEntry.DELIMITER);

          if (aclp.length != 2)
            throw new IllegalACLException("Node ACL permission contains wrong value '" + value
                + "'. ACL string format is 'IDENTITY PERMISSION'");

          if (aclPermissions == null)
            aclPermissions = new ArrayList<AccessControlEntry>();

          aclPermissions.add(new AccessControlEntry(aclp[0], aclp[1]));
        } else if (s.startsWith(IDATA_ACL_OWNER)) {
          // ACL owner
          aclOwner = value;
        }
      }
    }

    // ACL
    if (aclOwner != null || aclPermissions != null) {
      AccessControlList acl;
      if (aclOwner != null && aclPermissions != null) {
        acl = new AccessControlList(aclOwner, aclPermissions);
      } else if (aclOwner != null && aclPermissions == null) {
        if (parentACL != null && parentACL.hasPermissions())
          // use permissions from existed parent
          acl = new AccessControlList(aclOwner, parentACL.getPermissionEntries());
        else
          // have to search nearest ancestor permissions in ACL manager
          acl = new AccessControlList(aclOwner, null);
      } else if (aclOwner == null && aclPermissions != null) {
        if (parentACL != null)
          // use permissions from existed parent
          acl = new AccessControlList(parentACL.getOwner(), aclPermissions);
        else
          // have to search nearest ancestor owner in ACL manager
          acl = new AccessControlList(null, aclPermissions);
      } else {
        if (parentACL != null)
          // construct ACL from existed parent ACL
          acl = new AccessControlList(parentACL.getOwner(), parentACL.hasPermissions()
              ? parentACL.getPermissionEntries()
              : null);
        else
          // have to search nearest ancestor ACL in ACL manager
          acl = null;
      }

      idata.setAcl(acl);
    }

    return idata;
  }

  /**
   * Parse Property IData.
   * 
   * @param field
   *          - IData content
   * 
   * @return PropertyIData instance
   */
  protected PropertyIData parsePropertyIData(String field) {

    String[] fs = field.split(IDATA_DELIMITER_REGEXP);

    PropertyIData idata = new PropertyIData();

    for (int i = 0; i < fs.length; i++) {
      String s = fs[i];
      if (i == 0) {
        // version
        try {
          idata.setVersion(Integer.valueOf(s));
        } catch (final NumberFormatException e) {
          throw new SDBValueNumberFormatException("Property persisted version contains wrong value '"
                                                      + s + "'. Error " + e,
                                                  e);
        }
      } else if (i == 1) {
        // property type
        try {
          idata.setPtype(Integer.valueOf(s));
        } catch (final NumberFormatException e) {
          throw new SDBValueNumberFormatException("Property type contains wrong value '" + s
              + "'. Error " + e, e);
        }
      } else if (i == 2) {
        // multivalued
        idata.setMultivalued(Boolean.valueOf(s));
      }
    }

    return idata;
  }

  /**
   * Property Values processing. Extract Value data into String representation. If Value is
   * multivalued return sequence of Strings.
   * 
   * <br/> Each Value will be stored as a String with fixed prefix of 4 chars XNNN. Where X - 'D'
   * for data or 'S' for external storage link; NNN - Value order number.
   * 
   * @param data
   *          - Value data
   * @return sequence of Strings
   * @throws IOException
   *           - if I/O error occurs
   * @throws SDBRepositoryException
   *           if Property has move of 100 Values
   */
  protected String[] addValues(final PropertyData data) throws IOException, SDBRepositoryException {

    List<ValueData> vdata = data.getValues();

    if (vdata.size() > SDB_ATTRIBUTE_PER_PUT)
      throw new SDBRepositoryException("Property " + data.getQPath().getAsString()
          + " can has only " + SDB_ATTRIBUTE_PER_PUT + " Values (SimpleDB request limit).");

    String[] vseq = new String[vdata.size()];
    for (int i = 0; i < vdata.size(); i++) {
      ValueData vd = vdata.get(i);
      vd.setOrderNumber(i); // TODO do we have to do it here?

      // prepare prefix with data location flag and order number (for multivalued)
      char[] vprefix;
      if (data.isMultiValued()) {
        vprefix = new char[VALUEPREFIX_MULTIVALUED_LENGTH];
        // fill last 3 chars with order number (with zero padding)
        char[] orderNum = String.valueOf(i).toCharArray();
        int oi = orderNum.length - 1;
        for (int ci = vprefix.length - 1; ci > 0; ci--)
          vprefix[ci] = oi >= 0 ? orderNum[oi--] : '0';
      } else
        vprefix = new char[VALUEPREFIX_SINGLEVALUED_LENGTH];

      ValueIOChannel channel = valueStorageProvider.getApplicableChannel(data, i);

      String v;
      if (channel == null) {
        // store in SDB
        vprefix[0] = VALUEPREFIX_DATA;
        if (data.getType() == PropertyType.BINARY) {
          byte[] ba = vd.getAsByteArray();
          // encode(byte[] pBuffer, int pOffset, int pLength, int pLineSize, java.lang.String
          // pSeparator)
          v = new String(vprefix)
              + Base64.encode(ba, 0, ba.length, SDB_ATTRIBUTE_VALUE_MAXLENGTH, "\n");
        } else
          v = new String(vprefix) + new String(vd.getAsByteArray(), Constants.DEFAULT_ENCODING);
        // TODO it's SDB stuff, so leave it as is (SDB will throws an error)
        // if (v.getBytes(Constants.DEFAULT_ENCODING).length > SDB_ATTRIBUTE_VALUE_MAXLENGTH) {
        // // error
        // throw new SDBItemValueLengthExceeded("Property '" + data.getQPath().getAsString()
        // + "' value size too large. Maximum Value size can be stored in SimpleDB is "
        // + SDB_ATTRIBUTE_VALUE_MAXLENGTH
        // + " bytes. Use Extenal Value Storage (to Amazon S3) for large Values. "
        // + "NOTE: Size for Binary data calculated on BASE64 encoded String of the data");
        // }
      } else {
        // store in External storage
        channel.write(data.getIdentifier(), vd);
        vprefix[0] = VALUEPREFIX_STORAGEID;
        v = new String(vprefix) + channel.getStorageId();
      }
      vseq[i] = v;
    }

    return vseq;
  }

  /**
   * Get named attribute from the list.
   * 
   * @param atts
   *          - List of attributes
   * @param attrName
   *          - attribute name
   * @return String with attribute value
   */
  protected String getAttribute(List<Attribute> atts, String attrName) {
    for (Attribute attr : atts) {
      if (attr.getName().equals(attrName))
        return attr.getValue();
    }

    return null;
  }

  /**
   * Get named attributes from the list.
   * 
   * @param atts
   *          - List of attributes
   * @param attrName
   *          - attribute name
   * @return Strings array with attribute values
   */
  protected String[] getAttributes(List<Attribute> atts, String attrName) {
    List<String> vals = new ArrayList<String>();
    for (Attribute attr : atts) {
      if (attr.getName().equals(attrName))
        vals.add(attr.getValue());
    }

    return vals.toArray(new String[vals.size()]);
  }

  /**
   * Validate Add operation for JCR Item.
   * 
   * Fails on following conditions:
   * <ul>
   * <li>1. if Item with given ID already exists</li>
   * <li>2. if Parent not found, by parent ID</li>
   * <li>3. if there are more of one Item with given Parent ID (SimpleDB specific check)</li>
   * </ul>
   * <br/>
   * 
   * @param data
   *          - ItemData
   * @throws ItemExistsException
   *           - if Item already exists
   * @throws SDBRepositoryException
   *           - if other storage error occurs
   */
  protected void validateItemAdd(ItemData data) throws ItemExistsException, SDBRepositoryException {

    final String itemClass = data.isNode() ? "Node" : "Property";
    try {
      // 1. check if Item doesn't exist
      QueryWithAttributesResponse resp = queryItemAttrByID(sdbService,
                                                           domainName,
                                                           data.getIdentifier(),
                                                           ID);
      if (resp.isSetQueryWithAttributesResult()) {
        QueryWithAttributesResult res = resp.getQueryWithAttributesResult();
        // SDB items
        List<Item> items = res.getItem();
        if (items.size() > 0) {
          // item already exists, get Node to throw an error
          // TODO much escriptive exception (location(name), is it Node or Property)
          throw new ItemExistsException("(add) " + itemClass + " already exists. ID: "
              + data.getIdentifier() + ". " + itemClass + " " + data.getQPath().getAsString());
        }
      }

      // 2. check if Parent exists (except of root)
      if (!Constants.ROOT_PARENT_UUID.equals(data.getParentIdentifier())) {
        // check in current transaction changes
        if (!addedNodes.contains(data.getParentIdentifier())) {
          // check in SDB storage
          resp = queryItemAttrByID(sdbService, domainName, data.getParentIdentifier(), ID);
          if (resp.isSetQueryWithAttributesResult()) {
            QueryWithAttributesResult res = resp.getQueryWithAttributesResult();
            // SDB items
            List<Item> items = res.getItem();
            if (items.size() > 1) {
              // TODO list duplicated Items in error message
              throw new SDBRepositoryException("(add) FATAL Storage contains more of one Item with ID: "
                  + data.getParentIdentifier()
                  + ". Check of "
                  + itemClass
                  + " "
                  + data.getQPath().getAsString() + " Item parent.");
            } else if (items.size() <= 0) {
              // Parent not found
              throw new SDBRepositoryException("(add) " + itemClass + " "
                  + data.getQPath().getAsString() + " parent not found. Parent ID: "
                  + data.getParentIdentifier());
            } // else - Parent exists
          }
        }
      }
    } catch (AmazonSimpleDBException e) {
      throw new SDBStorageException("(add) " + itemClass + " " + data.getIdentifier() + " ("
          + data.getQPath().getAsString() + ") read request fails " + e, e);
    }
  }

  /**
   * Validate Item modification (delete or update).
   * 
   * Fails on following conditions:
   * <ul>
   * <li>1. if Item doesn't exists</li>
   * <li>2. TODO (remove it) if deleting Node has child Nodes (for Delete only)</li>
   * <li>3. if there are more of one Item with given ID (SimpleDB specific check)</li>
   * </ul>
   * <br/>
   * 
   * @param data
   *          - ItemData
   * @param modification
   *          - String, Item modification name (delete or update)
   * @throws SDBRepositoryException
   *           - if storage error occurs
   * @throws InvalidItemStateException
   *           - if Item in invalid state
   * @see ITEM_DELETE, ITEM_UPDATE
   */
  protected void validateItemChange(ItemData data, String modification) throws SDBRepositoryException,
                                                                       InvalidItemStateException {

    final String itemClass = data.isNode() ? "Node" : "Property";
    try {
      QueryWithAttributesResponse resp = queryItemAttrByID(sdbService,
                                                           domainName,
                                                           data.getIdentifier(),
                                                           IDATA);

      if (resp.isSetQueryWithAttributesResult()) {
        QueryWithAttributesResult res = resp.getQueryWithAttributesResult();
        // SDB items
        List<Item> items = res.getItem();
        if (items.size() <= 0) {
          // item doesn't exist - error
          throw new JCRInvalidItemStateException("("
                                                     + modification
                                                     + ") "
                                                     + itemClass
                                                     + " "
                                                     + data.getQPath().getAsString()
                                                     + " "
                                                     + data.getIdentifier()
                                                     + " not found. Probably was deleted by another session ",
                                                 data.getIdentifier(),
                                                 modification == ITEM_DELETE
                                                     ? ItemState.DELETED
                                                     : ItemState.UPDATED);
        } else if (items.size() > 1) {
          // TODO list duplicated Items in error message
          throw new SDBRepositoryException("(" + modification
              + ") FATAL Storage contains more of one Item with ID: " + data.getIdentifier() + ". "
              + itemClass + " " + data.getQPath().getAsString());
        } else {
          // check if persisted version is ok
          // TODO don't use parseNodeIData() if we need only version
          Item sdbItem = items.get(0);
          String idv = getAttribute(sdbItem.getAttribute(), IDATA);
          if (idv != null) {
            try {
              int v;
              if (data.isNode()) {
                v = parseNodeIData(idv, null).getVersion();
              } else {
                v = parsePropertyIData(idv).getVersion();
              }
              if (v >= data.getPersistedVersion()) {
                // TODO are we need the check here?
                LOG.warn(">>>>> InvalidItemState. " + itemClass + " "
                    + data.getQPath().getAsString());
              }
            } catch (SDBValueNumberFormatException e) {
              throw new SDBRepositoryException("(" + modification + ") FATAL " + itemClass + " "
                  + data.getQPath().getAsString() + " " + data.getIdentifier() + " " + IDATA
                  + " attribute error. " + e, e);
            } catch (IllegalNameException e) {
              throw new SDBRepositoryException("(" + modification + ") FATAL " + itemClass + " "
                  + data.getQPath().getAsString() + " " + data.getIdentifier() + " " + IDATA
                  + " attribute error. " + e, e);
            } catch (IllegalACLException e) {
              throw new SDBRepositoryException("(" + modification + ") FATAL " + itemClass + " "
                  + data.getQPath().getAsString() + " " + data.getIdentifier() + " " + IDATA
                  + " attribute error. " + e, e);
            }
          } else {
            // error state
            throw new SDBRepositoryException("(" + modification + ") FATAL " + itemClass + " "
                + data.getQPath().getAsString() + " " + data.getIdentifier() + " hasn't " + IDATA
                + " attribute.");
          }
        }
      }

      // TODO (don't check, we just believe the contract is done) if delete operation
      // if (ITEM_DELETE == modification) {
      // // check if the Node hasn't child Nodes
      // resp = queryChildNodesAttr(sdbService, domainName, data.getIdentifier(), ID);
      // if (resp.isSetQueryWithAttributesResult()) {
      // QueryWithAttributesResult res = resp.getQueryWithAttributesResult();
      // if (res.getItem().size() > 0)
      // throw new SDBRepositoryException("(" + modification + ") " + itemClass + " "
      // + data.getIdentifier() + " (" + data.getQPath().getAsString() + ") has child Nodes");
      // }
      // }
    } catch (AmazonSimpleDBException e) {
      throw new SDBStorageException("(" + modification + ") " + itemClass + " "
          + data.getIdentifier() + " (" + data.getQPath().getAsString() + ") read request fails "
          + e, e);
    }
  }

  /**
   * Build Item path by id.
   * 
   * @param itemId
   *          - Item id
   * @return QPath of
   * @throws InvalidItemStateException
   *           - if Item (or ancestor) not found
   * @throws IllegalNameException
   *           - if name on the path is wrong
   * @throws SDBRepositoryException
   *           - if storage inconsistency detected
   * @throws AmazonSimpleDBException
   *           - if SimpleDB storage error occurs
   */
  private QPath traverseQPath(String itemId) throws InvalidItemStateException,
                                            IllegalNameException,
                                            SDBRepositoryException,
                                            AmazonSimpleDBException {
    // get item by Identifier usecase
    List<QPathEntry> qrpath = new ArrayList<QPathEntry>(); // reverted path
    String ancestorId = itemId; // ancestor id
    do {
      QueryWithAttributesResponse resp = queryItemAttrByID(sdbService,
                                                           domainName,
                                                           ancestorId,
                                                           PID,
                                                           NAME,
                                                           ICLASS);

      if (resp.isSetQueryWithAttributesResult()) {
        QueryWithAttributesResult res = resp.getQueryWithAttributesResult();
        List<Item> items = res.getItem();
        if (items.size() == 1) {
          // got one item
          List<Attribute> atts = items.get(0).getAttribute();

          String iclass = getAttribute(atts, ICLASS);
          if (NODE_ICLASS.equals(iclass)) {
            // good - it's a next ancestor
            ancestorId = getAttribute(atts, PID);
            qrpath.add(QPathEntry.parse(getAttribute(atts, NAME)));
          } else
            throw new SDBRepositoryException("(item) FATAL Item with Id " + ancestorId
                + " shoudl be a Node but "
                + (PROPERTY_ICLASS.equals(iclass) ? "Property found." : "Undefined type found."));
        } else if (items.size() > 0) {
          // TODO much descriptive exception (location(name), is it Node or Property)
          throw new SDBRepositoryException("(item) FATAL Id '" + ancestorId
              + "' match multiple items in storage");
        } else
          throw new InvalidItemStateException("(item) Parent not found, Id " + ancestorId);
      }
    } while (!ancestorId.equals(Constants.ROOT_PARENT_UUID));

    QPathEntry[] qentries = new QPathEntry[qrpath.size()];
    int qi = 0;
    for (int i = qrpath.size() - 1; i >= 0; i--)
      qentries[qi++] = qrpath.get(i);

    return new QPath(qentries);
  }

  /**
   * Load NodeData from SimpleDB Item.
   * 
   * @param parentPath
   *          - parent path, can be null (getItemData by Id)
   * @param parentACL
   *          - parent ACL, can be null (getItemData by Id)
   * @param atts
   *          - SimpleDB Item attributes
   * @return NodeData
   * @throws IllegalNameException
   *           if QName stored in SimpleDB is wrong
   * @throws IllegalACLException
   *           - if ACL stored in SimpleDB is wrong
   * @throws NumberFormatException
   *           - if numeric values stored in SimpleDB is wrong
   * @throws AmazonSimpleDBException
   *           - if SimpleDB storage error occurs
   */
  protected NodeData loadNodeData(final QPath parentPath,
                                  final AccessControlList parentACL,
                                  final List<Attribute> atts) throws NumberFormatException,
                                                             IllegalNameException,
                                                             IllegalACLException,
                                                             InvalidItemStateException,
                                                             SDBRepositoryException,
                                                             AmazonSimpleDBException {

    String pid = getAttribute(atts, PID);

    QPath qpath;
    String parentId;

    try {
      if (pid != null) {
        // get by parent and name
        qpath = QPath.makeChildPath(parentPath, QPathEntry.parse(getAttribute(atts, NAME)));
        parentId = pid;
      } else {
        // get by id
        if (Constants.ROOT_PARENT_UUID.equals(pid)) {
          // root node
          qpath = Constants.ROOT_PATH;
          parentId = null;
        } else {
          qpath = QPath.makeChildPath(traverseQPath(pid),
                                      (QPathEntry) QPathEntry.parse(getAttribute(atts, NAME)));
          parentId = pid;
        }
      }
    } catch (IllegalNameException e) {
      throw new IllegalNameException("Node name contains wrong value '" + getAttribute(atts, NAME)
          + "'. Error " + e, e);
    }

    NodeIData idata = parseNodeIData(getAttribute(atts, IDATA), parentACL);

    return new PersistedNodeData(getAttribute(atts, ID),
                                 qpath,
                                 parentId,
                                 idata.getVersion(),
                                 idata.getOrderNumber(),
                                 idata.getPrimaryType(),
                                 idata.getMixinTypes()
                                      .toArray(new InternalQName[idata.getMixinTypes().size()]),
                                 idata.getACL());
  }

  /**
   * Load PropertyData from SimpleDb Item.
   * 
   * @param parentPath
   *          - parent path
   * @param atts
   *          - SimpleDB Item attributes
   * @param withValues
   *          - indicate if Property Value(s) data will be loaded
   * @return PropertyData instance
   * @throws IllegalNameException
   *           if QName stored in SimpleDB is wrong
   * @throws NumberFormatException
   *           - if numeric values stored in SimpleDB is wrong
   * @throws AmazonSimpleDBException
   *           - if SimpleDB storage error occurs
   * @throws RepositoryException
   *           if SimpleDB Item record contains wrong value
   * 
   * @see SDBWorkspaceStorageConnection.listChildPropertiesData()
   */
  protected PropertyData loadPropertyData(final QPath parentPath,
                                          final List<Attribute> atts,
                                          final boolean withValues) throws NumberFormatException,
                                                                   IllegalNameException,
                                                                   AmazonSimpleDBException,
                                                                   RepositoryException {

    String pid = getAttribute(atts, PID);

    PropertyIData idata = parsePropertyIData(getAttribute(atts, IDATA));

    PersistedPropertyData property = new PersistedPropertyData(getAttribute(atts, ID),
                                                               QPath.makeChildPath(parentPath == null
                                                                                       ? traverseQPath(pid)
                                                                                       : parentPath,
                                                                                   QPathEntry.parse(getAttribute(atts,
                                                                                                                 NAME))),
                                                               pid,
                                                               idata.getVersion(),
                                                               idata.getType(),
                                                               idata.isMultivalued());

    // Value
    if (withValues) {
      String[] vals = getAttributes(atts, DATA);
      List<ValueData> values = new ArrayList<ValueData>(vals.length);
      for (int i = 0; i < vals.length; i++) {
        char vp = vals[i].charAt(0);

        int orderNum;
        String value;
        if (property.isMultiValued()) {
          value = vals[i].substring(VALUEPREFIX_MULTIVALUED_LENGTH);
          // parse order number (3 chars with zero padding)
          // orderNum = Integer.parseInt(vals[i].substring(VALUEPREFIX_SINGLEVALUED_LENGTH,
          // VALUEPREFIX_MULTIVALUED_LENGTH));
          StringBuilder on = new StringBuilder();
          for (char ch : vals[i].substring(VALUEPREFIX_SINGLEVALUED_LENGTH,
                                           VALUEPREFIX_MULTIVALUED_LENGTH).toCharArray())
            if (ch != '0' || on.length() > 0)
              on.append(ch);
          orderNum = Integer.parseInt(on.toString());
        } else {
          value = vals[i].substring(VALUEPREFIX_SINGLEVALUED_LENGTH);
          orderNum = 0;
        }
        if (vp == VALUEPREFIX_DATA) {
          // data in SimpleDB
          if (property.getType() == PropertyType.BINARY)
            try {
              values.add(new ByteArrayPersistedValueData(Base64.decode(value), orderNum));
            } catch (DecodingException e) {
              throw new SDBAttributeValueCorruptedException("Property "
                  + property.getQPath().getName().getAsString() + " value[" + orderNum
                  + "] decoding error " + e, e);
            }
          else
            try {
              values.add(new ByteArrayPersistedValueData(value.getBytes(Constants.DEFAULT_ENCODING),
                                                         orderNum));
            } catch (UnsupportedEncodingException e) {
              throw new SDBAttributeValueFormatException("Property "
                  + property.getQPath().getName().getAsString() + " value[" + orderNum
                  + "] decoding error " + e, e);
            }
        } else if (vp == VALUEPREFIX_STORAGEID) {
          // data in external Value Storage
          try {
            ValueIOChannel channel = valueStorageProvider.getChannel(value);
            values.add(channel.read(property.getIdentifier(), orderNum, maxBufferSize));
          } catch (IOException e) {
            throw new RepositoryException("Property " + property.getQPath().getName().getAsString()
                + " value[" + orderNum + "] read I/O error " + e, e);
          }
        }
      }

      // sort multivalued
      if (values.size() > 1)
        Collections.sort(values, new ValueDataComparator());

      property.setValues(values);
    }

    return property;
  }

  // Interface impl

  /**
   * {@inheritDoc}
   */
  public void add(NodeData data) throws ItemExistsException, RepositoryException {

    // put in changes queue
    changes.add(new AddNodeOperation(data));
  }

  /**
   * {@inheritDoc}
   */
  public void add(PropertyData data) throws ItemExistsException, RepositoryException {

    // add
    changes.add(new AddPropertyOperation(data));
  }

  /**
   * {@inheritDoc}
   */
  public void commit() throws IllegalStateException, RepositoryException {
    // execute changes operations
    for (Iterator<WriteOperation> iter = changes.iterator(); iter.hasNext();) {
      WriteOperation o = iter.next();
      o.execute();
      o.markProcessed();
    }

    changes.clear();
    addedNodes.clear();
  }

  /**
   * {@inheritDoc}
   */
  public void rollback() throws IllegalStateException, RepositoryException {
    // iter backward
    try {
      for (int i = changes.size() - 1; i > 0; i--) {
        WriteOperation o = changes.get(i);
        if (!o.isProcessed())
          o.rollback();
      }
    } finally {
      // clear all on rollback anyway
      changes.clear();
      addedNodes.clear();
    }
  }

  /**
   * {@inheritDoc}
   */
  public void delete(NodeData data) throws RepositoryException, InvalidItemStateException {

    // delete
    changes.add(new DeleteNodeOperation(data));
  }

  /**
   * {@inheritDoc}
   */
  public void delete(PropertyData data) throws RepositoryException, InvalidItemStateException {

    // delete
    changes.add(new DeletePropertyOperation(data));
  }

  /**
   * {@inheritDoc}
   */
  public void update(NodeData data) throws RepositoryException,
                                   UnsupportedOperationException,
                                   InvalidItemStateException,
                                   IllegalStateException {

    // upadate
    changes.add(new UpdateNodeOperation(data));
  }

  /**
   * {@inheritDoc}
   */
  public void update(PropertyData data) throws RepositoryException,
                                       UnsupportedOperationException,
                                       InvalidItemStateException,
                                       IllegalStateException {
    // upadate
    changes.add(new UpdatePropertyOperation(data));
  }

  /**
   * {@inheritDoc}
   */
  public void rename(NodeData data) throws RepositoryException,
                                   UnsupportedOperationException,
                                   InvalidItemStateException,
                                   IllegalStateException {
    // TODO seems same as Update

    // upadate
    changes.add(new UpdateNodeOperation(data));
  }

  /**
   * {@inheritDoc}
   */
  public List<NodeData> getChildNodesData(NodeData parent) throws RepositoryException,
                                                          IllegalStateException {

    try {
      // TODO nextToken for large list

      QueryWithAttributesResponse resp = queryChildNodesAttr(sdbService,
                                                             domainName,
                                                             parent.getIdentifier(),
                                                             ID,
                                                             PID,
                                                             NAME,
                                                             ICLASS,
                                                             IDATA);

      List<NodeData> childItems = new ArrayList<NodeData>();

      if (resp.isSetQueryWithAttributesResult()) {
        QueryWithAttributesResult res = resp.getQueryWithAttributesResult();
        List<Item> items = res.getItem();
        for (Item item : items)
          childItems.add(loadNodeData(parent.getQPath(), parent.getACL(), item.getAttribute()));
      }

      // sort by order number
      if (childItems.size() > 1)
        Collections.sort(childItems, new NodeDataComparator());

      return childItems;
    } catch (AmazonSimpleDBException e) {
      throw new SDBStorageException("(child nodes) Parent " + parent.getQPath().getAsString() + " "
          + parent.getIdentifier() + ". Read request fails " + e, e);
    } catch (NumberFormatException e) {
      throw new SDBRepositoryException("(child nodes) Parent " + parent.getQPath().getAsString()
          + " " + parent.getIdentifier() + ". Read request fails " + e, e);
    } catch (IllegalNameException e) {
      throw new SDBRepositoryException("(child nodes) Parent " + parent.getQPath().getAsString()
          + " " + parent.getIdentifier() + ". Read request fails " + e, e);
    } catch (IllegalACLException e) {
      throw new SDBRepositoryException("(child nodes) Parent " + parent.getQPath().getAsString()
          + " " + parent.getIdentifier() + ". Read request fails " + e, e);
    }
  }

  /**
   * {@inheritDoc}
   */
  public List<PropertyData> getChildPropertiesData(NodeData parent) throws RepositoryException,
                                                                   IllegalStateException {
    try {
      // TODO nextToken for large list

      QueryWithAttributesResponse resp = queryChildPropertiesAttr(sdbService,
                                                                  domainName,
                                                                  parent.getIdentifier(),
                                                                  ID,
                                                                  PID,
                                                                  NAME,
                                                                  ICLASS,
                                                                  IDATA,
                                                                  DATA);

      List<PropertyData> childItems = new ArrayList<PropertyData>();

      if (resp.isSetQueryWithAttributesResult()) {
        QueryWithAttributesResult res = resp.getQueryWithAttributesResult();
        List<Item> items = res.getItem();
        for (Item item : items)
          childItems.add(loadPropertyData(parent.getQPath(), item.getAttribute(), true));
      }

      return childItems;
    } catch (AmazonSimpleDBException e) {
      throw new SDBStorageException("(child properties) Parent " + parent.getQPath().getAsString()
          + " " + parent.getIdentifier() + ". Read request fails " + e, e);
    } catch (NumberFormatException e) {
      throw new SDBRepositoryException("(child properties) Parent "
          + parent.getQPath().getAsString() + " " + parent.getIdentifier()
          + ". Read request fails " + e, e);
    } catch (IllegalNameException e) {
      throw new SDBRepositoryException("(child properties) Parent "
          + parent.getQPath().getAsString() + " " + parent.getIdentifier()
          + ". Read request fails " + e, e);
    }
  }

  /**
   * {@inheritDoc}
   */
  public ItemData getItemData(NodeData parent, QPathEntry qname) throws RepositoryException,
                                                                IllegalStateException {

    final String parentId = parent.getIdentifier();
    final String name = qname.getAsString(true);
    try {
      QueryWithAttributesResponse resp = queryItemAttrByName(sdbService,
                                                             domainName,
                                                             parentId,
                                                             name,
                                                             (String[]) null);

      if (resp.isSetQueryWithAttributesResult()) {
        QueryWithAttributesResult res = resp.getQueryWithAttributesResult();
        List<Item> items = res.getItem();
        if (items.size() == 1) {
          // got one item
          List<Attribute> atts = items.get(0).getAttribute();

          String iclass = getAttribute(atts, ICLASS);
          if (NODE_ICLASS.equals(iclass)) {
            // Node
            return loadNodeData(parent.getQPath(), parent.getACL(), atts);
          } else if (PROPERTY_ICLASS.equals(iclass)) {
            // Property
            return loadPropertyData(parent.getQPath(), atts, true);
          } else
            throw new SDBRepositoryException("(item) FATAL Item " + parent.getQPath().getAsString()
                + name + " (parentId=" + parentId + ") has undefined type (" + ICLASS + "="
                + iclass + ")");

        } else if (items.size() > 0) {
          // TODO much descriptive exception (location(name), is it Node or Property)
          throw new SDBRepositoryException("(item) FATAL Location "
              + parent.getQPath().getAsString() + name + " (parentId=" + parentId
              + ") match multiple items in storage");
        }
      }
    } catch (AmazonSimpleDBException e) {
      throw new SDBStorageException("(item) " + parent.getQPath().getAsString() + name
          + " (parentId=" + parentId + "). Read request fails " + e, e);
    } catch (NumberFormatException e) {
      throw new SDBRepositoryException("(item) " + parent.getQPath().getAsString() + name
          + " (parentId=" + parentId + "). Read request fails " + e, e);
    } catch (IllegalNameException e) {
      throw new SDBRepositoryException("(item) " + parent.getQPath().getAsString() + name
          + " (parentId=" + parentId + "). Read request fails " + e, e);
    } catch (IllegalACLException e) {
      throw new SDBRepositoryException("(item) " + parent.getQPath().getAsString() + name
          + " (parentId=" + parentId + "). Read request fails " + e, e);
    }

    return null;
  }

  /**
   * {@inheritDoc}
   */
  public ItemData getItemData(String identifier) throws RepositoryException, IllegalStateException {
    try {
      QueryWithAttributesResponse resp = queryItemAttrByID(sdbService,
                                                           domainName,
                                                           identifier,
                                                           (String[]) null);

      if (resp.isSetQueryWithAttributesResult()) {
        QueryWithAttributesResult res = resp.getQueryWithAttributesResult();
        List<Item> items = res.getItem();
        if (items.size() == 1) {
          // got one item
          List<Attribute> atts = items.get(0).getAttribute();

          String iclass = getAttribute(atts, ICLASS);
          if (NODE_ICLASS.equals(iclass)) {
            // Node
            return loadNodeData(null, null, atts);
          } else if (PROPERTY_ICLASS.equals(iclass)) {
            // Property
            return loadPropertyData(null, atts, true);
          } else
            throw new SDBRepositoryException("(item) FATAL Item with Id " + identifier
                + " has undefined type (" + ICLASS + "=" + iclass + ")");

        } else if (items.size() > 0) {
          // TODO much descriptive exception (location(name), is it Node or Property)
          throw new SDBRepositoryException("(item) FATAL Id " + identifier
              + " match multiple items in storage");
        }
      }
    } catch (AmazonSimpleDBException e) {
      throw new SDBStorageException("(item) Id " + identifier + ". Read request fails " + e, e);
    } catch (NumberFormatException e) {
      throw new SDBRepositoryException("(item) Id " + identifier + ". Read request fails " + e, e);
    } catch (IllegalNameException e) {
      throw new SDBRepositoryException("(item) Id " + identifier + ". Read request fails " + e, e);
    } catch (IllegalACLException e) {
      throw new SDBRepositoryException("(item) Id " + identifier + ". Read request fails " + e, e);
    }

    return null;
  }

  /**
   * {@inheritDoc}
   */
  public List<PropertyData> getReferencesData(String nodeIdentifier) throws RepositoryException,
                                                                    IllegalStateException,
                                                                    UnsupportedOperationException {
    // NOTE: REFERENCE Properties SHOULD be stored in SimpleDB storage (not in VS).

    try {
      // TODO nextToken for large list

      QueryWithAttributesResponse resp = queryReferencesAttr(sdbService,
                                                             domainName,
                                                             nodeIdentifier,
                                                             ID,
                                                             PID,
                                                             NAME,
                                                             ICLASS,
                                                             IDATA,
                                                             DATA);

      List<PropertyData> refProps = new ArrayList<PropertyData>();

      if (resp.isSetQueryWithAttributesResult()) {
        QueryWithAttributesResult res = resp.getQueryWithAttributesResult();
        List<Item> items = res.getItem();
        for (Item item : items)
          refProps.add(loadPropertyData(null, item.getAttribute(), true));
      }

      return refProps;
    } catch (AmazonSimpleDBException e) {
      throw new SDBStorageException("(references) Node Id " + nodeIdentifier
          + ". Read request fails " + e, e);
    } catch (NumberFormatException e) {
      throw new SDBRepositoryException("(references) Node Id " + nodeIdentifier
          + ". Read request fails " + e, e);
    } catch (IllegalNameException e) {
      throw new SDBRepositoryException("(references) Node Id " + nodeIdentifier
          + ". Read request fails " + e, e);
    }
  }

  /**
   * {@inheritDoc}
   */
  public boolean isOpened() {
    return true;
  }

  /**
   * {@inheritDoc}
   */
  public List<PropertyData> listChildPropertiesData(NodeData parent) throws RepositoryException,
                                                                    IllegalStateException {
    try {
      // TODO nextToken for large list

      QueryWithAttributesResponse resp = queryChildPropertiesAttr(sdbService,
                                                                  domainName,
                                                                  parent.getIdentifier(),
                                                                  ID,
                                                                  PID,
                                                                  NAME,
                                                                  ICLASS,
                                                                  IDATA);

      List<PropertyData> childItems = new ArrayList<PropertyData>();

      if (resp.isSetQueryWithAttributesResult()) {
        QueryWithAttributesResult res = resp.getQueryWithAttributesResult();
        List<Item> items = res.getItem();
        for (Item item : items)
          childItems.add(loadPropertyData(parent.getQPath(), item.getAttribute(), false));
      }

      return childItems;
    } catch (AmazonSimpleDBException e) {
      throw new SDBStorageException("(list child properties) Parent "
          + parent.getQPath().getAsString() + " " + parent.getIdentifier()
          + ". Read request fails " + e, e);
    } catch (NumberFormatException e) {
      throw new SDBRepositoryException("(list child properties) Parent "
          + parent.getQPath().getAsString() + " " + parent.getIdentifier()
          + ". Read request fails " + e, e);
    } catch (IllegalNameException e) {
      throw new SDBRepositoryException("(list child properties) Parent "
          + parent.getQPath().getAsString() + " " + parent.getIdentifier()
          + ". Read request fails " + e, e);
    }
  }

}
