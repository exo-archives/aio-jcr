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
package org.exoplatform.applications.repoload;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Calendar;

import javax.jcr.Node;

/**
 * Created by The eXo Platform SAS
 * Author : Alex Reshetnyak
 *          alex.reshetnyak@exoplatform.org.ua
 *          reshetnyak.alex@gmail.com		
 * 03.05.2007 11:39:58 
 * @version $Id: DataUploaderAPI.java 03.05.2007 11:39:58 rainfox 
 */
public class DataUploaderAPI extends DataUploader {
  
  protected boolean setFile;

  public DataUploaderAPI(String[] args) {
    super(args);
  }
  
  public void uploadDataAPI() throws Exception {
    long start, end, temp, localStart, localEnd; 
    
    int tree[] = getTree(mapConfig.get("-tree"));

    log.info(">>>>>>>>>>>---------- Upload data ----------<<<<<<<<<<<<");

    sName = "node";

    sFile = "file";

    date = Calendar.getInstance();
    
    setFile = !sVdfile.equals("");
    
    countNodes = 0/*tree[0] * tree[1] * tree[2] * tree[3]*/;

    start = System.currentTimeMillis();
    
    for (int i = 1; i <= tree[0]; i++) {
      try {
        Node nodeData_L1 = rootTestNode.addNode(sName + i, "nt:folder");

        for (int j = 1; j <= tree[1]; j++) {
          Node nodeData_L2 = nodeData_L1.addNode(sName + j, "nt:folder");

          localStart = System.currentTimeMillis();
          
          for (int k = 1; k <= tree[2]; k++) {
            Node nodeData_L3 = nodeData_L2.addNode(sName + k, "nt:folder");

            for (int index = 1; index <= tree[3]; index++){
               addNode_file(sFile + index, nodeData_L3, date);
              countNodes++;
            }

            session.save();
            
            log.info("Node " + i + " - " + j + " - " + k + " - " + "[1..." + tree[3]
                + "] add");
          }
         
          localEnd = System.currentTimeMillis();
                    
          log.info("\tThe time of adding of " + tree[2]*tree[3] + " nodes: "+ ((localEnd - localStart) / 1000.0) + " sec"  );
          log.info("\tTotal adding time " + countNodes + " nodes: "+ ((localEnd - start) / 1000.0) + " sec"  );
        }
      } catch (Exception e) {
        connection.rollback();
        log.error(">>>>>>>>>>>---------- Upload data Exception ----------<<<<<<<<<<<<", e);
      }
    }
    
    end = System.currentTimeMillis();
    log.info("The time of the adding of " + countNodes + " nodes: "
        + ((end - start) / 1000.0) + " sec");
  } 
  
  protected void addNode_file(String name, Node parentNode, Calendar ddate) throws Exception {
    Node nodeFile = parentNode.addNode(name, "nt:file");
    Node contentNode = nodeFile.addNode("jcr:content", "nt:resource");
    if (setFile)
      contentNode.setProperty("jcr:data", new FileInputStream(sVdfile));
    else
      contentNode.setProperty("jcr:data", new ByteArrayInputStream("".getBytes()));
    contentNode.setProperty("jcr:mimeType", sMimeType);
    contentNode.setProperty("jcr:lastModified", ddate);
    
//    nodeFile.addMixin("dc:elementSet");
//    nodeFile.setProperty("dc:title",        "0123456789");
//    nodeFile.setProperty("dc:subject",      "0123456789");
//    nodeFile.setProperty("dc:description",  "0123456789");
//    nodeFile.setProperty("dc:publisher",    "0123456789");
//    nodeFile.setProperty("dc:date",         "0123456789");
//    nodeFile.setProperty("dc:resourceType", "0123456789");
  }

}
