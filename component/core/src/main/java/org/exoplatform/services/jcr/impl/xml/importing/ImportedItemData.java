/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SAS. All rights reserved.          *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.services.jcr.impl.xml.importing;

import org.exoplatform.services.jcr.datamodel.QPath;

/**
 * Created by The eXo Platform SAS
 * 
 * @author <a href="mailto:Sergey.Kabashnyuk@gmail.com">Sergey Kabashnyuk</a>
 * @version $Id: $
 */
public interface ImportedItemData {
  /**
   * Set parent identifer
   * 
   * @param identifer
   */
  public void setParentIdentifer(String identifer);

  /**
   * Set path of item
   * 
   * @param path
   */
  public void setQPath(QPath path);
}
