/**
 * Copyright 2001-2007 The eXo Platform SAS         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 */

package org.exoplatform.services.rest.container;

import java.lang.reflect.Method;
import java.lang.annotation.Annotation;

import org.exoplatform.services.rest.URIPattern;

/**
 * Created by The eXo Platform SARL        .
 * @author Gennady Azarenkov
 * @version $Id: $
 */

public interface ResourceDescriptor {
  String getAcceptableMethod();
  String getConsumeMediaType();
  String getProduceMediaType();
  ResourceWrapper getWrapper();
  Method getServer();
  URIPattern getURIPattern();
  Annotation[] getMethodParameterAnnotations();
  Class[] getMethodParameters();
}
