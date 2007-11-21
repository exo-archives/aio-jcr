/***************************************************************************
 * Copyright 2001-2003 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.services.jcr.usecases.metainfo;

import java.io.InputStream;
import java.util.Calendar;

import javax.jcr.Node;

import org.exoplatform.services.jcr.usecases.BaseUsecasesTest;

public class TestMetaInfo extends BaseUsecasesTest {

  public void testXLSFile() throws Exception {
    InputStream is = TestMetaInfo.class.getResourceAsStream("/index/test_index.xls");

    Node file = root.addNode("testXLSFile", "nt:file");
    Node contentNode = file.addNode("jcr:content", "nt:resource");
    contentNode.setProperty("jcr:encoding", "UTF-8");
    contentNode.setProperty("jcr:data", is);
    contentNode.setProperty("jcr:mimeType", "application/excel");
    contentNode.setProperty("jcr:lastModified", Calendar.getInstance());
    root.save();

  }
}
