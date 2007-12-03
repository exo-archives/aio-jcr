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
  
  HashMap<String, String> getOwnerTable();
  
  PropertyRepresentation getPropertyRepresentation(String nameSpaceURI, String propertyName, String href);
  
  void registerProperty(String nameSpace, String name, String className);
  
  HashMap<String, HashMap<String, String>> getProperies();  
  
}
