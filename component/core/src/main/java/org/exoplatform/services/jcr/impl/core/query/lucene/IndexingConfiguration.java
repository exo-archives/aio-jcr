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
package org.exoplatform.services.jcr.impl.core.query.lucene;

import javax.jcr.RepositoryException;

import org.w3c.dom.Element;

import org.apache.lucene.analysis.Analyzer;

import org.exoplatform.services.jcr.datamodel.IllegalNameException;
import org.exoplatform.services.jcr.datamodel.InternalQName;
import org.exoplatform.services.jcr.datamodel.NodeData;
import org.exoplatform.services.jcr.impl.core.query.QueryHandlerContext;

/**
 * <code>IndexingConfiguration</code> defines the interface to check whether a certain property is
 * indexed or not. This interface also provides access to aggregate rules. Those define how node
 * indexes are combined into an aggregate to form a single node index that can be queried.
 */
public interface IndexingConfiguration {

  /**
   * The default boost: 1.0f.
   */
  public static final float DEFAULT_BOOST = 1.0f;

  /**
   * Initializes the configuration.
   * 
   * @param config
   *          the document element of the configuration DOM.
   * @param context
   *          the context of the query handler.
   * @param namespaceMappings
   *          the namespaceMappings.
   * @throws IllegalNameException
   * @throws Exception
   *           if initialization fails.
   */
  public void init(Element config, QueryHandlerContext context, NamespaceMappings namespaceMappings) throws RepositoryException,
                                                                                                    IllegalNameException;

  /**
   * Returns the configured indexing aggregate rules or <code>null</code> if none exist. The caller
   * must not modify the returned array!
   * 
   * @return the configured rules or <code>null</code> if none exist.
   */
  public AggregateRule[] getAggregateRules();

  /**
   * Returns <code>true</code> if the property with the given name is indexed according to this
   * configuration.
   * 
   * @param state
   *          the node state.
   * @param propertyName
   *          the name of a property.
   * @return <code>true</code> if the property is indexed; <code>false</code> otherwise.
   */
  boolean isIndexed(NodeData state, InternalQName propertyName);

  /**
   * Returns <code>true</code> if the property with the given name should be included in the node
   * scope fulltext index. If there is not configuration entry for that propery <code>false</code>
   * is returned.
   * 
   * @param state
   *          the node state.
   * @param propertyName
   *          the name of a property.
   * @return <code>true</code> if the property should be included in the node scope fulltext index.
   */
  boolean isIncludedInNodeScopeIndex(NodeData state, InternalQName propertyName);

  /**
   * Returns the boost value for the given property name. If there is no configuration entry for the
   * property name the {@link #DEFAULT_BOOST} is returned.
   * 
   * @param state
   *          the node state.
   * @param propertyName
   *          the name of a property.
   * @return the boost value for the property.
   */
  float getPropertyBoost(NodeData state, InternalQName propertyName);

  /**
   * Returns the boost for the node scope fulltext index field.
   * 
   * @param state
   *          the node state.
   * @return the boost for the node scope fulltext index field.
   */
  float getNodeBoost(NodeData state);

  /**
   * Returns the analyzer configured for the property with this fieldName (the string representation
   * ,JCR-style name, of the given <code>Name</code> prefixed with
   * <code>FieldNames.FULLTEXT_PREFIX</code>), and <code>null</code> if none is configured, or the
   * configured analyzer cannot be found. If <code>null</code> is returned, the default Analyzer is
   * used.
   * 
   * @param fieldName
   *          the string representation ,JCR-style name, of the given <code>Name</code>, prefixed
   *          with <code>FieldNames.FULLTEXT_PREFIX</code>)
   * @return the <code>analyzer</code> to use for indexing this property
   */
  Analyzer getPropertyAnalyzer(String fieldName);

}
