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
package org.exoplatform.services.jcr.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import javax.jcr.RepositoryException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import org.apache.commons.logging.Log;
import org.apache.lucene.search.Query;

import org.exoplatform.services.jcr.datamodel.IllegalNameException;
import org.exoplatform.services.jcr.impl.Constants;
import org.exoplatform.services.jcr.impl.core.query.ErrorLog;
import org.exoplatform.services.jcr.impl.core.query.QueryHandler;
import org.exoplatform.services.jcr.impl.core.query.QueryHandlerContext;
import org.exoplatform.services.jcr.impl.core.query.QueryImpl;
import org.exoplatform.services.jcr.impl.core.query.lucene.DefaultHTMLExcerpt;
import org.exoplatform.services.jcr.impl.core.query.lucene.ExcerptProvider;
import org.exoplatform.services.jcr.impl.core.query.lucene.IndexingConfiguration;
import org.exoplatform.services.jcr.impl.core.query.lucene.IndexingConfigurationEntityResolver;
import org.exoplatform.services.jcr.impl.core.query.lucene.IndexingConfigurationImpl;
import org.exoplatform.services.jcr.impl.core.query.lucene.JcrStandartAnalyzer;
import org.exoplatform.services.jcr.impl.core.query.lucene.NamespaceMappings;
import org.exoplatform.services.jcr.impl.core.query.lucene.SearchIndex;
import org.exoplatform.services.jcr.impl.core.query.lucene.SpellChecker;
import org.exoplatform.services.jcr.impl.core.query.lucene.SynonymProvider;
import org.exoplatform.services.log.ExoLogger;

/**
 * Created by The eXo Platform SAS.
 * 
 * @author <a href="mailto:geaz@users.sourceforge.net">Gennady Azarenkov </a>
 * @version $Id: QueryHandlerEntry.java 14931 2008-05-29 15:02:08Z ksm $
 */

public class QueryHandlerEntry extends MappedParametrizedObjectEntry {
  /**
   * The default value for property {@link #extractorBackLog}.
   */
  public static final int  DEFAULT_EXTRACTOR_BACKLOG          = 100;

  /**
   * The default value for property {@link #extractorPoolSize}.
   */
  public static final int  DEFAULT_EXTRACTOR_POOLSIZE         = 0;

  /**
   * The default timeout in milliseconds which is granted to the text extraction
   * process until fulltext indexing is deferred to a background thread.
   */
  public static final long  DEFAULT_EXTRACTOR_TIMEOUT          = 100;

  /**
   * the default value for property {@link #maxFieldLength}.
   */
  public static final int  DEFAULT_MAX_FIELD_LENGTH           = 10000;

  /**
   * The default value for property {@link #maxMergeDocs}.
   */
  public static final int  DEFAULT_MAX_MERGE_DOCS             = Integer.MAX_VALUE;

  /**
   * the default value for property {@link #mergeFactor}.
   */
  public static final int  DEFAULT_MERGE_FACTOR               = 10;

  /**
   * The default value for property {@link #minMergeDocs}.
   */
  public static final int  DEFAULT_MIN_MERGE_DOCS             = 100;

  /**
   * Name of the file to persist search internal namespace mappings.
   */
  public static final String  NS_MAPPING_FILE                    = "ns_mappings.properties";                    // TODO

  /**
   * The excerpt provider class. Implements {@link ExcerptProvider}.
   */
  private static final String DEDAULT_EXCERPTPROVIDER_CLASS      = DefaultHTMLExcerpt.class.getName();

  private static final String DEDAULT_INDEXINGCONFIGURATIONCLASS = IndexingConfigurationImpl.class.getName();

  private static final boolean DEFAULT_AUTOREPAIR                 = true;

  private static final int DEFAULT_BUFFER_SIZE                = 10;

  private static final int DEFAULT_CACHE_SIZE                 = 1000;

  private final static boolean DEFAULT_CONSISTENCYCHECKENABLED    = false;

  private final static boolean DEFAULT_DOCUMENTORDER              = true;

  private final static boolean DEFAULT_FORCECONSISTENCYCHECK      = false;

  /**
   * Name of the default query implementation class.
   */
  private static final String DEFAULT_QUERY_HANDLER_CLASS        = SearchIndex.class.getName();

  /**
   * Name of the default query implementation class.
   */
  private static final String DEFAULT_QUERY_IMPL_CLASS           = QueryImpl.class.getName();

  /**
   * The number of documents that are pre fetched when a query is executed. <p/>
   * Default value is: {@link Integer#MAX_VALUE}.
   */
  private final static int DEFAULT_RESULTFETCHSIZE            = Integer.MAX_VALUE;

  private final static boolean DEFAULT_SUPPORTHIGHLIGHTING        = false;

  private final static boolean DEFAULT_USECOMPOUNDFILE            = false;

  private final static int DEFAULT_VOLATILEIDLETIME           = 3;

  /** The logger instance for this class */
  private static final Log    log                                = ExoLogger.getLogger(QueryHandlerEntry.class);

  private final static String PARAM_AUTOREPAIR                   = "autoRepair";

  private final static String PARAM_BUFFER_SIZE                  = "bufferSize";

  private final static String PARAM_CACHE_SIZE                   = "cacheSize";

  private final static String PARAM_CONSISTENCYCHECKENABLED      = "consistencyCheckEnabled";

  private final static String PARAM_DOCUMENTORDER                = "documentOrder";

  private final static String PARAM_EXCERPTPROVIDER_CLASS        = "excerptproviderClass";

  private final static String PARAM_EXCLUDEDNODEIDENTIFERS       = "excludedNodeIdentifers";

  private final static String PARAM_EXTRACTOR_BACKLOG            = "extractorBackLog";

  private final static String PARAM_EXTRACTOR_POOLSIZE           = "extractorPoolSize";

  private final static String PARAM_EXTRACTOR_TIMEOUT            = "extractorTimeout";

  private final static String PARAM_FORCECONSISTENCYCHECK        = "forceConsistencyCheck";
  
  //ErrorLog file size in Kb
  private final static String PARAM_ERRORLOG_SIZE                = "defaultErrorLogSize";

  /**
   * The location of the search index. <p/> Note: This is a <b>mandatory</b>
   * parameter!
   */
  private final static String PARAM_INDEXDIR                     = "indexDir";

  private final static String PARAM_INDEXINGCONFIGPATH           = "indexingConfigPath";

  private final static String PARAM_INDEXINGCONFIGURATIONCLASS   = "indexingConfigurationClass";

  private final static String PARAM_INDEXINGROOTIDENTIFER        = "indexingRootIdentifer";

  private final static String PARAM_MAXFIELDLENGTH               = "maxFieldLength";

  private final static String PARAM_MAXMERGEDOCS                 = "maxMergeDocs";

  private final static String PARAM_MERGEFACTOR                  = "mergeFactor";

  private final static String PARAM_MINMERGEDOCS                 = "minMergeDocs";

  private final static String PARAM_QUERYCLASS                   = "queryClass";

  private final static String PARAM_RESULTFETCHSIZE              = "resultFetchSize";

  private final static String PARAM_ROOT_NODE_ID                 = "rootNodeId";

  private final static String PARAM_SPELLCHECKERCLASS            = "spellCheckerClass";

  private final static String PARAM_SUPPORTHIGHLIGHTING          = "supportHighlighting";

  private final static String PARAM_SYNONYMPROVIDERCLASS         = "synonymProviderClass";

  private final static String PARAM_SYNONYMPROVIDERCONFIGPATH    = "synonymProviderConfigPath";

  private final static String PARAM_USECOMPOUNDFILE              = "useCompoundFile";

  private final static String PARAM_VOLATILEIDLETIME             = "volatileIdleTime";

  public QueryHandlerEntry    queryHandler;
  
  public Integer volatileIdleTime;

  /**
   * The analyzer we use for indexing.
   */
  private JcrStandartAnalyzer  analyzer;


  private String              queryHandlerClass                  = DEFAULT_QUERY_HANDLER_CLASS;

                                                                                                                 
  public QueryHandlerEntry() {
    super();
    this.analyzer = new JcrStandartAnalyzer();
  }

  public QueryHandlerEntry(String type, List params) {
    super(type, params);
    this.analyzer = new JcrStandartAnalyzer();
  }

  /**
   * Creates an excerpt provider for the given <code>query</code>.
   * 
   * @param query the query.
   * @return an excerpt provider for the given <code>query</code>.
   * @throws IOException if the provider cannot be created.
   */
  public ExcerptProvider createExcerptProvider(Query query) throws IOException {
    ExcerptProvider ep;
    try {
      Class excerptProviderClass = Class.forName(getExcerptProviderClass(),
                                                 true,
                                                 this.getClass().getClassLoader());
      ep = (ExcerptProvider) excerptProviderClass.newInstance();
    } catch (Exception e) {
      IOException ex = new IOException();
      ex.initCause(e);
      throw ex;
    }

    return ep;
  }

  /**
   * @param namespaceMappings The namespace mappings
   * @return the fulltext indexing configuration or <code>null</code> if there
   *         is no configuration.
   */
  public IndexingConfiguration createIndexingConfiguration(NamespaceMappings namespaceMappings,
                                                           QueryHandlerContext context) throws IOException,
                                                                                       RepositoryConfigurationException {
    Element docElement = getIndexingConfigurationDOM();
    if (docElement == null) {
      return null;
    }
    IndexingConfiguration idxCfg = null;
    try {
      Class indexingConfigurationClass = Class.forName(getIndexingConfigurationClass(),
                                                       true,
                                                       this.getClass().getClassLoader());
      idxCfg = (IndexingConfiguration) indexingConfigurationClass.newInstance();
      idxCfg.init(docElement, context, namespaceMappings);
    } catch (InstantiationException e) {
      log.warn("Exception initializing indexing configuration from: "
          + getIndexingConfigurationPath(), e);
    } catch (IllegalAccessException e) {
      log.warn("Exception initializing indexing configuration from: "
          + getIndexingConfigurationPath(), e);
    } catch (RepositoryException e) {
      log.warn("Exception initializing indexing configuration from: "
          + getIndexingConfigurationPath(), e);
    } catch (IllegalNameException e) {
      log.warn("Exception initializing indexing configuration from: "
          + getIndexingConfigurationPath(), e);
    } catch (ClassNotFoundException e) {
      log.warn("Exception initializing indexing configuration from: "
          + getIndexingConfigurationPath(), e);
    }
    return idxCfg;
  }

  /**
   * Creates a spell checker for this query handler.
   * 
   * @return the spell checker or <code>null</code> if none is configured or
   *         an error occurs.
   */
  public SpellChecker createSpellChecker(QueryHandler handler) {
    SpellChecker spCheck = null;
    if (getSpellCheckerClass() != null) {
      try {
        Class spellCheckerClass = Class.forName(getSpellCheckerClass(), true, this.getClass()
                                                                                  .getClassLoader());
        spCheck = (SpellChecker) spellCheckerClass.newInstance();
        spCheck.init(handler);
      } catch (Exception e) {
        log.warn("Exception initializing spell checker: " + getSpellCheckerClass(), e);
      }
    }
    return spCheck;
  }

  /**
   * @return the configured synonym provider or <code>null</code> if none is
   *         configured or an error occurs.
   */
  public SynonymProvider createSynonymProvider() {
    SynonymProvider sp = null;
    if (getSynonymProviderClass() != null) {
      try {
        Class synonymProviderClass = Class.forName(getSynonymProviderClass(),
                                                   true,
                                                   this.getClass().getClassLoader());
        sp = (SynonymProvider) synonymProviderClass.newInstance();
        sp.initialize(createSynonymProviderConfigResource());
      } catch (Exception e) {
        log.warn("Exception initializing synonym provider: " + getSynonymProviderClass(), e);
        sp = null;
      }
    }
    return sp;
  }

  public JcrStandartAnalyzer getAnalyzer() {
    return analyzer;
  }

  /**
   * If set <code>true</code> errors detected by the consistency check are
   * repaired. If <code>false</code> the errors are only reported in the log.
   * <p/> Default value is: <code>true</code>.
   */
  public boolean getAutoRepair() {
    return getParameterBoolean(PARAM_AUTOREPAIR, DEFAULT_AUTOREPAIR);
  }

  /**
   * Number of documents that are buffered before they are added to the index.
   */
  public int getBufferSize() {
    return getParameterInteger(PARAM_BUFFER_SIZE, DEFAULT_BUFFER_SIZE);
  }

  public int getCacheSize() {
    return getParameterInteger(PARAM_CACHE_SIZE, DEFAULT_CACHE_SIZE);
  }

  /**
   * Flag indicating whether document order is enable as the default ordering.
   */

  public boolean getDocumentOrder() {
    return getParameterBoolean(PARAM_DOCUMENTORDER, DEFAULT_DOCUMENTORDER);
  }

  /**
   * @return the class name of the excerpt provider implementation.
   */
  public String getExcerptProviderClass() {
    return getParameterValue(PARAM_EXCERPTPROVIDER_CLASS, DEDAULT_EXCERPTPROVIDER_CLASS);
  }

  public String getExcludedNodeIdentifers() {
    return getParameterValue(PARAM_EXCLUDEDNODEIDENTIFERS, null);
  }

  /**
   * @return the size of the extractor queue back log.
   */
  public int getExtractorBackLogSize() {
    return getParameterInteger(PARAM_EXTRACTOR_BACKLOG, DEFAULT_EXTRACTOR_BACKLOG);
  }

  /**
   * @return the size of the thread pool which is used to run the text
   *         extractors when binary content is indexed.
   */
  public int getExtractorPoolSize() {
    return getParameterInteger(PARAM_EXTRACTOR_POOLSIZE, DEFAULT_EXTRACTOR_POOLSIZE);

  }

  /**
   * @return the extractor timeout in milliseconds.
   */
  public long getExtractorTimeout() {
    return getParameterTime(PARAM_EXTRACTOR_TIMEOUT, DEFAULT_EXTRACTOR_TIMEOUT);
  }

  /**
   * Returns the location of the search index. Returns <code>null</code> if
   * not set.
   * 
   * @return the location of the search index.
   * @throws RepositoryConfigurationException
   */
  public String getIndexDir() throws RepositoryConfigurationException {
    return getParameterValue(PARAM_INDEXDIR);
  }

  /**
   * @return the class name of the indexing configuration implementation.
   */
  public String getIndexingConfigurationClass() {
    return getParameterValue(PARAM_INDEXINGCONFIGURATIONCLASS, DEDAULT_INDEXINGCONFIGURATIONCLASS);
  }

  /**
   * @return the path to the indexing configuration file.
   * @throws RepositoryConfigurationException
   */
  public String getIndexingConfigurationPath() {
    return getParameterValue(PARAM_INDEXINGCONFIGPATH, null);
  }

  public int getMaxFieldLength() {
    return getParameterInteger(PARAM_MAXFIELDLENGTH, DEFAULT_MAX_FIELD_LENGTH);
  }

  /**
   * Returns the current value for maxMergeDocs.
   * 
   * @return the current value for maxMergeDocs.
   */
  public int getMaxMergeDocs() {
    return getParameterInteger(PARAM_MAXMERGEDOCS, DEFAULT_MAX_MERGE_DOCS);
  }

  /**
   * Returns the current value for the merge factor.
   * 
   * @return the current value for the merge factor.
   */
  public int getMergeFactor() {
    return getParameterInteger(PARAM_MERGEFACTOR, DEFAULT_MERGE_FACTOR);
  }

  /**
   * Returns the current value for minMergeDocs.
   * 
   * @return the current value for minMergeDocs.
   */
  public int getMinMergeDocs() {
    return getParameterInteger(PARAM_MINMERGEDOCS, DEFAULT_MIN_MERGE_DOCS);
  }

  public String getQueryClass() {
    return getParameterValue(PARAM_QUERYCLASS, DEFAULT_QUERY_IMPL_CLASS);
  }

  public QueryHandlerEntry getQueryHandler() {
    return queryHandler;
  }

  /**
   * @return the number of results the query handler will fetch initially when a
   *         query is executed.
   */
  public int getResultFetchSize() {
    return getParameterInteger(PARAM_RESULTFETCHSIZE, DEFAULT_RESULTFETCHSIZE);
  }

  public String getRootNodeIdentifer() {
    return getParameterValue(PARAM_ROOT_NODE_ID, Constants.ROOT_UUID);
  }

  /**
   * @return the class name of the spell checker implementation or
   *         <code>null</code> if none is set.
   */
  public String getSpellCheckerClass() {
    return getParameterValue(PARAM_SPELLCHECKERCLASS, null);
  }



  /**
   * @return <code>true</code> if highlighting support is enabled.
   */
  public boolean getSupportHighlighting() {
    return getParameterBoolean(PARAM_SUPPORTHIGHLIGHTING, DEFAULT_SUPPORTHIGHLIGHTING);
  }

  /**
   * @return the class name of the synonym provider implementation or
   *         <code>null</code> if none is set.
   */
  public String getSynonymProviderClass() {
    return getParameterValue(PARAM_SYNONYMPROVIDERCLASS, null);
  }

  /**
   * @return the configuration path for the synonym provider. If none is set
   *         this method returns <code>null</code>.
   */
  public String getSynonymProviderConfigPath() {
    return getParameterValue(PARAM_SYNONYMPROVIDERCONFIGPATH, null);
  }

  /**
   * Returns the current value for useCompoundFile.
   * 
   * @return the current value for useCompoundFile.
   */
  public boolean getUseCompoundFile() {
    return getParameterBoolean(PARAM_USECOMPOUNDFILE, DEFAULT_USECOMPOUNDFILE);
  }

  /**
   * Returns the current value for volatileIdleTime.
   * 
   * @return the current value for volatileIdleTime.
   */
  public int getVolatileIdleTime() {
    if(volatileIdleTime == null)
      volatileIdleTime = getParameterInteger(PARAM_VOLATILEIDLETIME, DEFAULT_VOLATILEIDLETIME);
    
    return volatileIdleTime;
  }

  /**
   * If set <code>true</code> the index is checked for consistency depending
   * on the {@link #forceConsistencyCheck} parameter. If set to
   * <code>false</code>, no consistency check is performed, even if the redo
   * log had been applied on startup. <p/> Default value is: <code>false</code>.
   */
  public boolean isConsistencyCheckEnabled() {
    return getParameterBoolean(PARAM_CONSISTENCYCHECKENABLED,
                                         DEFAULT_CONSISTENCYCHECKENABLED);
  }


  public boolean isForceConsistencyCheck() {
    return getParameterBoolean(PARAM_FORCECONSISTENCYCHECK, DEFAULT_FORCECONSISTENCYCHECK);
  }

  /**
   * Creates a file system resource to the synonym provider configuration.
   * 
   * @return a file system resource or <code>null</code> if no path was
   *         configured.
   * @throws FileSystemException if an exception occurs accessing the file
   *           system.
   */
  protected InputStream createSynonymProviderConfigResource() {
    if (getSynonymProviderConfigPath() != null) {
      return this.getClass().getResourceAsStream(getSynonymProviderConfigPath());
    }
    return null;
  }

  /**
   * Returns the document element of the indexing configuration or
   * <code>null</code> if there is no indexing configuration.
   * 
   * @return the indexing configuration or <code>null</code> if there is none.
   * @throws RepositoryConfigurationException
   * @throws RepositoryConfigurationException
   * @throws ParserConfigurationException
   * @throws SAXException
   */
  protected Element getIndexingConfigurationDOM() throws IOException,
                                                 RepositoryConfigurationException {
    String indexingConfigPath = getIndexingConfigurationPath();
    Element indexingConfiguration = null;
    if (indexingConfigPath != null) {

      InputStream is = this.getClass().getResourceAsStream(indexingConfigPath);

      if (is == null)
        throw new IOException("Resource does not exist: " + indexingConfigPath);

      DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
      try {
        DocumentBuilder builder = factory.newDocumentBuilder();
        builder.setEntityResolver(new IndexingConfigurationEntityResolver());
        indexingConfiguration = builder.parse(is).getDocumentElement();
      } catch (ParserConfigurationException e) {
        throw new RepositoryConfigurationException(e.getLocalizedMessage(), e);
      } catch (SAXException e) {
        throw new RepositoryConfigurationException(e.getLocalizedMessage(), e);
      }
    }

    return indexingConfiguration;
  }

  /**
   * Return ErrorLog file size in Kb. String representation
   * @throws RepositoryConfigurationException
   */
  public int getErrorLogSize(){
    String size = getParameterValue(PARAM_ERRORLOG_SIZE, null);
    if((size==null)||(size.equals(""))){
      return ErrorLog.DEFAULT_FILE_SIZE;
    }else{
      return new Integer(size);
    }
  }


}