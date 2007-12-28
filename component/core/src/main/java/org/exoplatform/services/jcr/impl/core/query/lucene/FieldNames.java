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

/**
 * Defines field names that are used internally to store UUID, etc in the
 * search index.
 */
public class FieldNames {

    /**
     * Private constructor.
     */
    private FieldNames() {
    }

    /**
     * Name of the field that contains the UUID of the node. Terms are stored
     * but not tokenized.
     */
    public static final String UUID = "_:UUID".intern();

    /**
     * Name of the field that contains the fulltext index including terms
     * from all properties of a node. Terms are tokenized.
     */
    public static final String FULLTEXT = "_:FULLTEXT".intern();

    /**
     * Prefix for all field names that are fulltext indexed by property name.
     */
    public static final String FULLTEXT_PREFIX = "FULL:";

    /**
     * Name of the field that contains the UUID of the parent node. Terms are
     * stored and but not tokenized.
     */
    public static final String PARENT = "_:PARENT".intern();

    /**
     * Name of the field that contains the label of the node. Terms are not
     * tokenized.
     */
    public static final String LABEL = "_:LABEL".intern();

    /**
     * Name of the field that contains the names of multi-valued properties that
     * hold more than one value. Terms are not tokenized and not stored, only
     * indexed.
     */
    public static final String MVP = "_:MVP".intern();

    /**
     * Name of the field that contains all values of properties that are indexed
     * as is without tokenizing. Terms are prefixed with the property name.
     */
    public static final String PROPERTIES = "_:PROPERTIES".intern();

    /**
     * Returns a named value for use as a term in the index. The named
     * value is of the form: <code>fieldName</code> + '\uFFFF' + value
     *
     * @param fieldName the field name.
     * @param value the value.
     * @return value prefixed with field name.
     */
    public static String createNamedValue(String fieldName, String value) {
        return fieldName + '\uFFFF' + value;
    }

}
