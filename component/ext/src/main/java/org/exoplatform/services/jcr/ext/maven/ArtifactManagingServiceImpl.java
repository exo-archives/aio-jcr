package org.exoplatform.services.jcr.ext.maven;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import javax.jcr.ItemNotFoundException;
import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Property;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.Value;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.SimpleLayout;

import org.exoplatform.container.xml.InitParams;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.config.RepositoryConfigurationException;
import org.exoplatform.services.jcr.core.ManageableRepository;
import org.exoplatform.services.jcr.core.nodetype.ExtendedNodeTypeManager;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.jcr.ext.registry.RegistryService;
import org.exoplatform.services.log.ExoLogger;
import org.picocontainer.Startable;
import org.apache.maven.wagon.observers.ChecksumObserver;

/**
 * Created by The eXo Platform SARL .<br/> Service responsible for
 * Administration Maven repository the served JCR structure inside workspaceName
 * is: 
 * rootPath (maven-root)/ 
 * ---part-of-group-folder1/ (nt:folder + exo:groupId) ..
 * ---part-of-group-foldern/ 
 * ------artifact-root-folder/ (nt:folder + exo:artifactId)

 * ---------maven-metadata.xml(nt:file)
 * ---------maven-metadata.xml.sha1(nt:file)
 * 
 * ---------artifact-version-folder/ (nt:folder + exo:versionId) 
 * ------------artifactId-version.jar (nt:file + exo:mavenjar / nt:resource)
 * ------------artifactId-version.jar.sha1 (nt:file + exo:mavensha1 / nt:resource )
 * 
 * ------------artifactId-version.pom (nt:file + exo:mavenpom / nt:resource)
 * ------------artifactId-version.pom.sha1 (nt:file + exo:mavensha1/ (nt:resource)

 * ------------maven-metadata.xml (nt:file + exo:mavenmetadata / (nt:resource )
 * ------------maven-metadata.xml.sha1 (nt:file + exo:mavensha1 / (nt:resource)
 * 
 * @author Gennady Azarenkov
 * @author Volodymyr Krasnikov
 * @version $Id: $
 */
public class ArtifactManagingServiceImpl implements ArtifactManagingService,
		Startable {

	private static final String STRING_TERMINATOR = "*";
	private static final String NT_FILE = "artifact-nodetypes.xml";
	private RepositoryService repositoryService;
	private RegistryService registryService;
	private InitParams initParams;
	private SessionProvider sessionProvider;
	private String repoWorkspaceName = "ws";
	private String repoPath;
	private ArtifactDescriptor artifactDescriptor;
	private static Log LOGGER = ExoLogger.getLogger(ArtifactManagingServiceImpl.class);
	private Map<String, String> mimeMap = new Hashtable<String, String>();

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
			InputStream jarFile, InputStream pomFile) throws RepositoryException {
		
		Session session = currentSession(sp);
		Node rootNode = session.getRootNode();
		
		Node groupId_tail = createGroupIdLayout(rootNode, artifact );
		
		Node artifactId_node = createArtifactIdLayout(groupId_tail, artifact);
		
		updateMetadata(artifactId_node, artifact);
		
		Node version_node = createVersionLayout(artifactId_node, artifact);
		
		importResource(version_node, jarFile, "jar", artifact);
		importResource(version_node, pomFile, "pom", artifact);
		
		updateMetadata( version_node, artifact );
		
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
		LOGGER.debug("Starting getDescriptors with parentFolder : "
				+ parentFolder.getAsString());
		LOGGER.debug("Getting session via SessionProvider");
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
		LOGGER.debug("Finishing with browsing artifacts");
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
		LOGGER.debug("Starting ArtifactManagingService ...");
		
		sessionProvider = SessionProvider.createSystemProvider();
		try {
			InputStream xml = getClass().getResourceAsStream(NT_FILE);
			ManageableRepository rep = repositoryService.getCurrentRepository();
			rep.getNodeTypeManager().registerNodeTypes(xml,
					ExtendedNodeTypeManager.IGNORE_IF_EXISTS);
			
			registryService.getEntry(sessionProvider,
					RegistryService.EXO_SERVICES, "ArtifactManaging");
			
			LOGGER.debug("Started successful");
			// TODO if registryService != null get workspaceName and rootPath
			// from registryService
			// else get it from init params
		} catch (ItemNotFoundException e) {
			LOGGER.debug("Getting workspaceName and rootPath from initParams");
			
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
		return sp.getSession(repoWorkspaceName,	repositoryService.getCurrentRepository());
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
			// Node do not has such child nodes
			if (!groupIdTail.hasNode(name)){ 
				levelNode = groupIdTail.addNode(name, "nt:folder");
				levelNode.addMixin("exo:groupId");
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
			artifactIdNode.addMixin("exo:artifactId");
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
		currentVersion.addMixin("exo:versionId");

		// version list property is already added to artifactId Node !!!!
		updateVersionList(artifactId, artifact); // Add current version to version list

		// creates all needed data - "maven-metadata.xml & checksums"
		//updateMetadata(artifactId, artifact); 

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
	
	private List<String> getAllArtifactVersions(Node artifactId) throws RepositoryException{
		Property property = artifactId.getProperty("exo:versionList");
		Value[] values = property.getValues();
		Vector<String> versionList = new Vector<String>();
		for (Value ver : values) {
			String str = ver.getString();
			if (!str.equals(ArtifactManagingServiceImpl.STRING_TERMINATOR)) {
				versionList.addElement(str);
			}
		}
		return versionList;
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
		
		File srcFile = null;
		try {
			if (parentNode.isNodeType("exo:artifactId")) {
				srcFile = createMultiMetadata(groupId, artifactId, getAllArtifactVersions(parentNode) );
			}
			if (parentNode.isNodeType("exo:versionId")) {
				srcFile = createSingleMetadata(groupId, artifactId, version);
			}
			
			InputStream file_in = new FileInputStream(srcFile);
			importResource(parentNode, file_in, "metadata", artifact);
			
		} catch (FileNotFoundException e) {
			LOGGER.error("Cannot create temporary file hor holding artifact versions",e);
		}
		
	}
	
	private String getRelativeMimeType(String key){
		return (String)mimeMap.get(key); 
	}
	//this method used for writing to repo jars, poms and their checksums
	private void importResource(Node parentNode, InputStream file_in, String resourceType, ArtifactDescriptor artifact )
			throws RepositoryException {
		// Note that artifactBean been initialized within constructor
		// resourceType can be jar, pom, metadata
		
		String filename;
		if(resourceType.equals("metadata"))
			filename = "maven-metadata.xml";
		else
			filename = String.format("%s-%s.%s",artifact.getArtifactId(), artifact.getVersionId(), resourceType);
				
		OutputStream fout = null;
		File tmp_file = null;
		try {
			String tmpFilename = getUniqueFilename(filename);
			tmp_file = File.createTempFile( tmpFilename, null );
			fout = new FileOutputStream(tmp_file);
			IOUtils.copy(file_in, fout);
			fout.flush();
		} catch (FileNotFoundException e) {
			LOGGER.error("Cannot create .tmp file for storing artifact", e);
		}
		catch(IOException e){
			LOGGER.error("IO exception on .tmp file for storing artifact", e);
		}finally {
			IOUtils.closeQuietly(file_in);
			IOUtils.closeQuietly(fout);
		}
				
		writePrimaryContent(parentNode, filename, resourceType, tmp_file);
		writeChecksum(parentNode, filename, tmp_file, "SHA1");
		
		try{
			//and collect all garbage : temporary files
			FileUtils.forceDelete(tmp_file);
		}catch(IOException e){
			LOGGER.error("Cannot delete tmp file", e);
		}
				
	}
	
	private void writePrimaryContent(Node parentNode, String filename,
			String resourceType, File srcFile) throws RepositoryException {
		String mimeType = getRelativeMimeType(resourceType);

		Node nodeResourceFile = parentNode.addNode(filename, "nt:file");

		String mixinType = "exo:maven".concat(resourceType);
		if (nodeResourceFile.canAddMixin(mixinType))
			nodeResourceFile.addMixin(mixinType);
		try {
			InputStream file_is = new FileInputStream(srcFile);
			
			Node content = nodeResourceFile.addNode("jcr:content", "nt:resource");
			content.setProperty("jcr:mimeType", mimeType);
			content.setProperty("jcr:lastModified", Calendar.getInstance());
			content.setProperty("jcr:data", file_is);
	
			IOUtils.closeQuietly(file_is);
		} catch (FileNotFoundException e) {
			LOGGER.error("Cannot read from .tmp resource file", e);
		}
	}
	private void writeChecksum(Node parentNode, String filename, File srcFile, String algorithm)
			throws RepositoryException {
		Node nodeChecksumFile = parentNode.addNode(filename.concat("."+algorithm.toLowerCase()),"nt:file");

		String mixinType = "exo:maven".concat( algorithm.toLowerCase() );
		if ( nodeChecksumFile.canAddMixin(mixinType))
			nodeChecksumFile.addMixin(mixinType);
		try {
			InputStream checksum_is = new ByteArrayInputStream(getChecksum(
					srcFile, algorithm).getBytes());
			String mimeType = "text/xml";
						
			Node content = nodeChecksumFile.addNode("jcr:content", "nt:resource");
			content.setProperty("jcr:mimeType", mimeType);
			content.setProperty("jcr:lastModified", Calendar.getInstance());
			content.setProperty("jcr:data", checksum_is);
			
			IOUtils.closeQuietly(checksum_is);
		} catch (FileNotFoundException e) {
			LOGGER.error("Cannot read from .tmp resource file", e);
		} catch (IOException e) {
			LOGGER.error("Cannot read from .tmp resource file", e);
		} catch (NoSuchAlgorithmException e) {
			LOGGER.error("No such algorithm for generating checksums", e);
		}
	}
		
	protected String getChecksum(File file, String algo)
			throws NoSuchAlgorithmException, IOException {
		ChecksumObserver checksum = null;
		try {
			byte[] buffer = FileUtils.readFileToByteArray(file);
			if ("MD5".equals(algo)) {
				checksum = new ChecksumObserver("MD5"); // md5 by default
			} else if ("SHA1".equals(algo)) {
				checksum = new ChecksumObserver("SHA-1");
			} else {
				throw new NoSuchAlgorithmException("No support for algorithm "
						+ algo + ".");
			}
			checksum.transferProgress(null, buffer, buffer.length);
			checksum.transferCompleted(null);

		} catch (IOException e) {
			LOGGER.error("Error reading from stream", e);
		}
		return checksum.getActualChecksum();
	}

	protected File createSingleMetadata(String groupId, String artifactId,
			String version) throws FileNotFoundException {
		File temp = null;
		try{
			String filename = getUniqueFilename("maven-metadata.xml");
			temp = File.createTempFile(filename, null);

			OutputStream os = new FileOutputStream(temp);
			XMLOutputFactory factory = XMLOutputFactory.newInstance();
			XMLStreamWriter writer = factory.createXMLStreamWriter(os);
			try{
				writer.writeStartDocument("UTF-8", "1.0");
				writer.writeStartElement("metadata");

				writer.writeStartElement("groupId");
				writer.writeCharacters(groupId);
				writer.writeEndElement();

				writer.writeStartElement("artifactId");
				writer.writeCharacters(artifactId);
				writer.writeEndElement();

				writer.writeStartElement("version");
				writer.writeCharacters(version);
				writer.writeEndElement();

				writer.writeEndElement();
				writer.writeEndDocument();
			} finally {
				writer.flush();
				writer.close();
				os.close();
			}
		}
		catch(XMLStreamException e){
			LOGGER.error("Error on creating metadata - XML", e);
		}
		catch(IOException e){
			LOGGER.error("Error on creating metadata - FILE", e);
		}
		return (temp.exists())?temp:null;
	}

	protected File createMultiMetadata(String groupId, String artifactId,
			List<String> versions) throws FileNotFoundException {
		File temp = null;
		try{
			String filename = getUniqueFilename("maven-metadata.xml");
			temp = File.createTempFile(filename, null);
			
			OutputStream os = new FileOutputStream(temp);
			XMLOutputFactory factory = XMLOutputFactory.newInstance();
			XMLStreamWriter writer = factory.createXMLStreamWriter(os);
			try{
				writer.writeStartDocument("UTF-8", "1.0");
				writer.writeStartElement("metadata");

				writer.writeStartElement("groupId");
				writer.writeCharacters(groupId);
				writer.writeEndElement();

				writer.writeStartElement("artifactId");
				writer.writeCharacters(artifactId);
				writer.writeEndElement();

				Collections.sort(versions); // sort list
				String elderVersion = versions.get(0); // get first element
				
				writer.writeStartElement("version");
				writer.writeCharacters(elderVersion);
				writer.writeEndElement();
				
				writer.writeStartElement("versions");
				writer.writeStartElement("versioning");

				for (Iterator<String> iterator = versions.iterator(); iterator.hasNext();) {
					writer.writeStartElement("version");
					writer.writeCharacters(iterator.next());
					writer.writeEndElement();
				}
				
				writer.writeEndElement();
				writer.writeEndElement();

				writer.writeEndElement();
				writer.writeEndDocument();
			} finally {
				writer.flush();
				writer.close();
				os.close();
			}
		}
		catch(XMLStreamException e){
			LOGGER.error("Error on creating metadata - XML", e);
		}
		catch(IOException e){
			LOGGER.error("Error on creating metadata - FILE", e);
		}
		return (temp.exists())?temp:null;
	}
	
	private void setMimeMap(Map<String, String > mimeTypes){
		mimeMap.clear();
		mimeMap.putAll(mimeTypes);
	}
	
	private String getUniqueFilename(String basename){
		String suffix = ((Double)Math.random()).toString().substring(2, 7);
		return basename +"."+ suffix;
	}
		
}
