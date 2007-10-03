/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.services.jcr.api.exporting;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.Calendar;

import javax.jcr.Node;
import javax.jcr.PropertyType;
import javax.jcr.Value;
import javax.xml.parsers.ParserConfigurationException;
import javax.jcr.ImportUUIDBehavior;

/**
 * @author <a href="mailto:Sergey.Kabashnyuk@gmail.com">Sergey Kabashnyuk</a>
 * @version $Id: $
 */
public class TestExportImport extends ExportBase {

  public TestExportImport() throws ParserConfigurationException {
    super();
  }

  public void testExportImportValuesSysView() throws Exception {
    Node testNode = root.addNode("testExportImport");
    for (int i = 0; i < valList.size(); i++) {
      testNode.setProperty("prop" + i + "_string", valList.get(i), PropertyType.STRING);
      testNode.setProperty("prop" + i + "_binary", valList.get(i), PropertyType.BINARY);
    }
    session.save();
    File destFile = File.createTempFile("testExportImportValuesSysView", ".xml");
    destFile.deleteOnExit();
    OutputStream outStream = new FileOutputStream(destFile);
    session.exportSystemView(testNode.getPath(), outStream, false, false);
    outStream.close();

    testNode.remove();
    session.save();

    session.importXML(root.getPath(),
                      new FileInputStream(destFile),
                      ImportUUIDBehavior.IMPORT_UUID_COLLISION_THROW);

    session.save();

    Node newNode = root.getNode("testExportImport");

    for (int i = 0; i < valList.size(); i++) {
      if (valList.get(i).length > 1) {
        Value[] stringValues = newNode.getProperty("prop" + i + "_string").getValues();
        for (int j = 0; j < stringValues.length; j++) {
          assertEquals(stringValues[j].getString(), valList.get(i)[j]);
        }
        Value[] binaryValues = newNode.getProperty("prop" + i + "_binary").getValues();
        for (int j = 0; j < stringValues.length; j++) {
          assertEquals(binaryValues[j].getString(), valList.get(i)[j]);
        }
      } else {
        assertEquals(valList.get(i)[0], newNode.getProperty("prop" + i + "_string")
                                               .getValue()
                                               .getString());
        assertEquals(valList.get(i)[0], newNode.getProperty("prop" + i + "_binary")
                                               .getValue()
                                               .getString());

      }
    }
    destFile.delete();
  }

  public void testExportImportCustomNodeType() throws Exception {
    Node folder = root.addNode("childNode", "nt:folder");
    Node file = folder.addNode("childNode2", "exo:salestool");

    Node contentNode = file.addNode("jcr:content", "nt:resource");
    contentNode.setProperty("jcr:data", session.getValueFactory()
                                               .createValue("this is the content",
                                                            PropertyType.BINARY));
    contentNode.setProperty("jcr:mimeType", "application/octet-stream");
    contentNode.setProperty("jcr:lastModified", session.getValueFactory()
                                                       .createValue(Calendar.getInstance()));

    session.save();

    File destFile = File.createTempFile("testExportImportValuesSysView", ".xml");
    destFile.deleteOnExit();
    OutputStream outStream = new FileOutputStream(destFile);
    session.exportSystemView(file.getPath(), outStream, false, false);
    outStream.close();

    folder.remove();
    session.save();

    session.importXML(root.getPath(),
                      new FileInputStream(destFile),
                      ImportUUIDBehavior.IMPORT_UUID_COLLISION_THROW);

    session.save();

  }

}
