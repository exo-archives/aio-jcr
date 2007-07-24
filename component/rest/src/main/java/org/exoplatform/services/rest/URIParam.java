/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.services.rest;

import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * UriParam define the names of URI. In this way ResourceContainer gets only URI
 * parameters wich it needs. For example:<br/>
 * <pre>
 * URI pattern: /level1/{id1}/level2/{id2}/level3
 * and URI:     /level1/myID1/level2/myID2/level3
 * ...
 * public getMethod(@URIParam(&quot;id2&quot;) String id) {
 * ...
 * }
 * </pre>
 * <br/>
 * Method getMethod gets URI parameter "id2" (in this example myID2) as String id.<br/>
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id: $
 */
@Target(value = {PARAMETER})
@Retention(RUNTIME)
public @interface URIParam {
  String value();
}
