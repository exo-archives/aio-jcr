package org.exoplatform.services.jcr.ext.maven;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import javax.jcr.ItemNotFoundException;
import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Property;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.Value;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.SimpleLayout;
import org.codehaus.plexus.digest.Digester;
import org.codehaus.plexus.digest.DigesterException;
import org.codehaus.plexus.digest.Md5Digester;
import org.codehaus.plexus.digest.Sha1Digester;
import org.codehaus.plexus.util.FileUtils;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.config.RepositoryConfigurationException;
import org.exoplatform.services.jcr.core.ManageableRepository;
import org.exoplatform.services.jcr.core.nodetype.ExtendedNodeTypeManager;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.jcr.ext.registry.RegistryService;
import org.picocontainer.Startable;

/**
 * Created by The eXo Platform SARL .<br/> Service responsible for
 * Administration Maven repository the served JCR structure inside workspaceName
 * is: rootPath (maven-root)/ part-of-group-folder1/ (nt:folder) ..
 * part-of-group-foldern/ artifact-root-folder/ (nt:folder)
 * artifact-version-folder/ (nt:folder) artifact-jar-file
 * (nt:file/(nt:resource+exo:mvnpom)) artifact-pom-file
 * (nt:file/(nt:resource+exo:mvnjar)) artifact-md5(sha)-file
 * 
 * @author Gennady Azarenkov
 * @version $Id: $
 */
public class ArtifactManagingServiceImpl implements ArtifactManagingService,
		Startable {
	
	public static int ROOT_ID_TYPE = -1;
	public static int GROUP_ID_TYPE = 0;
	public static int ARTIFACT_ID_TYPE = 1;
	public static int VERSION_ID_TYPE = 2;
	public static String STRING_TERMINATOR = "*";
	protected final static String NT_FILE = "artifact-nodetypes.xml";
	
	private RepositoryService repositoryService;
	private RegistryService registryService;
	private InitParams initParams;
	private String repoWorkspaceName = "ws";
	private String repoPath;

	private static Logger logger = Logger.getLogger(ArtifactManagingServiceImpl.class);
	protected Digester md5Digester = new Md5Digester();
	protected Digester sha1Digester = new Sha1Digester();

	/**
	 * @param params
	 * @param repositoryService
	 * @param registryService
	 * @throws RepositoryConfigurationException
	 */
	public ArtifactManagingServiceImpl(InitParams params,
			RepositoryService repositoryService, RegistryService registryService)
			throws RepositoryConfigurationException {
		this.repositoryService = repositoryService;
		this.registryService = registryService;
		this.initParams = params;

		SimpleLayout layout = new SimpleLayout();
		ConsoleAppender appender = new ConsoleAppender(layout);
		logger.addAppender(appender);
		logger.setLevel(Level.DEBUG);
	}

	/**
	 * without registry service
	 * 
	 * @param params
	 * @param repositoryService
	 * @throws RepositoryConfigurationException
	 */
	public ArtifactManagingServiceImpl(InitParams params,
			RepositoryService repositoryService)
			throws RepositoryConfigurationException {
		this(params, repositoryService, null);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.exoplatform.services.jcr.ext.maven.ArtifactManagingService#addArtifact(org.exoplatform.services.jcr.ext.common.SessionProvider,
	 *      org.exoplatform.services.jcr.ext.maven.ArtifactDescriptor,
	 *      java.io.InputStream)
	 */
	public void addArtifact(SessionProvider sp, ArtifactDescriptor artifact,
			File jarFile, File pomFile) throws RepositoryException {
		
		logger.debug("Starting adding artifact to Repository");
		logger.debug("Get session via SessionProvider");
		Session session = currentSession(sp);
		Node rootNode = session.getRootNode();
		
		logger.debug("Create groupId path structure");
		Node groupId_tail = createGroupIdLayout(rootNode, artifact );
		logger.debug("Create artifactId path structure");
		Node artifactId_node = createArtifactIdLayout(groupId_tail, artifact);
		logger.debug("Create versionId path structure");
		Node version_node = createVersionLayout(artifactId_node, artifact);
		
		logger.debug("Importing JAR");
		importJar( version_node, jarFile );
		logger.debug("Importing POM");
		importPom( version_node, pomFile );

		updateMetadata( version_node, artifact );
		
		logger.debug("Finishing with adding artifact to Repository");
		
		session.save();

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.exoplatform.services.jcr.ext.maven.ArtifactManagingService#exportArtifacts(org.exoplatform.services.jcr.ext.common.SessionProvider,
	 *      org.exoplatform.services.jcr.ext.maven.FolderDescriptor,
	 *      java.io.OutputStream)
	 */
	public void exportArtifacts(SessionProvider sp,
			FolderDescriptor parentFolder, OutputStream out)
			throws RepositoryException {
		// TODO Auto-generated method stub
		Session session = currentSession(sp);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.exoplatform.services.jcr.ext.maven.ArtifactManagingService#getDescriptors(org.exoplatform.services.jcr.ext.common.SessionProvider,
	 *      org.exoplatform.services.jcr.ext.maven.FolderDescriptor)
	 */
	public List<Descriptor> getDescriptors(SessionProvider sp,
			FolderDescriptor parentFolder) throws RepositoryException {
		// TODO Auto-generated method stub
		logger.debug("Starting getDescriptors with parentFolder : "
				+ parentFolder.getAsString());
		logger.debug("Getting session via SessionProvider");
		Session session = currentSession(sp);
		Node rootNode = session.getRootNode();
		String strPath = parentFolder.getAsPath();
		Node targetNode = rootNode.getNode(strPath);

		List<Descriptor> childNodes = new ArrayList<Descriptor>();
		for (NodeIterator iterator = targetNode.getNodes("exo:artifact"); iterator
				.hasNext();) {
			// descriptor holds names of all child nodes than makes up artifact
			// coordinates.
			Descriptor descriptor = new FolderDescriptor(iterator.nextNode()
					.getName());
			childNodes.add(descriptor);
		}
		logger.debug("Finishing with browsing artifacts");
		return (childNodes.size() == 0) ? null : childNodes;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.exoplatform.services.jcr.ext.maven.ArtifactManagingService#importArtifacts(org.exoplatform.services.jcr.ext.common.SessionProvider,
	 *      java.io.InputStream)
	 */
	public void importArtifacts(SessionProvider sp, InputStream in)
			throws RepositoryException {
		Session session = currentSession(sp);
		// TODO Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.exoplatform.services.jcr.ext.maven.ArtifactManagingService#removeArtifact(org.exoplatform.services.jcr.ext.common.SessionProvider,
	 *      org.exoplatform.services.jcr.ext.maven.ArtifactDescriptor)
	 */
	public void removeArtifact(SessionProvider sp, ArtifactDescriptor artifact)
			throws RepositoryException {
		Session session = currentSession(sp);
		// TODO find artifact and remove
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.exoplatform.services.jcr.ext.maven.ArtifactManagingService#searchArtifacts(org.exoplatform.services.jcr.ext.common.SessionProvider,
	 *      org.exoplatform.services.jcr.ext.maven.SearchCriteria)
	 */
	public List<Descriptor> searchArtifacts(SessionProvider sp,
			SearchCriteria criteria) throws RepositoryException {
		Session session = currentSession(sp);
		// TODO search artifact by fulltext query (contains) and remove
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.picocontainer.Startable#start()
	 */
	public void start() {
		// responsible for:
		// 1. reading parameters (such as repoWorkspaceName, repoPath) from
		// registryService (if present) or init params
		// 2. initializing artifact service Registry Entry if registryService is
		// present
		// if Entry is not initialized yet (first launch)
		// 3. initializing maven root if not initialized
		SessionProvider sessionProvider = SessionProvider
				.createSystemProvider();
		try {
			InputStream xml = getClass().getResourceAsStream(NT_FILE);
			ManageableRepository rep = repositoryService.getCurrentRepository();
			rep.getNodeTypeManager().registerNodeTypes(xml,
					ExtendedNodeTypeManager.IGNORE_IF_EXISTS);

			registryService.getEntry(sessionProvider,
					RegistryService.EXO_SERVICES, "ArtifactManaging");
			// TODO if registryService != null get workspaceName and rootPath
			// from registryService
			// else get it from init params
		} catch (ItemNotFoundException e) {
			// TODO get workspaceName and rootPath from initParams

			// if registryService != null
			// construct Entry from init params and createEntry
			// registryService.createEntry(sessionProvider,
			// RegistryService.EXO_SERVICES, "ArtifactM");
			// endif

			// TODO add maven root there (use rootPath)
		} catch (RepositoryException e) {
			e.printStackTrace();
		} finally {
			sessionProvider.close();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.picocontainer.Startable#stop()
	 */
	public void stop() {
	}

	private Session currentSession(SessionProvider sp)
			throws RepositoryException {
		return sp.getSession(repoWorkspaceName, repositoryService
				.getCurrentRepository());
	}

	// this function creates hierarchy in JCR storage acording to groupID
	// parameter : com.google.code...
	private Node createGroupIdLayout(Node rootNode, ArtifactDescriptor artifact)
			throws RepositoryException {
		FolderDescriptor groupId = artifact.getGroupId();
		Vector<String> struct_groupId = new Vector<String>();
		String[] items = groupId.getAsPath().split("/");
		for (String subString : items) {
			struct_groupId.add(subString);
		}
		Node groupIdTail = rootNode;

		for (Iterator<String> iterator = struct_groupId.iterator(); iterator
				.hasNext();) {
			String name = iterator.next();

			Node levelNode;
			if (!groupIdTail.hasNode(name)) { // Node do not has such child
												// nodes

				levelNode = groupIdTail.addNode(name, "nt:folder");
				levelNode.addMixin("exo:artifact");
				levelNode.setProperty("exo:pathType",
						ArtifactManagingServiceImpl.GROUP_ID_TYPE);

			} else {
				levelNode = groupIdTail.getNode(name);
			}
			groupIdTail = levelNode;
		}

		return groupIdTail;
	}
	
	private Node createArtifactIdLayout(Node groupId_NodeTail, ArtifactDescriptor artifact)
				throws RepositoryException {
		
		String artifactId = artifact.getArtifactId();
		
		Node artifactIdNode;
		if (!groupId_NodeTail.hasNode(artifactId)) {
			artifactIdNode = groupId_NodeTail.addNode(artifactId, "nt:folder");
			artifactIdNode.addMixin("exo:artifact");

			artifactIdNode.setProperty("exo:pathType",
					ArtifactManagingServiceImpl.ARTIFACT_ID_TYPE);
			artifactIdNode.setProperty("exo:versionList", new String[] {
					ArtifactManagingServiceImpl.STRING_TERMINATOR,
					ArtifactManagingServiceImpl.STRING_TERMINATOR });
		} else {
			artifactIdNode = groupId_NodeTail.getNode(artifactId);
		}
		return artifactIdNode;
	}


	private Node createVersionLayout(Node artifactId, ArtifactDescriptor artifact)
			throws RepositoryException {
		String version = artifact.getVersionId();
		Node currentVersion = artifactId.addNode(version, "nt:folder");
		currentVersion.addMixin("exo:artifact");
		currentVersion.setProperty("exo:pathType",
				ArtifactManagingServiceImpl.VERSION_ID_TYPE);
		// currentVersion.setProperty("exo:version", version);

		// version list property is already added to artifactId Node !!!!
		updateVersionList(artifactId, artifact); // Add current version to version list

		updateMetadata(artifactId, artifact); // creates all needed data -
									// "maven-metadata.xml & checksums"

		return currentVersion;
	}
	
	private void updateVersionList(Node artifactId, ArtifactDescriptor artifact) throws RepositoryException {
		// Update version list
		String version = artifact.getVersionId();

		Property property = artifactId.getProperty("exo:versionList");
		Value[] values = property.getValues();
		Vector<String> versions = new Vector<String>();
		for (Value ver : values) {
			String str = ver.getString();
			if (!str.equals(ArtifactManagingServiceImpl.STRING_TERMINATOR)) {
				versions.addElement(str);
			}
		}
		versions.addElement(version);
		String[] newValues = new String[versions.capacity()];
		Iterator<String> i = versions.iterator();
		int index = 0;
		while (i.hasNext()) {
			newValues[index++] = i.next();
		}
		artifactId.setProperty("exo:versionList", newValues);
	}
	
	
	// this function is used to add a metadata to artifact;
	// metadata can be placed in artifactId Node and contains a list of
	// available artifacts
	// also metadata is placed in each version forder with a jar and pom files.
	// In this case it contains only version of current artifact
	private void updateMetadata(Node parentNode, ArtifactDescriptor artifact) throws RepositoryException {

		String groupId = artifact.getGroupId().getAsString();
		String artifactId = artifact.getArtifactId();
		String version = artifact.getVersionId();

		Node xmlfile;
		if (!parentNode.hasNode("maven-metadata.xml")) {
			Node metadata = parentNode.addNode("maven-metadata.xml", "nt:file");
			xmlfile = metadata.addNode("jcr:content", "nt:resource");
			String mimeType = "plain/text";
			xmlfile.setProperty("jcr:mimeType", mimeType);
			xmlfile.setProperty("jcr:lastModified", Calendar.getInstance());
		} else {
			Node metadata = parentNode.getNode("maven-metadata.xml");
			xmlfile = metadata.getNode("jcr:content");
			xmlfile.setProperty("jcr:lastModified", Calendar.getInstance());
		}

		Property pathType = parentNode.getProperty("exo:pathType");
		// checks if we deal with multi version list or not - multi contains a
		// list of strings.
		try {
			if (pathType.getLong() == ArtifactManagingServiceImpl.ARTIFACT_ID_TYPE) {

				Property list = parentNode.getProperty("exo:versionList");
				Value[] values = list.getValues();
				ArrayList<String> versions = new ArrayList<String>();
				for (Value ver : values) {
					String str = ver.getString();
					versions.add(str);
				}
				ByteArrayInputStream ios = new ByteArrayInputStream(
						getXMLdescription(groupId, artifactId, versions) // use
																			// List
								.getBytes());
				xmlfile.setProperty("jcr:data", ios);
				ios.close();

			} else {
				ByteArrayInputStream ios = new ByteArrayInputStream(
						getXMLdescription(groupId, artifactId, version) // use
																		// simple
																		// String
								.getBytes());
				xmlfile.setProperty("jcr:data", ios);
				ios.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	
	private void importJar(Node versionNode, File fileJar)
			throws RepositoryException {
		// Note that artifactBean been initialized within constructor
		String mimeType; // for common use
		Node jarNode = versionNode.addNode("jar", "nt:file");
		Node jarFile = jarNode.addNode("jcr:content", "nt:resource");
		mimeType = "application/zip";
		jarFile.setProperty("jcr:mimeType", mimeType);
		jarFile.setProperty("jcr:lastModified", Calendar.getInstance());

		String filename = FileUtils.basename(fileJar.getName()); // gets
																				// filename
																				// without
																				// extension
		filename = filename.concat("jar");

		jarFile.addMixin("exo:artifact"); // adds ability to use md5 and sha1
											// properties;
		jarFile.setProperty("exo:filename", filename);

		try {
			FileInputStream ios = null;
			try {
				ios = new FileInputStream(new File(fileJar.getPath()));
				jarFile.setProperty("jcr:data", ios);
			} finally {
				ios.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (SecurityException e) {
			e.printStackTrace();
		}

		try {
			jarFile.setProperty("exo:md5", getChecksum(fileJar,	"MD5"));
			jarFile.setProperty("exo:sha1", getChecksum(fileJar, "SHA-1"));
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (DigesterException e) {
			e.printStackTrace();
		}

	}

	private void importPom(Node versionNode, File filePom) throws RepositoryException {
		Node pomNode = versionNode.addNode("pom", "nt:file");
		Node pomFile = pomNode.addNode("jcr:content", "nt:resource");
		String mimeType = "plain/text";
		pomFile.setProperty("jcr:mimeType", mimeType);
		pomFile.setProperty("jcr:lastModified", Calendar.getInstance());

		String filename = FileUtils.basename(filePom.getName()); // gets
		// filename
		// without
		// extension
		filename = filename.concat("jar.pom");
		pomFile.addMixin("exo:artifact");
		pomFile.setProperty("exo:filename", filename + ".pom");
		try {
			FileInputStream ios = null;
			try {
				ios = new FileInputStream(filePom.getPath());
				pomFile.setProperty("jcr:data", ios);
			} finally {
				ios.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (SecurityException e) {
			e.printStackTrace();
		}
		try {
			pomFile.setProperty("exo:md5", getChecksum(filePom,"MD5"));
			pomFile.setProperty("exo:sha1", getChecksum(filePom,"SHA-1"));
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (DigesterException e) {
			e.printStackTrace();
		}

	}

	protected String getChecksum(File file, String algo)
			throws NoSuchAlgorithmException, DigesterException {
		if ("MD5".equals(algo)) {
			return md5Digester.calc(file);
		} else if ("SHA-1".equals(algo)) {
			return sha1Digester.calc(file);
		} else {
			throw new NoSuchAlgorithmException("No support for algorithm "
					+ algo + ".");
		}
	}

	protected String getXMLdescription(String groupId, String artifactId,
			String version) {
		String template = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
				+ "<metadata>\n<groupId>%s</groupId>\n"
				+ "<artifactId>%s</artifactId>\n" + "<version>%s</version>\n"
				+ "</metadata>";
		String result = String.format(template, groupId, artifactId, version);
		return result;
	}

	protected String getXMLdescription(String groupId, String artifactId,
			List<String> versions) {
		String header_template = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
				+ "<metadata>\n<groupId>%s</groupId>\n"
				+ "<artifactId>%s</artifactId>\n";

		Collections.sort(versions); // sort list
		String elderVersion = versions.get(0); // get first element

		String content = "<version>" + elderVersion + "</version>\n"
				+ "<versioning>\n<versions>\n";
		for (Iterator<String> iterator = versions.iterator(); iterator
				.hasNext();) {
			content += String
					.format("<version>%s</version>\n", iterator.next());
		}
		content += "</versions>\n</versioning>\n";

		String footer = "</metadata>";
		String header = String.format(header_template, groupId, artifactId);
		String result = header + content + footer;

		return result;
	}

}
