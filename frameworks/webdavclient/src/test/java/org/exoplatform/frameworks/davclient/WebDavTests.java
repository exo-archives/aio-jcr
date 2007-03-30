/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.frameworks.davclient;

import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.apache.commons.logging.Log;
import org.exoplatform.frameworks.davclient.completed.CopyTest;
import org.exoplatform.frameworks.davclient.completed.DeleteTest;
import org.exoplatform.frameworks.davclient.completed.GetTest;
import org.exoplatform.frameworks.davclient.completed.HeadTest;
import org.exoplatform.frameworks.davclient.completed.LockUnLockTest;
import org.exoplatform.frameworks.davclient.completed.MkColTest;
import org.exoplatform.frameworks.davclient.completed.MoveTest;
import org.exoplatform.frameworks.davclient.completed.OptionsTest;
import org.exoplatform.frameworks.davclient.completed.PropFindTest;
import org.exoplatform.frameworks.davclient.completed.PropPatchTest;
import org.exoplatform.frameworks.davclient.completed.PutTest;
import org.exoplatform.frameworks.davclient.completed.additional.AdditionalPropertiesTest;
import org.exoplatform.frameworks.davclient.completed.additional.NullHrefTest;
import org.exoplatform.frameworks.davclient.completed.deltav.CheckInTest;
import org.exoplatform.frameworks.davclient.completed.deltav.CheckOutTest;
import org.exoplatform.frameworks.davclient.completed.deltav.ReportTest;
import org.exoplatform.frameworks.davclient.completed.deltav.UnCheckOutTest;
import org.exoplatform.frameworks.davclient.completed.deltav.VersionControlTest;
import org.exoplatform.frameworks.davclient.completed.search.SQLSearchTest;
import org.exoplatform.frameworks.davclient.completed.search.SupportedMethodSetTest;
import org.exoplatform.frameworks.davclient.completed.search.SupportedQueryGramarSetTest;
import org.exoplatform.services.log.ExoLogger;

/**
 * Created by The eXo Platform SARL
 * Author : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
 * @version $Id: $
 */

public class WebDavTests extends TestCase {

  private static Log log = ExoLogger.getLogger("jcr.WebDavTests");

  public static TestSuite suite() {
    log.info("Preparing...");
    
    TestSuite suite = new TestSuite("jcr.webdav tests");
    
    suite.addTestSuite(OptionsTest.class);
    suite.addTestSuite(PropFindTest.class);
    suite.addTestSuite(MkColTest.class);
    suite.addTestSuite(CopyTest.class);
    suite.addTestSuite(MoveTest.class);  
    suite.addTestSuite(PutTest.class);
    suite.addTestSuite(GetTest.class);
    suite.addTestSuite(HeadTest.class);
    suite.addTestSuite(DeleteTest.class);
    suite.addTestSuite(LockUnLockTest.class);      
    suite.addTestSuite(PropPatchTest.class);        
    suite.addTestSuite(NullHrefTest.class);
    suite.addTestSuite(AdditionalPropertiesTest.class);
    
    // DELTA V
    suite.addTestSuite(VersionControlTest.class);
    suite.addTestSuite(CheckInTest.class);
    suite.addTestSuite(CheckOutTest.class);
    suite.addTestSuite(UnCheckOutTest.class);
    suite.addTestSuite(ReportTest.class);    

    // SEARCHING
    suite.addTestSuite(SupportedQueryGramarSetTest.class);
    suite.addTestSuite(SupportedMethodSetTest.class);
    suite.addTestSuite(SQLSearchTest.class);

//    suite.addTestSuite(BasicSearchTest.class);
    
//    suite.addTestSuite(TestDCExt.class);
    
//    suite.addTestSuite(AdditionalSearchTest.class);
//    suite.addTestSuite(TestNodeConfig.class);
    
//  //suite.addTestSuite(XPathSearchTest.class);        
//    suite.addTestSuite(TestCreateResourceSomeNodeType.class); //creating webdav:folder & webdav:file nodes    
//    suite.addTestSuite(TestDC.class);
    //suite.addTestSuite(TestMappingTable.class);    
//  suite.addTestSuite(OrderPatchTest.class);    
//    suite.addTestSuite(DevTest.class);    
    
    return suite;
  }
  
}
