package org.exoplatform.services.jcr.ext.mvnadmin;

import java.net.URL;

/*15.08.2007-9:43:09 Volodymyr*/
public class ArtifactBean {
  private String groupId, artifactId, version;
  private URL file;
  
  public ArtifactBean(String groupId, String artifactId, String version, URL file) {
    this.groupId = groupId;
    this.artifactId = artifactId;
    this.version = version;
    this.file = file;
  }

  public String getArtifactId() {
    return artifactId;
  }

  public void setArtifactId(String artifactId) {
    this.artifactId = artifactId;
  }

  public String getGroupId() {
    return groupId;
  }

  public void setGroupId(String groupId) {
    this.groupId = groupId;
  }

  public String getVersion() {
    return version;
  }

  public void setVersion(String version) {
    this.version = version;
  }

  public URL getFile() {
    return file;
  }

  public void setFile(URL file) {
    this.file = file;
  }

}
 