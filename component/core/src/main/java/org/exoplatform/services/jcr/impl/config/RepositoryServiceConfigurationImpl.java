/**
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 */

package org.exoplatform.services.jcr.impl.config;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.jcr.RepositoryException;

import org.exoplatform.container.configuration.ConfigurationManager;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.container.xml.ValueParam;
import org.exoplatform.services.jcr.config.ConfigurationPersister;
import org.exoplatform.services.jcr.config.RepositoryConfigurationException;
import org.exoplatform.services.jcr.config.RepositoryServiceConfiguration;
import org.exoplatform.services.naming.InitialContextInitializer;
import org.jibx.runtime.BindingDirectory;
import org.jibx.runtime.IBindingFactory;
import org.jibx.runtime.IMarshallingContext;
import org.jibx.runtime.JiBXException;

/**
 * Created by The eXo Platform SARL .
 * 
 * @author Gennady Azarenkov
 * @version $Id: RepositoryServiceConfigurationImpl.java 12841 2007-02-16
 *          08:58:38Z peterit $
 */

public class RepositoryServiceConfigurationImpl extends RepositoryServiceConfiguration {

  private ValueParam             param;

  private ConfigurationManager   configurationService;

  private ConfigurationPersister configurationPersister;

  public RepositoryServiceConfigurationImpl(InitParams params,
      ConfigurationManager configurationService,
      InitialContextInitializer initialContextInitializer) throws RepositoryConfigurationException {

    param = params.getValueParam("conf-path");

    if (params.getPropertiesParam("working-conf") != null) {
      if (params.getPropertiesParam("working-conf").getProperty("persisterClassName") != null) {
        try {
          Class<ConfigurationPersister> configurationPersisterClass = (Class<ConfigurationPersister>) Class
              .forName(params.getPropertiesParam("working-conf").getProperty("persisterClassName"));
          configurationPersister = configurationPersisterClass.newInstance();
          configurationPersister.init(params.getPropertiesParam("working-conf"));
        } catch (InstantiationException e) {
          throw new RepositoryConfigurationException(e.getLocalizedMessage());
        } catch (IllegalAccessException e) {
          throw new RepositoryConfigurationException(e.getLocalizedMessage());
        } catch (ClassNotFoundException e) {
          throw new RepositoryConfigurationException(e.getLocalizedMessage());
        }
      }
    }
    this.configurationService = configurationService;

    InputStream jcrConfigurationInputStream;
    try {
      jcrConfigurationInputStream = configurationService.getInputStream(param.getValue());
      if (configurationPersister != null) {
        if (!configurationPersister.hasConfig()) {
          configurationPersister.write(jcrConfigurationInputStream);
        }
        init(configurationPersister.read());
      } else {
        init(jcrConfigurationInputStream);
        jcrConfigurationInputStream.close();
      }
    } catch (Exception e) {
      throw new RepositoryConfigurationException("Fail to init from xml! Reason: " + e, e); 
    }
  }


  public RepositoryServiceConfigurationImpl(InputStream is) throws RepositoryConfigurationException {
    init(is);
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.exoplatform.services.jcr.config.RepositoryServiceConfiguration#isRetainable()
   */
  public boolean isRetainable() {
    if (configurationPersister != null) {
      return true;
    }
    String strfileUri = param.getValue();
    URL fileURL;
    try {
      fileURL = configurationService.getURL(strfileUri);

    } catch (Exception e) {
      return false;
    }
    return fileURL.getProtocol().equals("file");
  }

  /**
   * Retain configuration of JCR If configurationPersister is configured it
   * write data in to the persister otherwise it try to save configuration in
   * file
   * 
   * @throws RepositoryException
   */
  public void retain() throws RepositoryException {
    try {

      if (!isRetainable())
        throw new RepositoryException("Unsupported  configuration place "
            + configurationService.getURL(param.getValue())
            + " If you want to save configuration, start repository from standalone file."
            + " Or persisterClassName not configured");

      OutputStream saveStream = null;

      if (configurationPersister != null) {
        saveStream = new ByteArrayOutputStream();
      } else {
        URL filePath = configurationService.getURL(param.getValue());
        File sourceConfig = new File(filePath.toURI());
        SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmm");
        File backUp = new File(sourceConfig.getAbsoluteFile() + "_" + format.format(new Date()));
        if (!sourceConfig.renameTo(backUp))
          throw new RepositoryException("Can't back up configuration on path "
              + sourceConfig.getAbsolutePath());
        saveStream = new FileOutputStream(sourceConfig);
      }

      IBindingFactory bfact = BindingDirectory.getFactory(RepositoryServiceConfiguration.class);
      IMarshallingContext mctx = bfact.createMarshallingContext();

      mctx.marshalDocument(this, "ISO-8859-1", null, saveStream);
      saveStream.close();

      // writing configuration in to the persister
      if (configurationPersister != null) {
        configurationPersister.write(new ByteArrayInputStream(((ByteArrayOutputStream) saveStream)
            .toByteArray()));
      }

    } catch (JiBXException e) {
      throw new RepositoryException(e);
    } catch (FileNotFoundException e) {
      throw new RepositoryException(e);
    } catch (IOException e) {
      throw new RepositoryException(e);
    } catch (RepositoryConfigurationException e) {
      throw new RepositoryException(e);
    } catch (Exception e) {
      throw new RepositoryException(e);
    }

  }
}