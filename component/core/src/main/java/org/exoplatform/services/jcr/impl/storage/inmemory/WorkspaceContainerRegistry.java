/***************************************************************************
 * Copyright 2001-2003 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.services.jcr.impl.storage.inmemory;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

/**
 * Created by The eXo Platform SARL        .
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
