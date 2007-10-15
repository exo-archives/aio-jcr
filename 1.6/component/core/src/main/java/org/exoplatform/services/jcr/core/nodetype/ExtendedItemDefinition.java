/**
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 */

package org.exoplatform.services.jcr.core.nodetype;

import javax.jcr.nodetype.ItemDefinition;
/**
 * Created by The eXo Platform SARL        .
 * @author Gennady Azarenkov
 * @version $Id: ExtendedItemDefinition.java 12843 2007-02-16 09:11:18Z peterit $
 */

public interface ExtendedItemDefinition extends ItemDefinition {

  public static String RESIDUAL_SET = "*";

  boolean isResidualSet();
}
