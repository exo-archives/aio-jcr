/**
 * Copyright 2001-2007 The eXo Platform SAS         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 */

package org.exoplatform.services.rest;

import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Created by The eXo Platform SARL        .
 * @author Gennady Azarenkov
 * @version $Id: $
 */

/**
 * HeaderParam define the names of header from HTTP request.
 * In this way ResourceContainer gets only header parameters wich it needs.
 * 
 * For example:
 * ...
 * public getMethod(@HeaderParam("accept") String accept_param) {
 * ...
 * }
 * Method getMethod gets header parameter "accept" as String accept_param
 */
@Target(value={PARAMETER})
@Retention(RUNTIME)
public @interface HeaderParam {
  String value();
}
