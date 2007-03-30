/***************************************************************************
 * Copyright 2001-2005 The eXo Platform SARL         All rights reserved.  *
 * Please visit http://www.exoplatform.org for more license detail.        *
 **************************************************************************/
package org.exoplatform.tools.xml.webapp.v23;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.exoplatform.tools.xml.ModificationTaskException;
import org.exoplatform.tools.xml.XMLModificationTask;
import org.exoplatform.tools.xml.XMLUtils;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
 * Modifies the web application deployment descriptor "web.xml" to satisfy eXo platfrom
 * requirement. A typical usage would be to first instanticate this class providing the
 * context root (can be null) to the constructor, then calling modifyWebXML(,) to perform
 * the actual modification.
 * 
 * @author Hatim
 */
public class ModifyWebXMLOperation
{

  private String mContextRoot = null;

  /**
   * @param pContextRoot if null the display name element wil not be modified
   */
  public ModifyWebXMLOperation(String pContextRoot)
  {
    mContextRoot = pContextRoot;
  }

  /**
   * pBefore and pAfter files can be the same.
   */
  public void modifyWebXML(File pBefore, File pAfter) throws InterruptedException, SAXException, IOException,
  ModificationTaskException
  {
    
    // load the pBefore file into a DOM object
    FileInputStream fileStream = new FileInputStream(pBefore);
    Document doc = XMLUtils.loadDOMDocument(fileStream);
    XMLUtils.closeStream(fileStream);
    // get the encoding
    String encoding = XMLUtils.getEncoding(doc);
    
    // modify the DOM object in memory according to the predefined list of modifications
    XMLModificationTask[] tasks = getModificationTasks();
    for (int i = 0, sizei = tasks.length; i < sizei; ++i)
    {
      tasks[i].modify(doc);
    }
    
    // get the result in a a byte array
    fileStream = new FileInputStream(pBefore);
    byte[] formatted = XMLUtils.getFormattedDoc(doc, encoding, fileStream);
    XMLUtils.closeStream(fileStream);
    
    FileOutputStream outputStream = new FileOutputStream(pAfter);
    outputStream.write(formatted);
    XMLUtils.closeStream(outputStream);
  }

  public XMLModificationTask[] getModificationTasks()
  {
    List list = new ArrayList();
    if (mContextRoot != null)
    {
      list.add(new WebAppDisplayNameModifyTask(mContextRoot));
    }
    list.add(new WebAppListenerClassModifyTask(
        "org.exoplatform.services.portletcontainer.impl.servlet.PortletApplicationListener"));
    /*
    list.add(new WebAppListenerClassModifyTask(
        "org.exoplatform.services.portal.skin.impl.SkinListener"));
        */
    list.add(new WebAppServletModifyTask("PortletWrapper",
        "org.exoplatform.services.portletcontainer.impl.servlet.ServletWrapper"));
    list.add(new WebAppServletMappingModifyTask("PortletWrapper", "/PortletWrapper"));

    XMLModificationTask[] tasks = new XMLModificationTask[list.size()];
    return (XMLModificationTask[]) list.toArray(tasks);
  }

}
