/**
 * Copyright 2001-2003 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 */

package org.exoplatform.services.jcr.usecases;

import java.io.File;
import java.io.FileInputStream;
import java.util.Calendar;
import javax.jcr.Node;


import org.exoplatform.services.jcr.impl.core.value.BinaryValue;
import org.exoplatform.services.jcr.impl.core.value.StringValue;

public class LoadTest extends BaseUsecasesTest {
	private String tiffFile = "src/test/resources/test_tiff_file.tiff";
	private int ntFolders = 1;
	private int ntSubFolders = 10;
	private int ntFiles = 1;
	
  public void testInitTree() throws Exception {
    if (!new File(tiffFile).exists()){
      tiffFile = "component/core/" + tiffFile;
    }
	  Node root = session.getRootNode();
	  Node fsn = root.addNode("FSN", "nt:folder");
	  
	  for(int l = 1; l <= ntFolders; l++) {
	    Node folder = fsn.addNode("Folder"+l, "nt:folder");
	    
  	    for(int i = 1; i <= ntSubFolders; i++) { 
  	    	Node subFolder = folder.addNode("SubFolder"+i, "nt:folder");
  	    	
	    	for(int j = 1; j <= ntFiles; j++) { 
	    		Node file = subFolder.addNode("File"+j, "nt:file");
	    		Node contentNode = file.addNode("jcr:content", "nt:resource");
	    		contentNode.setProperty("jcr:data", new FileInputStream(tiffFile));
	    		contentNode.setProperty("jcr:mimeType", new StringValue("image/tiff"));
	    		contentNode.setProperty("jcr:lastModified", session.getValueFactory().createValue(Calendar.getInstance()));
/*	    	
	    		contentNode.addMixin("dc:elementSet");
	    		Node elementNode = file.getNode("dc:elementSet");
	    		elementNode.setProperty("dc:title", "Title"+j);
	    		elementNode.setProperty("dc:description", "Description"+j);
	    		elementNode.setProperty("dc:creator", "Creator"+j);
	    		elementNode.setProperty("dc:subject", "Subject"+j);
	    		elementNode.setProperty("dc:publisher", "Publisher"+j);
	    		elementNode.setProperty("dc:contributor", "Contributor"+j);
	    		elementNode.setProperty("dc:identifier", "Identifier"+j);
	    		elementNode.setProperty("dc:language", "Language"+j);
	    		elementNode.setProperty("dc:source", "Source"+j);
	    		elementNode.setProperty("dc:rights", "Rights"+j);
*/
	    	}
	    	session.save();
	    	assertEquals("nt:file->", 1, subFolder.getNodes().getSize());
	    }	  
  	    assertEquals("nt:subFolder->", 10, folder.getNodes().getSize());
	  }
	  assertEquals("nt:folder->", 1, fsn.getNodes().getSize());	  
  }
  
  
}
