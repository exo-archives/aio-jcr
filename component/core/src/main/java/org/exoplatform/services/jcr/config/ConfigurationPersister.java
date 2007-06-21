package org.exoplatform.services.jcr.config;

import java.io.InputStream;

public interface ConfigurationPersister {

  /**
   * Read config data
   * @return - config data stream
   */
  InputStream read();
  
  /**
   * Create table, write data
   * @param confData - config data stream
   */
  void write(InputStream confData);
  
  /**
   * Tell if the config exists
   * @return - flag
   */
  boolean hasConfig();
  
}
