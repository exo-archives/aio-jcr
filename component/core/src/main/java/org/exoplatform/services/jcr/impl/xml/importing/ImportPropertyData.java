/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SAS. All rights reserved.          *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.services.jcr.impl.xml.importing;

import org.apache.commons.logging.Log;
import org.exoplatform.services.jcr.datamodel.QPath;
import org.exoplatform.services.jcr.impl.dataflow.TransientPropertyData;
import org.exoplatform.services.log.ExoLogger;

/**
 * Created by The eXo Platform SAS
 * 
 * @author <a href="mailto:Sergey.Kabashnyuk@gmail.com">Sergey Kabashnyuk</a>
 * @version $Id: $
 */
public class ImportPropertyData extends TransientPropertyData implements ImportItemData {
  private static Log log = ExoLogger.getLogger("jcr.ImportedPropertyData");

  public ImportPropertyData() {

  }

  public ImportPropertyData(QPath path,
                              String identifier,
                              int version,
                              int type,
                              String parentIdentifier,
                              boolean multiValued) {
    super(path, identifier, version, type, parentIdentifier, multiValued);
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.exoplatform.services.jcr.impl.xml.importing.ImportedItemData#setParentIdentifer(java.lang.String)
   */
  public void setParentIdentifer(String identifer) {
    this.parentIdentifier = identifer;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.exoplatform.services.jcr.impl.xml.importing.ImportedItemData#setQPath(org.exoplatform.services.jcr.datamodel.QPath)
   */
  public void setQPath(QPath path) {
    this.qpath = path;
  }

}
