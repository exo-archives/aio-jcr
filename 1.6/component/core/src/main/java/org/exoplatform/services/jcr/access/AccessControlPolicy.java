/**
 * Copyright 2001-2003 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 */

package org.exoplatform.services.jcr.access;

/**
 * Created by The eXo Platform SARL        .
 * @author <a href="mailto:gennady.azarenkov@exoplatform.com">Gennady Azarenkov</a>
 * @version $Id: AccessControlPolicy.java 12843 2007-02-16 09:11:18Z peterit $
 */

public interface AccessControlPolicy {
  
  public static final String DISABLE = "disable";
  public static final String OPTIONAL = "optional";
  public static final String MANDATORY = "mandatory";

}
