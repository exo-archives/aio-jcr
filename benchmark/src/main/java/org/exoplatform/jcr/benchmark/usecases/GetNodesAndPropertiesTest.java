/**
 * Copyright 2001-2007 The eXo Platform SAS         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 */

package org.exoplatform.jcr.benchmark.usecases;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.PathNotFoundException;
import javax.jcr.PropertyIterator;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.ValueFormatException;

import org.apache.commons.logging.Log;
import org.exoplatform.jcr.benchmark.JCRTestBase;
import org.exoplatform.jcr.benchmark.JCRTestContext;
import org.exoplatform.services.log.ExoLogger;

import com.sun.japex.TestCase;

/**
 * Created by The eXo Platform SARL .
 *
 * @author Gennady Azarenkov
 * @version $Id: $
 */
public class GetNodesAndPropertiesTest extends JCRTestBase {
  protected static Log log = ExoLogger.getLogger("GetNodesAndPropertiesTest");

  @Override
  public void doRun(TestCase tc, JCRTestContext context) throws Exception {
    Session session = context.getSession();
    String rootName = tc.getParam("jcr.rootNodeName");
    try {
      log.info("Get nodes start in " + rootName);

      Node node = (Node) session.getItem(rootName);

      int pcount = 0;
      PropertyIterator piter = node.getProperties();
      while (piter.hasNext()) {
        try {
          piter.nextProperty().getString();
        } catch (ValueFormatException e) {
          log.error(e);
        }
        pcount++;
      }

      NodeIterator ni = node.getNodes();

      int ncount = 0;
      while (ni.hasNext()) {
        ni.nextNode();
        ncount++;
      }

      log.info("Get nodes " + ncount + " (" + pcount + " properties) time in report ");
    } catch (PathNotFoundException e) {
      log.error(e);
    } catch (RepositoryException e) {
      log.error(e);
    }

  }

  @Override
  public void doFinish(TestCase tc, JCRTestContext context) throws Exception {
    // System.out.println("DO FINISH AddNodeTest "+runtime);
  }

}
