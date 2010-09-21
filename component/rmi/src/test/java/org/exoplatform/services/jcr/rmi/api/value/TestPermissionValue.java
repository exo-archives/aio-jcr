/*
 * Copyright (C) 2003-2010 eXo Platform SAS.
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
package org.exoplatform.services.jcr.rmi.api.value;

import junit.framework.TestCase;

/**
 * Created by The eXo Platform SAS
 * Author : eXoPlatform
 *          exo@exoplatform.com
 * Aug 20, 2010  
 */
public class TestPermissionValue extends TestCase {

  public void testParse() {
    
    String[] parsedValues = PermissionValue.parse("root read");
    assertEquals(2, parsedValues.length);
    assertEquals("root", parsedValues[0]);
    assertEquals("read", parsedValues[1]);
    
  }

  public void testAsString() {
    
    assertEquals("root read", PermissionValue.asString("root", "read"));
    
  }

}
