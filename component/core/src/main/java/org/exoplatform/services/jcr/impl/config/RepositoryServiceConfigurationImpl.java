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
package org.exoplatform.services.jcr.impl.config;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.jcr.RepositoryException;

import org.jibx.runtime.BindingDirectory;
import org.jibx.runtime.IBindingFactory;
import org.jibx.runtime.IMarshallingContext;
import org.jibx.runtime.JiBXException;

import org.exoplatform.container.configuration.ConfigurationManager;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.container.xml.ValueParam;
import org.exoplatform.services.jcr.config.ConfigurationPersister;
import org.exoplatform.services.jcr.config.RepositoryConfigurationException;
import org.exoplatform.services.jcr.config.RepositoryServiceConfiguration;
import org.exoplatform.services.naming.InitialContextInitializer;

/**
 * Created by The eXo Platform SAS.
 * 
 * @author Gennady Azarenkov
 * @version $Id$
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
      String cn = params.getPropertiesParam("working-conf").getProperty("persister-class-name");
      if (cn == null)
        cn = params.getPropertiesParam("working-conf").getProperty("persisterClassName"); // try old
      // name,
      // pre 1.9
      if (cn != null) {
        try {
          Class<ConfigurationPersister> configurationPersisterClass = (Class<ConfigurationPersister>) Class.forName(cn);
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
   * Retain configuration of JCR If configurationPersister is configured it write data in to the
   * persister otherwise it try to save configuration in file
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
        configurationPersister.write(new ByteArrayInputStream(((ByteArrayOutputStream) saveStream).toByteArray()));
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
