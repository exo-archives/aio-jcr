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

import java.io.IOException;

import org.exoplatform.services.jcr.impl.core.query.QueryHandler;
import org.exoplatform.services.jcr.impl.core.query.QueryRootNode;

/**
 * <code>SpellChecker</code> defines an interface to run a spellchecker over a fulltext query
 * statement.
 */
public interface SpellChecker {

  /**
   * Initializes this spell checker.
   * 
   * @param handler
   *            the query handler that created this spell checker.
   * @param minDistance
   *            minimal distance between  word and proposed close word. Float value 0..1.
   * @param morePopular
   *            return only the suggest words that are as frequent or more frequent than the searched word 
   * @throws IOException
   *             if an error occurs while initializing the spell checker.
   */
  public void init(QueryHandler handler, float minDistance, boolean morePopular) throws IOException;

  /**
   * Runs the spell checker over the first spellcheck relation query node in the abstract query tree
   * and returns a suggestion in case this spellchecker thinks the words are misspelled. If the
   * spellchecker determines that the words are spelled correctly <code>null</code> is returned.
   * 
   * @param aqt
   *          the abstract query tree, which may contain a relation query node with a spellcheck
   *          operation.
   * @return a suggestion or <code>null</code> if this spell checker determines that the fulltext
   *         query statement is spelled correctly.
   */
  public String check(QueryRootNode aqt) throws IOException;

  /**
   * Closes this spell checker and allows it to free resources.
   */
  public void close();
}
