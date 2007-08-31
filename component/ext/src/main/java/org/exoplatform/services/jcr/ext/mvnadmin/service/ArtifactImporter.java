/**
 * Copyright 2001-2007 The eXo Platform SASL   All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 */

package org.exoplatform.services.jcr.ext.mvnadmin.service;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;
import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.Value;
import org.apache.commons.logging.Log;
import org.codehaus.plexus.digest.Digester;
import org.codehaus.plexus.digest.DigesterException;
import org.codehaus.plexus.digest.Md5Digester;
import org.codehaus.plexus.digest.Sha1Digester;
import org.codehaus.plexus.util.FileUtils;
import org.exoplatform.services.jcr.ext.mvnadmin.ArtifactBean;
import org.exoplatform.services.log.ExoLogger;

/**
 * Created by The eXo Platform SARL        .
 * @author Volodymyr Krasnikov
 * @version $Id: ArtifactImporter.java 11:16:02
 */

public class ArtifactImporter {

	public static int ROOT_ID_TYPE = -1;
	public static int GROUP_ID_TYPE = 0;
	public static int ARTIFACT_ID_TYPE = 1;
	public static int VERSION_ID_TYPE = 2;
	public static String STRING_TERMINATOR = "*";
	
	private static Log log = ExoLogger.getLogger(ArtifactImporter.class);

	private ArtifactBean artifactBean;
	private Session session;
	 
	protected Digester md5Digester = new Md5Digester();
	protected Digester sha1Digester = new Sha1Digester();

	//Put Session object in constructor - 
	//This operation -ImportArtifact- works in SESSION, opens and closes it!
	public ArtifactImporter(Session session) {
		this.session = session;
	}

	public void addToRepository(ArtifactBean artifactBean)
			throws RepositoryException {
		this.artifactBean = artifactBean;
		addToRepository(artifactBean.getGroupId(),
				artifactBean.getArtifactId(), artifactBean.getVersion());
	}

	private void addToRepository(String groupId, String artifactId,
			String version) throws RepositoryException {
		Node rootNode;
		if (session.getRootNode().hasNode("ExoArtifactRepository"))
			rootNode = session.getRootNode().getNode("ExoArtifactRepository");
		else{
			rootNode = session.getRootNode().addNode("ExoArtifactRepository");
			rootNode.addMixin("exo:artifact"); //add mix type
			rootNode.setProperty("exo:pathType", ArtifactImporter.ROOT_ID_TYPE);
		}
		
		Node groupIdTailNode = createGroupIdLayout(rootNode, groupId);
		Node artifactIdNode = createArtifactIdLayout(groupIdTailNode, artifactId);
		
		Node versionNode = createVersionLayout(artifactIdNode);
				
		importArtifactToRepository(versionNode);
		
		createPom(versionNode);
		
		updateMetadata(versionNode);
		
		session.save();
	}

	private Node createArtifactIdLayout(Node groupId_NodeTail, String artifactId)
			throws RepositoryException {
		Node artifactIdNode;
		if (!groupId_NodeTail.hasNode(artifactId)) {
			artifactIdNode = groupId_NodeTail.addNode(artifactId, "nt:folder");
			artifactIdNode.addMixin("exo:artifact");

			artifactIdNode.setProperty("exo:pathType",
					ArtifactImporter.ARTIFACT_ID_TYPE);
			artifactIdNode.setProperty("exo:versionList", new String[] {
					ArtifactImporter.STRING_TERMINATOR,
					ArtifactImporter.STRING_TERMINATOR });
		} else {
			artifactIdNode = groupId_NodeTail.getNode(artifactId);
		}
		return artifactIdNode;
	}

	//this function creates hierarchy in JCR storage acording to groupID
	// parameter : com.google.code...
	private Node createGroupIdLayout(Node rootNode, String groupId)
			throws RepositoryException {
		groupId = groupId.replace('.', ':');
		Vector<String> struct_groupId = new Vector<String>();
		String[] items = groupId.split(":");
		for (String subString : items) {
			struct_groupId.add(subString);
		}
		Node groupIdTail = rootNode;
		
		for (Iterator<String> iterator = struct_groupId.iterator(); iterator.hasNext();) {
			String name = iterator.next();
					
			Node levelNode;
			if (!groupIdTail.hasNode(name)) { //Node do not has such child nodes
				
				levelNode = groupIdTail.addNode( name, "nt:folder");
				levelNode.addMixin("exo:artifact");
				levelNode.setProperty("exo:pathType", ArtifactImporter.GROUP_ID_TYPE);

			} else {
				levelNode = groupIdTail.getNode(name);
			}
			groupIdTail = levelNode;
		}

		return groupIdTail;
	}

	private void updateVersionList(Node artifactId)
			throws RepositoryException {
		    
		//Update version list
		String version = artifactBean.getVersion();
		
		Property property = artifactId.getProperty("exo:versionList");
		Value[] values = property.getValues();
		Vector<String> versions = new Vector<String>();
		// refactore it -
		for (Value ver : values) {
			String str = ver.getString();
			if (!str.equals(ArtifactImporter.STRING_TERMINATOR)) {
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
		//
	}

	private Node createVersionLayout(Node artifactId)
			throws RepositoryException {
		String version = artifactBean.getVersion();
		Node currentVersion = artifactId.addNode(version, "nt:folder");
		currentVersion.addMixin("exo:artifact");
		currentVersion.setProperty("exo:pathType", ArtifactImporter.VERSION_ID_TYPE);
		//currentVersion.setProperty("exo:version", version);
		
		//version list property is already added to artifactId Node !!!!
		updateVersionList(artifactId); //Add current version to version list
		
		updateMetadata(artifactId);	//creates all needed data - "maven-metadata.xml & checksums"

		return currentVersion;
	}

	private void importArtifactToRepository(Node versionNode)
			throws RepositoryException {
		//Note that artifactBean been initialized within constructor
		String mimeType; //for common use
		Node jarNode = versionNode.addNode("jar", "nt:file");
		Node jarFile = jarNode.addNode("jcr:content", "nt:resource");
		mimeType = "application/zip";
		jarFile.setProperty("jcr:mimeType", mimeType);
		jarFile.setProperty("jcr:lastModified", Calendar.getInstance());
		
		String filename = FileUtils.basename(artifactBean.getJar().getName());	//gets filename without extension
		filename = filename.concat("jar");
		
		jarFile.addMixin("exo:artifact");	//adds ability to use md5 and sha1 properties;
		jarFile.setProperty("exo:filename",  filename);
		
		try {
			FileInputStream ios = null;
			try {
				ios = new FileInputStream(new File(artifactBean.getJar().getPath()));
				jarFile.setProperty("jcr:data", ios);
			} finally {
				ios.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (SecurityException e) {
			e.printStackTrace();
		}
			
		try{
			jarFile.setProperty("exo:md5", getChecksum( artifactBean.getJar(), "MD5" ));
			jarFile.setProperty("exo:sha1", getChecksum( artifactBean.getJar(), "SHA-1" ));
		}
		catch(NoSuchAlgorithmException e){
			e.printStackTrace();
		}
		catch(DigesterException e){
			e.printStackTrace();
		}

	}
	private void createPom(Node versionNode) throws RepositoryException {
		Node pomNode = versionNode.addNode("pom", "nt:file");
		Node pomFile = pomNode.addNode("jcr:content", "nt:resource");
		String mimeType = "plain/text";
		pomFile.setProperty("jcr:mimeType", mimeType);
		pomFile.setProperty("jcr:lastModified", Calendar.getInstance());

		String filename = FileUtils.basename(artifactBean.getPom().getName()); // gets
																				// filename
																				// without
																				// extension
		filename = filename.concat("jar.pom");
		pomFile.addMixin("exo:artifact");
		pomFile.setProperty("exo:filename", filename + ".pom");
		try {
			FileInputStream ios = null;
			try {
				ios = new FileInputStream(artifactBean.getPom().getPath());
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
			pomFile.setProperty("exo:md5", getChecksum(artifactBean.getPom(),
					"MD5"));
			pomFile.setProperty("exo:sha1", getChecksum(artifactBean.getPom(),
					"SHA-1"));
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (DigesterException e) {
			e.printStackTrace();
		}

	}
	
	//this function is used to add a metadata to artifact;
	//metadata can be placed in artifactId Node and contains a list of available artifacts
	// also metadata is placed in each version forder with a jar and pom files. In this case it contains only version of current artifact  
	private void updateMetadata(Node parentNode) throws RepositoryException{
		
		String groupId = artifactBean.getGroupId();
		String artifactId = artifactBean.getArtifactId();
		String version = artifactBean.getVersion();
		
		Node xmlfile;
		if( !parentNode.hasNode("maven-metadata.xml")){
			Node metadata = parentNode.addNode("maven-metadata.xml", "nt:file");
			xmlfile = metadata.addNode("jcr:content","nt:resource");
			String mimeType="plain/text";
			xmlfile.setProperty("jcr:mimeType", mimeType);
			xmlfile.setProperty("jcr:lastModified", Calendar.getInstance());
		}else{
			Node metadata = parentNode.getNode("maven-metadata.xml");
			xmlfile = metadata.getNode("jcr:content");
			xmlfile.setProperty("jcr:lastModified", Calendar.getInstance());
		}
				
		Property pathType = parentNode.getProperty("exo:pathType");
		//checks if we deal with multi version list or not - multi contains a list of strings.
		try {
			if (pathType.getLong() == ArtifactImporter.ARTIFACT_ID_TYPE ) {

				Property list = parentNode.getProperty("exo:versionList");
				Value[] values = list.getValues();
				ArrayList<String> versions = new ArrayList<String>();
				for (Value ver : values) {
					String str = ver.getString();
					versions.add(str);
				}
				ByteArrayInputStream ios = new ByteArrayInputStream(
						getXMLdescription(groupId, artifactId, versions)	//use List
								.getBytes());
				xmlfile.setProperty("jcr:data", ios);
				ios.close();

			} else {
				ByteArrayInputStream ios = new ByteArrayInputStream(
						getXMLdescription(groupId, artifactId, version)		//use simple String
								.getBytes());
				xmlfile.setProperty("jcr:data", ios);
				ios.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/*
	private String getChecksum(File file, String algorithm){
		StringBuffer hexString = new StringBuffer();
		try{
			MessageDigest md = MessageDigest.getInstance(algorithm);	//use md5 or sha1
			byte[] binfile = null;
			try {
				FileInputStream ios = new FileInputStream(file);
				int size = ios.available();
				binfile = new byte[size];
				ios.read(binfile);
				ios.close();
			} catch (IOException e) {
				e.printStackTrace();
			}

			md.update( binfile );
            byte[] hash = md.digest();
                        
            for (int i = 0; i < hash.length; i++) {
                if ((0xff & hash[i]) < 0x10) {
                    hexString.append("0" + Integer.toHexString((0xFF & hash[i])));
                } else {
                    hexString.append(Integer.toHexString(0xFF & hash[i]));
                }
            }
		}
		catch(NoSuchAlgorithmException e){
			e.printStackTrace();
		}
		return hexString.toString();
	}
	*/
	
	protected String getChecksum(File file, String algo)
			throws NoSuchAlgorithmException, DigesterException {
		if ("MD5".equals(algo)) {
			return md5Digester.calc(file);
		} else if ("SHA-1".equals(algo)) {
			return sha1Digester.calc(file);
		} else {
			throw new NoSuchAlgorithmException("No support for algorithm " + algo + ".");
		}
	}

	protected String getXMLdescription(String groupId, String artifactId,
			String version) {
		String template = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
				+ "<metadata>\n<groupId>%s</groupId>\n"
				+ "<artifactId>%s</artifactId>\n"
				+ "<version>%s</version>\n" + "</metadata>";
		String result = String.format(template, groupId, artifactId, version);
		return result;
	}

	protected String getXMLdescription(String groupId, String artifactId,
			List<String> versions) {
		String header_template = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
				+ "<metadata>\n<groupId>%s</groupId>\n"
				+ "<artifactId>%s</artifactId>\n";
		
		Collections.sort(versions); //sort list
		String elderVersion = versions.get(0); //get first element

		String content = "<version>" + elderVersion + "</version>\n"
				+ "<versioning>\n<versions>\n";
		for (Iterator<String> iterator = versions.iterator(); iterator
				.hasNext();) {
			content += String.format("<version>%s</version>\n", iterator.next());
		}
		content += "</versions>\n</versioning>\n";
		
		String footer = "</metadata>";
		String header = String.format(header_template, groupId, artifactId);
		String result = header + content + footer;

		return result;
	}
		
	

}
