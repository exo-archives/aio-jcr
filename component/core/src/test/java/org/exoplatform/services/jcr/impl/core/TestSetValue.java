/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.services.jcr.impl.core;

import java.io.ByteArrayInputStream;

import javax.jcr.Node;

import org.exoplatform.services.jcr.JcrImplBaseTest;
import org.exoplatform.services.jcr.impl.Constants;

/**
 * @author <a href="mailto:Sergey.Kabashnyuk@gmail.com">Sergey Kabashnyuk</a>
 * @version $Id: TestSetValue.java 12841 2007-02-16 08:58:38Z peterit $
 */
public class TestSetValue extends JcrImplBaseTest {
  private String docView = "<exo:test xmlns:nt=\"http://www.jcp.org/jcr/nt/1.0\" " +
  "xmlns:jcr=\"http://www.jcp.org/jcr/1.0\" " +
  "xmlns:sv=\"http://www.jcp.org/jcr/sv/1.0\" " +
  "xmlns:exo=\"http://www.exoplatform.com/jcr/exo/1.0\" " +
  "xmlns:mix=\"http://www.jcp.org/jcr/mix/1.0\" " +
  "jcr:primaryType=\"nt:unstructured\">" +
    "<childNode jcr:created=\"2004-08-18T20:07:42.626+01:00\" jcr:primaryType=\"nt:folder\">" +
      "<childNode3 jcr:created=\"2004-08-18T20:07:42.636+01:00\" jcr:primaryType=\"nt:file\">" +
        "<jcr:content jcr:data=\"dGhpcyBpcyB0aGUgYmluYXJ5IGNvbnRlbnQ=\" jcr:primaryType=\"nt:resource\" jcr:lastModified=\"2004-08-18T20:07:42.626+01:00\" jcr:mimeType=\"text/html\" jcr:uuid=\"1092852462407_\">" +
        "</jcr:content>" +
      "</childNode3>" +
      "<childNode2 jcr:created=\"2004-08-18T20:07:42.636+01:00\" jcr:primaryType=\"nt:file\">" +
        "<jcr:content jcr:data=\"this is the content\" jcr:primaryType=\"nt:resource\" jcr:mimeType=\"text/html\" jcr:lastModified=\"2004-08-18T20:07:42.626+01:00\" jcr:uuid=\"1092852462406_\">" +
        "</jcr:content>" +
      "</childNode2>" +
    "</childNode>" +
    //"<testNodeWithText1 jcr:mixinTypes='mix:referenceable exo:accessControllable' testProperty='test property value'>Thisi is a text content of node &lt;testNodeWithText1/&gt; </testNodeWithText1>"+
    "<testNodeWithText1 jcr:mixinTypes='mix:referenceable' testProperty='test property value'>Thisi is a text content of node &lt;testNodeWithText1/&gt; </testNodeWithText1>"+
    "<testNodeWithText2><![CDATA[This is a text content of node <testNodeWithText2>]]></testNodeWithText2>"+
    "<uuidNode1 jcr:mixinTypes='mix:referenceable' jcr:uuid='id_uuidNode1' source='docView'/>"+
  "</exo:test>";



  public void testSetStream() throws Exception {
    //RepositoryService service = (RepositoryService)container.getComponentInstanceOfType(RepositoryService.class);
    //RepositoryImpl defRep = (RepositoryImpl)service.getRepository();
    Node root = session.getRootNode();
//    root.ge
//    
//    ConfigurationManager configService = (ConfigurationManager) container
//    .getComponentInstanceOfType(ConfigurationManager.class);
    ByteArrayInputStream stream = new ByteArrayInputStream(docView.getBytes());
    
    String ntResource = session.getNamespacePrefix(Constants.NS_NT_URI) + ":resource";

    String jcrEncoding = session.getNamespacePrefix(Constants.NS_JCR_URI) + ":encoding";

    Node contentNode = root.addNode("name", ntResource);
    //contentNode.setProperty(EXO_ROLES_PROP, template.getParsedRoles());
    contentNode.setProperty(jcrEncoding, stream);
    root.save();
    
  }
}
