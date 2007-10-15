/**
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 */

package org.exoplatform.frameworks.jcr.cli;

import org.apache.commons.chain.Command;
import org.apache.commons.chain.Context;
/**
 * Created by The eXo Platform SARL        .
 * @author Gennady Azarenkov
 * @version $Id: $
 */

public abstract class AbstractCliCommand implements Command {

  public final boolean execute(Context ctx) throws Exception {
    CliAppContext cliCtx = (CliAppContext)ctx;
    cliCtx.clearOutput();
    boolean result = perform(cliCtx);
    return result;
  }
  
  public abstract boolean perform(CliAppContext ctx) throws Exception;

}
