/**
 * Copyright 2001-2003 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 */

package org.exoplatform.services.jcr;


/**
 * Created by The eXo Platform SARL        .
 * @author <a href="mailto:gennady.azarenkov@exoplatform.com">Gennady Azarenkov</a>
 * @version $Id: JcrImplBaseTest.java 12841 2007-02-16 08:58:38Z peterit $
 */

public abstract class JcrImplBaseTest extends BaseStandaloneTest {
  
  protected final String getRepositoryName() {
    String repName = System.getProperty("test.repository");
    if(repName == null)
      throw new RuntimeException("Test repository is undefined. Set test.repository system property "+
          "(For maven: in project.properties: maven.junit.sysproperties=test.repository\ntest.repository=<rep-name>)");
    return repName;
  }

}
