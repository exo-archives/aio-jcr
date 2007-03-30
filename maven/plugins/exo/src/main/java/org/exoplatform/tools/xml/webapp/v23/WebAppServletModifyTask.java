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
 * This task inspects to see if there is a servlet element with the given servlet name, if
 * it is not, then a servlet element that contains the servlet name and class will be
 * inserted, if there is already one then nothing will happen. If the servlet element
 * contains the correct servlet name but not the correct servlet class, then it will be
 * modified to reflect the correct value. When insertation is needed, it will insert one
 * in the correct position according to the web app DTD (see
 * http://java.sun.com/dtd/web-app_2_3.dtd)
 * 
 * @author Hatim Khan
 * @version $Revision: 1.1 $
 */
public class WebAppServletModifyTask extends WebAppModifyTask
{
  public static final String CLASS_VERSION = "$Id: WebAppServletModifyTask.java,v 1.1 2004/04/19 03:45:49 hatimk Exp $"; //$NON-NLS-1$
  private String mServletName;
  private String mServletClassName;
  private XPath mServletNameXPath;
  private XPath mServletClassXPath;

  public WebAppServletModifyTask(String pServletName, String pServletClassName)
  {
    mServletName = pServletName;
    mServletClassName = pServletClassName;
  }

  /**
   * @see org.exoplatform.eclipse.core.xml.XMLModificationTask#detect(org.w3c.dom.Document)
   */
  protected int detect(Document pDoc) throws JaxenException
  {
    if (mServletNameXPath == null)
    {
      mServletNameXPath = new DOMXPath("/web-app/servlet[servlet-name=\"" + mServletName + "\"]");
    }

    if (mServletClassXPath == null)
    {
      mServletClassXPath = new DOMXPath("servlet-class=\"" + mServletClassName + "\"");
    }

    Node servletNode = (Node) mServletNameXPath.selectSingleNode(pDoc);
    if (servletNode != null)
    {
      if (mServletClassXPath.booleanValueOf(servletNode))
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
    insertElement(parent, createServletElement(pDoc), WEBAPP_CHILD_ELEMENTS);
  }

  /**
   * @see org.exoplatform.eclipse.core.xml.XMLModificationTask#replace(org.w3c.dom.Document)
   */
  protected void replace(Document pDoc) throws JaxenException
  {
    Node parent = (Node) mServletNameXPath.selectSingleNode(pDoc);
    XPath servletClassXPath = new DOMXPath("servlet-class");
    Node servletClassNode = (Node) servletClassXPath.selectSingleNode(parent);
    if (servletClassNode == null)
    {
      insertElement(parent, createServletClassElement(pDoc), SERVLET_CHILD_ELEMENTS);
    }
    else
    {
      parent.replaceChild(createServletClassElement(pDoc), servletClassNode);
    }
  }

  private Node createServletElement(Document pDoc)
  {
    Node servletClass = createServletClassElement(pDoc);
    Node servletNameText = pDoc.createTextNode(mServletName);
    Node servletName = pDoc.createElement("servlet-name");
    Node servlet = pDoc.createElement("servlet");

    servletName.appendChild(servletNameText);
    servlet.appendChild(servletName);
    servlet.appendChild(servletClass);

    return servlet;
  }

  private Node createServletClassElement(Document pDoc)
  {
    Node servletClassText = pDoc.createTextNode(mServletClassName);
    Node servletClass = pDoc.createElement("servlet-class");
    servletClass.appendChild(servletClassText);

    return servletClass;
  }
}