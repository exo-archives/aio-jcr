/*
 * Copyright 2004-2005 The Apache Software Foundation or its licensors,
 *                     as applicable.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.exoplatform.services.jcr.impl.core.query.lucene;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;

import javax.jcr.RepositoryException;
import javax.jcr.query.InvalidQueryException;

import org.apache.commons.collections.iterators.AbstractIteratorDecorator;
import org.apache.commons.logging.Log;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.MultiReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.Hits;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortField;
import org.exoplatform.services.document.DocumentReaderService;
import org.exoplatform.services.jcr.config.RepositoryConfigurationException;
import org.exoplatform.services.jcr.config.WorkspaceEntry;
import org.exoplatform.services.jcr.dataflow.ItemState;
import org.exoplatform.services.jcr.dataflow.ItemStateChangesLog;
import org.exoplatform.services.jcr.dataflow.persistent.ItemsPersistenceListener;
import org.exoplatform.services.jcr.datamodel.InternalQName;
import org.exoplatform.services.jcr.datamodel.ItemData;
import org.exoplatform.services.jcr.datamodel.NodeData;
import org.exoplatform.services.jcr.impl.Constants;
import org.exoplatform.services.jcr.impl.core.LocationFactory;
import org.exoplatform.services.jcr.impl.core.NodeImpl;
import org.exoplatform.services.jcr.impl.core.SessionImpl;
import org.exoplatform.services.jcr.impl.core.query.AbstractQueryHandler;
import org.exoplatform.services.jcr.impl.core.query.ExecutableQuery;
import org.exoplatform.services.jcr.impl.core.query.TextFilter;
import org.exoplatform.services.jcr.impl.dataflow.persistent.WorkspacePersistentDataManager;
import org.exoplatform.services.log.ExoLogger;

/**
 * Implements a {@link org.apache.jackrabbit.core.query.QueryHandler} using
 * Lucene.
 */
public class SearchIndex extends AbstractQueryHandler implements ItemsPersistenceListener {

  /** The logger instance for this class */
  private static Log                     log                      = ExoLogger.getLogger("jcr.SearchIndex");

  /**
   * The default value for property {@link #minMergeDocs}.
   */
  public static final int                DEFAULT_MIN_MERGE_DOCS   = 100;

  /**
   * The default value for property {@link #maxMergeDocs}.
   */
  public static final int                DEFAULT_MAX_MERGE_DOCS   = 100000;

  /**
   * the default value for property {@link #mergeFactor}.
   */
  public static final int                DEFAULT_MERGE_FACTOR     = 10;

  /**
   * the default value for property {@link #maxFieldLength}.
   */
  public static final int                DEFAULT_MAX_FIELD_LENGTH = 10000;

  /**
   * The actual index
   */
  private MultiIndex                     index;

  /**
   * The analyzer we use for indexing.
   */
  private final Analyzer                 analyzer;

  /**
   * List of {@link org.apache.jackrabbit.core.query.TextFilter} instance.
   */
  private List                           textFilters;

  /**
   * The location of the search index. <p/> Note: This is a <b>mandatory</b>
   * parameter!
   */
  private String                         path;

  /**
   * minMergeDocs config parameter.
   */
  private int                            minMergeDocs             = DEFAULT_MIN_MERGE_DOCS;

  /**
   * volatileIdleTime config parameter.
   */
  private int                            volatileIdleTime         = 3;

  /**
   * maxMergeDocs config parameter
   */
  private int                            maxMergeDocs             = DEFAULT_MAX_MERGE_DOCS;

  /**
   * mergeFactor config parameter
   */
  private int                            mergeFactor              = DEFAULT_MERGE_FACTOR;

  /**
   * maxFieldLength config parameter
   */
  private int                            maxFieldLength           = DEFAULT_MAX_FIELD_LENGTH;

  /**
   * Number of documents that are buffered before they are added to the index.
   */
  private int                            bufferSize               = 10;

  /**
   * Compound file flag
   */
  private boolean                        useCompoundFile          = true;

  /**
   * Flag indicating whether document order is enable as the default ordering.
   * was true, [PN] 07.08.07
   */
  private boolean                        documentOrder            = true;

  /**
   * If set <code>true</code> the index is checked for consistency on startup.
   * If <code>false</code> a consistency check is only performed when there
   * are entries in the redo log on startup. <p/> Default value is:
   * <code>false</code>.
   */
  private boolean                        forceConsistencyCheck    = false;

  /**
   * If set <code>true</code> errors detected by the consistency check are
   * repaired. If <code>false</code> the errors are only reported in the log.
   * <p/> Default value is: <code>true</code>.
   */
  private boolean                        autoRepair               = true;

  /**
   * The identifier resolver cache size. <p/> Default value is:
   * <code>1000</code>.
   */
  private int                            cacheSize                = 1000;

  /**
   * The field for transferring a DocumentReaderService variable by the
   * SearchIndex constructor to the NodeIndexer.
   */
  private DocumentReaderService          documentReaderService    = null;

  private WorkspacePersistentDataManager dataManager;

  private final LocationFactory          sysLocationFactory;

  /**
   * The Identifier of the root node.
   */
  private final String                   rootIdentifier;

  /**
   * Indicates if this <code>SearchIndex</code> is closed and cannot be used
   * anymore.
   */
  private boolean                        closed                   = false;

  /**
   * Monitor to use to synchronize access in search index to {@link onSaveItems}
   */
  private final Object                   onSaveMonitor        = new Object();

  /**
   * Default constructor.
   */
  public SearchIndex(WorkspaceEntry config, DocumentReaderService ds, WorkspacePersistentDataManager dataManager,
      LocationFactory sysLocationFactory) throws RepositoryConfigurationException, IOException {
    this.analyzer = new StandardAnalyzer();
    String indexDir = config.getQueryHandler().getParameterValue("indexDir");
    indexDir = indexDir.replace("${java.io.tmpdir}", System.getProperty("java.io.tmpdir"));
    this.path = indexDir + "/" + config.getName();
    this.documentReaderService = ds;
    this.dataManager = dataManager;
    this.sysLocationFactory = sysLocationFactory;
    this.rootIdentifier = Constants.ROOT_UUID;
  }

  /**
   * Initializes this <code>QueryHandler</code>. This implementation requires
   * that a path parameter is set in the configuration. If this condition is not
   * met, a <code>IOException</code> is thrown.
   * 
   * @throws IOException if an error occurs while initializing this handler.
   */
  public void init() throws IOException {
    if (path == null) {
      throw new IOException("SearchIndex requires 'path' parameter in configuration!");
    }
    index = new MultiIndex(new File(path), this, dataManager, rootIdentifier);
    if (index.getRedoLogApplied() || forceConsistencyCheck) {
      log.info("Running consistency check...");
      try {
        ConsistencyCheck check = ConsistencyCheck.run(index, dataManager);
        if (autoRepair) {
          check.repair(true);
        } else {
          List errors = check.getErrors();
          if (errors.size() == 0) {
            log.info("No errors detected.");
          }
          for (Iterator it = errors.iterator(); it.hasNext();) {
            ConsistencyCheckError err = (ConsistencyCheckError) it.next();
            log.info(err.toString());
          }
        }
      } catch (Exception e) {
        log.warn("Failed to run consistency check on index: " + e);
      }
    }

    dataManager.addItemPersistenceListener(this);

  }

  /**
   * Adds the <code>node</code> to the search index.
   * 
   * @param node the node to add.
   * @throws RepositoryException if an error occurs while indexing the node.
   * @throws IOException if an error occurs while adding the node to the index.
   */
  public void addNode(NodeImpl node) throws RepositoryException, IOException {
    throw new UnsupportedOperationException("addNode");
  }

  /**
   * Removes the node with <code>identifier</code> from the search index.
   * 
   * @param identifier the Identifier of the node to remove from the index.
   * @throws IOException if an error occurs while removing the node from the
   *           index.
   */
  public void deleteNode(String identifier) throws IOException {
    throw new UnsupportedOperationException("deleteNode");
  }

  /**
   * This implementation forwards the call to
   * {@link MultiIndex#update(java.util.Iterator, java.util.Iterator)} and
   * transforms the two iterators to the required types.
   * 
   * @param remove uuids of nodes to remove.
   * @param add NodeStates to add. Calls to <code>next()</code> on this
   *          iterator may return <code>null</code>, to indicate that a node
   *          could not be indexed successfully.
   * @throws RepositoryException if an error occurs while indexing a node.
   * @throws IOException if an error occurs while updating the index.
   */
  public void updateNodes(final Iterator remove, final Iterator add) throws RepositoryException, IOException {
    checkOpen();
    index.update(new AbstractIteratorDecorator(remove) {
      public Object next() {
        String identifier = (String) super.next();
        return new Term(FieldNames.UUID, identifier);
      }
    }, new AbstractIteratorDecorator(add) {
      public Object next() {
        NodeData state = (NodeData) super.next();
        try {
          if (state != null)
            // [PN] return createDocument(state);            
            return new NodeIndexer(state, sysLocationFactory, documentReaderService, dataManager).createDoc();
          else
            return null;
        } catch (RepositoryException e) {
          log.error("Exception while creating document for node: " + state.getQPath().getAsString() + ": "
              + e.toString(), e);
          return null;
        }
      }
    });
  }

  /**
   * Creates a new query by specifying the query statement itself and the
   * language in which the query is stated. If the query statement is
   * syntactically invalid, given the language specified, an
   * InvalidQueryException is thrown. <code>language</code> must specify a
   * query language string from among those returned by
   * QueryManager.getSupportedQueryLanguages(); if it is not then an
   * <code>InvalidQueryException</code> is thrown.
   * 
   * @param session the session of the current user creating the query object.
   * @param itemMgr the item manager of the current user.
   * @param statement the query statement.
   * @param language the syntax of the query statement.
   * @throws InvalidQueryException if statement is invalid or language is
   *           unsupported.
   * @return A <code>Query</code> object.
   */
  public ExecutableQuery createExecutableQuery(SessionImpl session,
  // ItemManager itemMgr,
      String statement, String language) throws InvalidQueryException {

    QueryImpl query = new QueryImpl(session, this, statement, language);
    query.setRespectDocumentOrder(documentOrder);
    return query;

    // return null;
  }

  /**
   * Closes this <code>QueryHandler</code> and frees resources attached to
   * this handler.
   */
  public void close() {
    if (!closed) {
      index.close();
      closed = true;
      log.info("Search index closed.");
    } else
      log.warn("Search index already closed.");
  }

  /**
   * Executes the query on the search index.
   * 
   * @param query the lucene query.
   * @param orderProps name of the properties for sort order.
   * @param orderSpecs the order specs for the sort order properties.
   *          <code>true</code> indicates ascending order, <code>false</code>
   *          indicates descending.
   * @return the lucene Hits object.
   * @throws IOException if an error occurs while searching the index.
   */
  QueryHits executeQuery(Query query, InternalQName[] orderProps, boolean[] orderSpecs) throws IOException {

    checkOpen();
    SortField[] sortFields = createSortFields(orderProps, orderSpecs);
    IndexReader reader = index.getIndexReader();
    IndexSearcher searcher = new IndexSearcher(reader);

    Hits hits;
    if (sortFields.length > 0) {
      hits = searcher.search(query, new Sort(sortFields));
      searcher.close();
    } else {
      hits = searcher.search(query);
      searcher.close();
    }
    return new QueryHits(hits, reader);
  }

  // ///////////////////

  // TODO synchronized 
  public void onSaveItems(final ItemStateChangesLog changesLog) {

    // nodes that need to be removed from the index.
    final Set<String> removedNodes = new LinkedHashSet<String>();
    // nodes that need to be added to the index.
    final Set<NodeData> addedNodes = new LinkedHashSet<NodeData>();
    
    // for error message
    final ThreadLocal<ItemState> currentItemState = new ThreadLocal<ItemState>();
    
    //final long start = System.currentTimeMillis();
    //log.info(Thread.currentThread().getName() + "\t" + new SimpleDateFormat("HH:mm:ss.SSS").format(new Date(start)) + "\t start\t\t>>>>");
    synchronized (onSaveMonitor) {
      List<ItemState> itemStates = changesLog.getAllStates();
      for (ItemState itemState : itemStates) {
        currentItemState.set(itemState);
        if (itemState.isNode()) {
          if (itemState.isAdded() || itemState.isRenamed()) {
            addedNodes.add((NodeData) itemState.getData());
          } else if (itemState.isDeleted()) {
            removedNodes.add(itemState.getData().getIdentifier());
          }else if (itemState.isMixinChanged()){
            removedNodes.add(itemState.getData().getIdentifier());
            addedNodes.add((NodeData) itemState.getData());
          }
        } else {
          String parentIdentifier = itemState.getData().getParentIdentifier();
  
          if (getItemState(parentIdentifier, itemStates) == null) {
            // add parent id to reindex
            removedNodes.add(parentIdentifier);
            try {
              addedNodes.add((NodeData) dataManager.getItemData(parentIdentifier));
            } catch (RepositoryException e) {
              log.error("Error indexing node (addNode: " + parentIdentifier + ").", e);
            }
          }
        }
      }
      onSaveMonitor.notifyAll();
    }
    try {
      //log.info(Thread.currentThread().getName() + "\t" + new SimpleDateFormat("HH:mm:ss.SSS").format(new Date()) + "\t update " + (System.currentTimeMillis() - start) + "ms\t....");
      updateNodes(removedNodes.iterator(), addedNodes.iterator());
      //log.info(Thread.currentThread().getName() + "\t" + new SimpleDateFormat("HH:mm:ss.SSS").format(new Date()) + "\t done " + (System.currentTimeMillis() - start) + "ms\t<<<<");
    } catch (RepositoryException e) {
      log.error("Error indexing node. " + (currentItemState.get() != null ? 
          currentItemState.get().getData().getQPath().getAsString() : "<null>"), e);
    } catch (IOException e) {
      log.error("Error indexing node. " + (currentItemState.get() != null ? 
          currentItemState.get().getData().getQPath().getAsString() : "<null>"), e);
    } catch (Throwable e) {
      log.error("Error indexing node. " + (currentItemState.get() != null ? 
          currentItemState.get().getData().getQPath().getAsString() : "<null>"), e);
    }
  }

  private ItemState getItemState(final String identifier, final List<ItemState> itemStates) {
    for (ItemState istate : itemStates) {
      if (istate.getData().getIdentifier().equals(identifier)) {
        return istate;
      }
    }

    return null;
  }

  // ////////////////////////

  /**
   * Creates the SortFields for the order properties.
   * 
   * @param orderProps the order properties.
   * @param orderSpecs the order specs for the properties.
   * @return an array of sort fields
   */
  protected SortField[] createSortFields(InternalQName[] orderProps, boolean[] orderSpecs) {
    List sortFields = new ArrayList();
    for (int i = 0; i < orderProps.length; i++) {
      String prop = null;
      if ("jcr:score".equals(orderProps[i])) {
        // order on jcr:score does not use the natural order as
        // implemented in lucene. score ascending in lucene means that
        // higher scores are first. JCR specs that lower score values
        // are first.
        sortFields.add(new SortField(null, SortField.SCORE, orderSpecs[i]));
      } else {
        try {
          prop = sysLocationFactory.createJCRName(orderProps[i]).getAsString();
        } catch (RepositoryException e) {
          e.printStackTrace();
          // will never happen ?
        }
        sortFields.add(new SortField(prop, SharedFieldSortComparator.PROPERTIES, !orderSpecs[i]));
      }
    }
    return (SortField[]) sortFields.toArray(new SortField[sortFields.size()]);
  }

  /**
   * Returns the analyzer in use for indexing.
   * 
   * @return the analyzer in use for indexing.
   */
  public Analyzer getAnalyzer() {
    return analyzer;
  }

  /**
   * Creates a lucene <code>Document</code> from a node state using the
   * namespace mappings <code>nsMappings</code>.
   * 
   * @param node the node state to index.
   * @param nsMappings the namespace mappings of the search index.
   * @return a lucene <code>Document</code> that contains all properties of
   *         <code>node</code>.
   * @throws RepositoryException if an error occurs while indexing the
   *           <code>node</code>.
   */
  protected Document createDocument(final NodeData node) throws RepositoryException {
    // [PN] return NodeIndexer.createDocument(node, sysLocationFactory, documentReaderService, dataManager);
    return node != null ? new NodeIndexer(node, sysLocationFactory, documentReaderService, dataManager).createDoc() : null;
  }

  /**
   * Returns the actual index.
   * 
   * @return the actual index.
   */
  protected MultiIndex getIndex() {
    return index;
  }

  /**
   * Sets a new set of text filter classes that are in use for indexing binary
   * properties. The <code>filterClasses</code> must be a comma separated
   * <code>String</code> of fully qualified class names implementing
   * {@link org.apache.jackrabbit.core.query.TextFilter}. Each class must
   * provide a default constructor.
   * </p>
   * Filter class names that cannot be resolved are skipped and a warn message
   * is logged.
   * 
   * @param filterClasses comma separated list of filter class names
   */
  public void setTextFilterClasses(String filterClasses) {
    List filters = new ArrayList();
    StringTokenizer tokenizer = new StringTokenizer(filterClasses, ", \t\n\r\f");
    while (tokenizer.hasMoreTokens()) {
      String className = tokenizer.nextToken();
      try {
        Class filterClass = Class.forName(className);
        TextFilter filter = (TextFilter) filterClass.newInstance();
        filters.add(filter);
      } catch (Exception e) {
        log.warn("Invalid TextFilter class: " + className, e);
      } catch (LinkageError e) {
        log.warn("Missing dependency for text filter: " + className);
        log.warn(e.toString());
      }
    }
    textFilters = Collections.unmodifiableList(filters);
  }

  /**
   * Returns the fully qualified class names of the text filter instances
   * currently in use. The names are comma separated.
   * 
   * @return class names of the text filters in use.
   */
  public String getTextFilterClasses() {
    StringBuffer names = new StringBuffer();
    String delim = "";
    for (Iterator it = textFilters.iterator(); it.hasNext();) {
      names.append(delim);
      names.append(it.next().getClass().getName());
      delim = ",";
    }
    return names.toString();
  }

  // ----------------------------< internal >----------------------------------

  /**
   * Combines multiple {@link CachingMultiReader} into a
   * <code>MultiReader</code> with {@link HierarchyResolver} support.
   */
  protected static final class CombinedIndexReader extends MultiReader implements HierarchyResolver {

    /**
     * The sub readers.
     */
    private CachingMultiReader[] subReaders;

    /**
     * Doc number starts for each sub reader
     */
    private int[]                starts;

    public CombinedIndexReader(CachingMultiReader[] indexReaders) throws IOException {
      super(indexReaders);
      this.subReaders = indexReaders;
      this.starts = new int[subReaders.length + 1];

      int maxDoc = 0;
      for (int i = 0; i < subReaders.length; i++) {
        starts[i] = maxDoc;
        maxDoc += subReaders[i].maxDoc();
      }
      starts[subReaders.length] = maxDoc;
    }

    /**
     * @inheritDoc
     */
    public int getParent(int n) throws IOException {
      int i = readerIndex(n);
      DocId id = subReaders[i].getParentDocId(n - starts[i]);
      id = id.applyOffset(starts[i]);
      return id.getDocumentNumber(this);
    }

    /**
     * Returns the reader index for document <code>n</code>. Implementation
     * copied from lucene MultiReader class.
     * 
     * @param n document number.
     * @return the reader index.
     */
    private int readerIndex(int n) {
      int lo = 0; // search starts array
      int hi = subReaders.length - 1; // for first element less

      while (hi >= lo) {
        int mid = (lo + hi) >> 1;
        int midValue = starts[mid];
        if (n < midValue) {
          hi = mid - 1;
        } else if (n > midValue) {
          lo = mid + 1;
        } else { // found a match
          while (mid + 1 < subReaders.length && starts[mid + 1] == midValue) {
            mid++; // scan to last match
          }
          return mid;
        }
      }
      return hi;
    }

  }

  // --------------------------< properties >----------------------------------

  /**
   * Sets the location of the search index.
   * 
   * @param path the location of the search index.
   */
  public void setPath(String path) {
    this.path = path;
  }

  /**
   * Returns the location of the search index. Returns <code>null</code> if
   * not set.
   * 
   * @return the location of the search index.
   */
  public String getPath() {
    return path;
  }

  /**
   * The lucene index writer property: useCompoundFile
   */
  public void setUseCompoundFile(boolean b) {
    useCompoundFile = b;
  }

  /**
   * Returns the current value for useCompoundFile.
   * 
   * @return the current value for useCompoundFile.
   */
  public boolean getUseCompoundFile() {
    return useCompoundFile;
  }

  /**
   * The lucene index writer property: minMergeDocs
   */
  public void setMinMergeDocs(int minMergeDocs) {
    this.minMergeDocs = minMergeDocs;
  }

  /**
   * Returns the current value for minMergeDocs.
   * 
   * @return the current value for minMergeDocs.
   */
  public int getMinMergeDocs() {
    return minMergeDocs;
  }

  /**
   * Sets the property: volatileIdleTime
   * 
   * @param volatileIdleTime idle time in seconds
   */
  public void setVolatileIdleTime(int volatileIdleTime) {
    this.volatileIdleTime = volatileIdleTime;
  }

  /**
   * Returns the current value for volatileIdleTime.
   * 
   * @return the current value for volatileIdleTime.
   */
  public int getVolatileIdleTime() {
    return volatileIdleTime;
  }

  /**
   * The lucene index writer property: maxMergeDocs
   */
  public void setMaxMergeDocs(int maxMergeDocs) {
    this.maxMergeDocs = maxMergeDocs;
  }

  /**
   * Returns the current value for maxMergeDocs.
   * 
   * @return the current value for maxMergeDocs.
   */
  public int getMaxMergeDocs() {
    return maxMergeDocs;
  }

  /**
   * The lucene index writer property: mergeFactor
   */
  public void setMergeFactor(int mergeFactor) {
    this.mergeFactor = mergeFactor;
  }

  /**
   * Returns the current value for the merge factor.
   * 
   * @return the current value for the merge factor.
   */
  public int getMergeFactor() {
    return mergeFactor;
  }

  /**
   * @see VolatileIndex#setBufferSize(int)
   */
  public void setBufferSize(int size) {
    bufferSize = size;
  }

  /**
   * Returns the current value for the buffer size.
   * 
   * @return the current value for the buffer size.
   */
  public int getBufferSize() {
    return bufferSize;
  }

  public void setRespectDocumentOrder(boolean docOrder) {
    documentOrder = docOrder;
  }

  public boolean getRespectDocumentOrder() {
    return documentOrder;
  }

  public void setForceConsistencyCheck(boolean b) {
    forceConsistencyCheck = b;
  }

  public boolean getForceConsistencyCheck() {
    return forceConsistencyCheck;
  }

  public void setAutoRepair(boolean b) {
    autoRepair = b;
  }

  public boolean getAutoRepair() {
    return autoRepair;
  }

  public void setCacheSize(int size) {
    cacheSize = size;
  }

  public int getCacheSize() {
    return cacheSize;
  }

  public void setMaxFieldLength(int length) {
    maxFieldLength = length;
  }

  public int getMaxFieldLength() {
    return maxFieldLength;
  }

  /**
   * @return Returns the index.
   */
  public IndexReader getIndexReader() throws IOException {
    return index.getIndexReader();
  }

  // ----------------------------< internal >----------------------------------

  /**
   * Checks if this <code>SearchIndex</code> is open, otherwise throws an
   * <code>IOException</code>.
   * 
   * @throws IOException if this <code>SearchIndex</code> had been closed.
   */
  private void checkOpen() throws IOException {
    if (closed) {
      throw new IOException("query handler closed and cannot be used anymore.");
    }
  }

  public LocationFactory getSysLocationFactory() {
    return sysLocationFactory;
  }
}
