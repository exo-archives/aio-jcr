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
package org.exoplatform.services.jcr.config;

import java.util.ArrayList;

/**
 * Created by The eXo Platform SAS.
 * 
 * @author <a href="mailto:gennady.azarenkov@exoplatform.com">Gennady Azarenkov</a>
 * @version $Id: ValueStorageEntry.java 11907 2008-03-13 15:36:21Z ksm $
 */

public class ValueStorageEntry extends MappedParametrizedObjectEntry {

  private String                             id;

  private ArrayList<ValueStorageFilterEntry> filters;

  public ValueStorageEntry() {
    super();
  }

  public ValueStorageEntry(String type, ArrayList<ValueStorageFilterEntry> params) {
    super(type, params);
  }

  public ArrayList<ValueStorageFilterEntry> getFilters() {
    return filters;
  }

  public void setFilters(ArrayList<ValueStorageFilterEntry> filters) {
    this.filters = filters;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }
}
