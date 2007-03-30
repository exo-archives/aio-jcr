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
 * This task inspects to see if there is a servlet mapping element with the given url
 * pattern, if it is not, then a servlet mapping element that contains the servlet name
 * and url pattern will be inserted, if there is already one then nothing will happen. If
 * the servlet mapping element contains the correct url pattern but not the correct
 * servlet name, then it will be modified to reflect the correct value. When insertation
 * is needed, it will insert one in the correct position according to the web app DTD (see
 * http://java.sun.com/dtd/web-app_2_3.dtd)
 * 
 * @author Hatim Khan
 * @version $Revision: 1.1 $
 */
public class WebAppServletMappingModifyTask extends WebAppModifyTask
{
  public static final String CLASS_VERSION = "$Id: WebAppServletMappingModifyTask.java,v 1.1 2004/04/19 03:45:49 hatimk Exp $"; //$NON-NLS-1$
  private String mServletName;
  private String mURLPattern;
  private XPath mServletNameXPath;
  private XPath mURLPatternXPath;

  public WebAppServletMappingModifyTask(String pServletName, String pURLPattern)
  {
    mServletName = pServletName;
    mURLPattern = pURLPattern;
  }

  /**
   * @see org.exoplatform.eclipse.core.xml.XMLModificationTask#detect(org.w3c.dom.Document)
   */
  protected int detect(Document pDoc) throws JaxenException
  {
    if (mURLPatternXPath == null)
    {
      mURLPatternXPath = new DOMXPath("/web-app/servlet-mapping[url-pattern=\"" + mURLPattern + "\"]");
    }

    if (mServletNameXPath == null)
    {
      mServletNameXPath = new DOMXPath("servlet-name=\"" + mServletName + "\"");
    }

    Node servletNode = (Node) mURLPatternXPath.selectSingleNode(pDoc);
    if (servletNode != null)
    {
      if (mServletNameXPath.booleanValueOf(servletNode))
      {
        return NOMODIFICATION;
      }
      else
      {
        return REPLACE;
      }
    }
    else
    {
      return INSERT;
    }
  }

  /**
   * @see org.exoplatform.eclipse.core.xml.XMLModificationTask#insert(org.w3c.dom.Document)
   */
  protected void insert(Document pDoc) throws JaxenException
  {
    Node parent = getWebAppNode(pDoc);
    insertElement(parent, createServletMappingElement(pDoc), WEBAPP_CHILD_ELEMENTS);
  }

  /**
   * @see org.exoplatform.eclipse.core.xml.XMLModificationTask#replace(org.w3c.dom.Document)
   */
  protected void replace(Document pDoc) throws JaxenException
  {
    Node parent = (Node) mURLPatternXPath.selectSingleNode(pDoc);
    XPath servletNameXPath = new DOMXPath("servlet-name");
    Node servletNameNode = (Node) servletNameXPath.selectSingleNode(parent);
    if (servletNameNode == null)
    {
      insertElement(parent, createServletNameElement(pDoc), SERVLETMAPPING_CHILD_ELEMENTS);
    }
    else
    {
      parent.replaceChild(createServletNameElement(pDoc), servletNameNode);
    }
  }

  private Node createServletMappingElement(Document pDoc)
  {
    Node urlPattern = createURLPatternElement(pDoc);
    Node servletName = createServletNameElement(pDoc);
    Node servletMapping = pDoc.createElement("servlet-mapping");

    servletMapping.appendChild(servletName);
    servletMapping.appendChild(urlPattern);

    return servletMapping;
  }

  private Node createURLPatternElement(Document pDoc)
  {
    Node urlPatternText = pDoc.createTextNode(mURLPattern);
    Node urlPattern = pDoc.createElement("url-pattern");
    urlPattern.appendChild(urlPatternText);

    return urlPattern;
  }

  private Node createServletNameElement(Document pDoc)
  {
    Node servletNameText = pDoc.createTextNode(mServletName);
    Node servletName = pDoc.createElement("servlet-name");
    servletName.appendChild(servletNameText);

    return servletName;
  }

}