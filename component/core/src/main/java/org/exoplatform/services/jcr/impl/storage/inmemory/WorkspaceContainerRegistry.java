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
package org.exoplatform.services.jcr.impl.storage.inmemory;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

/**
 * Created by The eXo Platform SAS.
 *
 * @author <a href="mailto:geaz@users.sourceforge.net">Gennady Azarenkov</a>
 * @version $Id: WorkspaceContainerRegistry.java 12841 2007-02-16 08:58:38Z peterit $
 */
public class WorkspaceContainerRegistry {

    /*# private WorkspaceContainerRegistry _workspaceRegistry; */
    private static WorkspaceContainerRegistry instance = null;

  //map that contain a map of items
    private Map workspaces;
    private String defaultWorkspace;

    protected WorkspaceContainerRegistry() {
       workspaces = new HashMap();
    }

    public static WorkspaceContainerRegistry getInstance() {
        if (instance == null) {
            instance = new WorkspaceContainerRegistry ();
        }
        return instance;
    }


    public TreeMap getWorkspaceContainer(String name) {

        if(workspaces.get(name) == null)
            initWorkspaceContainer(name);

        return (TreeMap)workspaces.get(name);
    }

     private void initWorkspaceContainer(String name) {

        TreeMap workspace = new TreeMap();
        workspaces.put(name, workspace);

    }

}
