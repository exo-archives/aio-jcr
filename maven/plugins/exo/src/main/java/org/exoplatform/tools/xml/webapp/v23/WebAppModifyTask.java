/***************************************************************************
 * Copyright 2001-2004 The eXo Platform SARL         All rights reserved.  *
 * Please visit http://www.exoplatform.org for more license detail.        *
 **************************************************************************/

package org.exoplatform.tools.xml.webapp.v23;

import org.exoplatform.tools.xml.XMLModificationTask;
import org.jaxen.JaxenException;
import org.jaxen.XPath;
import org.jaxen.dom.DOMXPath;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

/**
 * If you plan to write a class to modify the web.xml (web application deployment
 * descriptor file) then extends this class as it provides a template method pattern
 * (inhereted from XMLModificationTask), it also contains some convenient methods to
 * locate certain web.xml elements that subclasses might find useful. Just for convenient
 * here is the web-app 2.3 DTD:
 * 
 * <p>
 * &lt;!ELEMENT web-app (icon?, display-name?, description?, distributable?,
 * context-param, filter, filter-mapping, listener, servlet, servlet-mapping,
 * session-config?, mime-mapping, welcome-file-list?, error-page, taglib,
 * resource-env-ref, resource-ref, security-constraint, login-config?, security-role,
 * env-entry, ejb-ref, ejb-local-ref)&gt;
 * </p>
 * 
 * @author Hatim Khan
 * @version $Revision: 1.1 $
 */
public abstract class WebAppModifyTask extends XMLModificationTask
{
  public static final String CLASS_VERSION = "$Id: WebAppModifyTask.java,v 1.1 2004/04/19 03:45:49 hatimk Exp $"; //$NON-NLS-1$
  private Map mCachedXPaths = new HashMap();
  private Map mCachedXPathsResult = new HashMap();
  protected static final String[] WEBAPP_CHILD_ELEMENTS = { "icon", "display-name", "description",
      "distributable", "context-param", "filter", "filter-mapping", "listener", "servlet", "servlet-mapping",
      "session-config", "mime-mapping", "welcome-file-list", "error-page", "taglib", "resource-env-ref",
      "resource-ref", "security-constraint", "login-config", "security-role", "env-entry", "ejb-ref",
      "ejb-local-ref" };
  protected static final String[] SERVLET_CHILD_ELEMENTS = { "icon", "servlet-name", "display-name",
      "description", "servlet-class", "init-param", "load-on-startup", "run-as", "security-role-ref" };
  protected static final String[] SERVLETMAPPING_CHILD_ELEMENTS = { "servlet-name", "url-pattern" };

  protected void insertElement(Node pParent, Node pNewNode, String[] pElementDTD) throws JaxenException
  {
    String elementName = pNewNode.getNodeName();
    Node closestNode = getClosestSiblingAccordingToDTD(pParent, elementName, pElementDTD);

    Node parent = pParent;
    Node sibling = null;

    if (closestNode != null)
    {
      sibling = closestNode.getNextSibling();
    }
    else
    {
      if (parent.hasChildNodes())
      {
        sibling = parent.getFirstChild();
      }
    }

    if (sibling != null)
    {
      parent.insertBefore(pNewNode, sibling);
    }
    else
    {
      parent.appendChild(pNewNode);
    }
  }

  protected Node getClosestSiblingAccordingToDTD(Node pParent, String pElement, String[] pElementDTD)
      throws JaxenException
  {
    Node result = null;
    List siblingList = buildResultTableUpToElement(pParent, pElement, pElementDTD);
    if (siblingList == null)
    {
      return result;
    }

    ListIterator iterator = siblingList.listIterator(siblingList.size());
    while (iterator.hasPrevious())
    {
      String elementName = (String) iterator.previous();
      Node siblingNode = (Node) mCachedXPathsResult.get(elementName);
      if (siblingNode != null)
      {
        result = siblingNode;

        break;
      }
    }

    return result;
  }

  private List getChildElementNames(String[] pElements)
  {
    int size = pElements.length;
    List result = new ArrayList(size);
    for (int i = 0; i < size; ++i)
    {
      result.add(pElements[i]);
    }

    return result;
  }

  private XPath getCachedXPath(String pElementName) throws JaxenException
  {
    XPath xpath = (XPath) mCachedXPaths.get(pElementName);
    if (xpath == null)
    {
      xpath = new DOMXPath(pElementName + "[last()]");
    }
    mCachedXPaths.put(pElementName, xpath);

    return xpath;
  }

  private List buildResultTableUpToElement(Node pParent, String pElementName, String[] pElementDTD)
      throws JaxenException
  {
    // given element will not be included in the result
    List result = new ArrayList();
    List all = getChildElementNames(pElementDTD);
    int index = all.indexOf(pElementName);
    if (index < 0)
    {
      return result;
    }

    List sublist = all.subList(0, index);
    Iterator iterator = sublist.iterator();
    while (iterator.hasNext())
    {
      String elementName = (String) iterator.next();
      XPath xpath = getCachedXPath(elementName);
      mCachedXPathsResult.put(elementName, xpath.selectSingleNode(pParent));
      result.add(elementName);
    }

    return result;
  }

  protected Node getWebAppNode(Document pDoc) throws JaxenException
  {
    Node result = null;
    XPath webappXPath = new DOMXPath("/web-app");
    result = (Node) webappXPath.selectSingleNode(pDoc);

    return result;
  }
}