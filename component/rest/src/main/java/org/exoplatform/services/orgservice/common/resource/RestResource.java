/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.services.orgservice.common.resource;

import java.util.List;

import org.exoplatform.services.orgservice.common.template.RestTemplate;
import org.exoplatform.services.orgservice.common.template.Template;


/**
 * Created by The eXo Platform SARL
 * Author : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
 * @version $Id: $
 */

public interface RestResource {
  
  String getLocalHref();
  
  RestResource findChildResource(List<String> splittedPath) throws Exception;
  
  RestTemplate getTemplate();
  
  int putTemplate(Template template) throws Exception;

}
