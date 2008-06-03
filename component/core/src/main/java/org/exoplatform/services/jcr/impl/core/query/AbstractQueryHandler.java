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

import java.io.IOException;
import java.util.Iterator;

import javax.jcr.RepositoryException;
import org.exoplatform.services.jcr.datamodel.NodeData;

/**
 * Implements default behaviour for some methods of {@link QueryHandler}.
 */
public abstract class AbstractQueryHandler implements QueryHandler {
//
//  /**
//   * The context for this query handler.
//   */
//  private QueryHandlerContext context;
//
//  /**
//   * Initializes this query handler by setting all properties in this class with
//   * appropriate parameter values.
//   * 
//   * @param context the context for this query handler.
//   */
//  public final void init(QueryHandlerContext queryHandlerContext) throws IOException {
//    this.context = queryHandlerContext;
//  }
//
//  /**
//   * This method must be implemented by concrete sub classes and will be called
//   * from {@link #init}.
//   */
//  @Deprecated
//  protected abstract void doInit() throws IOException;
//
//  /**
//   * Returns the context for this query handler.
//   * 
//   * @return the <code>QueryHandlerContext</code> instance for this
//   *         <code>QueryHandler</code>.
//   */
//  public QueryHandlerContext getContext() {
//    return context;
//  }

//  /**
//   * This default implementation calls the individual
//   * {@link #deleteNode(NodeId)} and
//   * {@link #addNode(org.apache.jackrabbit.core.state.NodeState)} methods for
//   * each entry in the iterators. First the nodes to remove are processed then
//   * the nodes to add.
//   * 
//   * @param remove uuids of nodes to remove.
//   * @param add NodeStates to add.
//   * @throws RepositoryException if an error occurs while indexing a node.
//   * @throws IOException if an error occurs while updating the index.
//   */
//  public  void updateNodes(Iterator<String> remove, Iterator<NodeData> add) throws RepositoryException,
//                                                                                       IOException {
//    while (remove.hasNext()) {
//      deleteNode(remove.next());
//    }
//    while (add.hasNext()) {
//      addNode(add.next());
//    }
//
//  }
}
