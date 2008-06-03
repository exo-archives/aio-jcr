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

package org.exoplatform.frameworks.webdavclient.common;

import junit.framework.TestSuite;

import org.exoplatform.frameworks.webdavclient.http.Log;

/**
 * Created by The eXo Platform SAS
 * Author : Vitaly Guly <gavrikvetal@gmail.com>
 * @version $Id: $
 */

public class CommonTestSuite extends TestSuite {
  
  public CommonTestSuite() {
    Log.info("CommonTestSuite:Preparing....");    

    // 4 tests
    addTestSuite(DeleteTest.class);
    
    // 9 tests
    addTestSuite(GetTest.class);    
    
    // 4 tests
    addTestSuite(MkColTest.class);
    
    // 9 tests - 1 failures
    addTestSuite(PropFindTest.class);    
    
    // 4 tests
    addTestSuite(DepthTest.class);
 
    // 2 tests
    addTestSuite(PutTest.class);

    // 1 test
    addTestSuite(OptionsTest.class);
    
    // 7 tests
    addTestSuite(HeadTest.class);

    // 3 tests
    addTestSuite(CopyTest.class);

    // 3 tests
    addTestSuite(MoveTest.class);

    // 5 tests
    addTestSuite(PropPatchTest.class);
    
    // 1 test
    addTestSuite(PropFindHrefsTest.class);
    
    // 1 test
    addTestSuite(SupportedMethodSetTest.class);
    
    // 1 test
    addTestSuite(AdditionalPropertiesTest.class);
    
    Log.info("CommonTestSuite:Run tests...");
  }
  
  public void testVoid() throws Exception {
  }  
  
}
