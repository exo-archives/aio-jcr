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
package org.exoplatform.services.jcr.impl.core.query.sql;

import org.exoplatform.services.jcr.datamodel.InternalQName;

public class ASTContainsExpression extends SimpleNode {

    private String query;

    private InternalQName property;

    public ASTContainsExpression(int id) {
        super(id);
    }

    public ASTContainsExpression(JCRSQLParser p, int id) {
        super(p, id);
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public InternalQName getPropertyName() {
        return property;
    }

    public void setPropertyName(InternalQName property) {
        this.property = property;
    }

    /**
     * Accept the visitor. *
     */
    public Object jjtAccept(JCRSQLParserVisitor visitor, Object data) {
        return visitor.visit(this, data);
    }
}
