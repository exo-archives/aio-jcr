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
 * This task inspects to see if there is a listener element with the given
 * listener class, if it is not, then one will be inserted, if there is
 * already one then nothing will happen. When insertation is needed, it will
 * insert one in the correct position according to the web app DTD (see
 * http://java.sun.com/dtd/web-app_2_3.dtd)
 *
 * @author Hatim Khan
 * @version $Revision: 1.1 $
 */
public class WebAppListenerClassModifyTask extends WebAppModifyTask
{
  public static final String CLASS_VERSION = "$Id: WebAppListenerClassModifyTask.java,v 1.1 2004/04/19 03:45:49 hatimk Exp $"; //$NON-NLS-1$
  private String mListenerClass;
  private XPath mListenerClassXPath;

  /**
   * The constructor method.
   *
   * @param pListenerClassName  
   */
  public WebAppListenerClassModifyTask(String pListenerClassName)
  {
    mListenerClass = pListenerClassName;
  }

  /**   
   * @see org.exoplatform.eclipse.core.xml.XMLModificationTask#detect(org.w3c.dom.Document)
   */
  protected int detect(Document pDoc) throws JaxenException
  {
    if (mListenerClassXPath == null)
    {
      mListenerClassXPath =
        new DOMXPath("/web-app/listener[listener-class=\"" + mListenerClass
          + "\"]");
    }
    
    // notice that replace does not make sense here. Either the listener class
    // exists or not. If it does not exists then it needs to be inserted.
    return mListenerClassXPath.booleanValueOf(pDoc) ? NOMODIFICATION : INSERT;
  }

  /**   
   * @see org.exoplatform.eclipse.core.xml.XMLModificationTask#insert(org.w3c.dom.Document)
   */
  protected void insert(Document pDoc) throws JaxenException
  {
    Node parent = getWebAppNode(pDoc);    
    insertElement(parent, createListenerElement(pDoc), WEBAPP_CHILD_ELEMENTS);
  }

  /**   
   * @see org.exoplatform.eclipse.core.xml.XMLModificationTask#replace(org.w3c.dom.Document)
   */
  protected void replace(Document pDoc) throws JaxenException
  {
    // notice that replace does not make sense here. Either the listener class
    // exists or not. If it does not exists then it needs to be inserted.    
  }
  
  private Node createListenerElement(Document pDoc)
  {
    Node listenerClassText = pDoc.createTextNode(mListenerClass);
    Node listenerClass = pDoc.createElement("listener-class");
    Node listener = pDoc.createElement("listener");
    listenerClass.appendChild(listenerClassText);
    listener.appendChild(listenerClass);
    
    return listener;
  }
}
