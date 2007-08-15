package org.exoplatform.applications.wiseamt;
/*14.08.2007-12:08:54 Volodymyr*/
public class ArtifactServiceConfig {
  public static int NAVIGATION_WIDTH = 0;
  public static int NAVIGATION_DEPTH = 1;
  
  private int navigationType;
  
  public ArtifactServiceConfig(int navigationType){
    setNavigationType(navigationType);
  }

  public int getNavigationType() {
    return navigationType;
  }
  public void setNavigationType(int navigationType) {
    this.navigationType = navigationType;
  }
  
}
 