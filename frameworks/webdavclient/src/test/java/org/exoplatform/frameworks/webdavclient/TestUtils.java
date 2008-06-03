/*
 * Copyright (C) 2003-2007 eXo Platform SAS.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
 */

package org.exoplatform.frameworks.webdavclient;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;

import junit.framework.TestCase;

import org.exoplatform.frameworks.webdavclient.commands.DavCommand;
import org.exoplatform.frameworks.webdavclient.commands.DavDelete;
import org.exoplatform.frameworks.webdavclient.commands.DavMkCol;
import org.exoplatform.frameworks.webdavclient.commands.DavPut;
import org.exoplatform.frameworks.webdavclient.documents.Multistatus;
import org.exoplatform.frameworks.webdavclient.documents.ResponseDoc;
import org.exoplatform.frameworks.webdavclient.http.Log;

/**
 * Created by The eXo Platform SAS
 * Author : Vitaly Guly <gavrikvetal@gmail.com>
 * @version $Id: $
 */

public class TestUtils extends TestCase {

  //private static Log log = ExoLogger.getLogger("jcr.TestUtils");
  
  public static void logStart() {
    try {
      throw new Exception();
    } catch (Exception exc) {
      StackTraceElement []elements = exc.getStackTrace();
      StackTraceElement element = elements[1];      
      String getMethodName = element.getMethodName();
      String className = element.getClassName();
      className = className.substring(className.lastIndexOf(".") + 1);
      Log.info(className + ":" + getMethodName + " - testing...");
    }
  }  
  
  public static void showMultistatus(Multistatus multistatus) {
    Log.info(">>> multistatus ------------------------");
    ArrayList<ResponseDoc> responses = multistatus.getResponses();
    for (int i = 0; i < responses.size(); i++) {
      String href = responses.get(i).getHref();
      href = href.substring("http://localhost:8080/jcr-webdav/repository".length());
      Log.info("HREH: [" + href + "]");
    }
    Log.info(">>> ------------------------------------");
  }
  
  public static void createCollection(String path) throws Exception {
    DavMkCol davMkCol = new DavMkCol(TestContext.getContextAuthorized());
    davMkCol.setResourcePath(path);
    assertEquals(Const.HttpStatus.CREATED, davMkCol.execute());
  }
  
  public static void createFile(String path, byte []content) throws Exception {
    DavPut davPut = new DavPut(TestContext.getContextAuthorized());
    davPut.setResourcePath(path);
    davPut.setRequestDataBuffer(content);
    assertEquals(Const.HttpStatus.CREATED, davPut.execute());
  }
  
  public static void removeResource(String path) throws Exception {
    DavDelete davDelete = new DavDelete(TestContext.getContextAuthorized());
    davDelete.setResourcePath(path);    
    assertEquals(Const.HttpStatus.NOCONTENT, davDelete.execute());
  }
  
  public static void logXML(DavCommand davCommand) throws Exception {
    String xmlFileName = "D://exo/projects/exoprojects/jcr/trunk/frameworks/webdavclient/testlog.xml";
    File outFile = new File(xmlFileName);
    FileOutputStream logStream = new FileOutputStream(outFile);
    logStream.write(davCommand.getResponseDataBuffer());            
  }
  
}
