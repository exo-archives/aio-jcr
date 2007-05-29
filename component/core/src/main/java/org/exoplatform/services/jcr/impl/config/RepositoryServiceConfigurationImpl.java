/**
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 */

package org.exoplatform.services.jcr.impl.config;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.jcr.RepositoryException;

import org.exoplatform.container.configuration.ConfigurationManager;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.container.xml.ValueParam;
import org.exoplatform.services.jcr.config.RepositoryConfigurationException;
import org.exoplatform.services.jcr.config.RepositoryServiceConfiguration;
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

  private ValueParam param;

  public RepositoryServiceConfigurationImpl(InitParams params,
      ConfigurationManager configurationService) throws RepositoryConfigurationException {

    try {
      param = params.getValueParam("conf-path");
      InputStream is = configurationService.getInputStream(param.getValue());
      init(is);
    } catch (Exception e) {
      throw new RepositoryConfigurationException("XML config data not found! Reason: " + e);
    }

  }

  public RepositoryServiceConfigurationImpl(InputStream is) throws RepositoryConfigurationException {
    init(is);
  }

  public ValueParam getParam() {
    return param;
  }

  public boolean canSave() {
    String fileUri = getParam().getValue();
    return fileUri.startsWith("file:");
  }
  /**
   * Replace configuration file with runtime configuration.
   * 
   * @throws RepositoryException
   */
  
  public void saveConfiguration() throws RepositoryException {
    try {
      String fileUri = getParam().getValue();
      if (!canSave())
        throw new RepositoryException("Unsupported  configuration place " + fileUri
            + " If you want to save configuration, start repository from standalone file");

      File sourceConfig = new File(fileUri.substring("file:".length()).trim());
      SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmm");
      File backUp = new File(sourceConfig.getAbsoluteFile() + "_" + format.format(new Date()));
      if (!sourceConfig.renameTo(backUp))
        throw new RepositoryException("Can't back up configuration on path "
            + sourceConfig.getAbsolutePath());

      IBindingFactory bfact = BindingDirectory.getFactory(RepositoryServiceConfiguration.class);
      IMarshallingContext mctx = bfact.createMarshallingContext();

      mctx.marshalDocument(this, "ISO-8859-1", null, new FileOutputStream(sourceConfig));
    } catch (JiBXException e) {

      throw new RepositoryException(e);
    } catch (FileNotFoundException e) {
      throw new RepositoryException(e);
    }

  }
}