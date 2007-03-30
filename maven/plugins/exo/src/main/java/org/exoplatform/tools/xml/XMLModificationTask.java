/***************************************************************************
 * Copyright 2001-2004 The eXo Platform SARL         All rights reserved.  *
 * Please visit http://www.exoplatform.org for more license detail.        *
 **************************************************************************/

package org.exoplatform.tools.xml;

import org.jaxen.JaxenException;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.traversal.DocumentTraversal;
import org.w3c.dom.traversal.NodeFilter;
import org.w3c.dom.traversal.NodeIterator;

/**
 * If you plan to write a class to modify an xml file then extends this class as it
 * provides a template method pattern.
 * 
 * @author Hatim Khan
 * @version $Revision: 1.1 $
 */
public abstract class XMLModificationTask
{
  public static final String CLASS_VERSION = "$Id: XMLModificationTask.java,v 1.1 2004/04/19 03:45:49 hatimk Exp $"; //$NON-NLS-1$
  protected static final int NOMODIFICATION = 0;
  protected static final int REPLACE = 1;
  protected static final int INSERT = 2;

  /**
   * Tells you if this task will modify the DOM document.
   * 
   * @param pDoc DOM document
   * 
   * @return true if the task will modify the DOM document when you call the modify()
   *         method, false otherwise
   * 
   * @throws ModificationTaskException if parsing error or unexpected error
   */
  public boolean needModification(Document pDoc) throws ModificationTaskException
  {
    boolean modify = false;
    try
    {
      int detectStatus = detect(pDoc);
      switch (detectStatus)
      {
        case REPLACE:
          modify = true;

          break;

        case INSERT:
          modify = true;

          break;

        default:
          modify = false;
      }
    }
    catch (JaxenException e)
    {
      throw new ModificationTaskException("Unable to process the DOM document " + e.getMessage(), e);
    }

    return modify;
  }

  /**
   * Call this method to do the actual DOM document modification.
   * 
   * @param pDoc DOM document to modify
   * 
   * @return true if modification occrued, false otherwise
   * 
   * @throws ModificationTaskException if parsing error or unexpected error
   */
  public boolean modify(Document pDoc) throws ModificationTaskException
  {
    boolean result = false;
    try
    {
      int detectStatus = detect(pDoc);
      switch (detectStatus)
      {
        case REPLACE:
          replace(pDoc);
          result = true;

          break;

        case INSERT:
          insert(pDoc);
          result = true;

          break;
      }
    }
    catch (JaxenException e)
    {
      throw new ModificationTaskException("Unable to modify the DOM document" + e.getMessage(), e);

    }

    return result;
  }

  /**
   * Detects if the DOM document needs modification.
   * 
   * @param pDoc DOM document
   * 
   * @return NOMODIFICATION (no modification is required), REPLACE (the modification is
   *         likely to result in a node being replaced), INSERT (the modification is
   *         likely to result in a node being inserted)
   * 
   * @throws JaxenException if parsing error or unexpected error
   */
  protected abstract int detect(Document pDoc) throws JaxenException;

  protected abstract void insert(Document pDoc) throws JaxenException;

  protected abstract void replace(Document pDoc) throws JaxenException;

  /**
   * Simple returns all descendant text nodes and CDATA nodes of the given node
   * 
   * @param pNode the node in question
   * 
   * @return all descendant text nodes and CDATA nodes of the given node as a string
   */
  protected static String getTextContent(Node pNode)
  {
    Document doc = pNode.getOwnerDocument();
    DocumentTraversal traversable = (DocumentTraversal) doc;
    int whatToShow = NodeFilter.SHOW_TEXT | NodeFilter.SHOW_CDATA_SECTION;
    NodeIterator iterator = traversable.createNodeIterator(pNode, whatToShow, null, false);

    StringBuffer result = new StringBuffer();
    Node current;
    while ((current = iterator.nextNode()) != null)
    {
      result.append(current.getNodeValue());
    }

    return result.toString();
  }
}