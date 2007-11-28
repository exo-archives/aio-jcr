/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SAS. All rights reserved.          *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
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
public class BackupDataImporter extends StreamImporter {
  /**
   * 
   */
  private final Log log = ExoLogger.getLogger("jcr.BackupImporter");

  public BackupDataImporter(InvocationContext context) {
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
    return new BackupContentImporter(parent, uuidBehavior, saveType, context);
  }

}
