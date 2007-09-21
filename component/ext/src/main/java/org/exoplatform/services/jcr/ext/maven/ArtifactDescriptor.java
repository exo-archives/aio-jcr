/**
 * Copyright 2001-2007 The eXo Platform SAS         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 */

package org.exoplatform.services.jcr.ext.maven;

/**
 * Created by The eXo Platform SARL        .<br/>
 * Artifact Descriptor 
 * @author Gennady Azarenkov
 * @version $Id: $
 */

public final class ArtifactDescriptor implements Descriptor {

	private final FolderDescriptor groupId;
	private final String artifactId;
	private final String versionId;
	
	public ArtifactDescriptor(FolderDescriptor groupId, String artifactId, String versionId) {
		this.groupId = groupId;
		this.artifactId = artifactId;
		this.versionId = versionId;
	}
	

	public FolderDescriptor getGroupId() {
		return groupId;
	}

	public String getArtifactId() {
		return artifactId;
	}

	public String getVersionId() {
		return versionId;
	}
	
	/* (non-Javadoc)
	 * @see org.exoplatform.services.jcr.ext.maven.Descriptor#getAsString()
	 */
	public String getAsString() {
		// is that correct?
		return groupId.getAsString()+"-"+artifactId+"-"+versionId;
	}


	/* (non-Javadoc)
	 * @see org.exoplatform.services.jcr.ext.maven.Descriptor#getAsPath()
	 */
	public String getAsPath() {
		// is that correct?
		return groupId.getAsString()+"/"+artifactId+"/"+versionId;
	}
	
	
}
