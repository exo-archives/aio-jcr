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

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Calendar;

import javax.jcr.RepositoryException;

import org.exoplatform.services.jcr.impl.core.NodeImpl;
import org.exoplatform.services.jcr.impl.dataflow.TransientNodeData;
import org.exoplatform.services.jcr.impl.dataflow.TransientValueData;
import org.exoplatform.services.jcr.impl.storage.WorkspaceDataContainerBase;
import org.exoplatform.services.jcr.storage.WorkspaceStorageConnection;

/**
 * Created by The eXo Platform SAS Author : Alex Reshetnyak alex.reshetnyak@exoplatform.org.ua
 * reshetnyak.alex@gmail.com 06.04.2007 10:26:43
 * 
 * @version $Id: DataUploaderTh.java 06.04.2007 10:26:43 rainfox
 */
public class DataUploaderTh extends DataUploader implements Runnable {
  private int                        nodeNumber;

  private long                       start = 0, end = 0, uploadTime = 0;

  private WorkspaceDataContainerBase wsDataContainer;

  public DataUploaderTh(String[] args,
                        WorkspaceDataContainerBase wsDataContainer,
                        NodeImpl parentNode,
                        int nodeNumber) {
    super(args);
    this.nodeNumber = nodeNumber;
    this.workspaceDataContainer = wsDataContainer;
    rootTestNode = parentNode;

    try {
      connection = getConnection();
    } catch (Exception e1) {
      e1.printStackTrace();
    }

    sConf = mapConfig.get("-conf");
    sRepository = mapConfig.get("-repo");
    sWorkspace = mapConfig.get("-ws");
    sRoot = mapConfig.get("-root");
    sVdfile = mapConfig.get("-vdfile");
    sReadTree = mapConfig.get("-readtree");

    try {
      fileData = new TransientValueData(new FileInputStream(sVdfile));
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    }

  }

  public void run() {
    int tree[] = getTree(mapConfig.get("-tree"));

    System.out.println(">>>>>>>>>>>---------- Upload data ----------<<<<<<<<<<<<");

    sName = "folder";

    sFile = "file";

    date = Calendar.getInstance();

    countNodes = 1 * tree[1] * tree[2] * tree[3];

    start = System.currentTimeMillis();

    int i = nodeNumber;
    try {
      TransientNodeData nodeData_L1 = addNode(connection, sName + i, i, rootTestNode, date);

      for (int j = 1; j <= tree[1]; j++) {
        TransientNodeData nodeData_L2 = addNode(connection, sName + j, j, nodeData_L1, date);

        for (int k = 1; k <= tree[2]; k++) {
          TransientNodeData nodeData_L3 = addNode(connection, sName + k, k, nodeData_L2, date);

          for (int index = 1; index <= tree[3]; index++)
            addNode_file(connection, sFile + index, index, nodeData_L3, date, fileData);

          connection.commit();
          connection = getConnection();

          System.out.println("Node " + i + " - " + j + " - " + k + " - " + "[1..." + tree[3]
              + "] add");
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
      try {
        connection.rollback();
      } catch (Exception e1) {
        e1.printStackTrace();
        System.out.println("Error: Rollback Exception");
      }
      System.out.println(">>>>>>>>>>>---------- Upload data Exception ----------<<<<<<<<<<<<");
    }

    end = System.currentTimeMillis();

    uploadTime = end - start;

    System.out.println("The time of the adding of " + countNodes + " nodes: "
        + (uploadTime / 1000.0) + " sec");
  }

}
