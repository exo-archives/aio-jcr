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
package org.exoplatform.services.jcr.ext.hierarchy;

import javax.jcr.Node;

import org.exoplatform.container.component.ComponentPlugin;
import org.exoplatform.services.jcr.ext.common.SessionProvider;

/**
 * Created by The eXo Platform SAS Author : Dang Van Minh minh.dang@exoplatform.com Nov 15, 2007
 * 10:10:10 AM
 */
public interface NodeHierarchyCreator {

  public String getJcrPath(String alias);

  public void init(String repository) throws Exception;

  public Node getUserNode(SessionProvider sessionProvider, String userName) throws Exception;

  public Node getUserApplicationNode(SessionProvider sessionProvider, String userName) throws Exception;

  public Node getPublicApplicationNode(SessionProvider sessionProvider) throws Exception;

  public void addPlugin(ComponentPlugin plugin);
}
