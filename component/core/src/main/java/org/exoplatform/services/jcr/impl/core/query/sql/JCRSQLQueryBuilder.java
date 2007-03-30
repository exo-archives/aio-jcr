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

import java.io.StringReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.WeakHashMap;

import javax.jcr.RepositoryException;
import javax.jcr.query.InvalidQueryException;

import org.apache.commons.logging.Log;
import org.exoplatform.commons.utils.ISO8601;
import org.exoplatform.services.jcr.datamodel.InternalQName;
import org.exoplatform.services.jcr.impl.Constants;
import org.exoplatform.services.jcr.impl.core.LocationFactory;
import org.exoplatform.services.jcr.impl.core.query.AndQueryNode;
import org.exoplatform.services.jcr.impl.core.query.LocationStepQueryNode;
import org.exoplatform.services.jcr.impl.core.query.NAryQueryNode;
import org.exoplatform.services.jcr.impl.core.query.NodeTypeQueryNode;
import org.exoplatform.services.jcr.impl.core.query.NotQueryNode;
import org.exoplatform.services.jcr.impl.core.query.OrQueryNode;
import org.exoplatform.services.jcr.impl.core.query.OrderQueryNode;
import org.exoplatform.services.jcr.impl.core.query.PathQueryNode;
import org.exoplatform.services.jcr.impl.core.query.QueryConstants;
import org.exoplatform.services.jcr.impl.core.query.QueryNode;
import org.exoplatform.services.jcr.impl.core.query.QueryRootNode;
import org.exoplatform.services.jcr.impl.core.query.RelationQueryNode;
import org.exoplatform.services.jcr.impl.core.query.TextsearchQueryNode;
import org.exoplatform.services.log.ExoLogger;

/**
 * Implements the query builder for the JCR SQL syntax.
 */
public class JCRSQLQueryBuilder implements JCRSQLParserVisitor {

    /**
     * logger instance for this class
     */
    private static Log log = ExoLogger.getLogger("jcr.JCRSQLQueryBuilder");

    /**
     * DateFormat pattern for type
     * {@link org.apache.jackrabbit.core.query.QueryConstants.TYPE_DATE}.
     */
    private static final String DATE_PATTERN = "yyyy-MM-dd";

    /**
     * Map of reusable JCRSQL parser instances indexed by NamespaceResolver.
     */
    private static Map parsers = new WeakHashMap();


    /**
     * The root node of the sql query syntax tree
     */
    private final ASTQuery stmt;

    /**
     * The root query node
     */
    private QueryRootNode root;

    /**
     * To resolve QNames
     */
//    private NamespaceResolver resolver;
    private LocationFactory locationFactory;

    /**
     * Query node to gather the constraints defined in the WHERE clause
     */
    private final AndQueryNode constraintNode = new AndQueryNode(null);

    /**
     * List of PathQueryNode constraints that need to be merged
     */
    private final List pathConstraints = new ArrayList();

    /**
     * Creates a new <code>JCRSQLQueryBuilder</code>.
     *
     * @param statement the root node of the SQL syntax tree.
     * @param resolver  a namespace resolver to use for names in the
     *                  <code>statement</code>.
     */
    private JCRSQLQueryBuilder(ASTQuery statement, LocationFactory locationFactory) {
        this.stmt = statement;
        this.locationFactory = locationFactory;
    }

    /**
     * Creates a <code>QueryNode</code> tree from a SQL <code>statement</code>.
     *
     * @param statement the SQL statement.
     * @param resolver  the namespace resolver to use.
     * @return the <code>QueryNode</code> tree.
     * @throws InvalidQueryException if <code>statement</code> is malformed.
     */
    public static QueryRootNode createQuery(String statement, LocationFactory locationFactory)
            throws InvalidQueryException {
        try {
          JCRSQLParser parser = new JCRSQLParser(new StringReader(statement));
          parser.setLocationfactory(locationFactory);
          JCRSQLQueryBuilder builder = new JCRSQLQueryBuilder(parser.Query(), locationFactory);
          return builder.getRootNode();
         
          
        } catch (ParseException e) {
            throw new InvalidQueryException(e.getMessage());
        } catch (IllegalArgumentException e) {
            throw new InvalidQueryException(e.getMessage());
        } catch (Throwable t) {
          t.printStackTrace();
            // javacc parser may also throw an error in some cases
            throw new InvalidQueryException(t.getMessage());
        }
    }

    /**
     * Creates a String representation of the query node tree in SQL syntax.
     *
     * @param root     the root of the query node tree.
     * @param resolver to resolve QNames.
     * @return a String representation of the query node tree.
     * @throws InvalidQueryException if the query node tree cannot be converted
     *                               into a String representation due to restrictions in SQL.
     */
    public static String toString(QueryRootNode root, LocationFactory locationFactory)
            throws InvalidQueryException {
        return QueryFormat.toString(root, locationFactory);
    }

    /**
     * Parses the statement and returns the root node of the <code>QueryNode</code>
     * tree.
     *
     * @return the root node of the <code>QueryNode</code> tree.
     */
    private QueryRootNode getRootNode() {
        if (root == null) {
            stmt.jjtAccept(this, null);
        }
        return root;
    }

    //----------------< JCRSQLParserVisitor >------------------------------------

    public Object visit(SimpleNode node, Object data) {
        // do nothing, should never be called actually
        return data;
    }

    public Object visit(ASTQuery node, Object data) {
        root = new QueryRootNode();
        root.setLocationNode(new PathQueryNode(root));

        // pass to select, from, where, ...
        node.childrenAccept(this, root);

        // use //* if no path has been set
        PathQueryNode pathNode = root.getLocationNode();
        pathNode.setAbsolute(true);
        if (pathConstraints.size() == 0) {
            pathNode.addPathStep(new LocationStepQueryNode(pathNode, null, true));
        } else {
            try {
                while (pathConstraints.size() > 1) {
                    // merge path nodes
                    MergingPathQueryNode path = null;
                    for (Iterator it = pathConstraints.iterator(); it.hasNext();) {
                        path = (MergingPathQueryNode) it.next();
                        if (path.needsMerge()) {
                            break;
                        } else {
                            path = null;
                        }
                    }
                    if (path == null) {
                        throw new IllegalArgumentException("Invalid combination of jcr:path clauses");
                    } else {
                        pathConstraints.remove(path);
                        MergingPathQueryNode[] paths = (MergingPathQueryNode[]) pathConstraints.toArray(new MergingPathQueryNode[pathConstraints.size()]);
                        paths = path.doMerge(paths);
                        pathConstraints.clear();
                        pathConstraints.addAll(Arrays.asList(paths));
                    }
                }
            } catch (NoSuchElementException e) {
                throw new IllegalArgumentException("Invalid combination of jcr:path clauses");
            }
            MergingPathQueryNode path = (MergingPathQueryNode) pathConstraints.get(0);
            LocationStepQueryNode[] steps = path.getPathSteps();
            for (int i = 0; i < steps.length; i++) {
                LocationStepQueryNode step = new LocationStepQueryNode(pathNode, steps[i].getNameTest(), steps[i].getIncludeDescendants());
                step.setIndex(steps[i].getIndex());
                pathNode.addPathStep(step);
            }
        }

        if (constraintNode.getNumOperands() > 0) {
            // attach constraint to last path step
            LocationStepQueryNode[] steps = pathNode.getPathSteps();
            steps[steps.length - 1].addPredicate(constraintNode);
        }

        return root;
    }

    public Object visit(ASTSelectList node, Object data) {
        final QueryRootNode root = (QueryRootNode) data;

        node.childrenAccept(new DefaultParserVisitor() {
            public Object visit(ASTIdentifier node, Object data) {
                root.addSelectProperty(node.getName());
                return data;
            }
        }, root);

        return data;
    }

    public Object visit(ASTFromClause node, Object data) {
        QueryRootNode root = (QueryRootNode) data;

        return node.childrenAccept(new DefaultParserVisitor() {
            public Object visit(ASTIdentifier node, Object data) {
                if (!node.getName().equals(Constants.NT_BASE)) {
                    // node is either primary or mixin node type
                    NodeTypeQueryNode nodeType
                            = new NodeTypeQueryNode(constraintNode, node.getName());
                    constraintNode.addOperand(nodeType);
                }
                return data;
            }
        }, root);
    }

    public Object visit(ASTWhereClause node, Object data) {
        return node.childrenAccept(this, constraintNode);
    }

    public Object visit(ASTPredicate node, Object data) {
        NAryQueryNode parent = (NAryQueryNode) data;

        int type = node.getOperationType();
        QueryNode predicateNode;

        try {
            final InternalQName[] tmp = new InternalQName[2];
            final ASTLiteral[] value = new ASTLiteral[1];
            node.childrenAccept(new DefaultParserVisitor() {
                public Object visit(ASTIdentifier node, Object data) {
                    if (tmp[0] == null) {
                        tmp[0] = node.getName();
                    } else if (tmp[1] == null) {
                        tmp[1] = node.getName();
                    }
                    return data;
                }

                public Object visit(ASTLiteral node, Object data) {
                    value[0] = node;
                    return data;
                }
            }, data);
            InternalQName identifier = tmp[0];

            if (identifier.equals(Constants.JCR_PATH)) {
                if (tmp[1] != null) {
                    // simply ignore, this is a join of a mixin node type
                } else {
                    createPathQuery(value[0].getValue(), parent.getType());
                }
                // done
                return data;
            }

            if (type == QueryConstants.OPERATION_BETWEEN) {
                AndQueryNode between = new AndQueryNode(parent);
                RelationQueryNode rel = createRelationQueryNode(between,
                        identifier, QueryConstants.OPERATION_GE_GENERAL, (ASTLiteral) node.children[1]);
                between.addOperand(rel);
                rel = createRelationQueryNode(between,
                        identifier, QueryConstants.OPERATION_LE_GENERAL, (ASTLiteral) node.children[2]);
                between.addOperand(rel);
                predicateNode = between;
            } else if (type == QueryConstants.OPERATION_GE_GENERAL
                    || type == QueryConstants.OPERATION_GT_GENERAL
                    || type == QueryConstants.OPERATION_LE_GENERAL
                    || type == QueryConstants.OPERATION_LT_GENERAL
                    || type == QueryConstants.OPERATION_NE_GENERAL
                    || type == QueryConstants.OPERATION_EQ_GENERAL) {
                predicateNode = createRelationQueryNode(parent,
                        identifier, type, value[0]);
            } else if (type == QueryConstants.OPERATION_LIKE) {
                ASTLiteral pattern = value[0];
                if (node.getEscapeString() != null) {
                    if (node.getEscapeString().length() == 1) {
                        // backslash is the escape character we use internally
                        pattern.setValue(translateEscaping(pattern.getValue(), node.getEscapeString().charAt(0), '\\'));
                    } else {
                        throw new IllegalArgumentException("ESCAPE string value must have length 1: '" + node.getEscapeString() + "'");
                    }
                } else {
                    // no escape character specified.
                    // if the pattern contains any backslash characters we need
                    // to escape them.
                    pattern.setValue(pattern.getValue().replaceAll("\\\\", "\\\\\\\\"));
                }
                predicateNode = createRelationQueryNode(parent,
                        identifier, type, pattern);
            } else if (type == QueryConstants.OPERATION_IN) {
                OrQueryNode in = new OrQueryNode(parent);
                for (int i = 1; i < node.children.length; i++) {
                    RelationQueryNode rel = createRelationQueryNode(in,
                            identifier, QueryConstants.OPERATION_EQ_VALUE, (ASTLiteral) node.children[i]);
                    in.addOperand(rel);
                }
                predicateNode = in;
            } else if (type == QueryConstants.OPERATION_NULL
                    || type == QueryConstants.OPERATION_NOT_NULL) {
                // create a dummy literal
                ASTLiteral star = new ASTLiteral(JCRSQLParserTreeConstants.JJTLITERAL);
                star.setType(QueryConstants.TYPE_STRING);
                star.setValue("%");
                predicateNode = createRelationQueryNode(parent,
                        identifier, type, star);
            } else {
                throw new IllegalArgumentException("Unknown operation type: " + type);
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new IllegalArgumentException("Too few arguments in predicate");
        }

        if (predicateNode != null) {
            parent.addOperand(predicateNode);
        }

        return data;
    }

    public Object visit(ASTOrExpression node, Object data) {
        NAryQueryNode parent = (NAryQueryNode) data;
        OrQueryNode orQuery = new OrQueryNode(parent);
        // pass to operands
        node.childrenAccept(this, orQuery);

        if (orQuery.getNumOperands() > 0) {
            parent.addOperand(orQuery);
        }
        return parent;
    }

    public Object visit(ASTAndExpression node, Object data) {
        NAryQueryNode parent = (NAryQueryNode) data;
        AndQueryNode andQuery = new AndQueryNode(parent);
        // pass to operands
        node.childrenAccept(this, andQuery);

        parent.addOperand(andQuery);
        return parent;
    }

    public Object visit(ASTNotExpression node, Object data) {
        NAryQueryNode parent = (NAryQueryNode) data;
        NotQueryNode notQuery = new NotQueryNode(parent);
        // pass to operand
        node.childrenAccept(this, notQuery);

        parent.addOperand(notQuery);
        return parent;
    }

    public Object visit(ASTBracketExpression node, Object data) {
        // bracket expression only has influence on how the syntax tree
        // is created.
        // simply pass on to children
        return node.childrenAccept(this, data);
    }

    public Object visit(ASTLiteral node, Object data) {
        // do nothing, should never be called actually
        return data;
    }

    public Object visit(ASTIdentifier node, Object data) {
        // do nothing, should never be called actually
        return data;
    }

    public Object visit(ASTOrderByClause node, Object data) {
        QueryRootNode root = (QueryRootNode) data;

        OrderQueryNode order = new OrderQueryNode(root);
        root.setOrderNode(order);
        node.childrenAccept(this, order);
        return root;
    }

    public Object visit(ASTOrderSpec node, Object data) {
        OrderQueryNode order = (OrderQueryNode) data;

        final InternalQName[] identifier = new InternalQName[1];

        // collect identifier
        node.childrenAccept(new DefaultParserVisitor() {
            public Object visit(ASTIdentifier node, Object data) {
                identifier[0] = node.getName();
                return data;
            }
        }, data);

        OrderQueryNode.OrderSpec spec = new OrderQueryNode.OrderSpec(identifier[0], true);
        order.addOrderSpec(spec);

        node.childrenAccept(this, spec);

        return data;
    }

    public Object visit(ASTAscendingOrderSpec node, Object data) {
        // do nothing ascending is default anyway
        return data;
    }

    public Object visit(ASTDescendingOrderSpec node, Object data) {
        OrderQueryNode.OrderSpec spec = (OrderQueryNode.OrderSpec) data;
        spec.setAscending(false);
        return data;
    }

    public Object visit(ASTContainsExpression node, Object data) {
        NAryQueryNode parent = (NAryQueryNode) data;
        parent.addOperand(new TextsearchQueryNode(parent, node.getQuery(), node.getPropertyName()));
        return parent;
    }

    //------------------------< internal >--------------------------------------

    /**
     * Creates a new {@link org.apache.jackrabbit.core.query.RelationQueryNode}.
     *
     * @param parent        the parent node for the created <code>RelationQueryNode</code>.
     * @param propertyName  the property name for the relation.
     * @param operationType the operation type.
     * @param literal       the literal value for the relation.
     * @return a <code>RelationQueryNode</code>.
     * @throws IllegalArgumentException if the literal value does not conform
     *                                  to its type. E.g. a malformed String representation of a date.
     */
    private RelationQueryNode createRelationQueryNode(QueryNode parent,
                                                      InternalQName propertyName,
                                                      int operationType,
                                                      ASTLiteral literal)
            throws IllegalArgumentException {

        String stringValue = literal.getValue();
        RelationQueryNode node = null;

        try {
            if (literal.getType() == QueryConstants.TYPE_DATE) {
                SimpleDateFormat format = new SimpleDateFormat(DATE_PATTERN);
                Date date = format.parse(stringValue);
                node = new RelationQueryNode(parent, propertyName, date, operationType);
            } else if (literal.getType() == QueryConstants.TYPE_DOUBLE) {
                double d = Double.parseDouble(stringValue);
                node = new RelationQueryNode(parent, propertyName, d, operationType);
            } else if (literal.getType() == QueryConstants.TYPE_LONG) {
                long l = Long.parseLong(stringValue);
                node = new RelationQueryNode(parent, propertyName, l, operationType);
            } else if (literal.getType() == QueryConstants.TYPE_STRING) {
                node = new RelationQueryNode(parent, propertyName, stringValue, operationType);
            } else if (literal.getType() == QueryConstants.TYPE_TIMESTAMP) {
                Calendar c = ISO8601.parse(stringValue);
                node = new RelationQueryNode(parent, propertyName, c.getTime(), operationType);
            }
        } catch (java.text.ParseException e) {
            throw new IllegalArgumentException(e.toString());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(e.toString());
        }

        if (node == null) {
            throw new IllegalArgumentException("Unknown type for literal: " + literal.getType());
        }
        return node;
    }

    /**
     * Creates <code>LocationStepQueryNode</code>s from a <code>path</code>.
     *
     * @param path      the path pattern
     * @param operation the type of the parent node
     */
    private void createPathQuery(String path, int operation) {
        MergingPathQueryNode pathNode = new MergingPathQueryNode(operation);
        pathNode.setAbsolute(true);

        if (path.equals("/")) {
            pathNode.addPathStep(new LocationStepQueryNode(pathNode, new InternalQName("", ""), false));
            pathConstraints.add(pathNode);
            return;
        }

        String[] names = path.split("/");

        for (int i = 0; i < names.length; i++) {
            if (names[i].length() == 0) {
                if (i == 0) {
                    // root
                    pathNode.addPathStep(new LocationStepQueryNode(pathNode, new InternalQName("", ""), false));
                } else {
                    // descendant '//' -> invalid path
                    // todo throw or ignore?
                    // we currently do not throw and add location step for an
                    // empty name (which is basically the root node)
                    pathNode.addPathStep(new LocationStepQueryNode(pathNode, new InternalQName("", ""), false));
                }
            } else {
                int idx = names[i].indexOf('[');
                String name;
                int index = LocationStepQueryNode.NONE;
                if (idx > -1) {
                    // contains index
                    name = names[i].substring(0, idx);
                    String suffix = names[i].substring(idx);
                    String indexStr = suffix.substring(1, suffix.length() - 1);
                    if (indexStr.equals("%")) {
                        // select all same name siblings
                        index = LocationStepQueryNode.NONE;
                    } else {
                        try {
                            index = Integer.parseInt(indexStr);
                        } catch (NumberFormatException e) {
                            log.warn("Unable to parse index for path element: " + names[i]);
                        }
                    }
                    if (name.equals("%")) {
                        name = null;
                    }
                } else {
                    // no index specified
                    // - index defaults to 1 if there is an explicit name test
                    // - index defaults to NONE if name test is %
                    name = names[i];
                    if (name.equals("%")) {
                        name = null;
                    } else {
                        index = 1;
                    }
                }
                InternalQName qName = null;
                if (name != null) {
                    try {
                        qName = locationFactory.parseJCRName(name).getInternalName();
                    } catch (RepositoryException e) {
                        throw new IllegalArgumentException("Illegal name: " + name);
                    } 
                    //catch (UnknownPrefixException e) {
                    //    throw new IllegalArgumentException("Unknown prefix: " + name);
                    //}
                }
                // if name test is % this means also search descendants
                boolean descendant = name == null;
                LocationStepQueryNode step = new LocationStepQueryNode(pathNode, qName, descendant);
                if (index > 0) {
                    step.setIndex(index);
                }
                pathNode.addPathStep(step);
            }
        }
        pathConstraints.add(pathNode);
    }

    /**
     * Translates a pattern using the escape character <code>from</code> into
     * a pattern using the escape character <code>to</code>.
     *
     * @param pattern the pattern to translate
     * @param from    the currently used escape character.
     * @param to      the new escape character to use.
     * @return the new pattern using the escape character <code>to</code>.
     */
    private static String translateEscaping(String pattern, char from, char to) {
        // if escape characters are the same OR pattern does not contain any
        // escape characters -> simply return pattern as is.
        if (from == to || (pattern.indexOf(from) < 0 && pattern.indexOf(to) < 0)) {
            return pattern;
        }
        StringBuffer translated = new StringBuffer(pattern.length());
        boolean escaped = false;
        for (int i = 0; i < pattern.length(); i++) {
            if (pattern.charAt(i) == from) {
                if (escaped) {
                    translated.append(from);
                    escaped = false;
                } else {
                    escaped = true;
                }
            } else if (pattern.charAt(i) == to) {
                if (escaped) {
                    translated.append(to).append(to);
                    escaped = false;
                } else {
                    translated.append(to).append(to);
                }
            } else {
                if (escaped) {
                    translated.append(to);
                    escaped = false;
                }
                translated.append(pattern.charAt(i));
            }
        }
        return translated.toString();
    }

    /**
     * Extends the <code>PathQueryNode</code> with merging capability. A
     * <code>PathQueryNode</code> <code>n1</code> can be merged with another
     * node <code>n2</code> in the following case:
     * <p/>
     * <code>n1</code> contains a location step at position <code>X</code> with
     * a name test that matches any node and has the descending flag set. Where
     * <code>X</code> &lt; number of location steps.
     * <code>n2</code> contains no location step to match any node name and
     * the sequence of name tests is the same as the sequence of name tests
     * of <code>n1</code>.
     * The merged node then contains a location step at position <code>X</code>
     * with the name test of the location step at position <code>X+1</code> and
     * the descending flag set.
     * <p/>
     * The following path patterns:<br/>
     * <code>/foo/%/bar</code> OR <code>/foo/bar</code><br/>
     * are merged into:<br/>
     * <code>/foo//bar</code>.
     * <p/>
     * The path patterns:<br/>
     * <code>/foo/%</code> AND NOT <code>/foo/%/%</code><br/>
     * are merged into:<br/>
     * <code>/foo/*</code>
     */
    private static class MergingPathQueryNode extends PathQueryNode {

        /**
         * The operation type of the parent node
         */
        private int operation;

        /**
         * Creates a new <code>MergingPathQueryNode</code> with the operation
         * tpye of a parent node. <code>operation</code> must be one of:
         * {@link org.apache.jackrabbit.core.query.QueryNode#TYPE_OR},
         * {@link org.apache.jackrabbit.core.query.QueryNode#TYPE_AND} or
         * {@link org.apache.jackrabbit.core.query.QueryNode#TYPE_NOT}.
         *
         * @param operation the operation type of the parent node.
         */
        MergingPathQueryNode(int operation) {
            super(null);
            if (operation != QueryNode.TYPE_OR && operation != QueryNode.TYPE_AND && operation != QueryNode.TYPE_NOT) {
                throw new IllegalArgumentException("operation");
            }
            this.operation = operation;
        }

        /**
         * Merges this node with a node from <code>nodes</code>. If a merge
         * is not possible an NoSuchElementException is thrown.
         *
         * @param nodes the nodes to try to merge with.
         * @return the merged array containing a merged version of this node.
         */
        MergingPathQueryNode[] doMerge(MergingPathQueryNode[] nodes) {
            if (operation == QueryNode.TYPE_OR) {
                return doOrMerge(nodes);
            } else {
                return doAndMerge(nodes);
            }
        }

        /**
         * Merges two nodes into a node which selects any child nodes of a
         * given node.
         * <p/>
         * Example:<br/>
         * The path patterns:<br/>
         * <code>/foo/%</code> AND NOT <code>/foo/%/%</code><br/>
         * are merged into:<br/>
         * <code>/foo/*</code>
         *
         * @param nodes the nodes to merge with.
         * @return the merged nodes.
         */
        private MergingPathQueryNode[] doAndMerge(MergingPathQueryNode[] nodes) {
            if (operation == QueryNode.TYPE_AND) {
                // check if there is an node with operation OP_AND_NOT
                MergingPathQueryNode n = null;
                for (int i = 0; i < nodes.length; i++) {
                    if (nodes[i].operation == QueryNode.TYPE_NOT) {
                        n = nodes[i];
                        nodes[i] = this;
                    }
                }
                if (n == null) {
                    throw new NoSuchElementException("Merging not possible with any node");
                } else {
                    return n.doAndMerge(nodes);
                }
            }
            // check if this node is valid as an operand
            if (operands.size() < 3) {
                throw new NoSuchElementException("Merging not possible");
            }
            int size = operands.size();
            LocationStepQueryNode n1 = (LocationStepQueryNode) operands.get(size - 1);
            LocationStepQueryNode n2 = (LocationStepQueryNode) operands.get(size - 2);
            if (n1.getNameTest() != null || n2.getNameTest() != null
                    || !n1.getIncludeDescendants() || !n2.getIncludeDescendants()) {
                throw new NoSuchElementException("Merging not possible");
            }
            // find a node to merge with
            MergingPathQueryNode matchedNode = null;
            for (int i = 0; i < nodes.length; i++) {
                if (nodes[i].operands.size() == operands.size() - 1) {
                    boolean match = true;
                    for (int j = 0; j < operands.size() - 1 && match; j++) {
                        LocationStepQueryNode step = (LocationStepQueryNode) operands.get(j);
                        LocationStepQueryNode other = (LocationStepQueryNode) nodes[i].operands.get(j);
                        match &= (step.getNameTest() == null) ? other.getNameTest() == null : step.getNameTest().equals(other.getNameTest());
                    }
                    if (match) {
                        matchedNode = nodes[i];
                        break;
                    }
                }
            }
            if (matchedNode == null) {
                throw new NoSuchElementException("Merging not possible with any node");
            }
            // change descendants flag to only match child nodes
            // that's the result of the merge.
            ((LocationStepQueryNode) matchedNode.operands.get(matchedNode.operands.size() - 1)).setIncludeDescendants(false);
            return nodes;
        }

        /**
         * Merges two nodes into one node selecting a node on the
         * descendant-or-self axis.
         * <p/>
         * Example:<br/>
         * The following path patterns:<br/>
         * <code>/foo/%/bar</code> OR <code>/foo/bar</code><br/>
         * are merged into:<br/>
         * <code>/foo//bar</code>.
         *
         * @param nodes the node to merge.
         * @return the merged nodes.
         */
        private MergingPathQueryNode[] doOrMerge(MergingPathQueryNode[] nodes) {
            // compact this
            MergingPathQueryNode compacted = new MergingPathQueryNode(QueryNode.TYPE_OR);
            for (Iterator it = operands.iterator(); it.hasNext();) {
                LocationStepQueryNode step = (LocationStepQueryNode) it.next();
                if (step.getIncludeDescendants() && step.getNameTest() == null) {
                    // check if has next
                    if (it.hasNext()) {
                        LocationStepQueryNode next = (LocationStepQueryNode) it.next();
                        next.setIncludeDescendants(true);
                        compacted.addPathStep(next);
                    } else {
                        compacted.addPathStep(step);
                    }
                } else {
                    compacted.addPathStep(step);
                }
            }

            MergingPathQueryNode matchedNode = null;
            for (int i = 0; i < nodes.length; i++) {
                // loop over the steps and compare the names
                if (nodes[i].operands.size() == compacted.operands.size()) {
                    boolean match = true;
                    Iterator compactedSteps = compacted.operands.iterator();
                    Iterator otherSteps = nodes[i].operands.iterator();
                    while (match && compactedSteps.hasNext()) {
                        LocationStepQueryNode n1 = (LocationStepQueryNode) compactedSteps.next();
                        LocationStepQueryNode n2 = (LocationStepQueryNode) otherSteps.next();
                        match &= (n1.getNameTest() == null) ? n2.getNameTest() == null : n1.getNameTest().equals(n2.getNameTest());
                    }
                    if (match) {
                        matchedNode = nodes[i];
                        break;
                    }
                }
            }
            if (matchedNode == null) {
                throw new NoSuchElementException("Merging not possible with any node.");
            }
            // construct new list
            List mergedList = new ArrayList(Arrays.asList(nodes));
            mergedList.remove(matchedNode);
            mergedList.add(compacted);
            return (MergingPathQueryNode[]) mergedList.toArray(new MergingPathQueryNode[mergedList.size()]);
        }

        /**
         * Returns <code>true</code> if this node needs merging; <code>false</code>
         * otherwise.
         *
         * @return <code>true</code> if this node needs merging; <code>false</code>
         *         otherwise.
         */
        boolean needsMerge() {
            for (Iterator it = operands.iterator(); it.hasNext();) {
                LocationStepQueryNode step = (LocationStepQueryNode) it.next();
                if (step.getIncludeDescendants() && step.getNameTest() == null) {
                    return true;
                }
            }
            return false;
        }
    }
}
