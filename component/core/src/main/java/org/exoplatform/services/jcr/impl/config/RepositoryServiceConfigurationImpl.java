/**
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 */

package org.exoplatform.services.jcr.impl.config;

import java.io.InputStream;

import org.exoplatform.container.configuration.ConfigurationManager;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.container.xml.ValueParam;
import org.exoplatform.services.jcr.config.RepositoryConfigurationException;
import org.exoplatform.services.jcr.config.RepositoryServiceConfiguration;

/**
 * Created by The eXo Platform SARL .
 * 
 * @author Gennady Azarenkov
 * @version $Id: RepositoryServiceConfigurationImpl.java 12841 2007-02-16 08:58:38Z peterit $
 */

public class RepositoryServiceConfigurationImpl extends RepositoryServiceConfiguration {

	public RepositoryServiceConfigurationImpl(InitParams params,
			ConfigurationManager configurationService)
			throws RepositoryConfigurationException {
 
		try {
			ValueParam param = params.getValueParam("conf-path");
			InputStream is = configurationService.getInputStream((String) param.getValue());
			init(is);
		} catch (Exception e) {
			throw new RepositoryConfigurationException(
					"XML config data not found! Reason: " + e);
		}

	}

	public RepositoryServiceConfigurationImpl(InputStream is) throws RepositoryConfigurationException {
		init(is);
	}
	
}