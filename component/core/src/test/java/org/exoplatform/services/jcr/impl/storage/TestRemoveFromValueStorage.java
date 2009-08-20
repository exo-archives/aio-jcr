/*
 * Copyright (C) 2003-2009 eXo Platform SAS.
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
package org.exoplatform.services.jcr.impl.storage;

import java.io.ByteArrayInputStream;
import java.util.Random;

import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.Value;

import org.exoplatform.services.jcr.BaseStandaloneTest;

/**
 * Created by The eXo Platform SAS. <br/>
 * Test to reproduce bug when values from value storages are not removed in some
 * cases. This test has no asserts and it's result is a value-storage folder. It
 * should be empty if all o'k. But if bug exists some value-storage folder
 * wouldn't be empty.
 * 
 * @author Nikolay Zamosenchuk
 * @version $Id: $
 */
public class TestRemoveFromValueStorage extends BaseStandaloneTest {

  private Node     testRoot;

  private Property prop = null;

  private Value[]  values;

  @Override
  public void setUp() throws Exception {
    super.setUp();
    testRoot = root.addNode("TestRoot");
    int largeCount = 5;
    int smallCount = 5;
    values = new Value[largeCount + smallCount];

    // creating small one's
    for (int i = 0; i < smallCount; i++) {
      // 1000K will be stored in second value storage
      byte[] smallValue = new byte[1000 * 1024];
      Random generator = new Random();
      generator.nextBytes(smallValue);
      values[i] = testRoot.getSession()
                          .getValueFactory()
                          .createValue(new ByteArrayInputStream(smallValue));
    }
    // creating large one's
    for (int i = 0; i < largeCount; i++) {
      // 2M will be stored in first value storage
      byte[] largeValue = new byte[1024 * 1024 * 2];
      Random generator = new Random();
      generator.nextBytes(largeValue);
      values[smallCount + i] = testRoot.getSession()
                                       .getValueFactory()
                                       .createValue(new ByteArrayInputStream(largeValue));
    }
    if (values.length == 1) {
      prop = testRoot.setProperty("binaryProperty", values[0]);
    } else {
      prop = testRoot.setProperty("binaryProperty", values);
    }
    session.save();
  }

  public void testRemoveValue() throws Exception {
    prop.remove();
    session.save();
  }

  @Override
  protected String getRepositoryName() {
    return repository.getName();
  }

}
