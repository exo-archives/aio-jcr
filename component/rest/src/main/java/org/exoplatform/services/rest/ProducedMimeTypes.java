/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.services.rest;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import org.exoplatform.services.rest.data.MimeTypes;

/**
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id: $
 * 
 * ProducedMimeTypes defines the produced mimetype for ResourceContainer
 */
@Target({METHOD})
@Retention(RUNTIME)
public @interface ProducedMimeTypes {
  String value() default MimeTypes.ALL;
}
