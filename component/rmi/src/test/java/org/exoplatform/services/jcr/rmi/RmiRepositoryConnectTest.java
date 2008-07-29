/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.exoplatform.services.jcr.rmi;

import javax.jcr.Node;
import javax.jcr.Repository;
import javax.jcr.RepositoryException;

import junit.framework.TestCase;

/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SAS         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

/**
 * Created by The eXo Platform SAS
 * <p>
 * The test case connect to remote JCR using JCR-RMI wrapper
 * 
 * @author Sergey Kabashnyuk
 */
public class RmiRepositoryConnectTest extends TestCase {
  private RMITestHelper rmiTestHelper = new RMITestHelper();;
  /**
   * The repository instance
   */
  private Repository repository;

  protected void setUp() throws Exception {

    rmiTestHelper.setUrl("//Sergey:9999/repository");
    rmiTestHelper.setWorkspace("production");
    rmiTestHelper.login("admin","admin");

  }
  
  public void testGetRootNode() throws RepositoryException{
    Node node = rmiTestHelper.getRootNode();
    System.out.println("root node path: " + node.getPath());
  }
  
  protected void tearDown() throws Exception {
    
  }

}
