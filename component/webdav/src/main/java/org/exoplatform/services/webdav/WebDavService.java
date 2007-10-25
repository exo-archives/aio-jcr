/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SAS          All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.services.webdav;

import java.util.ArrayList;
import java.util.HashMap;

import javax.jcr.RepositoryException;

import org.exoplatform.services.jcr.core.ManageableRepository;
import org.exoplatform.services.webdav.common.representation.property.PropertyRepresentation;
import org.exoplatform.services.webdav.config.WebDavConfig;
import org.exoplatform.services.webdav.lock.FakeLockTable;

/**
 * Created by The eXo Platform SAS
 * Author : Vitaly Guly <gavrikvetal@gmail.com>
 * @version $Id: $
 */

public interface WebDavService {
  
  String DAV_NAMESPACE = "DAV:"; 

  WebDavConfig getConfig();
  
  ArrayList<String> getAvailableCommands();
  
  ManageableRepository getRepository(String repositoryName) throws RepositoryException;
  
  FakeLockTable getLockTable();
  
  PropertyRepresentation getPropertyRepresentation(String nameSpaceURI, String propertyName, String href);
  
  void registerProperty(String nameSpace, String name, String className);
  
  HashMap<String, HashMap<String, String>> getProperies();  
  
}
