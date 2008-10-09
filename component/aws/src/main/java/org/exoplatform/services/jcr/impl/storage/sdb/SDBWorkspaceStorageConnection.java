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
import java.util.Iterator;
import java.util.List;

import javax.jcr.InvalidItemStateException;
import javax.jcr.ItemExistsException;
import javax.jcr.RepositoryException;

import org.apache.commons.logging.Log;
import org.apache.ws.commons.util.Base64;
import org.apache.ws.commons.util.Base64.DecodingException;
import org.exoplatform.services.jcr.access.AccessControlEntry;
import org.exoplatform.services.jcr.access.AccessControlList;
import org.exoplatform.services.jcr.dataflow.ItemState;
import org.exoplatform.services.jcr.dataflow.persistent.PersistedNodeData;
import org.exoplatform.services.jcr.dataflow.persistent.PersistedPropertyData;
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
 * @version $Id: SDBWorkspaceStorageConnection.java 111 2008-11-11 11:11:11Z pnedonosko $
 */
public class SDBWorkspaceStorageConnection implements WorkspaceStorageConnection, SDBConstants {

  /**
   * Connection logger.
   */
  protected static final Log                 LOG                       = ExoLogger.getLogger("jcr.SDBWorkspaceStorageConnection");

  /**
   * Value prefix for actual data stored in Property Data attribute.
   */
  protected static final char                VALUEPREFIX_DATA          = 'D';

  /**
   * Value prefix for storage-id stored in Property Data attribute.
   */
  protected static final char                VALUEPREFIX_STORAGEID     = 'S';

  /**
   * Item Delete operation constant. Should be INTERNED.
   */
  protected static final String              ITEM_DELETE               = "delete".intern();

  /**
   * Item Update operation constant. Should be INTERNED.
   */
  protected static final String              ITEM_UPDATE               = "update".intern();

  /**
   * IData fields delimiter.
   */
  protected static final String              IDATA_DELIMITER           = "|";

  /**
   * IData version field key.
   */
  @Deprecated
  protected static final String              IDATA_VERSION             = "VN";

  /**
   * IData orderNumber field key.
   */
  @Deprecated
  protected static final String              IDATA_ORDERNUMBER         = "NN";

  /**
   * IData primaryType field key.
   */
  @Deprecated
  protected static final String              IDATA_PRIMARYTYPE         = "NT";

  /**
   * IData mixinType field key.
   */
  protected static final String              IDATA_MIXINTYPE           = "NM";

  /**
   * IData Property type field key.
   */
  @Deprecated
  protected static final String              IDATA_PTYPE               = "PT";

  /**
   * IData multiValued field key.
   */
  @Deprecated
  protected static final String              IDATA_MULTIVALUED         = "PM";

  /**
   * IData ACL permission field key.
   */
  protected static final String              IDATA_ACL_PERMISSION      = "AP";

  /**
   * IData ACL owner field key.
   */
  protected static final String              IDATA_ACL_OWNER           = "AO";

  /**
   * Get Item by ID query.
   */
  protected static final String              QUERY_GET_ITEM            = "['" + ID + "' = '%s']";

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

    private int                       version;

    private int                       orderNumber;

    private InternalQName             primaryType;

    private final List<InternalQName> mixinTypes = new ArrayList<InternalQName>();

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

    private int     version;

    private int     ptype;

    private boolean multivalued;

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
     * @throws AmazonSimpleDBException
     *           in case of SimpleDB service error
     */
    abstract Object rollback() throws AmazonSimpleDBException;

    /**
     * 
     * Execute write operation.
     * 
     * @return SimpleDB responce for the operation or error
     * @throws AmazonSimpleDBException
     *           in case of SimpleDB service error
     * @throws IOException
     *           if Value save fails
     * @throws SDBItemValueLengthExceeded
     *           if SimpleDB error occured
     */
    abstract Object execute() throws AmazonSimpleDBException,
                             SDBItemValueLengthExceeded,
                             IOException;

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
    Object execute() throws AmazonSimpleDBException {
      final List<ReplaceableAttribute> list = new ArrayList<ReplaceableAttribute>();

      list.add(new ReplaceableAttribute(ID, node.getIdentifier(), false));
      list.add(new ReplaceableAttribute(PID, node.getParentIdentifier(), false));
      list.add(new ReplaceableAttribute(NAME,
                                        node.getQPath().getEntries()[node.getQPath().getEntries().length - 1].getAsString(),
                                        false));
      list.add(new ReplaceableAttribute(ICLASS, NODE_ICLASS, false));
      // list.add(new ReplaceableAttribute(VERSION, String.valueOf(node.getPersistedVersion()),
      // false));
      // list.add(new ReplaceableAttribute(ORDERNUM, String.valueOf(node.getOrderNumber()), false));
      list.add(new ReplaceableAttribute(IDATA, formatIData(node), false));

      return createReplaceItem(sdbService, domainName, node.getIdentifier(), list);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    Object rollback() throws AmazonSimpleDBException {
      return deleteItem(sdbService, domainName, node.getIdentifier());
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
    Object execute() throws AmazonSimpleDBException, SDBItemValueLengthExceeded, IOException {

      // process Values firts,
      // if some Values matches VS filters they will be stored there.
      String[] values = addValues(property);

      final List<ReplaceableAttribute> list = new ArrayList<ReplaceableAttribute>();

      list.add(new ReplaceableAttribute(ID, property.getIdentifier(), false));
      list.add(new ReplaceableAttribute(PID, property.getParentIdentifier(), false));
      list.add(new ReplaceableAttribute(NAME,
                                        property.getQPath().getEntries()[property.getQPath()
                                                                                 .getEntries().length - 1].getAsString(),
                                        false));
      list.add(new ReplaceableAttribute(ICLASS, NODE_ICLASS, false));
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

      return createReplaceItem(sdbService, domainName, property.getIdentifier(), list);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    Object rollback() throws AmazonSimpleDBException {
      return deleteItem(sdbService, domainName, property.getIdentifier());
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
    Object execute() throws AmazonSimpleDBException {
      final List<ReplaceableAttribute> list = new ArrayList<ReplaceableAttribute>();

      // list.add(new ReplaceableAttribute(ID, node.getIdentifier(), true));
      list.add(new ReplaceableAttribute(PID, node.getParentIdentifier(), true));
      list.add(new ReplaceableAttribute(NAME,
                                        node.getQPath().getEntries()[node.getQPath().getEntries().length - 1].getAsString(),
                                        true));
      // list.add(new ReplaceableAttribute(ICLASS, NODE_ICLASS, true));

      // list.add(new ReplaceableAttribute(VERSION, String.valueOf(node.getPersistedVersion()),
      // true));
      // list.add(new ReplaceableAttribute(ORDERNUM, String.valueOf(node.getOrderNumber()), true));
      list.add(new ReplaceableAttribute(IDATA, formatIData(node), true));

      return createReplaceItem(sdbService, domainName, node.getIdentifier(), list);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    Object rollback() throws AmazonSimpleDBException {
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
     */
    @Override
    Object execute() throws AmazonSimpleDBException, SDBItemValueLengthExceeded, IOException {

      // process Values firts,
      // if some Values matches VS filters they will be stored there.
      String[] values = addValues(property);

      final List<ReplaceableAttribute> list = new ArrayList<ReplaceableAttribute>();

      // list.add(new ReplaceableAttribute(ID, property.getIdentifier(), true));
      list.add(new ReplaceableAttribute(PID, property.getParentIdentifier(), true));
      list.add(new ReplaceableAttribute(NAME,
                                        property.getQPath().getEntries()[property.getQPath()
                                                                                 .getEntries().length - 1].getAsString(),
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

      return createReplaceItem(sdbService, domainName, property.getIdentifier(), list);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    Object rollback() throws AmazonSimpleDBException {
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
    Object execute() throws AmazonSimpleDBException {

      // update existsing Item ID to deleted 'D'
      final List<ReplaceableAttribute> list = new ArrayList<ReplaceableAttribute>();
      list.add(new ReplaceableAttribute(ID, ITEM_DELETED_ID, true));

      return createReplaceItem(sdbService, domainName, node.getIdentifier(), list);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    Object rollback() throws AmazonSimpleDBException {

      // back Item ID to the actual
      final List<ReplaceableAttribute> list = new ArrayList<ReplaceableAttribute>();
      list.add(new ReplaceableAttribute(ID, node.getIdentifier(), false));

      return createReplaceItem(sdbService, domainName, node.getIdentifier(), list);
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
    Object execute() throws AmazonSimpleDBException {

      // update existsing Item ID to deleted 'D'
      final List<ReplaceableAttribute> list = new ArrayList<ReplaceableAttribute>();
      list.add(new ReplaceableAttribute(ID, ITEM_DELETED_ID, true));

      return createReplaceItem(sdbService, domainName, property.getIdentifier(), list);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    Object rollback() throws AmazonSimpleDBException {

      // back Item ID to the actual
      final List<ReplaceableAttribute> list = new ArrayList<ReplaceableAttribute>();
      list.add(new ReplaceableAttribute(ID, property.getIdentifier(), false));

      return createReplaceItem(sdbService, domainName, property.getIdentifier(), list);
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

    try {
      List<String> domains = getDomainsList();
      if (!domains.contains(this.domainName)) {
        // create
        createDomain(sdbService, this.domainName);
      }
    } catch (AmazonSimpleDBException e) {
      throw new SDBRepositoryException("Can not create SDB domain " + this.domainName, e);
    }

    this.changes = new ArrayList<WriteOperation>();
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
   * @param attributes
   *          SimpleDB item attributes for responce. If <code>null</code> all attributes will be
   *          returned
   * @return QueryWithAttributesResponse
   * @throws AmazonSimpleDBException
   *           in case of SDB error
   */
  protected QueryWithAttributesResponse queryItemAttr(final AmazonSimpleDB service,
                                                      final String domainName,
                                                      final String itemId,
                                                      final String... attributes) throws AmazonSimpleDBException {

    String query = String.format(QUERY_GET_ITEM, itemId);
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

    for (InternalQName mixin : node.getMixinTypeNames()) {
      idata.append(IDATA_DELIMITER);
      idata.append(IDATA_MIXINTYPE);
      idata.append(mixin.getAsString());
    }

    AccessControlList acl = node.getACL();
    if (acl != null) {
      for (AccessControlEntry ace : node.getACL().getPermissionEntries()) {
        idata.append(IDATA_DELIMITER);
        idata.append(IDATA_ACL_PERMISSION);
        idata.append(ace.getAsString());
      }

      idata.append(IDATA_DELIMITER);
      idata.append(IDATA_ACL_OWNER);
      idata.append(node.getACL().getOwner());
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
   * parseNodeIData.
   * 
   * @param field
   * @return
   * @throws IllegalNameException
   * @throws NumberFormatException
   */
  protected NodeIData parseNodeIData(String field) throws IllegalNameException,
                                                  NumberFormatException {

    String[] fs = field.split(IDATA_DELIMITER);

    NodeIData idata = new NodeIData();

    List<AccessControlEntry> aclPermissions = null;
    String aclOwner = null;

    for (int i = 0; i < fs.length; i++) {
      String s = fs[i];
      if (i == 0) {
        // version
        idata.setVersion(Integer.valueOf(s));
      } else if (i == 1) {
        // orderNumber
        idata.setOrderNumber(Integer.valueOf(s));
      } else if (i == 2) {
        // primaryType
        idata.setPrimaryType(InternalQName.parse(s));
      } else {
        // parse for mixins and ACL
        String value = s.substring(2);
        if (s.startsWith(IDATA_MIXINTYPE)) {
          // mixin
          idata.addMixinType(InternalQName.parse(value));
        } else if (s.startsWith(IDATA_ACL_PERMISSION)) {
          // ACL permission
          String[] aclp = value.split(AccessControlEntry.DELIMITER);

          if (aclPermissions == null)
            aclPermissions = new ArrayList<AccessControlEntry>();

          aclPermissions.add(new AccessControlEntry(aclp[0], aclp[1]));
        } else if (s.startsWith(IDATA_ACL_OWNER)) {
          // ACL owner
          aclOwner = value;
        }
      }
    }

    // ACL TODO (use JDBC conn logic)
    if (aclOwner != null || aclPermissions != null) {
      AccessControlList acl;
      if (aclOwner != null && aclPermissions != null) {
        acl = new AccessControlList(aclOwner, aclPermissions);
      } else if (aclOwner != null && aclPermissions == null) {
        acl = new AccessControlList();
        acl.setOwner(aclOwner);
      } else if (aclOwner == null && aclPermissions != null) {
        acl = new AccessControlList();
        // acl.addPermissions(rawData)
      }
    }

    return idata;
  }

  protected PropertyIData parsePropertyIData(String field) {

    String[] fs = field.split(IDATA_DELIMITER);

    PropertyIData idata = new PropertyIData();

    for (String s : fs) {
      // TODO
    }

    return idata;
  }

  /**
   * Property Values processing. Extract Value data into String representation. If Vaslue is
   * multivalued return sequence of Strings.
   * 
   * @param data
   *          - Value data
   * @return sequence of Strings
   * @throws IOException
   *           - if I/O error occurs
   * @throws SDBItemValueLengthExceeded
   *           - if storage error occurs
   */
  protected String[] addValues(PropertyData data) throws IOException, SDBItemValueLengthExceeded {

    List<ValueData> vdata = data.getValues();
    String[] vseq = new String[vdata.size()];
    for (int i = 0; i < vdata.size(); i++) {
      ValueData vd = vdata.get(i);
      vd.setOrderNumber(i); // TODO do we have to do it here?
      ValueIOChannel channel = valueStorageProvider.getApplicableChannel(data, i);
      String v;
      if (channel == null) {
        // store in SDB
        byte[] bytes = vd.getAsByteArray();
        if (bytes.length < SDB_ATTRIBUTE_VALUE_MAXLENGTH) {
          v = VALUEPREFIX_DATA + Base64.encode(bytes);
        } else {
          // error
          throw new SDBItemValueLengthExceeded("Property '" + data.getQPath().getAsString()
              + "' value size too large. Maximum Value size can be stored in SimpleDB is "
              + SDB_ATTRIBUTE_VALUE_MAXLENGTH
              + " bytes. Use Extenal Value Storage (to Amazon S3) for large Values.");
        }
      } else {
        // store in External storage
        channel.write(data.getIdentifier(), vd);
        v = VALUEPREFIX_STORAGEID + channel.getStorageId();
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
      QueryWithAttributesResponse resp = queryItemAttr(sdbService,
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
        resp = queryItemAttr(sdbService, domainName, data.getParentIdentifier(), ID);
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
   * @throws SDBAttributeValueFormatException
   *           - if persisted Version attribute format is invalid
   * @see ITEM_DELETE, ITEM_UPDATE
   */
  protected void validateItemChange(ItemData data, String modification) throws SDBRepositoryException,
                                                                       InvalidItemStateException,
                                                                       SDBAttributeValueFormatException {

    final String itemClass = data.isNode() ? "Node" : "Property";
    try {
      QueryWithAttributesResponse resp = queryItemAttr(sdbService,
                                                       domainName,
                                                       data.getIdentifier(),
                                                       IDATA);

      if (resp.isSetQueryWithAttributesResult()) {
        QueryWithAttributesResult res = resp.getQueryWithAttributesResult();
        // SDB items
        List<Item> items = res.getItem();
        if (items.size() <= 0) {
          // if Item exists...
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
                v = parseNodeIData(idv).getVersion();
              } else {
                v = parsePropertyIData(idv).getVersion();
              }
              if (v != data.getPersistedVersion()) {
                // TODO are we need the check here?
                LOG.warn(">>>>> InvalidItemState. " + itemClass + " "
                    + data.getQPath().getAsString());
              }
            } catch (NumberFormatException nfe) {
              throw new SDBAttributeValueFormatException("(" + modification
                  + ") Persisted Version attribute contains wrong data '" + idv + "'. " + itemClass
                  + " " + data.getQPath().getAsString(), nfe);
            } catch (IllegalNameException e) {
              // TODO Auto-generated catch block
              e.printStackTrace();
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
   * Load NodeData from SimpleDB Item.
   * 
   * @param parent
   *          - parent NodeData
   * @param atts
   *          - SimpleDB Item attributes
   * @return NodeData
   * @throws SDBAttributeValueFormatException
   *           if SimpleDB Item record contains wrong value
   */
  protected NodeData loadNodeData(final NodeData parent, final List<Attribute> atts) throws SDBAttributeValueFormatException {
    // TODO null parent
    NodeIData idata;
    try {
      idata = parseNodeIData(getAttribute(atts, IDATA));
    } catch (IllegalNameException e) {
      throw new SDBAttributeValueFormatException("(child nodes) Node "
          + parent.getQPath().getAsString() + " " + parent.getIdentifier() + ". Node's nodetype ("
          + IDATA + " jcr:primaryType or jcr:mixinTypes) contains wrong value '"
          + getAttribute(atts, IDATA) + "'. Error " + e, e);
    } catch (NumberFormatException e) {
      throw new SDBAttributeValueFormatException("(child nodes) Node " + parent.getIdentifier()
          + " (" + parent.getQPath().getAsString() + ") " + IDATA
          + " attribute (version or orderNumber) contains wrong integer value '"
          + getAttribute(atts, IDATA) + "'. Error " + e, e);
    }
    try {
      return new PersistedNodeData(getAttribute(atts, ID),
                                   QPath.makeChildPath(parent.getQPath(),
                                                       InternalQName.parse(getAttribute(atts, NAME))),
                                   getAttribute(atts, PID),
                                   idata.getVersion(),
                                   idata.getOrderNumber(),
                                   idata.getPrimaryType(),
                                   idata.getMixinTypes()
                                        .toArray(new InternalQName[idata.getMixinTypes().size()]),
                                   idata.getACL());
    } catch (IllegalNameException e) {
      throw new SDBAttributeValueFormatException("(child nodes) Node "
          + parent.getQPath().getAsString() + " " + parent.getIdentifier()
          + ". Node's child Node name contains wrong value '" + getAttribute(atts, NAME)
          + "'. Error " + e, e);
    }
  }
  
  /**
   * Load PropertyData from SimpleDb Item.
   *
   * @param parent
   *          - parent NodeData
   * @param atts
   *          - SimpleDB Item attributes
   * @return PropertyData
   * @throws RepositoryException
   *           if SimpleDB Item record contains wrong value
   */
  protected PropertyData loadPropertyData(final NodeData parent, final List<Attribute> atts) throws RepositoryException {
    // TODO null parent
    PropertyIData idata = parsePropertyIData(getAttribute(atts, IDATA));
    try {
      PersistedPropertyData property = new PersistedPropertyData(getAttribute(atts, ID),
                                                                 QPath.makeChildPath(parent.getQPath(),
                                                                                     InternalQName.parse(getAttribute(atts,
                                                                                                                      NAME))),
                                                                 getAttribute(atts, PID),
                                                                 idata.getVersion(),
                                                                 idata.getType(),
                                                                 idata.isMultivalued());

      // Value
      String[] vals = getAttributes(atts, DATA);
      List<ValueData> values = new ArrayList<ValueData>(vals.length);
      for (int i = 0; i < vals.length; i++) {
        char vp = vals[i].charAt(0);
        if (vp == VALUEPREFIX_DATA) {
          // data in SimpleDB
          String value = vals[i].substring(1);
          try {
            values.add(new ByteArrayPersistedValueData(Base64.decode(value), i));
          } catch (DecodingException e) {
            throw new SDBAttributeValueCorruptedException("(child properties) Node "
                + parent.getQPath().getAsString() + " " + parent.getIdentifier()
                + ". Property " + property.getQPath().getName().getAsString() + " value[" + i
                + "] decoding error " + e, e);
          }
        } else if (vp == VALUEPREFIX_STORAGEID) {
          // data in external Value Storage
          String storageId = vals[i].substring(1);
          try {
            ValueIOChannel channel = valueStorageProvider.getChannel(storageId);
            values.add(channel.read(property.getIdentifier(), i, maxBufferSize));
          } catch (IOException e) {
            throw new RepositoryException("(child properties) Node "
                + parent.getQPath().getAsString() + " " + parent.getIdentifier()
                + ". Node's Property " + property.getQPath().getName().getAsString()
                + " value[" + i + "] read I/O error " + e, e);
          }
        }
      }

      property.setValues(values);
      return property; 
    } catch (IllegalNameException e) {
      throw new SDBAttributeValueFormatException("(child properties) Node "
          + parent.getQPath().getAsString() + " " + parent.getIdentifier()
          + ". Node's Property name contains wrong value '" + getAttribute(atts, NAME)
          + "'. Error " + e, e);
    }
  }
  // Interface impl

  /**
   * {@inheritDoc}
   */
  public void add(NodeData data) throws ItemExistsException, RepositoryException {

    // validate
    validateItemAdd(data);

    // put in changes queue
    changes.add(new AddNodeOperation(data));
  }

  /**
   * {@inheritDoc}
   */
  public void add(PropertyData data) throws ItemExistsException, RepositoryException {

    // validate if Item aready exists
    validateItemAdd(data);

    // add
    changes.add(new AddPropertyOperation(data));
  }

  /**
   * {@inheritDoc}
   */
  public void commit() throws IllegalStateException, RepositoryException {
    // execute changes operations
    WriteOperation o = null;
    try {
      for (Iterator<WriteOperation> iter = changes.iterator(); iter.hasNext();) {
        o = iter.next();
        o.execute();
        o.markProcessed();
      }
    } catch (AmazonSimpleDBException e) {
      throw new RepositoryException("Storage commit error "
          + (o != null ? "on item " + o.getPath().getAsString() + ", " : "") + e, e);
    } catch (IOException e) {
      throw new RepositoryException("Storage commit I/O error "
          + (o != null ? "on item " + o.getPath().getAsString() + ", " : "") + e, e);
    }

  }

  /**
   * {@inheritDoc}
   */
  public void rollback() throws IllegalStateException, RepositoryException {
    try {
      // iter backward
      for (int i = changes.size() - 1; i > 0; i--) {
        WriteOperation o = changes.get(i);
        if (!o.isProcessed())
          o.rollback();
      }
    } catch (AmazonSimpleDBException e) {
      // throw an error
      throw new RepositoryException("Storage rollback error " + e, e);
    }
  }

  /**
   * {@inheritDoc}
   */
  public void delete(NodeData data) throws RepositoryException, InvalidItemStateException {

    // validate
    validateItemChange(data, ITEM_DELETE);

    // delete
    changes.add(new DeleteNodeOperation(data));
  }

  /**
   * {@inheritDoc}
   */
  public void delete(PropertyData data) throws RepositoryException, InvalidItemStateException {
    // validate
    validateItemChange(data, ITEM_DELETE);

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

    // validate
    validateItemChange(data, ITEM_UPDATE);

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
    // validate
    validateItemChange(data, ITEM_UPDATE);

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

    // validate
    validateItemChange(data, ITEM_UPDATE);

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
          childItems.add(loadNodeData(parent, item.getAttribute()));
      }

      return childItems;
    } catch (AmazonSimpleDBException e) {
      throw new SDBStorageException("(child properties) Node " + parent.getQPath().getAsString()
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
          childItems.add(loadPropertyData(parent, item.getAttribute()));
      }

      return childItems;
    } catch (AmazonSimpleDBException e) {
      throw new SDBStorageException("(child properties) Node " + parent.getQPath().getAsString()
          + " " + parent.getIdentifier() + ". Read request fails " + e, e);
    }
  }

  /**
   * {@inheritDoc}
   */
  public ItemData getItemData(NodeData parentData, QPathEntry name) throws RepositoryException,
                                                                   IllegalStateException {

    return null;
  }

  /**
   * {@inheritDoc}
   */
  public ItemData getItemData(String identifier) throws RepositoryException, IllegalStateException {

    try {
      QueryWithAttributesResponse resp = queryItemAttr(sdbService,
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
            return loadNodeData(null, atts);
          } else if (PROPERTY_ICLASS.equals(iclass)) {
            // Property
            return loadPropertyData(null, atts);
          } else
            throw new SDBRepositoryException("(item) FATAL Item with Id " + identifier
                + " has undefined type (" + ICLASS + "=" + iclass + ")");

        } else if (items.size() > 0) {
          // TODO much descriptive exception (location(name), is it Node or Property)
          throw new SDBRepositoryException("(item) FATAL Id '" + identifier
              + "' match multiple items in storage");
        }
      }
    } catch (AmazonSimpleDBException e) {
      throw new SDBStorageException("(item) Id " + identifier + ". Read request fails " + e, e);
    }
    
    return null;
  }

  /**
   * {@inheritDoc}
   */
  public List<PropertyData> getReferencesData(String nodeIdentifier) throws RepositoryException,
                                                                    IllegalStateException,
                                                                    UnsupportedOperationException {
    // TODO Auto-generated method stub
    return null;
  }

  /**
   * {@inheritDoc}
   */
  public boolean isOpened() {
    // TODO Auto-generated method stub
    return false;
  }

  /**
   * {@inheritDoc}
   */
  public List<PropertyData> listChildPropertiesData(NodeData parent) throws RepositoryException,
                                                                    IllegalStateException {
    // TODO Auto-generated method stub
    return null;
  }

}
