package org.exoplatform.services.jcr.ext.mvnadmin;

import java.io.File;

/*15.08.2007-9:43:09 Volodymyr*/
//this bean is used for holding info about artifact
public class ArtifactBean {
private String groupId, artifactId, version;
private File file;
private File pom;

public ArtifactBean(String groupId, String artifactId, String version,
File file, File pom) {
this.groupId = groupId;
this.artifactId = artifactId;
this.version = version;
this.file = file;
this.pom = pom;
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

public File getJar() {
return file;
}

public void setJar(File file) {
this.file = file;
}

public File getPom() {
return pom;
}

public void setPom(File pom) {
this.pom = pom;
}

}