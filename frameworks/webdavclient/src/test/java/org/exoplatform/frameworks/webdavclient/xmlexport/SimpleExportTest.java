/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SAS          All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.frameworks.webdavclient.xmlexport;

import java.util.ArrayList;

import junit.framework.TestCase;

import org.exoplatform.frameworks.webdavclient.Const;
import org.exoplatform.frameworks.webdavclient.TestContext;
import org.exoplatform.frameworks.webdavclient.commands.DavDelete;
import org.exoplatform.frameworks.webdavclient.commands.DavGet;
import org.exoplatform.frameworks.webdavclient.commands.DavMkCol;
import org.exoplatform.frameworks.webdavclient.commands.DavPut;
import org.exoplatform.frameworks.webdavclient.http.Log;

/**
 * Created by The eXo Platform SAS
 * Author : Vitaly Guly <gavrikvetal@gmail.com>
 * @version $Id: $
 */

public class SimpleExportTest extends TestCase {
  
  public void testNtFolderContent() {
    Log.info("SimpleExportTest:testNtFolderContent");
    
    String nodeName = "testnode_" + System.currentTimeMillis();
    String nodePath = "/production/" + nodeName;
    
    String child1Name = "childnode1";
    String child1Path = nodePath + "/" + child1Name;
    
    String child2Name = "childnode2";
    String child2Path = nodePath + "/" + child2Name;
    
    try {

      {
        DavMkCol davMkCol = new DavMkCol(TestContext.getContextAuthorized());
        davMkCol.setResourcePath(nodePath);
        assertEquals(Const.HttpStatus.CREATED, davMkCol.execute());

        davMkCol = new DavMkCol(TestContext.getContextAuthorized());
        davMkCol.setResourcePath(child1Path);
        assertEquals(Const.HttpStatus.CREATED, davMkCol.execute());

        davMkCol = new DavMkCol(TestContext.getContextAuthorized());
        davMkCol.setResourcePath(child2Path);
        assertEquals(Const.HttpStatus.CREATED, davMkCol.execute());
      }

      {
        DavGet davGet = new DavGet(TestContext.getContextAuthorized());
        davGet.setResourcePath(nodePath);
        assertEquals(Const.HttpStatus.OK, davGet.execute());

        JcrXmlContent content = new JcrXmlContent(davGet);
        assertTrue(content.isNode());
    
        String href1 = TestContext.getContextAuthorized().getServerPrefix() + nodePath;
        
        XmlNodeDescription nodeDescription = (XmlNodeDescription)content.getItemDescription();
        assertEquals(nodeName, nodeDescription.getName());
        assertEquals(href1, nodeDescription.getHref());

        {
          String propPrimaryType = "jcr:primaryType";
          String hrefPrimaryType = href1 + "/" + propPrimaryType;

          XmlPropertyDescription property1 = nodeDescription.getProperty(propPrimaryType);
          assertEquals(propPrimaryType, property1.getName());
          assertEquals(hrefPrimaryType, property1.getHref());
        }

        {
          String propCreated = "jcr:created";
          String hrefCreated = href1 + "/" + propCreated;
          
          XmlPropertyDescription property2 = nodeDescription.getProperty(propCreated);
          assertEquals(propCreated, property2.getName());
          assertEquals(hrefCreated, property2.getHref());
        }
                
        ArrayList<XmlNodeDescription> nodes = nodeDescription.getNodes();
        assertEquals(2, nodes.size());
        
        XmlNodeDescription node1 = nodeDescription.getNode(child1Name);
        String hrefNode1 = href1 + "/" + child1Name; 
        assertEquals(hrefNode1, node1.getHref());
        
        
        XmlNodeDescription node2 = nodeDescription.getNode(child2Name);
        String hrefNode2 = href1 + "/" + child2Name;
        assertEquals(hrefNode2, node2.getHref());
        
      }
      
      {
        DavDelete davDelete = new DavDelete(TestContext.getContextAuthorized());
        davDelete.setResourcePath(nodePath);
        assertEquals(Const.HttpStatus.NOCONTENT, davDelete.execute());
      }
      
    } catch (Exception exc) {
      Log.info("Unhandled exception", exc);
      fail();
    }
    
    Log.info("done.");
  }
  
  public void testNtFileContent() {
    Log.info("SimpleExportTest:testNtFileContent");
    
    String folderName = "test_folder_" + System.currentTimeMillis();
    String folderPath = "/production/" + folderName;
    
    String fileName = "test_file_" + System.currentTimeMillis();
    String filePath = folderPath + "/" + fileName;
    
    String fileContent = "TEST FILE CONTENT";

    try {

      // create nt:folder
      {
        DavMkCol davMkCol = new DavMkCol(TestContext.getContextAuthorized());
        davMkCol.setResourcePath(folderPath);
        assertEquals(Const.HttpStatus.CREATED, davMkCol.execute());
      }
      
      // create nt:file
      {
        DavPut davPut = new DavPut(TestContext.getContextAuthorized());
        davPut.setResourcePath(filePath);
        davPut.setRequestDataBuffer(fileContent.getBytes());
        assertEquals(Const.HttpStatus.CREATED, davPut.execute());
      }
      
      // get on it jcr:content
      {
        String contentName = "jcr:content";
        String contentPath = filePath + "/" + contentName;
        
        String hrefContent = TestContext.getContextAuthorized().getServerPrefix();
        hrefContent += contentPath;
        
        DavGet davGet = new DavGet(TestContext.getContextAuthorized());
        davGet.setResourcePath(contentPath);
        assertEquals(Const.HttpStatus.OK, davGet.execute());
        
        JcrXmlContent content = new JcrXmlContent(davGet);
        assertTrue(content.isNode());
        
        XmlNodeDescription contentNode = (XmlNodeDescription)content.getItemDescription();
        assertEquals(contentName, contentNode.getName());
        assertEquals(hrefContent, contentNode.getHref());
                
        String []nodeProperties = {
            "jcr:primaryType", "jcr:mixinTypes", "jcr:uuid", 
            "jcr:data", "jcr:lastModified", "jcr:mimeType"
        };
        
        assertEquals(nodeProperties.length, contentNode.getProperties().size());
        
        for (int i = 0; i < nodeProperties.length; i++) {
          String curPropertyName = nodeProperties[i];          
          String curPropHref = hrefContent + "/" + curPropertyName;          
          XmlPropertyDescription propDescr = contentNode.getProperty(curPropertyName);          
          assertEquals(curPropHref, propDescr.getHref());          
        }
                
      }      
      
      // clear
      {
        DavDelete davDelete = new DavDelete(TestContext.getContextAuthorized());
        davDelete.setResourcePath(folderPath);
        assertEquals(Const.HttpStatus.NOCONTENT, davDelete.execute());
      }
      
    } catch (Exception exc) {
      Log.info("Unhandled exception." + exc);
      fail();
    }
    
    Log.info("done.");
  }
  
  public void testPropertyEjection() {
    Log.info("testPropertyEjection");
    
    String folderName = "test_folder_" + System.currentTimeMillis();
    String folderPath = "/production/" + folderName;
    
    String fileName = "test_file_" + System.currentTimeMillis();
    String filePath = folderPath + "/" + fileName;
    String fileContent = "TEST FILE CONTENT";
    
    String fileContentName = "jcr:content";
    String fileContentPath = filePath + "/" + fileContentName;    
    
    try {
      
      // created folder
      {
        DavMkCol davMKCol = new DavMkCol(TestContext.getContextAuthorized());
        davMKCol.setResourcePath(folderPath);
        assertEquals(Const.HttpStatus.CREATED, davMKCol.execute());
      }
      
      // put file
      {
        DavPut davPut = new DavPut(TestContext.getContextAuthorized());
        davPut.setResourcePath(filePath);
        davPut.setRequestDataBuffer(fileContent.getBytes());
        assertEquals(Const.HttpStatus.CREATED, davPut.execute());
      }
      
      // get it jcr:content properties
      {
        DavGet davGet = new DavGet(TestContext.getContextAuthorized());
        davGet.setResourcePath(fileContentPath);
        assertEquals(Const.HttpStatus.OK, davGet.execute());
        
//        String replyXML = new String(davGet.getResponseDataBuffer());
//        Log.info("REPLY XML: " + replyXML);
//        
//        File logFile = new File("D:/exo/projects/exoprojects/jcr/trunk/frameworks/webdavclient/testlog.xml");
//        OutputStream outStream = new FileOutputStream(logFile);
//        outStream.write(davGet.getResponseDataBuffer());
      }
      
      // eject property values
      {
        String []nodeProperties = {
            "jcr:primaryType", "jcr:mixinTypes", "jcr:uuid", 
            "jcr:data", "jcr:lastModified", "jcr:mimeType"
        };

        for (int i = 0; i < nodeProperties.length; i++) {
          DavGet davGet = new DavGet(TestContext.getContextAuthorized());
          davGet.setResourcePath(fileContentPath + "/" + nodeProperties[i]);
          assertEquals(Const.HttpStatus.OK, davGet.execute());
        }
        
      }
      
      // clearing
      {
        DavDelete davDelete = new DavDelete(TestContext.getContextAuthorized());
        davDelete.setResourcePath(folderPath);
        assertEquals(Const.HttpStatus.NOCONTENT, davDelete.execute());
      }
      
    } catch (Exception exc) {
      Log.info("Unhandled exception ", exc);
      fail();
    }
    
    Log.info("done.");
  }

}

