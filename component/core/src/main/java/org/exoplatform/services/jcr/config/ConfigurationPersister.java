package org.exoplatform.services.jcr.config;

import java.io.InputStream;

import org.exoplatform.container.xml.PropertiesParam;

public interface ConfigurationPersister {

  /**
   * Init persister. Used by RepositoryServiceConfiguration on init.
   * 
   * @return - config data stream
   */
  void init(PropertiesParam params) throws RepositoryConfigurationException;

  /**
   * Read config data.
   * 
   * @return - config data stream
   */
  InputStream read() throws RepositoryConfigurationException;

  /**
   * Create table, write data.
   * 
   * @param confData
   *          - config data stream
   */
  void write(InputStream confData) throws RepositoryConfigurationException;

  /**
   * Tell if the config exists.
   * 
   * @return - flag
   */
  boolean hasConfig() throws RepositoryConfigurationException;

}
