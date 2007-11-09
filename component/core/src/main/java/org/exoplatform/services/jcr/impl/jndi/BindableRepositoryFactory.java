/**
 **************************************************************************
 * Copyright 2001-2003 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 */

package org.exoplatform.services.jcr.impl.jndi;

import java.util.Hashtable;
import java.util.Map;

import javax.naming.Context;
import javax.naming.Name;
import javax.naming.RefAddr;
import javax.naming.Reference;
import javax.naming.spi.ObjectFactory;

import org.apache.commons.collections.map.ReferenceMap;
import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.container.StandaloneContainer;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.core.ManageableRepository;

/**
 * Created by The eXo Platform SARL .<br/>
 * 
 * ObjectFactory to produce BindableRepositoryImpl objects 
 * 
 * @author <a href="mailto:lautarul@gmail.com">Roman Pedchenko</a>
 * @version $Id: BindableRepositoryFactory.java 12841 2007-02-16 08:58:38Z peterit $
 */

public class BindableRepositoryFactory implements ObjectFactory {
  
  static final String REPOSITORYNAME_ADDRTYPE = "repositoryName";
  static final String CONTAINERCONFIG_ADDRTYPE = "containerConfig";

  private static Map cache = new ReferenceMap(ReferenceMap.HARD,
      ReferenceMap.SOFT);

  public BindableRepositoryFactory() {
  }

  public Object getObjectInstance(Object obj, Name name, Context nameCtx,
      Hashtable environment) throws Exception {
    if (obj instanceof Reference) {
      Reference ref = (Reference) obj;
      synchronized (cache) {
        if (cache.containsKey(ref)) {
          return cache.get(ref);
        }
        RefAddr containerConfig = ref.get(CONTAINERCONFIG_ADDRTYPE);
        String repositoryName = (String) ref.get(REPOSITORYNAME_ADDRTYPE).getContent();
        ExoContainer container = ExoContainerContext.getCurrentContainerIfPresent();
        if (containerConfig != null) {
          // here the code will work properly only when no StandaloneContainer instance created yet
          if(container == null) {
            StandaloneContainer.setConfigurationURL((String) containerConfig.getContent());
            container = StandaloneContainer.getInstance();
          }
        }
        ManageableRepository rep = ((RepositoryService)container.getComponentInstanceOfType(RepositoryService.class))
        .getRepository(repositoryName);
//        BindableRepositoryImpl brep = new BindableRepositoryImpl(rep);
        cache.put(ref, rep);
        return rep;
      }
    }
    return null;
  }
}
