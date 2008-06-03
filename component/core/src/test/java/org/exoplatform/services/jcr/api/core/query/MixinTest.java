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
package org.exoplatform.services.jcr.api.core.query;

import javax.jcr.RepositoryException;
import javax.jcr.Node;

import org.exoplatform.services.jcr.core.nodetype.ExtendedNodeTypeManager;
import org.exoplatform.services.jcr.datamodel.InternalQName;
import org.exoplatform.services.jcr.impl.core.SessionImpl;
import org.exoplatform.services.jcr.impl.core.nodetype.NodeTypeManagerImpl;

// import org.apache.jackrabbit.api.JackrabbitNodeTypeManager;

import java.io.ByteArrayInputStream;
import java.util.Calendar;

/**
 * Tests if mixin types are queried correctly when using element test: element()
 */
public class MixinTest extends AbstractQueryTest {

  protected void setUp() throws Exception {
    super.setUp();
    ExtendedNodeTypeManager manager = (ExtendedNodeTypeManager) superuser.getWorkspace()
                                                                         .getNodeTypeManager();

    String cnd = "<nodeTypes><nodeType name='test:referenceable' isMixin='true' hasOrderableChildNodes='false' primaryItemName=''>"
        + "<supertypes>"
        + "     <supertype>mix:referenceable</supertype>"
        + "</supertypes>"
        + "</nodeType>"
        +"</nodeTypes>";

    manager.registerNodeTypes(new ByteArrayInputStream(cnd.getBytes()),
                              ExtendedNodeTypeManager.IGNORE_IF_EXISTS);
  }

  public void testBuiltInMixin() throws RepositoryException {
    // nt:resoure is referenceable by its node type definition
    Node n1 = testRootNode.addNode("n1", "nt:resource");
    n1.setProperty("jcr:data", new ByteArrayInputStream("hello world".getBytes()));
    n1.setProperty("jcr:lastModified", Calendar.getInstance());
    n1.setProperty("jcr:mimeType", "application/octet-stream");

    // assign mix:referenceable to arbitrary node
    Node n2 = testRootNode.addNode("n2");
    n2.addMixin("mix:referenceable");

    // make node referenceable using a mixin that extends from mix:referenceable
    Node n3 = testRootNode.addNode("n3");
    n3.addMixin("test:referenceable");

    testRootNode.save();

    String query = testPath + "//element(*, mix:referenceable)";
    executeXPathQuery(query, new Node[] { n1, n2, n3 });
  }

}
