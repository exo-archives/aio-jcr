package org.exoplatform.services.jcr.ext.mvnadmin;

import org.exoplatform.services.jcr.ext.BaseStandaloneTest;

/*20.08.2007-15:23:16 Volodymyr*/
public class ArtifactServiceTest extends BaseStandaloneTest {
  
  public void setUp() throws Exception {
    super.setUp();
  
  }
  
  public void testImportArtifact() throws Exception{
    
    ArtifactServiceImpl asImpl = 
      (ArtifactServiceImpl)container.getComponentInstanceOfType(ArtifactServiceImpl.class);
    
    ArtifactBean artifact = new ArtifactBean("com.wellnet.myapp", "super", "1.0", null, null);
    asImpl.importArtifact(artifact);
    
    assertTrue(true);
  }
    
  public void testBrowseRepository() throws Exception{
    assertEquals(true, true);
  }
  
}
 