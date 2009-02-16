/*
 * Copyright (C) 2003-2009 eXo Platform SAS.
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

import java.io.File;

import org.apache.commons.logging.Log;
import org.exoplatform.services.jcr.impl.storage.value.ValueFile;
import org.exoplatform.services.jcr.impl.storage.value.cas.RecordNotFoundException;
import org.exoplatform.services.jcr.impl.storage.value.cas.VCASException;
import org.exoplatform.services.jcr.impl.storage.value.cas.ValueContentAddressStorage;
import org.exoplatform.services.jcr.impl.util.io.FileCleaner;
import org.exoplatform.services.log.ExoLogger;

/**
 * Created by The eXo Platform SAS. <br/>Date:
 * 
 * @author <a href="karpenko.sergiy@gmail.com">Karpenko Sergiy</a>
 * @version $Id: CASValueFileImpl.java 111 2008-11-11 11:11:11Z serg $
 */
public class CASValueFileImpl implements ValueFile {

  static private final Log   LOG = ExoLogger.getLogger("jcr.CASValueFileImpl");

  String                     propId;

  ValueContentAddressStorage vcas;

  File                       file;

  FileCleaner                cleaner;

  CASValueFileImpl(String propId, File file, ValueContentAddressStorage vcas) {
    this.propId = propId;
    this.vcas = vcas;
    this.file = file;
  }

  CASValueFileImpl(String propId, File file, ValueContentAddressStorage vcas, FileCleaner cleaner) {
    this(propId, file, vcas);
    this.cleaner = cleaner;
  }

  public void rollback() {
    try {
      vcas.delete(propId);
      if (!vcas.hasSharedContent(propId)) {
        if (!file.delete() && cleaner != null) {
          cleaner.addFile(file);
        }
      }
    } catch (RecordNotFoundException e) {
      LOG.error(e.getMessage(), e);
    } catch (VCASException e) {
      LOG.error(e.getMessage(), e);
    }
  }

}
