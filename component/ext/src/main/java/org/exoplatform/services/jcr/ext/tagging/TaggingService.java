/*
 * Copyright (C) 2003-2007 eXo Platform SAS.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
 */

package org.exoplatform.services.jcr.ext.tagging;

import java.net.URI;
import java.util.Collection;
import java.util.Comparator;

import javax.jcr.RepositoryException;

import org.exoplatform.services.jcr.config.RepositoryConfigurationException;

/**
 * Created by The eXo Platform SAS .
 * 
 * @author eXo Platform
 * @version $Id$
 */

public interface TaggingService {

  void addTag(String name, URI uri, String description, String repository, String workspace) throws RepositoryException,
                                                                                            RepositoryConfigurationException;

  Collection<Tag> getTags(String userId);

  Collection<Tag> getTags(URI uri);

  TagRate getRate(String tagName);

  Collection<TagRate> getRates();

  Collection<TagRate> getRates(Comparator comparator);

  TagURICrossrate getCrossrate(String tagName, URI uri);

  Collection<TagURICrossrate> getCrossrate(URI uri);

}
