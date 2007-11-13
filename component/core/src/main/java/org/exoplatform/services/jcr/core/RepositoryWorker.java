/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SAS. All rights reserved.          *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.services.jcr.core;

import org.picocontainer.Startable;

/**
 * Created by The eXo Platform SAS
 * Author : Peter Nedonosko
 *          peter.nedonosko@exoplatform.com.ua
 * 13.11.2007  
 * 
 * Repository worker has not a dedicated methods. The interface only means that class it implemented 
 * will be register in repository/workspace. 
 * All work will be managed by DI (dependency injection) mechanism of container. 
 * The component constructor should has all required dependencies for work.
 * 
 * E.g. component dependent on persistence manager will acquire it via DI and 
 * may register itself as a listener to acquired data manager.
 * 
 * @author <a href="mailto:peter.nedonosko@exoplatform.com.ua">Peter Nedonosko</a>
 * @version $Id$
 */
public interface RepositoryWorker extends Startable {

}
 