/**
 * Copyright 2001-2007 The eXo Platform SAS         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 */
package org.exoplatform.services.jcr.ext.maven;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import javax.jcr.RepositoryException;

import org.exoplatform.services.jcr.ext.common.SessionProvider;

/**
 * Created by The eXo Platform SARL        .<br/>
 * Service responsible for Administration maven repository
 * @author Gennady Azarenkov
 * @version $Id: $
 */

public interface ArtifactManagingService {

	/**
	 * @param sp the session provider
	 * @param parentFolder the folder which children we need to get
	 * @return list of child descriptors
	 * @throws RepositoryException
	 */
	List <Descriptor> getDescriptors(SessionProvider sp, FolderDescriptor parentFolder) throws RepositoryException;

	/**
	 * adds (deploys) artifact including creating necessary group folders, pom and checksum files
	 * @param sp the session provider
	 * @param artifact descriptor
	 * @param jarFile
	 * @throws RepositoryException
	 */
	void addArtifact(SessionProvider sp, ArtifactDescriptor artifact, InputStream jarFile)
			throws RepositoryException;

	/**
	 * removes artifact
	 * @param sp the session provider
	 * @param artifact descriptor
	 * @throws RepositoryException
	 */
	void removeArtifact(SessionProvider sp, ArtifactDescriptor artifact) throws RepositoryException;

	/**
	 * @param sp the session provider
	 * @param criteria for search
	 * @return list of descriptors
	 * @throws RepositoryException
	 */
	List <Descriptor> searchArtifacts(SessionProvider sp, SearchCriteria criteria) throws RepositoryException;

	/**
	 * imports list of artifacts into maven repo
	 * @param sp the session provider
	 * @param in input stream which contains artifact related files
	 * @throws RepositoryException
	 */
	void importArtifacts(SessionProvider sp, InputStream in) throws RepositoryException;

	/**
	 * exports list of artifacts from maven repo into output stream
	 * @param sp the session provider
	 * @param parentFolder the folder which children we need to get
	 * @param out output stream to export to
	 * @throws RepositoryException
	 */
	void exportArtifacts(SessionProvider sp, FolderDescriptor parentFolder, OutputStream out) throws RepositoryException;

}
