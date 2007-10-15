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
package org.exoplatform.services.jcr.impl.core.query.xpath;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

import javax.jcr.RepositoryException;
import javax.jcr.query.InvalidQueryException;

import org.exoplatform.commons.utils.ISO8601;
import org.exoplatform.services.jcr.datamodel.InternalQName;
import org.exoplatform.services.jcr.impl.Constants;
import org.exoplatform.services.jcr.impl.core.LocationFactory;
import org.exoplatform.services.jcr.impl.core.query.AndQueryNode;
import org.exoplatform.services.jcr.impl.core.query.DerefQueryNode;
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
import org.exoplatform.services.jcr.impl.util.ISO9075;

/**
 * Query builder that translates a XPath statement into a query tree structure.
 */
public class XPathQueryBuilder implements XPathVisitor, XPathTreeConstants {

    /**
     * InternalQName for 'fn:not'
     */
    static final InternalQName FN_NOT = new InternalQName(Constants.NS_FN_URI, "not");

    /**
     * InternalQName for 'not' as defined in XPath 1.0 (no prefix)
     */
    static final InternalQName FN_NOT_10 = new InternalQName("", "not");

    /**
     * InternalQName for true function.
     */
    static final InternalQName FN_TRUE = new InternalQName("", "true");

    /**
     * InternalQName for false function.
     */
    static final InternalQName FN_FALSE = new InternalQName("", "false");

    /**
     * InternalQName for position function.
     */
    static final InternalQName FN_POSITION = new InternalQName("", "position");

    /**
     * InternalQName for element function.
     */
    static final InternalQName FN_ELEMENT = new InternalQName("", "element");

    /**
     * InternalQName for the full position function including bracket
     */
    static final InternalQName FN_POSITION_FULL = new InternalQName("", "position()");

    /**
     * InternalQName for jcr:xmltext
     */
    static final InternalQName JCR_XMLTEXT = new InternalQName(Constants.NS_JCR_URI, "xmltext");

    /**
     * InternalQName for last function.
     */
    static final InternalQName FN_LAST = new InternalQName("", "last");

    /**
     * InternalQName for first function.
     */
    static final InternalQName FN_FIRST = new InternalQName("", "first");

    /**
     * InternalQName for xs:dateTime
     */
    static final InternalQName XS_DATETIME = new InternalQName(Constants.NS_XS_URI, "dateTime");

    /**
     * InternalQName for jcr:like
     */
    static final InternalQName JCR_LIKE = new InternalQName(Constants.NS_JCR_URI, "like");

    /**
     * InternalQName for jcr:deref
     */
    static final InternalQName JCR_DEREF = new InternalQName(Constants.NS_JCR_URI, "deref");

    /**
     * InternalQName for jcr:contains
     */
    static final InternalQName JCR_CONTAINS = new InternalQName(Constants.NS_JCR_URI, "contains");

    /**
     * InternalQName for jcr:root
     */
    static final InternalQName JCR_ROOT = new InternalQName(Constants.NS_JCR_URI, "root");

    /**
     * InternalQName for jcr:score
     */
    static final InternalQName JCR_SCORE = new InternalQName(Constants.NS_JCR_URI, "score");

    /**
     * String constant for operator 'eq'
     */
    private static final String OP_EQ = "eq";

    /**
     * String constant for operator 'ne'
     */
    private static final String OP_NE = "ne";

    /**
     * String constant for operator 'gt'
     */
    private static final String OP_GT = "gt";

    /**
     * String constant for operator 'ge'
     */
    private static final String OP_GE = "ge";

    /**
     * String constant for operator 'lt'
     */
    private static final String OP_LT = "lt";

    /**
     * String constant for operator 'le'
     */
    private static final String OP_LE = "le";

    /**
     * String constant for operator '='
     */
    private static final String OP_SIGN_EQ = "=";

    /**
     * String constant for operator '!='
     */
    private static final String OP_SIGN_NE = "!=";

    /**
     * String constant for operator '>'
     */
    private static final String OP_SIGN_GT = ">";

    /**
     * String constant for operator '>='
     */
    private static final String OP_SIGN_GE = ">=";

    /**
     * String constant for operator '<'
     */
    private static final String OP_SIGN_LT = "<";

    /**
     * String constant for operator '<='
     */
    private static final String OP_SIGN_LE = "<=";

    /**
     * Map of reusable XPath parser instances indexed by NamespaceResolver.
     */
    private static Map parsers = new WeakHashMap();

    /**
     * The root <code>QueryNode</code>
     */
    private final QueryRootNode root = new QueryRootNode();

    /**
     * The {@link NamespaceResolver} in use
     */
    //private final NamespaceResolver resolver;

    private final LocationFactory locationFactory;

    /**
     * List of exceptions that are created while building the query tree
     */
    private final List exceptions = new ArrayList();

    /**
     * Creates a new <code>XPathQueryBuilder</code> instance.
     *
     * @param statement the XPath statement.
     * @param resolver  the namespace resolver to use.
     * @throws InvalidQueryException if the XPath statement is malformed.
     */
    private XPathQueryBuilder(String statement, LocationFactory locationFactory)
            throws InvalidQueryException {
        this.locationFactory = locationFactory;
        try {
            // create an XQuery statement because we're actually using an
            // XQuery parser.
            statement = "for $v in " + statement + " return $v";
            // get parser
            XPath parser;
            synchronized (parsers) {
                parser = (XPath) parsers.get(locationFactory);
                if (parser == null) {
                    parser = new XPath(new StringReader(statement));
                    parsers.put(locationFactory, parser);
                }
            }

            SimpleNode query;
            // guard against concurrent use within same session
            synchronized (parser) {
                parser.ReInit(new StringReader(statement));
                query = parser.XPath2();
            }
            query.jjtAccept(this, root);
        } catch (ParseException e) {
          e.printStackTrace();
            throw new InvalidQueryException(e.getMessage(), e);
        } catch (Throwable t) {
            // also catch any other exception
            throw new InvalidQueryException(t.getMessage(), t);
        }
        if (exceptions.size() > 0) {
            // simply report the first one
            Exception e = (Exception) exceptions.get(0);
            if (e instanceof InvalidQueryException) {
                // just re-throw
                throw (InvalidQueryException) e;
            } else {
                // otherwise package
                throw new InvalidQueryException(e.getMessage(), e);
            }
        }
    }

    /**
     * Creates a <code>QueryNode</code> tree from a XPath statement.
     *
     * @param statement the XPath statement.
     * @param resolver  the namespace resolver to use.
     * @return the <code>QueryNode</code> tree for the XPath statement.
     * @throws InvalidQueryException if the XPath statement is malformed.
     */
    public static QueryRootNode createQuery(String statement,
        LocationFactory locationFactory)
            throws InvalidQueryException {
        QueryRootNode root = new XPathQueryBuilder(statement, locationFactory).getRootNode();
        return root;
    }

    /**
     * Creates a String representation of the query node tree in XPath syntax.
     *
     * @param root     the root of the query node tree.
     * @param resolver to resolve InternalQNames.
     * @return a String representation of the query node tree.
     * @throws InvalidQueryException if the query node tree cannot be converted
     *                               into a String representation due to restrictions in XPath.
     */
    public static String toString(QueryRootNode root, LocationFactory locationFactory)
            throws InvalidQueryException {
        return QueryFormat.toString(root, locationFactory);
    }

    /**
     * Returns the root node of the <code>QueryNode</code> tree.
     *
     * @return the root node of the <code>QueryNode</code> tree.
     */
    QueryRootNode getRootNode() {
        return root;
    }

    //---------------------< XPathVisitor >-------------------------------------

    /**
     * Implements the generic visit method for this <code>XPathVisitor</code>.
     *
     * @param node the current node as created by the XPath parser.
     * @param data the current <code>QueryNode</code> created by this
     *             <code>XPathVisitor</code>.
     * @return the current <code>QueryNode</code>. Can be different from
     *         <code>data</code>.
     */
    public Object visit(SimpleNode node, Object data) {
        QueryNode queryNode = (QueryNode) data;
        switch (node.getId()) {
            case JJTXPATH2:
                queryNode = createPathQueryNode(node);
                break;
            case JJTROOT:
            case JJTROOTDESCENDANTS:
                ((PathQueryNode) queryNode).setAbsolute(true);
                break;
            case JJTSTEPEXPR:
                if (isAttributeAxis(node)) {
                    if (queryNode.getType() == QueryNode.TYPE_RELATION
                            || queryNode.getType() == QueryNode.TYPE_DEREF 
                            || queryNode.getType() == QueryNode.TYPE_ORDER
                            || queryNode.getType() == QueryNode.TYPE_PATH
                            || queryNode.getType() == QueryNode.TYPE_TEXTSEARCH) {
                        // traverse
                        node.childrenAccept(this, queryNode);
                    } else if (queryNode.getType() == QueryNode.TYPE_NOT) {
                        // is null expression
                        RelationQueryNode isNull
                                = new RelationQueryNode(queryNode,
                                        RelationQueryNode.OPERATION_NULL);
                        node.childrenAccept(this, isNull);
                        NotQueryNode notNode = (NotQueryNode) queryNode;
                        NAryQueryNode parent = (NAryQueryNode) notNode.getParent();
                        parent.removeOperand(notNode);
                        parent.addOperand(isNull);
                    } else {
                        // not null expression
                        RelationQueryNode notNull
                                = new RelationQueryNode(queryNode,
                                        RelationQueryNode.OPERATION_NOT_NULL);
                        node.childrenAccept(this, notNull);
                        ((NAryQueryNode) queryNode).addOperand(notNull);
                    }
                } else {
                    if (queryNode.getType() == QueryNode.TYPE_PATH) {
                        queryNode = createLocationStep(node, (NAryQueryNode) queryNode);
                    } else if (queryNode.getType() == QueryNode.TYPE_TEXTSEARCH) {
                        // ignore
                    } else {
                        exceptions.add(new InvalidQueryException("Only attribute axis is allowed in predicate"));
                    }
                }
                break;
            case JJTNAMETEST:
                if (queryNode.getType() == QueryNode.TYPE_LOCATION
                        || queryNode.getType() == QueryNode.TYPE_DEREF
                        || queryNode.getType() == QueryNode.TYPE_RELATION
                        || queryNode.getType() == QueryNode.TYPE_TEXTSEARCH
                        || queryNode.getType() == QueryNode.TYPE_PATH) {
                    createNodeTest(node, queryNode);
                } else if (queryNode.getType() == QueryNode.TYPE_ORDER) {
                    createOrderSpec(node, (OrderQueryNode) queryNode);
                } else {
                    // traverse
                    node.childrenAccept(this, queryNode);
                }
                break;
            case JJTELEMENTNAMEORWILDCARD:
                if (queryNode.getType() == QueryNode.TYPE_LOCATION) {
                    SimpleNode child = (SimpleNode) node.jjtGetChild(0);
                    if (child.getId() != JJTANYNAME) {
                        createNodeTest(child, queryNode);
                    }
                }
                break;
            case JJTTEXTTEST:
                if (queryNode.getType() == QueryNode.TYPE_LOCATION) {
                    LocationStepQueryNode loc = (LocationStepQueryNode) queryNode;
                    loc.setNameTest(JCR_XMLTEXT);
                }
                break;
            case JJTTYPENAME:
                if (queryNode.getType() == QueryNode.TYPE_LOCATION) {
                    LocationStepQueryNode loc = (LocationStepQueryNode) queryNode;
                    String ntName = ((SimpleNode) node.jjtGetChild(0)).getValue();
                    try {
                        InternalQName nt = locationFactory.parseJCRName(ntName).getInternalName();
                        NodeTypeQueryNode nodeType = new NodeTypeQueryNode(loc, nt);
                        loc.addPredicate(nodeType);
                    } catch (RepositoryException e) {
                        exceptions.add(new InvalidQueryException("Invalid name: " + ntName));
                    }
                }
                break;
            case JJTOREXPR:
                NAryQueryNode parent = (NAryQueryNode) queryNode;
                queryNode = new OrQueryNode(parent);
                parent.addOperand(queryNode);
                // traverse
                node.childrenAccept(this, queryNode);
                break;
            case JJTANDEXPR:
                parent = (NAryQueryNode) queryNode;
                queryNode = new AndQueryNode(parent);
                parent.addOperand(queryNode);
                // traverse
                node.childrenAccept(this, queryNode);
                break;
            case JJTCOMPARISONEXPR:
                createExpression(node, (NAryQueryNode) queryNode);
                break;
            case JJTSTRINGLITERAL:
            case JJTDECIMALLITERAL:
            case JJTDOUBLELITERAL:
            case JJTINTEGERLITERAL:
                if (queryNode.getType() == QueryNode.TYPE_RELATION) {
                    assignValue(node, (RelationQueryNode) queryNode);
                } else if (queryNode.getType() == QueryNode.TYPE_LOCATION) {
                    if (node.getId() == JJTINTEGERLITERAL) {
                        int index = Integer.parseInt(node.getValue());
                        ((LocationStepQueryNode) queryNode).setIndex(index);
                    } else {
                        exceptions.add(new InvalidQueryException("LocationStep only allows integer literal as position index"));
                    }
                } else {
                    exceptions.add(new InvalidQueryException("Internal error: data is not a RelationQueryNode"));
                }
                break;
            case JJTUNARYMINUS:
                if (queryNode.getType() == QueryNode.TYPE_RELATION) {
                    ((RelationQueryNode) queryNode).setUnaryMinus(true);
                } else {
                    exceptions.add(new InvalidQueryException("Parse error: data is not a RelationQueryNode"));
                }
                break;
            case JJTFUNCTIONCALL:
                queryNode = createFunction(node, queryNode);
                break;
            case JJTORDERBYCLAUSE:
                root.setOrderNode(new OrderQueryNode(root));
                queryNode = root.getOrderNode();
                node.childrenAccept(this, queryNode);
                break;
            case JJTORDERMODIFIER:
                if (node.jjtGetNumChildren() > 0
                        && ((SimpleNode) node.jjtGetChild(0)).getId() == JJTDESCENDING) {
                    OrderQueryNode.OrderSpec[] specs = ((OrderQueryNode) queryNode).getOrderSpecs();
                    specs[specs.length - 1].setAscending(false);
                }
                break;
            case JJTPREDICATELIST:
                if (queryNode.getType() == QueryNode.TYPE_PATH) {
                    // switch to last location
                    QueryNode[] operands = ((PathQueryNode) queryNode).getOperands();
                    queryNode = operands[operands.length - 1];
                }
                node.childrenAccept(this, queryNode);
                break;
            default:
                // per default traverse
                node.childrenAccept(this, queryNode);
        }
        return queryNode;
    }

    //----------------------< internal >----------------------------------------

    /**
     * Creates a <code>LocationStepQueryNode</code> at the current position
     * in parent.
     *
     * @param node   the current node in the xpath syntax tree.
     * @param parent the parent <code>PathQueryNode</code>.
     * @return the created <code>LocationStepQueryNode</code>.
     */
    private LocationStepQueryNode createLocationStep(SimpleNode node, NAryQueryNode parent) {
        LocationStepQueryNode queryNode = null;
        boolean descendant = false;
        Node p = node.jjtGetParent();
        for (int i = 0; i < p.jjtGetNumChildren(); i++) {
            SimpleNode c = (SimpleNode) p.jjtGetChild(i);
            if (c == node) {
                queryNode = new LocationStepQueryNode(parent, null, descendant);
                parent.addOperand(queryNode);
                break;
            }
            descendant = (c.getId() == JJTSLASHSLASH
                    || c.getId() == JJTROOTDESCENDANTS);
        }
        node.childrenAccept(this, queryNode);

        return queryNode;
    }

    /**
     * Assigns a InternalQName to one of the follwing QueryNodes:
     * {@link RelationQueryNode}, {@link DerefQueryNode}, {@link RelationQueryNode},
     * {@link PathQueryNode}, {@link OrderQueryNode}, {@link TextsearchQueryNode}.
     *
     * @param node      the current node in the xpath syntax tree.
     * @param queryNode the query node.
     */
    private void createNodeTest(SimpleNode node, QueryNode queryNode) {
        if (node.jjtGetNumChildren() > 0) {
            SimpleNode child = (SimpleNode) node.jjtGetChild(0);
            if (child.getId() == JJTQNAME || child.getId() == JJTQNAMEFORITEMTYPE) {
                try {
                  InternalQName name = ISO9075.decode(locationFactory.parseJCRName(child.getValue()).getInternalName());
                    if (queryNode.getType() == QueryNode.TYPE_LOCATION) {
                        if (name.equals(JCR_ROOT)) {
                            name = new InternalQName("", "");
                        }
                        ((LocationStepQueryNode) queryNode).setNameTest(name);
                    } else if (queryNode.getType() == QueryNode.TYPE_DEREF) {
                        ((DerefQueryNode) queryNode).setRefProperty(name);
                    } else if (queryNode.getType() == QueryNode.TYPE_RELATION) {
                        ((RelationQueryNode) queryNode).setProperty(name);
                    } else if (queryNode.getType() == QueryNode.TYPE_PATH) {
                        root.addSelectProperty(name);
                    } else if (queryNode.getType() == QueryNode.TYPE_ORDER) {
                        root.getOrderNode().addOrderSpec(name, true);
                    } else if (queryNode.getType() == QueryNode.TYPE_TEXTSEARCH) {
                        ((TextsearchQueryNode) queryNode).setPropertyName(name);
                    }
                } catch (RepositoryException e) {
                    exceptions.add(new InvalidQueryException("Unknown prefix: " + child.getValue()));
                }
            } else if (child.getId() == JJTSTAR) {
                if (queryNode.getType() == QueryNode.TYPE_LOCATION) {
                    ((LocationStepQueryNode) queryNode).setNameTest(null);
                }
            } else {
                exceptions.add(new InvalidQueryException("Unsupported location for name test: " + child));
            }
        }
    }

    /**
     * Creates a new {@link org.apache.jackrabbit.core.query.RelationQueryNode}
     * with <code>queryNode</code> as its parent node.
     *
     * @param node      a comparison expression node.
     * @param queryNode the current <code>QueryNode</code>.
     */
    private void createExpression(SimpleNode node, NAryQueryNode queryNode) {
        if (node.getId() != JJTCOMPARISONEXPR) {
            throw new IllegalArgumentException("node must be of type ComparisonExpr");
        }
        // get operation type
        String opType = node.getValue();
        int type = 0;
        if (opType.equals(OP_EQ)) {
            type = RelationQueryNode.OPERATION_EQ_VALUE;
        } else if (opType.equals(OP_SIGN_EQ)) {
            type = RelationQueryNode.OPERATION_EQ_GENERAL;
        } else if (opType.equals(OP_GT)) {
            type = RelationQueryNode.OPERATION_GT_VALUE;
        } else if (opType.equals(OP_SIGN_GT)) {
            type = RelationQueryNode.OPERATION_GT_GENERAL;
        } else if (opType.equals(OP_GE)) {
            type = RelationQueryNode.OPERATION_GE_VALUE;
        } else if (opType.equals(OP_SIGN_GE)) {
            type = RelationQueryNode.OPERATION_GE_GENERAL;
        } else if (opType.equals(OP_LE)) {
            type = RelationQueryNode.OPERATION_LE_VALUE;
        } else if (opType.equals(OP_SIGN_LE)) {
            type = RelationQueryNode.OPERATION_LE_GENERAL;
        } else if (opType.equals(OP_LT)) {
            type = RelationQueryNode.OPERATION_LT_VALUE;
        } else if (opType.equals(OP_SIGN_LT)) {
            type = RelationQueryNode.OPERATION_LT_GENERAL;
        } else if (opType.equals(OP_NE)) {
            type = RelationQueryNode.OPERATION_NE_VALUE;
        } else if (opType.equals(OP_SIGN_NE)) {
            type = RelationQueryNode.OPERATION_NE_GENERAL;
        } else {
            exceptions.add(new InvalidQueryException("Unsupported ComparisonExpr type:" + node.getValue()));
        }

        RelationQueryNode rqn = new RelationQueryNode(queryNode, type);

        // traverse
        node.childrenAccept(this, rqn);

        // if property name is jcr:primaryType treat special
        if (Constants.JCR_PRIMARYTYPE.equals(rqn.getProperty())) {
            if (rqn.getValueType() == RelationQueryNode.TYPE_STRING) {
                try {
                    InternalQName ntName = locationFactory.parseJCRName(rqn.getStringValue()).getInternalName();
                    NodeTypeQueryNode ntNode = new NodeTypeQueryNode(queryNode, ntName);
                    queryNode.addOperand(ntNode);
                } catch (RepositoryException e) {
                    exceptions.add(new InvalidQueryException("Unknown prefix in name: " + rqn.getStringValue()));
                }
            } else {
                // value is not of type string
                exceptions.add(new InvalidQueryException("Invalid type: jcr:primaryType must be a string"));
            }
        } else {
            // property name is <> jcr:primaryType
            queryNode.addOperand(rqn);
        }
    }

    /**
     * Creates the primary path query node.
     *
     * @param node xpath node representing the root of the parsed tree.
     * @return
     */
    private PathQueryNode createPathQueryNode(SimpleNode node) {
        root.setLocationNode(new PathQueryNode(root));
        node.childrenAccept(this, root.getLocationNode());
        return root.getLocationNode();
    }

    /**
     * Assigns a value to the <code>queryNode</code>.
     *
     * @param node      must be of type string, decimal, double or integer; otherwise
     *                  an InvalidQueryException is added to {@link #exceptions}.
     * @param queryNode current node in the query tree.
     */
    private void assignValue(SimpleNode node, RelationQueryNode queryNode) {
        if (node.getId() == JJTSTRINGLITERAL) {
            queryNode.setStringValue(unescapeQuotes(node.getValue()));
        } else if (node.getId() == JJTDECIMALLITERAL) {
            queryNode.setDoubleValue(Double.parseDouble(node.getValue()));
        } else if (node.getId() == JJTDOUBLELITERAL) {
            queryNode.setDoubleValue(Double.parseDouble(node.getValue()));
        } else if (node.getId() == JJTINTEGERLITERAL) {
            // if this is an expression that contains position() do not change
            // the type.
            if (queryNode.getValueType() == QueryConstants.TYPE_POSITION) {
                queryNode.setPositionValue(Integer.parseInt(node.getValue()));
            } else {
                queryNode.setLongValue(Long.parseLong(node.getValue()));
            }
        } else {
            exceptions.add(new InvalidQueryException("Unsupported literal type:" + node.toString()));
        }
    }

    /**
     * Creates a function based on <code>node</code>.
     *
     * @param node      the function node from the xpath tree.
     * @param queryNode the current query node.
     * @return
     */
    private QueryNode createFunction(SimpleNode node, QueryNode queryNode) {
        // find out function name
        String fName = ((SimpleNode) node.jjtGetChild(0)).getValue();
        fName = fName.substring(0, fName.length() - 1);
        try {
            if (locationFactory.createJCRName(FN_NOT).getAsString().equals(fName)
                    || locationFactory.createJCRName(FN_NOT_10).getAsString().equals(fName)) {
                if (queryNode instanceof NAryQueryNode) {
                    QueryNode not = new NotQueryNode(queryNode);
                    ((NAryQueryNode) queryNode).addOperand(not);
                    // @todo is this needed?
                    queryNode = not;
                    // traverse
                    if (node.jjtGetNumChildren() == 2) {
                        node.jjtGetChild(1).jjtAccept(this, queryNode);
                    } else {
                        exceptions.add(new InvalidQueryException("fn:not only supports one expression argument"));
                    }
                } else {
                    exceptions.add(new InvalidQueryException("Unsupported location for function fn:not"));
                }
            } else if (locationFactory.createJCRName(XS_DATETIME).getAsString().equals(fName)) {
                // check arguments
                if (node.jjtGetNumChildren() == 2) {
                    if (queryNode instanceof RelationQueryNode) {
                        RelationQueryNode rel = (RelationQueryNode) queryNode;
                        SimpleNode literal = (SimpleNode) node.jjtGetChild(1).jjtGetChild(0);
                        if (literal.getId() == JJTSTRINGLITERAL) {
                            String value = literal.getValue();
                            // strip quotes
                            value = value.substring(1, value.length() - 1);
                            Calendar c = ISO8601.parse(value);
                            if (c == null) {
                                exceptions.add(new InvalidQueryException("Unable to parse string literal for xs:dateTime: " + value));
                            } else {
                                rel.setDateValue(c.getTime());
                            }
                        } else {
                            exceptions.add(new InvalidQueryException("Wrong argument type for xs:dateTime"));
                        }
                    } else {
                        exceptions.add(new InvalidQueryException("Unsupported location for function xs:dateTime"));
                    }
                } else {
                    // wrong number of arguments
                    exceptions.add(new InvalidQueryException("Wrong number of arguments for xs:dateTime"));
                }
            } else if (locationFactory.createJCRName(JCR_CONTAINS).getAsString().equals(fName)) {
                // check number of arguments
                if (node.jjtGetNumChildren() == 3) {
                    if (queryNode instanceof NAryQueryNode) {
                        SimpleNode literal = (SimpleNode) node.jjtGetChild(2).jjtGetChild(0);
                        if (literal.getId() == JJTSTRINGLITERAL) {
                            TextsearchQueryNode contains = new TextsearchQueryNode(queryNode,
                                    unescapeQuotes(literal.getValue()));
                            // assign property name
                            SimpleNode path = (SimpleNode) node.jjtGetChild(1);
                            path.jjtAccept(this, contains);
                            ((NAryQueryNode) queryNode).addOperand(contains);
                        } else {
                            exceptions.add(new InvalidQueryException("Wrong argument type for jcr:contains"));
                        }
                    }
                } else {
                    // wrong number of arguments
                    exceptions.add(new InvalidQueryException("Wrong number of arguments for jcr:contains"));
                }
            } else if (locationFactory.createJCRName(JCR_LIKE).getAsString().equals(fName)) {
                // check number of arguments
                if (node.jjtGetNumChildren() == 3) {
                    if (queryNode instanceof NAryQueryNode) {
                        RelationQueryNode like = new RelationQueryNode(queryNode, RelationQueryNode.OPERATION_LIKE);
                        ((NAryQueryNode) queryNode).addOperand(like);

                        // assign property name
                        node.jjtGetChild(1).jjtAccept(this, like);
                        // check property name
                        if (like.getProperty() == null) {
                            exceptions.add(new InvalidQueryException("Wrong first argument type for jcr:like"));
                        }

                        SimpleNode literal = (SimpleNode) node.jjtGetChild(2).jjtGetChild(0);
                        if (literal.getId() == JJTSTRINGLITERAL) {
                            like.setStringValue(unescapeQuotes(literal.getValue()));
                        } else {
                            exceptions.add(new InvalidQueryException("Wrong second argument type for jcr:like"));
                        }
                    } else {
                        exceptions.add(new InvalidQueryException("Unsupported location for function jcr:like"));
                    }
                } else {
                    // wrong number of arguments
                    exceptions.add(new InvalidQueryException("Wrong number of arguments for jcr:like"));
                }
            } else if (locationFactory.createJCRName(FN_TRUE).getAsString().equals(fName)) {
                if (queryNode.getType() == QueryNode.TYPE_RELATION) {
                    RelationQueryNode rel = (RelationQueryNode) queryNode;
                    rel.setStringValue("true");
                } else {
                    exceptions.add(new InvalidQueryException("Unsupported location for true()"));
                }
            } else if (locationFactory.createJCRName(FN_FALSE).getAsString().equals(fName)) {
                if (queryNode.getType() == QueryNode.TYPE_RELATION) {
                    RelationQueryNode rel = (RelationQueryNode) queryNode;
                    rel.setStringValue("false");
                } else {
                    exceptions.add(new InvalidQueryException("Unsupported location for false()"));
                }
            } else if (locationFactory.createJCRName(FN_POSITION).getAsString().equals(fName)) {
                if (queryNode.getType() == QueryNode.TYPE_RELATION) {
                    RelationQueryNode rel = (RelationQueryNode) queryNode;
                    if (rel.getOperation() == RelationQueryNode.OPERATION_EQ_GENERAL) {
                        // set dummy value to set type of relation query node
                        // will be overwritten when the tree is furhter parsed.
                        rel.setPositionValue(1);
                        rel.setProperty(FN_POSITION_FULL);
                    } else {
                        exceptions.add(new InvalidQueryException("Unsupported expression with position(). Only = is supported."));
                    }
                } else {
                    exceptions.add(new InvalidQueryException("Unsupported location for position()"));
                }
            } else if (locationFactory.createJCRName(FN_FIRST).getAsString().equals(fName)) {
                if (queryNode.getType() == QueryNode.TYPE_RELATION) {
                    ((RelationQueryNode) queryNode).setPositionValue(1);
                } else if (queryNode.getType() == QueryNode.TYPE_LOCATION) {
                    ((LocationStepQueryNode) queryNode).setIndex(1);
                } else {
                    exceptions.add(new InvalidQueryException("Unsupported location for first()"));
                }
            } else if (locationFactory.createJCRName(FN_LAST).getAsString().equals(fName)) {
                if (queryNode.getType() == QueryNode.TYPE_RELATION) {
                    ((RelationQueryNode) queryNode).setPositionValue(LocationStepQueryNode.LAST);
                } else if (queryNode.getType() == QueryNode.TYPE_LOCATION) {
                    ((LocationStepQueryNode) queryNode).setIndex(LocationStepQueryNode.LAST);
                } else {
                    exceptions.add(new InvalidQueryException("Unsupported location for last()"));
                }
            } else if (locationFactory.createJCRName(JCR_DEREF).getAsString().equals(fName)) {
                // check number of arguments
                if (node.jjtGetNumChildren() == 3) {
                    boolean descendant = false;
                    if (queryNode.getType() == QueryNode.TYPE_LOCATION) {
                        LocationStepQueryNode loc = (LocationStepQueryNode) queryNode;
                        // remember if descendant axis
                        descendant = loc.getIncludeDescendants();
                        queryNode = loc.getParent();
                        ((NAryQueryNode) queryNode).removeOperand(loc);
                    }
                    if (queryNode.getType() == QueryNode.TYPE_PATH) {
                        PathQueryNode pathNode = (PathQueryNode) queryNode;
                        DerefQueryNode derefNode = new DerefQueryNode(pathNode, null, false);

                        // assign property name
                        node.jjtGetChild(1).jjtAccept(this, derefNode);
                        // check property name
                        if (derefNode.getRefProperty() == null) {
                            exceptions.add(new InvalidQueryException("Wrong first argument type for jcr:deref"));
                        }

                        SimpleNode literal = (SimpleNode) node.jjtGetChild(2).jjtGetChild(0);
                        if (literal.getId() == JJTSTRINGLITERAL) {
                            String value = literal.getValue();
                            // strip quotes
                            value = value.substring(1, value.length() - 1);
                            if (!value.equals("*")) {
                                InternalQName name = null;
                                try {
                                    name = ISO9075.decode(locationFactory.parseJCRName(value).getInternalName());
                                } catch (RepositoryException e) {
                                    exceptions.add(new InvalidQueryException("Unknown prefix: " + value));
                                }
                                derefNode.setNameTest(name);
                            }
                        } else {
                            exceptions.add(new InvalidQueryException("Wrong second argument type for jcr:like"));
                        }

                        // check if descendant
                        if (!descendant) {
                            Node p = node.jjtGetParent();
                            for (int i = 0; i < p.jjtGetNumChildren(); i++) {
                                SimpleNode c = (SimpleNode) p.jjtGetChild(i);
                                if (c == node) {
                                    break;
                                }
                                descendant = (c.getId() == JJTSLASHSLASH
                                        || c.getId() == JJTROOTDESCENDANTS);
                            }
                        }
                        derefNode.setIncludeDescendants(descendant);
                        pathNode.addPathStep(derefNode);
                    } else {
                        exceptions.add(new InvalidQueryException("Unsupported location for jcr:deref()"));
                    }
                }
            } else if (locationFactory.createJCRName(JCR_SCORE).getAsString().equals(fName)) {
                if (queryNode.getType() == QueryNode.TYPE_ORDER) {
                    createOrderSpec(node, (OrderQueryNode) queryNode);
                } else {
                    exceptions.add(new InvalidQueryException("Unsupported location for jcr:score()"));
                }
            } else {
                exceptions.add(new InvalidQueryException("Unsupported function: " + fName));
            }
        } catch (RepositoryException e) {
            exceptions.add(e);
        }
        return queryNode;
    }

    private OrderQueryNode.OrderSpec createOrderSpec(SimpleNode node,
                                                     OrderQueryNode queryNode) {
        SimpleNode child = (SimpleNode) node.jjtGetChild(0);
        OrderQueryNode.OrderSpec spec = null;
        try {
            String propName = child.getValue();
            if (child.getId() == JJTQNAMELPAR) {
                // function name
                // cut off left parenthesis at end
                propName.substring(0, propName.length() - 1);
            }
            InternalQName name = ISO9075.decode(locationFactory.parseJCRName(propName).getInternalName());
            spec = new OrderQueryNode.OrderSpec(name, true);
            queryNode.addOrderSpec(spec);
        } catch (RepositoryException e) {
            exceptions.add(new InvalidQueryException("Unknown prefix: " + child.getValue()));
        }
        return spec;
    }

    /**
     * Returns true if <code>node</code> has a child node which is the attribute
     * axis.
     *
     * @param node a node with type {@link org.apache.jackrabbit.core.query.xpath.XPathTreeConstants.JJTSTEPEXPR}.
     * @return <code>true</code> if this step expression uses the attribute axis.
     */
    private boolean isAttributeAxis(SimpleNode node) {
        for (int i = 0; i < node.jjtGetNumChildren(); i++) {
            if (((SimpleNode) node.jjtGetChild(i)).getId() == JJTAT) {
                return true;
            }
        }
        return false;
    }

    /**
     * Unescapes single or double quotes depending on how <code>literal</code>
     * is enclosed and strips enclosing quotes.
     *
     * </p>
     * Examples:</br>
     * <code>"foo""bar"</code> -&gt; <code>foo"bar</code></br>
     * <code>'foo''bar'</code> -&gt; <code>foo'bar</code></br>
     * but:</br>
     * <code>'foo""bar'</code> -&gt; <code>foo""bar</code>
     *
     * @param literal the string literal to unescape
     * @return the unescaped and stripped literal.
     */
    private String unescapeQuotes(String literal) {
        String value = literal.substring(1, literal.length() - 1);
        if (value.length() == 0) {
            // empty string
            return value;
        }
        if (literal.charAt(0) == '"') {
            value = value.replaceAll("\"\"", "\"");
        } else {
            value = value.replaceAll("''", "'");
        }
        return value;
    }
}
