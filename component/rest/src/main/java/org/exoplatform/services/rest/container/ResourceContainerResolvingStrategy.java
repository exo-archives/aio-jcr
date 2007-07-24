/**
 * Copyright 2001-2007 The eXo Platform SAS         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 */

package org.exoplatform.services.rest.container;

import java.util.List;

/**
 * Created by The eXo Platform SARL. <br/>
 * Abstraction of administration strategy for ResourceContainer
 * @author Gennady Azarenkov
 * @version $Id: $
 */
public interface ResourceContainerResolvingStrategy {
  /**
   * Add new ResourceContainer to the collection.
   * @param resourceContainer the ResourceContainer which should be added.
   * @return the collection of ResourceDescription
   */
  List < ResourceDescriptor > resolve(ResourceContainer resourceContainer);
}
