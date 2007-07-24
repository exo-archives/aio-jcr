/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.services.rest;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.ElementType.METHOD;

import org.exoplatform.services.rest.transformer.OutputEntityTransformer;

/**
 * Define entity output transformer. Output transformer is used for
 * serialization entity.
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id: $
 */
@Retention(RUNTIME)
@Target({ TYPE, METHOD })
public @interface OutputTransformer {
  Class < ? extends OutputEntityTransformer > value();
}
