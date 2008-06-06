/*
 * Copyright (C) 2003-2008 eXo Platform SAS.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
 */
package org.exoplatform.services.jcr.impl.core.query;

import java.util.ArrayList;
import java.util.List;

import javax.jcr.RepositoryException;

import org.apache.commons.logging.Log;
import org.exoplatform.services.document.DocumentReaderService;
import org.exoplatform.services.jcr.config.QueryHandlerEntry;
import org.exoplatform.services.jcr.config.RepositoryConfigurationException;
import org.exoplatform.services.jcr.dataflow.ItemStateChangesLog;
import org.exoplatform.services.jcr.impl.Constants;
import org.exoplatform.services.jcr.impl.core.NamespaceRegistryImpl;
import org.exoplatform.services.jcr.impl.core.nodetype.NodeTypeManagerImpl;
import org.exoplatform.services.jcr.impl.dataflow.persistent.WorkspacePersistentDataManager;
import org.exoplatform.services.log.ExoLogger;

/**
 * Created by The eXo Platform SAS.
 * 
 * @author <a href="mailto:Sergey.Kabashnyuk@gmail.com">Sergey Kabashnyuk</a>
 * @version $Id: SystemSearchManager.java 13891 2008-05-05 16:02:30Z pnedonosko $
 */
public class SystemSearchManager extends SearchManager {

  /**
   * Class logger.
   */
  private final Log                 log              = ExoLogger.getLogger("jcr.SystemSearchManager");

  private boolean                   isStarted        = false;

  private List<ItemStateChangesLog> changesLogBuffer = new ArrayList<ItemStateChangesLog>();

  public static final String        INDEX_DIR_SUFFIX = "system";

  public SystemSearchManager(QueryHandlerEntry config,
                             NamespaceRegistryImpl nsReg,
                             NodeTypeManagerImpl ntReg,
                             WorkspacePersistentDataManager itemMgr,
                             DocumentReaderService service) throws RepositoryException,
      RepositoryConfigurationException {
    super(config, nsReg, ntReg, itemMgr, null, service);
  }

  @Override
  public void onSaveItems(ItemStateChangesLog changesLog) {
    if (!isStarted) {
      changesLogBuffer.add(changesLog);
    } else {
      super.onSaveItems(changesLog);
    }
  }

  @Override
  public void start() {
    // SystemSearchManager.start can be called twice
    // 1. From system WorkspaceContainer
    // 2. From RepositoryContainer
    // to avoid double initialization
    indexingRoot = Constants.JCR_SYSTEM_PATH;
    excludedPaths.remove(Constants.JCR_SYSTEM_PATH);
    isStarted = true;
    handler.init();
    for (ItemStateChangesLog bufferedChangesLog : changesLogBuffer) {
      super.onSaveItems(bufferedChangesLog);
    }
  }

  @Override
  protected QueryHandlerContext createQueryHandlerContext(QueryHandler parentHandler) throws RepositoryConfigurationException {
    QueryHandlerContext context = new QueryHandlerContext(itemMgr,
                                                          Constants.SYSTEM_UUID,
                                                          ntReg,
                                                          nsReg,
                                                          parentHandler,
                                                          config.getIndexDir() + "_" + INDEX_DIR_SUFFIX,
                                                          extractor);
    return context;
  }
}
