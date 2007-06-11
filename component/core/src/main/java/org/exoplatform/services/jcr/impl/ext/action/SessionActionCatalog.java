/**
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 */

package org.exoplatform.services.jcr.impl.ext.action;

import javax.jcr.RepositoryException;

import org.apache.commons.logging.Log;
import org.exoplatform.container.component.ComponentPlugin;
import org.exoplatform.services.command.action.Action;
import org.exoplatform.services.command.action.ActionCatalog;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.config.RepositoryConfigurationException;
import org.exoplatform.services.jcr.datamodel.InternalQName;
import org.exoplatform.services.jcr.datamodel.QPath;
import org.exoplatform.services.jcr.impl.core.LocationFactory;
import org.exoplatform.services.jcr.impl.core.RepositoryImpl;
import org.exoplatform.services.jcr.observation.ExtendedEvent;
import org.exoplatform.services.log.ExoLogger;


/**
 * Created by The eXo Platform SARL        .
 * @author Gennady Azarenkov
 * @version $Id: SessionActionCatalog.java 13003 2007-02-27 14:16:39Z ksm $
 */

public class SessionActionCatalog extends ActionCatalog {
  
  private static Log log = ExoLogger.getLogger("jcr.SessionActionCatalog");
  
  private LocationFactory locFactory;

  public SessionActionCatalog(RepositoryService repService) throws 
  RepositoryException, RepositoryConfigurationException {
    
    RepositoryImpl rep = (RepositoryImpl)repService.getCurrentRepository();
    locFactory = rep.getLocationFactory();
    
    // add predefined actions
//    SessionEventMatcher matcher = new SessionEventMatcher(
//        ExtendedEvent.PROPERTY_ADDED | ExtendedEvent.PROPERTY_CHANGED, //ExtendedEvent.NODE_ADDED, // | ExtendedEvent.ADD_MIXIN, 
//        null, true, Constants.NT_RESOURCE, null, null);
//    addAction(matcher, new AddMetadata());
    
  }
  
  public void addPlugin(ComponentPlugin plugin) {
    if (plugin instanceof AddActionsPlugin) {
      AddActionsPlugin cplugin = (AddActionsPlugin) plugin;
      for(ActionConfiguration ac:cplugin.getActions()) {
        try {
          
          SessionEventMatcher matcher = new SessionEventMatcher(
            getEventTypes(ac.getEventTypes()), 
            getPaths(ac.getPath()),
            ac.isDeep(),
            getNames(ac.getNodeType()),
            getNames(ac.getParentNodeType()),
            getWorkspaces(ac.getWorkspace()));

          Action action = (Action)Class.forName(ac.getActionClassName()).newInstance();
          addAction(matcher, action);
        } catch (Exception e) {
          e.printStackTrace();
        } 
      }
    }
  }
  

  private String[] getWorkspaces(String workspaces) throws RepositoryException {
    if(workspaces == null)
      return null;
    return workspaces.split(",");
  }
  
  private QPath[] getPaths(String paths) throws RepositoryException {
    if(paths == null)
      return null;

    String[] pathList = paths.split(",");
    QPath[] qpaths = new QPath[pathList.length];
    for(int i=0; i<pathList.length; i++) {
      qpaths[i] = locFactory.parseAbsPath(pathList[i]).getInternalPath();
    }
    return qpaths;
  }

  private InternalQName[] getNames(String names) throws RepositoryException {
    if(names == null)
      return null;
    
    String[] nameList = names.split(",");
    InternalQName[] qnames = new InternalQName[nameList.length];
    for(int i=0; i<nameList.length; i++) {
      qnames[i] = locFactory.parseJCRName(nameList[i]).getInternalName();
    }
    return qnames;
  }

  private static int getEventTypes(String names) {
    if(names == null)
      return -1;
    
    String[] nameList = names.split(",");
    int res = 0;
    
    for (String name : nameList) {
      if (name.equalsIgnoreCase("addNode")) {
        res |= ExtendedEvent.NODE_ADDED;
      } else if (name.equalsIgnoreCase("addProperty")) {
        res |= ExtendedEvent.PROPERTY_ADDED;
      } else if (name.equalsIgnoreCase("changeProperty")) {
        res |= ExtendedEvent.PROPERTY_CHANGED;
      } else if (name.equalsIgnoreCase("addMixin")) {
        res |= ExtendedEvent.ADD_MIXIN;
      } else if (name.equalsIgnoreCase("removeProperty")) {
        res |= ExtendedEvent.PROPERTY_REMOVED;
      } else if (name.equalsIgnoreCase("removeNode")) {
        res |= ExtendedEvent.NODE_REMOVED;
      } else if (name.equalsIgnoreCase("removeMixin")) {
        res |= ExtendedEvent.REMOVE_MIXIN;
      } else if (name.equalsIgnoreCase("lock")) {
        res |= ExtendedEvent.LOCK;
      } else if (name.equalsIgnoreCase("unlock")) {
        res |= ExtendedEvent.UNLOCK;
      } else if (name.equalsIgnoreCase("checkin")) {
        res |= ExtendedEvent.CHECKIN;
      } else if (name.equalsIgnoreCase("checkout")) {
        res |= ExtendedEvent.CHECKOUT;
      } else if (name.equalsIgnoreCase("read")) {
        res |= ExtendedEvent.READ;
      }
      else {
        log.error("Unknown event type '"+name+"' ignored");
      }
    }
    return res;
  }

}
