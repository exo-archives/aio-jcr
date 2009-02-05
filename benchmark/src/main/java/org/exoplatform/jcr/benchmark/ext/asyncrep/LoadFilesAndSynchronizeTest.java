/*
 * Copyright (C) 2003-2009 eXo Platform SAS.
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
package org.exoplatform.jcr.benchmark.ext.asyncrep;

import javax.jcr.Node;
import javax.jcr.Session;

import org.exoplatform.jcr.benchmark.JCRTestContext;
import org.exoplatform.services.jcr.ext.replication.async.AsyncReplication;

import com.sun.japex.TestCase;


/**
 * Created by The eXo Platform SAS.
 * 
 * <br/>Date: 
 *
 * @author <a href="karpenko.sergiy@gmail.com">Karpenko Sergiy</a> 
 * @version $Id: LoadFilesAndSynchronizeTest.java 111 2008-11-11 11:11:11Z serg $
 */
public class LoadFilesAndSynchronizeTest extends AsyncTestBase{

  private final int COUNT_I = 3;
  private final int COUNT_J = 2;
  private Node root;
  
  @Override
  public void doPrepare(TestCase tc, AsyncTestContext context) throws Exception {
    String rootFolder = tc.getParam("ext.rootFolder");

    Session s = context.getSession();
    
    root = s.getRootNode().addNode(rootFolder); //, "nt:folder"
    
    s.save();
  }
  
  
  public void doRun(final TestCase tc, AsyncTestContext context) throws Exception{
    Session s = context.getSession();
    
    for(int i=0; i<COUNT_I;i++){
      for(int j=0; j<COUNT_J;j++){
        //create file
        //root
        Node n = root.addNode("file"+i+"_"+j);
        System.out.println(n.getName() + " file added");
      }
      s.save();
      System.out.println(i +" log saved");
    }
    
    AsyncReplication rep = context.getReplicationServer();
    rep.synchronize();
    System.out.println("Synchronize started.");
    
  }
}
