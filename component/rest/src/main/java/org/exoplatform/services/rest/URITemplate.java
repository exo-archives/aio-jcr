/**
 * Copyright 2001-2007 The eXo Platform SAS         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 */

package org.exoplatform.services.rest;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Created by The eXo Platform SARL        .
 * @author Gennady Azarenkov
 * @version $Id: $
 */

/**
 * URITemplate defines URI pattern for ResourceContainer
 */
@Target(value={TYPE,METHOD})
@Retention(RUNTIME)
public @interface URITemplate {
  String value() default "/";
}
