/***************************************************************************
 * Copyright 2001-2004 The eXo Platform SARL         All rights reserved.  *
 * Please visit http://www.exoplatform.org for more license detail.        *
 **************************************************************************/

package org.exoplatform.tools.xml.webapp.v23;

import org.jaxen.JaxenException;
import org.jaxen.XPath;
import org.jaxen.dom.DOMXPath;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

/**
 * This task inspects the current value of the display-name element in web.xml and
 * modifies it (if needed), so that the value matches the given context root name. If
 * there is no display-name element, it will insert one in the correct position according
 * to the web app DTD (see http://java.sun.com/dtd/web-app_2_3.dtd)
 * 
 * @author Hatim Khan
 * @version $Revision: 1.1 $
 */
public class WebAppDisplayNameModifyTask extends WebAppModifyTask
{
  public static final String CLASS_VERSION = "$Id: WebAppDisplayNameModifyTask.java,v 1.1 2004/04/19 03:45:49 hatimk Exp $"; //$NON-NLS-1$
  private XPath mDisplayNameXPath;
  private String mContextRootName;

  /**
   * The constructor method.
   * 
   * @param pContextRootName the context root name
   */
  public WebAppDisplayNameModifyTask(String pContextRootName)
  {
    mContextRootName = pContextRootName;
  }

  /**
   * @see org.exoplatform.eclipse.core.xml.XMLModificationTask#detect(org.w3c.dom.Document)
   */
  protected int detect(Document pDoc) throws JaxenException
  {
    Node displayNameNode = getDisplayNameNode(pDoc);
    if (displayNameNode == null)
    {
      return INSERT;
    }

    String displayNameValue = getTextContent(displayNameNode);
    if ((displayNameValue != null) && (displayNameValue.equals(mContextRootName)))
    {
      return NOMODIFICATION;
    }
    else
    {
      return REPLACE;
    }
  }

  /**
   * @see org.exoplatform.eclipse.core.xml.XMLModificationTask#insert(org.w3c.dom.Document)
   */
  protected void insert(Document pDoc) throws JaxenException
  {
    Node parent = getWebAppNode(pDoc);
    insertElement(parent, createDisplayNameNode(pDoc), WEBAPP_CHILD_ELEMENTS);
  }

  /**
   * @see org.exoplatform.eclipse.core.xml.XMLModificationTask#replace(org.w3c.dom.Document)
   */
  protected void replace(Document pDoc) throws JaxenException
  {
    Node displayNameNode = getDisplayNameNode(pDoc);
    Node parent = displayNameNode.getParentNode();
    parent.replaceChild(createDisplayNameNode(pDoc), displayNameNode);
  }

  private Node getDisplayNameNode(Document pDoc) throws JaxenException
  {
    Node result = null;
    if (mDisplayNameXPath == null)
    {
      mDisplayNameXPath = new DOMXPath("/web-app/display-name");
    }
    result = (Node) mDisplayNameXPath.selectSingleNode(pDoc);

    return result;
  }

  private Node createDisplayNameNode(Document pDoc)
  {
    Node displayName = pDoc.createElement("display-name");
    Node displayNameValue = pDoc.createTextNode(mContextRootName);
    displayName.appendChild(displayNameValue);

    return displayName;
  }
}