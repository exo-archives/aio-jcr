/**
* Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
* Please look at license.txt in info directory for more license detail.   *
*/

package org.exoplatform.services.webdav.common.command;

import javax.jcr.AccessDeniedException;
import javax.jcr.LoginException;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.apache.commons.chain.Command;
import org.apache.commons.chain.Context;
import org.apache.commons.logging.Log;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.webdav.WebDavCommandContext;
import org.exoplatform.services.webdav.common.request.WebDavRequest;
import org.exoplatform.services.webdav.common.resource.factory.ResourceFactory;
import org.exoplatform.services.webdav.common.resource.factory.ResourceFactoryImpl;
import org.exoplatform.services.webdav.common.response.WebDavResponse;

/**
 * Created by The eXo Platform SARL
 * Author : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
 * @version $Id: WebDavCommand.java 12304 2007-01-25 10:23:57Z gavrikvetal $
 */

public abstract class WebDavCommand implements Command {

  private static Log log = ExoLogger.getLogger("jcr.WebDavCommand");

  protected ThreadLocal<WebDavCommandContext> commandContext = new ThreadLocal<WebDavCommandContext>();

  public final boolean execute(Context context) throws Exception {
    commandContext.set((WebDavCommandContext)context);

    boolean status = false;

    try {

      try {
        status = process();
      } catch (Exception exc) {
        log.info("Unhandled exception. " + exc.getMessage(), exc);
        System.out.println("Exception. " + exc);
        exc.printStackTrace();
        throw exc;
      }

    } catch (LoginException lexc) {
      String wwwAuthencticate = davContext().getConfig().getAuthHeader();
      davResponse().answerUnAuthorized(wwwAuthencticate);

    } catch (PathNotFoundException pexc) {
      davResponse().answerNotFound();

    } catch (AccessDeniedException aexc) {
      davResponse().answerForbidden();

    } catch (RepositoryException rexr) {
      davResponse().answerForbidden();

    } finally {
      davContext().getSessionProvider().logOutAllSessions();
    }

    return status;
  }

  protected abstract boolean process() throws Exception;

  public final WebDavCommandContext davContext() {
    return commandContext.get();
  }

  public final WebDavRequest davRequest() {
    return commandContext.get().getWebDavRequest();
  }

  public final WebDavResponse davResponse() {
    return commandContext.get().getWebDavResponse();
  }

  public final ResourceFactory getResourceFactory() {
    return new ResourceFactoryImpl(davContext());
  }

  public final Session jcrSrcSession() throws RepositoryException {
    return davRequest().getSourceSession(davContext().getSessionProvider());
  }

  public final Session jcrDestSession() throws RepositoryException {
    return davRequest().getDestinationSession(davContext().getSessionProvider());
  }

}
