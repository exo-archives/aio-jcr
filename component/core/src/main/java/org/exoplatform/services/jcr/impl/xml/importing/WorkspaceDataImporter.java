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
package org.exoplatform.services.jcr.impl.xml.importing;

import javax.jcr.ImportUUIDBehavior;

import org.apache.commons.logging.Log;
import org.exoplatform.services.ext.action.InvocationContext;
import org.exoplatform.services.jcr.impl.Constants;
import org.exoplatform.services.jcr.impl.core.NodeImpl;
import org.exoplatform.services.jcr.impl.xml.XmlSaveType;
import org.exoplatform.services.log.ExoLogger;

/**
 * Created by The eXo Platform SAS.
 * 
 * @author <a href="mailto:Sergey.Kabashnyuk@gmail.com">Sergey Kabashnyuk</a>
 * @version $Id: $
 */
public class WorkspaceDataImporter extends StreamImporter {
  private final Log log = ExoLogger.getLogger("jcr.WorkspaceDataImporter");

  public WorkspaceDataImporter(InvocationContext context) {
    super((NodeImpl) context.getCurrentItem(),
          ImportUUIDBehavior.IMPORT_UUID_COLLISION_THROW,
          XmlSaveType.WORKSPACE,
          context);
    if (!Constants.ROOT_PATH.equals(((NodeImpl) context.getCurrentItem()).getData().getQPath())) {
      throw new IllegalArgumentException("Current element should be root");
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.exoplatform.services.jcr.impl.xml.importing.StreamImporter#createContentImporter(org.exoplatform.services.jcr.impl.core.NodeImpl,
   *      int, org.exoplatform.services.jcr.impl.xml.XmlSaveType,
   *      org.exoplatform.services.ext.action.InvocationContext)
   */
  @Override
  public ContentImporter createContentImporter(NodeImpl parent,
                                               int uuidBehavior,
                                               XmlSaveType saveType,
                                               InvocationContext context) {
    return new WorkspaceContentImporter(parent, uuidBehavior, saveType, context);
  }

}
