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
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.jcr.RepositoryException;

import org.apache.commons.logging.Log;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.exoplatform.services.jcr.datamodel.NodeData;
import org.exoplatform.services.jcr.impl.dataflow.persistent.WorkspacePersistentDataManager;
import org.exoplatform.services.log.ExoLogger;

/**
 * Implements a consistency check on the search index. Currently the following
 * checks are implemented:
 * <ul>
 * <li>Does not node exist in the ItemStateManager? If it does not exist
 * anymore the node is deleted from the index.</li>
 * <li>Is the parent of a node also present in the index? If it is not present it
 * will be indexed.</li>
 * <li>Is a node indexed multiple times? If that is the case, all occurrences
 * in the index for such a node are removed, and the node is re-indexed.</li>
 * </ul>
 */
class ConsistencyCheck {

    /**
     * Logger instance for this class
     */
    private static Log log = ExoLogger.getLogger("jcr.ConsistencyCheck");

    private WorkspacePersistentDataManager dataManager;
    /**
     * The index to check.
     */
    private final MultiIndex index;

    /**
     * All the documents within the index.
     */
    private Map documents;

    /**
     * List of all errors.
     */
    private final List errors = new ArrayList();

    /**
     * Private constructor.
     */
    private ConsistencyCheck(MultiIndex index, WorkspacePersistentDataManager dataManager) {
        this.index = index;
        this.dataManager = dataManager;
    }

    /**
     * Runs the consistency check on <code>index</code>.
     *
     * @param index the index to check.
     * @param mgr   the ItemStateManager from where to load content.
     * @return the consistency check with the results.
     * @throws IOException if an error occurs while checking.
     */
    static ConsistencyCheck run(MultiIndex index, WorkspacePersistentDataManager dataManager) throws IOException, RepositoryException {
        ConsistencyCheck check = new ConsistencyCheck(index, dataManager);
        check.run();
        return check;
    }

    /**
     * Repairs detected errors during the consistency check.
     * @param ignoreFailure if <code>true</code> repair failures are ignored,
     *   the repair continues without throwing an exception. If
     *   <code>false</code> the repair procedure is aborted on the first
     *   repair failure.
     * @throws IOException if a repair failure occurs.
     */
    void repair(boolean ignoreFailure) throws IOException {
        if (errors.size() == 0) {
            log.info("No errors found.");
            return;
        }
        int notRepairable = 0;
        for (Iterator it = errors.iterator(); it.hasNext(); ) {
            ConsistencyCheckError error = (ConsistencyCheckError) it.next();
            try {
                if (error.repairable()) {
                    error.repair();
                } else {
                    log.warn("Not repairable: " + error);
                    notRepairable++;
                }
            } catch (Exception e) {
                if (ignoreFailure) {
                  e.printStackTrace();
                    log.warn("Exception while reparing: " + e);
                } else {
                    if (!(e instanceof IOException)) {
                        e = new IOException(e.getMessage());
                    }
                    throw (IOException) e;
                }
            }
        }
        log.info("Repaired " + (errors.size() - notRepairable) + " errors.");
        if (notRepairable > 0) {
            log.warn("" + notRepairable + " error(s) not repairable.");
        }
    }

    /**
     * Returns the errors detected by the consistency check.
     * @return the errors detected by the consistency check.
     */
    List getErrors() {
        return new ArrayList(errors);
    }

    /**
     * Runs the consistency check.
     * @throws IOException if an error occurs while running the check.
     */
    private void run() throws IOException, RepositoryException {
        // Identifiers of multiple nodes in the index
        Set multipleEntries = new HashSet();
        // collect all documents
        documents = new HashMap();
        IndexReader reader = index.getIndexReader();
        try {
            for (int i = 0; i < reader.maxDoc(); i++) {
                if (reader.isDeleted(i)) {
                    continue;
                }
                Document d = reader.document(i);
                String identifier = d.get(FieldNames.UUID);
                if (dataManager.getItemData(identifier) != null) {
                    Document old = (Document) documents.put(identifier, d);
                    if (old != null) {
                        multipleEntries.add(identifier);
                    }
                } else {
                    errors.add(new NodeDeleted(identifier));
                }
            }
        } finally {
            reader.close();
        }

        // create multiple entries errors
        for (Iterator it = multipleEntries.iterator(); it.hasNext(); ) {
            errors.add(new MultipleEntries((String) it.next()));
        }

        // run through documents
        for (Iterator it = documents.values().iterator(); it.hasNext(); ) {
            Document d = (Document) it.next();
            String identifier = d.get(FieldNames.UUID);
            String parentIdentifier = d.get(FieldNames.PARENT);
            if (documents.containsKey(parentIdentifier) || parentIdentifier.length() == 0) {
                continue;
            }
            // parent is missing
            if (dataManager.getItemData(identifier) != null) {
                errors.add(new MissingAncestor(identifier, parentIdentifier));
            } else {
                errors.add(new UnknownParent(identifier, parentIdentifier));
            }
        }
    }

    /**
     * Returns the path for <code>node</code>. If an error occurs this method
     * returns the uuid of the node.
     *
     * @param node the node to retrieve the path from
     * @return the path of the node or its uuid.
     */
 /*   
    private String getPath(NodeState node) {
        // remember as fallback
        String uuid = node.getUUID();
        StringBuffer path = new StringBuffer();
        List elements = new ArrayList();
        try {
            while (node.getParentUUID() != null) {
                NodeId parentId = new NodeId(node.getParentUUID());
                NodeState parent = (NodeState) stateMgr.getItemState(parentId);
                NodeState.ChildNodeEntry entry = parent.getChildNodeEntry(node.getUUID());
                elements.add(entry);
                node = parent;
            }
            for (int i = elements.size() - 1; i > -1; i--) {
                NodeState.ChildNodeEntry entry = (NodeState.ChildNodeEntry) elements.get(i);
                path.append('/').append(entry.getName().getLocalName());
                if (entry.getIndex() > 1) {
                    path.append('[').append(entry.getIndex()).append(']');
                }
            }
            if (path.length() == 0) {
                path.append('/');
            }
            return path.toString();
        } catch (ItemStateException e) {
            return uuid;
        }
    }
*/
    //-------------------< ConsistencyCheckError classes >----------------------

    /**
     * One or more ancestors of an indexed node are not available in the index.
     */
    private class MissingAncestor extends ConsistencyCheckError {

        private final String parentIdentifier;

        private MissingAncestor(String identifier, String parentIdentifier) {
            super("Parent of " + identifier + " missing in index. Parent: " + parentIdentifier, identifier);
            this.parentIdentifier = parentIdentifier;
        }

        /**
         * Returns <code>true</code>.
         * @return <code>true</code>.
         */
        public boolean repairable() {
            return true;
        }

        /**
         * Repairs the missing node by indexing the missing ancestors.
         * @throws IOException if an error occurs while repairing.
         */
        public void repair() throws IOException {
            String pIdentifier = parentIdentifier;
            while (pIdentifier != null && !documents.containsKey(pIdentifier)) {
                try {
                    NodeData n = (NodeData) dataManager.getItemData(pIdentifier);
                    if (n != null) {
                      log.info("Reparing missing node " + n.getQPath().getAsString());
                      Document d = index.createDocument(n);
                      index.addDocument(d);
                      documents.put(n.getIdentifier(), d);
                      pIdentifier = n.getParentIdentifier();
                    } else
                      pIdentifier = null;
                } catch (RepositoryException e) {
                    throw new IOException(e.toString());
                }
            }
        }
    }

    /**
     * The parent of a node is not available through the ItemStateManager.
     */
    private class UnknownParent extends ConsistencyCheckError {

        private UnknownParent(String identifier, String parentIdentifier) {
            super("Node " + identifier + " has unknown parent: " + parentIdentifier, identifier);
        }

        /**
         * Not reparable (yet).
         * @return <code>false</code>.
         */
        public boolean repairable() {
            return false;
        }

        /**
         * No operation.
         */
        public void repair() throws IOException {
            log.warn("Unknown parent for " + identifier + " cannot be repaired");
        }
    }

    /**
     * A node is present multiple times in the index.
     */
    private class MultipleEntries extends ConsistencyCheckError {

        MultipleEntries(String identifier) {
            super("Multiple entries found for node " + identifier, identifier);
        }

        /**
         * Returns <code>true</code>.
         * @return <code>true</code>.
         */
        public boolean repairable() {
            return true;
        }

        /**
         * Removes the nodes with the identical identifiers from the index and
         * re-index the node.
         * @throws IOException if an error occurs while repairing.
         */
        public void repair() throws IOException {
            // first remove all occurrences
            Term id = new Term(FieldNames.UUID, identifier);
            index.removeAllDocuments(id);
            // then re-index the node
            try {
                NodeData node = (NodeData) dataManager.getItemData(identifier); 
                log.info("Re-indexing duplicate node occurrences in index: " + node.getQPath().getAsString());
                Document d = index.createDocument(node);
                index.addDocument(d);
                documents.put(node.getIdentifier(), d);
            } catch (RepositoryException e) {
                throw new IOException(e.toString());
            }
        }
    }

    /**
     * Indicates that a node has been deleted but is still in the index.
     */
    private class NodeDeleted extends ConsistencyCheckError {

        NodeDeleted(String identifier) {
            super("Node " + identifier + " does not longer exist.", identifier);
        }

        /**
         * Returns <code>true</code>.
         * @return <code>true</code>.
         */
        public boolean repairable() {
            return true;
        }

        /**
         * Deletes the nodes from the index.
         * @throws IOException if an error occurs while repairing.
         */
        public void repair() throws IOException {
            log.info("Removing deleted node from index: " + identifier);
            index.removeDocument(new Term(FieldNames.UUID, identifier));
        }
    }
}
