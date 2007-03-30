/**
 * Copyright 2001-2003 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 */

package org.exoplatform.services.jcr.impl.storage.value;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import javax.naming.NamingException;

import org.apache.commons.logging.Log;
import org.exoplatform.services.jcr.config.RepositoryConfigurationException;
import org.exoplatform.services.jcr.config.SimpleParameterEntry;
import org.exoplatform.services.jcr.config.ValueStorageEntry;
import org.exoplatform.services.jcr.config.ValueStorageFilterEntry;
import org.exoplatform.services.jcr.config.WorkspaceEntry;
import org.exoplatform.services.jcr.datamodel.PropertyData;
import org.exoplatform.services.jcr.storage.WorkspaceStorageConnection;
import org.exoplatform.services.jcr.storage.value.ValueIOChannel;
import org.exoplatform.services.jcr.storage.value.ValuePluginFilter;
import org.exoplatform.services.jcr.storage.value.ValueStoragePlugin;
import org.exoplatform.services.jcr.storage.value.ValueStoragePluginProvider;
import org.exoplatform.services.log.ExoLogger;
/**
 * Created by The eXo Platform SARL        .
 * Per-workspace factory object for ValueStoragePlugin
 * @author <a href="mailto:gennady.azarenkov@exoplatform.com">Gennady Azarenkov</a>
 * @version $Id: StandaloneStoragePluginProvider.java 13463 2007-03-16 09:17:29Z geaz $
 */

public class StandaloneStoragePluginProvider extends ArrayList<ValueStoragePlugin> implements ValueStoragePluginProvider {
  
  private static Log log = ExoLogger.getLogger("jcr.ValueStoragePluginFactory");
  
//  private BinaryValueSwapStorage binarySwap = null;
  
  public StandaloneStoragePluginProvider(WorkspaceEntry wsConfig) 
    throws RepositoryException, RepositoryConfigurationException, NamingException, IOException {

//    this(null, wsConfig);
//  }
//  
//  public StandaloneStoragePluginProvider(BinaryValueSwapStorage binarySwap, WorkspaceEntry wsConfig) 
//    throws RepositoryException, RepositoryConfigurationException, NamingException, IOException {
//
//    this.binarySwap = binarySwap;
    
    List <ValueStorageEntry> storages = wsConfig.getContainer().getValueStorages();

    if (storages != null) 
      for(ValueStorageEntry storageEntry: storages) {
        Object o = null;
        try {
          o = Class.forName(storageEntry.getType()).newInstance();
        } catch (Exception e) {
          log.error("Value Storage Plugin instantiation FAILED. ", e);
          continue;
        } 
        if(!(o instanceof ValueStoragePlugin)) {
          log.error("Not a ValueStoragePlugin object IGNORED: "+o);
          continue;
        } 
        
        ValueStoragePlugin plugin = (ValueStoragePlugin) o;
        // init filters
        ArrayList <ValuePluginFilter> filters = new ArrayList <ValuePluginFilter>();
        List <ValueStorageFilterEntry> filterEntries = storageEntry.getFilters();
        for(ValueStorageFilterEntry filterEntry : filterEntries) {
          
          ValuePluginFilter filter = new ValuePluginFilter(
              PropertyType.valueFromName(filterEntry.getPropertyType()),
              null,
              null);
          filters.add(filter);
        }
        
        // init properties
        Properties props = new Properties();
        List <SimpleParameterEntry> paramEntries = storageEntry.getParameters();
        for(SimpleParameterEntry paramEntry : paramEntries) {
          props.setProperty(paramEntry.getName(), paramEntry.getValue());
        }
  
        plugin.init(props);
        plugin.setFilters(filters);
        
        // if swapStorage is null swopping will be disabled in anycase
//        plugin.initBinarySwap(this.binarySwap);
        
        add(plugin);
        log.info("Value Storage Plugin initialized "+plugin);
      }    
  }
  
  /**
   * @param property
   * @return ValueIOChannel appropriate for this property (by path, id etc)
   * or null if no such channel found 
   * @throws IOException
   */
  public ValueIOChannel getApplicableChannel(PropertyData property) throws IOException {
    Iterator plugins = iterator();
    while(plugins.hasNext()) {
      ValueStoragePlugin plugin = (ValueStoragePlugin)plugins.next();
      List <ValuePluginFilter> filters = plugin.getFilters();
      for(ValuePluginFilter filter:filters) {
        if(filter.match(property))
          return plugin.openIOChannel();
      }
    }
    return null;
  }

//  public BinaryValueSwapStorage getBinaryValueSwap() {
//    return binarySwap;
//  }

  public Iterator<ValueStoragePlugin> plugins() {
    return iterator();
  }

  public void checkConsistency(WorkspaceStorageConnection dataConnection) {
    Iterator<ValueStoragePlugin> plugins = plugins();
    while(plugins.hasNext()) {
      ValueStoragePlugin plugin = plugins.next();
      plugin.checkConsistency(dataConnection);
    }
  }
}
