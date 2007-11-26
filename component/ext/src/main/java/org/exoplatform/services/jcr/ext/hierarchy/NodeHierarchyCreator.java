/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.services.jcr.ext.hierarchy;

import javax.jcr.Node;

import org.exoplatform.services.jcr.ext.common.SessionProvider;

/**
 * Created by The eXo Platform SARL
 * Author : Dang Van Minh
 *          minh.dang@exoplatform.com
 * Nov 15, 2007 10:10:10 AM
 */
public interface NodeHierarchyCreator {

  public String getContentLocation();
  
  public String getJcrPath(String alias);
  
  public void init(String repository) throws Exception ;
  
  public Node getUserApplicationNode(SessionProvider sessionProvider, String userName) throws Exception ;
  
  public Node getPublicApplicationNode(SessionProvider sessionProvider) throws Exception ;
}