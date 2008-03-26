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
package org.exoplatform.jcr.benchmark.jcrapi;

import java.util.ArrayList;
import java.util.List;

import org.exoplatform.jcr.benchmark.JCRTestContext;

import com.sun.japex.TestCase;

/**
 * Created by The eXo Platform SAS
 * 
 * @author <a href="mailto:peter.nedonosko@exoplatform.com.ua">Peter Nedonosko</a>
 * @version $Id$
 */

public abstract class AbstractGetItemNameTest extends AbstractItemsTest {

  private List<String> names     = new ArrayList<String>();
  
  private List<String> uuids     = new ArrayList<String>();

  private volatile int iteration = 0;
  
  private volatile int iterationUUID = 0;

  protected String nextName() {
    return names.get(iteration++);
  }
  
  protected String nextUUID() {
    return uuids.get(iterationUUID++);
  }

  protected void addName(String name) {
    names.add(name);
  }
  
  protected void addUUID(String uuid) {
    uuids.add(uuid);
  }

  @Override
  public void doFinish(TestCase tc, JCRTestContext context) throws Exception {
    super.doFinish(tc, context);

    names.clear();
    uuids.clear();
  }

}
