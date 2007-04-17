package org.exoplatform.applications.repoload;

import junit.framework.TestCase;

public class TestUpload extends TestCase {

	protected void setUp() throws Exception {
		super.setUp();
	}
	
	public void testRunUpload() {
		// Usecase of:
		// java -Duser.language=en -Duser.region=us \
		//			-jar repoload-1.0.jar \
		//			-conf="configuration-oraee.xml" \
		//			-root="/testStorage/root3" \
		//			-tree="10-100-100-100" \
		//			-vdfile=" img.tif" \
		//			-repo="db1" -ws="ws" \
		//			-readtree="false"
		String[] params = new String[] {
				"-conf=bin\\config\\configuration-oraee1.xml",
				"-root=/testStorage/root1",
				"-tree=10-100-100-100",
				"-vdfile=bin\\img.tif",
				"-repo=db1", 
        "-ws=ws",
				"-readtree=false"
		};
		RepositoryDataUploader.main(params);
	}
  
  public void _testRunRead() {
    // Usecase of:
    // java -Duser.language=en -Duser.region=us \
    //      -jar repoload-1.0.jar \
    //      -conf="configuration-oraee.xml" \
    //      -root="/testStorage/root3" \
    //      -tree="10-100-100-100" \
    //      -vdfile=" img.tif" \
    //      -repo="db1" -ws="ws" \
    //      -readdc -read
    String[] params = new String[] {
        "-conf=bin\\config\\configuration-oraee.xml",
        "-root=/testStorage/root1",
        "-tree=10-100-100-100",
        "-vdfile=bin\\img.tif",
        "-repo=db1", 
        "-ws=ws",
        "-read",
        "-readdc"
    };
    RepositoryDataUploader.main(params);
  }

}
