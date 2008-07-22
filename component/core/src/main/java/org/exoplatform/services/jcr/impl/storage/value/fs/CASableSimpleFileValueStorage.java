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

package org.exoplatform.services.jcr.impl.storage.value.fs;

import java.io.IOException;
import java.util.Properties;

import org.exoplatform.services.jcr.config.RepositoryConfigurationException;
import org.exoplatform.services.jcr.impl.storage.value.cas.ValueContentAddressStorage;
import org.exoplatform.services.jcr.storage.value.ValueIOChannel;

/**
 * Created by The eXo Platform SAS        .
 * @author Gennady Azarenkov
 * @version $Id$
 */

public class CASableSimpleFileValueStorage extends FileValueStorage {
  
  public static final String DIGEST_ALGO_PARAM = "digest-algo";
  public static final String VCAS_TYPE_PARAM = "vcas-type";
  
  private ValueContentAddressStorage vcas;
  
  private String digestAlgo;
  

  @Override
  public void init(Properties props) throws IOException,
      RepositoryConfigurationException {
    super.init(props);
    this.digestAlgo = props.getProperty(DIGEST_ALGO_PARAM);
    String vcasType = props.getProperty(VCAS_TYPE_PARAM);
    
    // get other vcas specific props and make VCAS
    try {
      vcas = (ValueContentAddressStorage) Class.forName(vcasType).newInstance();
    } catch (Exception e) {
      e.printStackTrace();
      throw new RepositoryConfigurationException(e.getMessage());
    }
    vcas.init(props);
  }

  @Override
  public ValueIOChannel openIOChannel() throws IOException {    
    return new CASableSimpleFileIOChannel(rootDir, cleaner, id, vcas, digestAlgo);
  }

}

