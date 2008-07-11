package org.exoplatform.services.jcr.config;

import java.io.InputStream;

import org.exoplatform.container.xml.PropertiesParam;

public interface ConfigurationPersister {

  /**
   * Init persister.
   * 
   * @param params - PropertiesParam
   * @throws RepositoryConfigurationException
   */
  void init(PropertiesParam params) throws RepositoryConfigurationException;

  /**
   * Read config data.
   * 
   * @return - config data stream
   * @throws RepositoryConfigurationException
   */
  InputStream read() throws RepositoryConfigurationException;

  /**
   * Create table, write data.
   * 
   * @param confData - config data stream
   * @throws RepositoryConfigurationException
   */
  void write(InputStream confData) throws RepositoryConfigurationException;

  /**
   * Tell if the config exists.
   * 
   * @return - flag
   * @throws RepositoryConfigurationException
   */
  boolean hasConfig() throws RepositoryConfigurationException;

}
