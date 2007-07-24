/**
 * Copyright 2001-2007 The eXo Platform SAS         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 */

package org.exoplatform.services.rest;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Created by The eXo Platform SARL.<br/> HTTPMethod defines HTTP method for
 * ResourceContainer
 * @author Gennady Azarenkov
 * @version $Id: $
 */
@Target(value = { METHOD })
@Retention(RUNTIME)
public @interface HTTPMethod {
  String value();
}
