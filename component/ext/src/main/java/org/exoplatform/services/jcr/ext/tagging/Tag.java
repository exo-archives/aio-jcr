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

/**
 * Created by The eXo Platform SAS .
 * 
 * @author eXo Platform
 * @version $Id: $
 */

public final class Tag {

  private final String name;

  private final URI    uri;

  private final String owner;

  private final String desription;

  public Tag(String name, URI uri, String owner, String desription) {
    this.name = name;
    this.uri = uri;
    this.owner = owner;
    this.desription = desription;
  }

  public String getName() {
    return name;
  }

  public URI getUri() {
    return uri;
  }

  public String getOwner() {
    return owner;
  }

  public String getDesription() {
    return desription;
  }

}
