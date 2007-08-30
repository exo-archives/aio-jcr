package org.exoplatform.services.jcr.ext.mvnadmin;

import java.io.File;
import java.net.URL;
import java.util.List;
import javax.jcr.Credentials;
import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.SimpleCredentials;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.config.RepositoryConfigurationException;
import org.exoplatform.services.jcr.ext.mvnadmin.service.ArtifactBrowser;
import org.exoplatform.services.jcr.ext.mvnadmin.service.ArtifactImporter;

/*13.08.2007-15:10:05 Volodymyr*/
public class ArtifactServiceImpl implements ArtifactService {

	private final RepositoryService repositoryService;
	private Session session;

	public ArtifactServiceImpl(RepositoryService repositoryService)
			throws RepositoryException, RepositoryConfigurationException {
		this.repositoryService = repositoryService;
		Credentials credentials = new SimpleCredentials("admin", "admin"
				.toCharArray());
		session = repositoryService.getDefaultRepository().login(credentials,
				"ws");
	}

	public void importArtifact(ArtifactBean artifact) throws RepositoryException {
		new ArtifactImporter(session).addToRepository(artifact);
	}

	public List<Node> broseRepositories(Node parentNode) throws RepositoryException {
		return new ArtifactBrowser(session).getChildLevel(parentNode);
	}

	public void exportRepository(File exportPath) {
	}

	public void importRepository(URL externalRepository) {

	}

	public void removeArtifact(ArtifactBean artifactBean) {

	}

	public void searchArtifact(String query) {

	}

	public Session getSession() {
		return session;
	}

	public void setSession(Session session) {
		this.session = session;
	}
}
