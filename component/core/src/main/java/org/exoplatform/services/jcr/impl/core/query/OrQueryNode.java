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
package org.exoplatform.services.jcr.impl.core.query;



/**
 * Implements a query node that defines an OR operation between arbitrary
 * other {@link QueryNode}s.
 */
public class OrQueryNode extends NAryQueryNode {

    /**
     * Creates a new <code>OrQueryNode</code> with a <code>parent</code>
     * query node.
     *
     * @param parent the parent of <code>this</code> <code>OrQueryNode</code>.
     */
    public OrQueryNode(QueryNode parent) {
        super(parent);
    }

    /**
     * Creates a new <code>OrQueryNode</code> with a <code>parent</code> query
     * node and <code>operands</code> for <code>this</code>
     * <code>OrQueryNode</code>.
     *
     * @param parent   the parent of <code>this</code> <code>OrQueryNode</code>.
     * @param operands the operands for this OR operation.
     */
    public OrQueryNode(QueryNode parent, QueryNode[] operands) {
        super(parent, operands);
    }

    /**
     * {@inheritDoc}
     */
    public Object accept(QueryNodeVisitor visitor, Object data) {
        return visitor.visit(this, data);
    }

    /**
     * Returns the type of this node.
     *
     * @return the type of this node.
     */
    public int getType() {
        return QueryNode.TYPE_OR;
    }

    /**
     * @inheritDoc
     */
    public boolean equals(Object obj) {
        if (obj instanceof OrQueryNode) {
            return super.equals(obj);
        }
        return false;
    }
}
