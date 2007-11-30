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

package org.exoplatform.services.jcr.ext.s3;

import java.io.InputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Calendar;

import javax.jcr.Node;
import javax.jcr.Property;
import org.exoplatform.services.jcr.JcrAPIBaseTest;


/**
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id: $
 */
public class TestBinaryValueS3 extends JcrAPIBaseTest {
 
  private Node testS3Node   = null;

  // -------------- TEST FILES ------------------
  private static final String DOC_FILE = "src/test/resources/index/test_index.doc";
  private static final String HTML_FILE = "src/test/resources/index/test_index.htm";
  private static final String PDF_FILE = "src/test/resources/index/test_index.pdf";
  private static final String PPT_FILE = "src/test/resources/index/test_index.ppt";
  private static final String TXT_FILE = "src/test/resources/index/test_index.txt";
  private static final String XLS_FILE = "src/test/resources/index/test_index.xls";
  private static final String XML_FILE = "src/test/resources/index/test_index.xml";
  
  private static final String DOC_FILE_ = "src/test/resources/index/_test_index.doc";
  private static final String HTML_FILE_ = "src/test/resources/index/_test_index.htm";
  private static final String PDF_FILE_ = "src/test/resources/index/_test_index.pdf";
  private static final String PPT_FILE_ = "src/test/resources/index/_test_index.ppt";
  private static final String TXT_FILE_ = "src/test/resources/index/_test_index.txt";
  private static final String XLS_FILE_ = "src/test/resources/index/_test_index.xls";
  private static final String XML_FILE_ = "src/test/resources/index/_test_index.xml";

  private int j = 0;

  public void setUp() throws Exception {
    super.setUp();
    testS3Node = root.addNode("testS3Node");
    session.save();
  }
  
  public void testUploadFiles() throws Exception {
    uploadFile(DOC_FILE);
    uploadFile(HTML_FILE);
    uploadFile(PDF_FILE);
    uploadFile(PPT_FILE);
    uploadFile(TXT_FILE);
    uploadFile(XLS_FILE);
    uploadFile(XML_FILE);
    session.save();
    downloadFile(XML_FILE_);
    downloadFile(XLS_FILE_);
    downloadFile(TXT_FILE_);
    downloadFile(PPT_FILE_);
    downloadFile(PDF_FILE_);
    downloadFile(HTML_FILE_);
    downloadFile(DOC_FILE_);
  }
  
  
//  public void tearDown() {
//  }
  
  private void uploadFile(String fileName) throws Exception {
 
    Node testS3Node_ = testS3Node.addNode("testS3Node_" + j);
    long startTime = System.currentTimeMillis();
    Node s3File = testS3Node_.addNode(j+"", "nt:file");
    Node s3Content = s3File.addNode("jcr:content", "nt:resource");
    s3Content.setProperty("jcr:data", new FileInputStream(fileName)); 
    s3Content.setProperty("jcr:mimeType", "application/octet-stream ");
    s3Content.setProperty("jcr:lastModified", Calendar.getInstance());

    session.save();
    long endTime = System.currentTimeMillis();
    log.info("+++++>>> - S3PluginTest: (FILE: " +fileName+"). Upload time : " 
        + ((endTime - startTime) / 1000) + "s");
    j++;
  }
  

  private void downloadFile(String fileName) throws Exception {

    j--;
    long startTime = System.currentTimeMillis();
    Node testS3Node_ = testS3Node.getNode("testS3Node_"+j);
    Node s3File = testS3Node_.getNode(j+"");
    assertNotNull(s3File);
    Property property = s3File.getNode("jcr:content").getProperty("jcr:data");
    FileOutputStream out =new FileOutputStream(fileName);
    InputStream in = property.getStream();
    int rd = -1;
    byte[] buff = new byte[4096]; 
    while((rd = in.read(buff)) != -1)
      out.write(buff, 0, rd);
    long endTime = System.currentTimeMillis();
    log.info("+++++>>> + S3PluginTest: (FILE: " +fileName+").Download time :" 
        + ((endTime - startTime) / 1000) + "s");
  }


}
