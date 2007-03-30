/***************************************************************************
 * Copyright 2001-2003 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.maven2.plugin.exo;

/**
 * Created by The eXo Platform SARL
 * Author : Phung Hai Nam
 *          phunghainam@gmail.com
 * Jan 3, 2006
 */
abstract public class Command {
  
  abstract public void execute(PackagingConfiguration pconfig, 
                               DeployConfiguration dconfig) throws Exception ;

}
