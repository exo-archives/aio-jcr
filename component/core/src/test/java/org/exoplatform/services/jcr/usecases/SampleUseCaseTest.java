/**
 * Copyright 2001-2003 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 */

package org.exoplatform.services.jcr.usecases;

import javax.jcr.Node;

/**
 * Created by The eXo Platform SARL        .
 * @author <a href="mailto:gennady.azarenkov@exoplatform.com">Gennady Azarenkov</a>
 * @version $Id: SampleUseCaseTest.java 12841 2007-02-16 08:58:38Z peterit $
 *
 * JCR Use Case test sample
 */

public class SampleUseCaseTest extends BaseUsecasesTest {


  /**
   * Sample test. An example how to make it
   * @throws Exception
   */
  public void testSomething() throws Exception {
    // make sub-root with unique name;
    Node subRootNode = root.addNode("testSomething");

    // make the structure under subRootNode if you need so...
    subRootNode.setProperty("someProperty", "someValue");

    // and save if you need so...
    session.save();


    // and test
    this.assertNotNull(subRootNode);

    // you have to remove and save it at the end of method(recommended) or at the
    // tearDown() as well
    subRootNode.remove();
    session.save();
  }
}
