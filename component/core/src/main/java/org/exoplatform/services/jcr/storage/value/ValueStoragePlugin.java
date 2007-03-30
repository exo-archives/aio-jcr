/**
 * Copyright 2001-2003 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 */

package org.exoplatform.services.jcr.storage.value;

import java.io.IOException;
import java.util.List;
import java.util.Properties;

import javax.naming.NamingException;

import org.exoplatform.services.jcr.config.RepositoryConfigurationException;
import org.exoplatform.services.jcr.storage.WorkspaceStorageConnection;

/**
 * Created by The eXo Platform SARL        .
 * @author <a href="mailto:gennady.azarenkov@exoplatform.com">Gennady Azarenkov</a>
 * @version $Id: ValueStoragePlugin.java 12843 2007-02-16 09:11:18Z peterit $
 */

public abstract class ValueStoragePlugin {
  
  protected List <ValuePluginFilter> filters;
    
//  protected BinaryValueSwapStorage swapStorage = null; 
//  protected boolean enableSwap = false;
  
  /**
   * Initialize this plugin.
   * 
   * @param props
   * @throws RepositoryConfigurationException
   * @throws IOException
   * @throws NamingException
   */
  public abstract void init(Properties props) throws RepositoryConfigurationException, IOException;

  public abstract ValueIOChannel openIOChannel() throws IOException;
 
  /**
   * @return filters
   */
  public final List <ValuePluginFilter> getFilters() {
    return filters;
  }
  
  public final void setFilters(List <ValuePluginFilter> filters) {
    this.filters = filters;
  }

//  /**
//   * Initialize this plugin to work with binary swap if given parameter not is null and enable this option.
//   * If binarySwap is null swopping will be disabled in anycase.
//   * 
//   * @see: disableBinarySwap(), enableBinarySwap()  
//   */
//  public void initBinarySwap(BinaryValueSwapStorage swap) {
//    this.swapStorage = swap;
//    enableBinarySwap();
//  }
//  
//  /**
//   * Enable swapping if binary swap was successful initialized before.
//   */
//  public void enableBinarySwap() {
//    this.enableSwap = swapStorage != null;
//  }
//  
//  /**
//   * Disabe swapping.
//   * 
//   *  @see: enableBinarySwap() for enable operations after disable
//   */
//  public void disableBinarySwap() {
//    this.enableSwap = false;
//  }
//  
//  public boolean isBinarySwapEnabled() {
//    return enableSwap;
//  }
  
  /**
   * Run consistency check operation
   * 
   * @param dataConnection - connection to metadata storage
   */
  public abstract void checkConsistency(WorkspaceStorageConnection dataConnection);
    
}

