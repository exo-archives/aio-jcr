package org.exoplatform.services.jcr.ext.maven;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import javax.jcr.ItemNotFoundException;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.exoplatform.container.xml.InitParams;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.config.RepositoryConfigurationException;
import org.exoplatform.services.jcr.core.ManageableRepository;
import org.exoplatform.services.jcr.core.nodetype.ExtendedNodeTypeManager;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.jcr.ext.registry.RegistryService;
import org.picocontainer.Startable;
/**
 * Created by The eXo Platform SARL        .<br/>
 * Service responsible for Administration Maven repository
 * the served JCR structure inside workspaceName is: 
 * rootPath (maven-root)/
 *   part-of-group-folder1/         (nt:folder)
 *    ..
 *     part-of-group-foldern/
 *       artifact-root-folder/      (nt:folder)   
 *         artifact-version-folder/ (nt:folder)
 *           artifact-jar-file      (nt:file/(nt:resource+exo:mvnpom)) 
 *           artifact-pom-file      (nt:file/nt:resource)
 *           artifact-md5(sha)-file
 *   
 * @author Gennady Azarenkov
 * @version $Id: $
 */
public class ArtifactManagingServiceImpl implements ArtifactManagingService, Startable {
	 
	protected final static String NT_FILE = "artifact-nodetypes.xml";
	
	private RepositoryService repositoryService;
	private RegistryService registryService;
	private InitParams initParams;
	private String repoWorkspaceName;
	private String repoPath;
	
	/**
	 * @param params
	 * @param repositoryService
	 * @param registryService
	 * @throws RepositoryConfigurationException
	 */
	public ArtifactManagingServiceImpl(InitParams params, RepositoryService repositoryService, 
			RegistryService registryService)
			throws RepositoryConfigurationException {
		this.repositoryService = repositoryService;
		this.registryService = registryService;
		this.initParams = params;
	}

	/**
	 * without registry service
	 * @param params
	 * @param repositoryService
	 * @throws RepositoryConfigurationException
	 */
	public ArtifactManagingServiceImpl(InitParams params, RepositoryService repositoryService)
			throws RepositoryConfigurationException {
		this(params, repositoryService, null);
	}

	/* (non-Javadoc)
	 * @see org.exoplatform.services.jcr.ext.maven.ArtifactManagingService#addArtifact(org.exoplatform.services.jcr.ext.common.SessionProvider, org.exoplatform.services.jcr.ext.maven.ArtifactDescriptor, java.io.InputStream)
	 */
	public void addArtifact(SessionProvider sp, ArtifactDescriptor artifact,
			InputStream jarFile) throws RepositoryException {

	}

	/* (non-Javadoc)
	 * @see org.exoplatform.services.jcr.ext.maven.ArtifactManagingService#exportArtifacts(org.exoplatform.services.jcr.ext.common.SessionProvider, org.exoplatform.services.jcr.ext.maven.FolderDescriptor, java.io.OutputStream)
	 */
	public void exportArtifacts(SessionProvider sp,
			FolderDescriptor parentFolder, OutputStream out)
			throws RepositoryException {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.exoplatform.services.jcr.ext.maven.ArtifactManagingService#getDescriptors(org.exoplatform.services.jcr.ext.common.SessionProvider, org.exoplatform.services.jcr.ext.maven.FolderDescriptor)
	 */
	public List<Descriptor> getDescriptors(SessionProvider sp,
			FolderDescriptor parentFolder) throws RepositoryException {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.exoplatform.services.jcr.ext.maven.ArtifactManagingService#importArtifacts(org.exoplatform.services.jcr.ext.common.SessionProvider, java.io.InputStream)
	 */
	public void importArtifacts(SessionProvider sp, InputStream in)
			throws RepositoryException {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.exoplatform.services.jcr.ext.maven.ArtifactManagingService#removeArtifact(org.exoplatform.services.jcr.ext.common.SessionProvider, org.exoplatform.services.jcr.ext.maven.ArtifactDescriptor)
	 */
	public void removeArtifact(SessionProvider sp, ArtifactDescriptor artifact)
			throws RepositoryException {
		Session session = currentSession(sp);
		// TODO find artifact and remove
	}


	/* (non-Javadoc)
	 * @see org.exoplatform.services.jcr.ext.maven.ArtifactManagingService#searchArtifacts(org.exoplatform.services.jcr.ext.common.SessionProvider, org.exoplatform.services.jcr.ext.maven.SearchCriteria)
	 */
	public List<Descriptor> searchArtifacts(SessionProvider sp, SearchCriteria criteria)
			throws RepositoryException {
		Session session = currentSession(sp);
		// TODO search artifact by fulltext query (contains) and remove
		return null;
	}

	/* (non-Javadoc)
	 * @see org.picocontainer.Startable#start()
	 */
	public void start() {
		// responsible for:
		// 1. reading parameters (such as repoWorkspaceName, repoPath) from 
		// registryService (if present) or init params
		// 2. initializing artifact service Registry Entry if registryService is present  
		// if Entry is not initialized yet (first launch)
		// 3. initializing maven root if not initialized
		SessionProvider sessionProvider = SessionProvider.createSystemProvider();
		try {
      InputStream xml = getClass().getResourceAsStream(NT_FILE);
      ManageableRepository rep = repositoryService.getCurrentRepository();
      rep.getNodeTypeManager().registerNodeTypes(xml, ExtendedNodeTypeManager.IGNORE_IF_EXISTS);
      
			registryService.getEntry(sessionProvider, RegistryService.EXO_SERVICES, "ArtifactManaging");
			//TODO if registryService != null get workspaceName and rootPath from registryService
			// else get it from init params
		} catch (ItemNotFoundException e) {
			// TODO get workspaceName and rootPath from initParams

			// if registryService != null
			// construct Entry from init params and createEntry
			//registryService.createEntry(sessionProvider, RegistryService.EXO_SERVICES, "ArtifactM");
			// endif
			
			// TODO add maven root there (use rootPath)
		} catch (RepositoryException e) {
			e.printStackTrace();
		} finally {
		  sessionProvider.close();
		}
	}

	/* (non-Javadoc)
	 * @see org.picocontainer.Startable#stop()
	 */
	public void stop() {
	}


	private Session currentSession(SessionProvider sp) throws RepositoryException  {
		return sp.getSession(repoWorkspaceName, repositoryService.getCurrentRepository());		
	}
}
