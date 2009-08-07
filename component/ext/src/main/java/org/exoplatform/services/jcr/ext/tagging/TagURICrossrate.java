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
import java.util.Calendar;

/**
 * Created by The eXo Platform SAS .
 * 
 * @author eXo Platform
 * @version $Id$
 */

public class TagURICrossrate extends TagRate {

  private final URI uri;

  public TagURICrossrate(String tagName, int count, int rate, Calendar updated, URI uri) {
    super(tagName, count, rate, updated);
    this.uri = uri;
  }

  public URI getUri() {
    return uri;
  }

}
