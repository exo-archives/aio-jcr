/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.frameworks.restclient.command;

import org.exoplatform.frameworks.httpclient.Log;
import org.exoplatform.frameworks.restclient.RestCommands;
import org.exoplatform.frameworks.restclient.RestContext;
import org.exoplatform.frameworks.restclient.common.template.RestTemplate;

/**
 * Created by The eXo Platform SARL
 * Author : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
 * @version $Id: $
 */

public class PostCommand extends RestCommand{

  public PostCommand(RestContext context, RestTemplate restTemplate) throws Exception {
    super(context, restTemplate);
    
    this.commandName = RestCommands.POST;
    
    Log.info("public GetCommand(RestContext context)");    
  }  
  
}
