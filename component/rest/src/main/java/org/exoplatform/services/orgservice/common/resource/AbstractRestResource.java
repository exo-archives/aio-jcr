/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.services.orgservice.common.resource;

import java.util.List;

import org.apache.commons.logging.Log;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.orgservice.HttpStatus;
import org.exoplatform.services.orgservice.RestCommandContext;
import org.exoplatform.services.orgservice.common.template.RestTemplate;
import org.exoplatform.services.orgservice.common.template.Template;

/**
 * Created by The eXo Platform SARL
 * Author : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
 * @version $Id: $
 */

public abstract class AbstractRestResource implements RestResource {
  
  private static Log log = ExoLogger.getLogger("jcr.AbstractRestResource");
  
  private String localHref;
  
  protected RestCommandContext commandContext;
  
  public AbstractRestResource(RestCommandContext commandContext, String localHref) {
    this.commandContext = commandContext;
    this.localHref = localHref;
    log.info("LOCAL HREF: " + localHref);
  }
  
  protected RestResource getChildByName(String childName) throws Exception {
    return new FakeResource(commandContext, getLocalHref());
  }

  public RestResource findChildResource(List<String> splittedPath) throws Exception {
    log.info("SPLITTED PATH SIZE: " + splittedPath.size()); 
    String pp = "";
    for (int i = 0; i < splittedPath.size(); i++) {
      pp += ":" + splittedPath.get(i);
    }
    log.info("PP: [" + pp + "]");

    if (splittedPath.size() < 1) {
      return this;
    }
    
    String childName = splittedPath.get(0);
    log.info("CHILD NAME: " + childName);
    RestResource childResource = getChildByName(childName);
    return childResource.findChildResource(splittedPath.subList(1, splittedPath.size()));
  }  
  
  public String getLocalHref() {
    return localHref;
  }
  
  public RestTemplate getTemplate() {
    return null;
  }
  
  public int putTemplate(Template template) throws Exception {
    return HttpStatus.ERROR;
  }
  
}
