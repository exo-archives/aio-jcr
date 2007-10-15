/**
 * Copyright 2001-2007 The eXo Platform SAS         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 */

package org.exoplatform.services.rest.container;

import java.util.List;

/**
 * Created by The eXo Platform SARL        .
 * @author Gennady Azarenkov
 * @version $Id: $
 */

public interface ResourceContainerResolvingStrategy {
  List <ResourceDescriptor> resolve(ResourceContainer c);
}
