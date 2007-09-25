package org.exoplatform.services.jcr.ext.maven;

public class FolderDescriptor implements Descriptor {
	
	private final String folderId;
	
	FolderDescriptor(String folderId) {
		this.folderId = folderId;
	}

	/* (non-Javadoc)
	 * @see org.exoplatform.services.jcr.ext.maven.Descriptor#getAsString()
	 */
	public String getAsString() {
		return folderId;
	}

	/* (non-Javadoc)
	 * @see org.exoplatform.services.jcr.ext.maven.Descriptor#getAsPath()
	 */
	public String getAsPath() {
		return folderId.replaceAll("\\W", "/");
	}

}
