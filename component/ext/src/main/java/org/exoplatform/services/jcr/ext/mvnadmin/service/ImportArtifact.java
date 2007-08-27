package org.exoplatform.services.jcr.ext.mvnadmin.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Calendar;
import java.util.Iterator;
import java.util.Vector;

import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.Value;

import org.exoplatform.services.jcr.ext.mvnadmin.ArtifactBean;

/*27.08.2007-11:55:09 Volodymyr*/
public class ImportArtifact {

	public static int ROOT_ID_TYPE = -1;
	public static int GROUP_ID_TYPE = 0;
	public static int ARTIFACT_ID_TYPE = 1;
	public static int VERSION_ID_TYPE = 2;
	public static String STRING_TERMINATOR = "*";

	private ArtifactBean artifactBean;
	private Session session;

	//Put Session object in constructor - 
	//This operation -ImportArtifact- works in SESSION, opens and closes it!
	public ImportArtifact(Session session) {
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
		else
			rootNode = session.getRootNode().addNode("ExoArtifactRepository");

		rootNode.addMixin("exo:artifact"); //add mix type
		rootNode.setProperty("exo:pathType", ImportArtifact.ROOT_ID_TYPE);

		Node groupIdTailNode = createGroupIdLayout(rootNode, groupId);
		Node artifactIdNode = createArtifactIdLayout(groupIdTailNode,
				artifactId);
		Node versionNode = createVersionLayout(artifactIdNode, version);

		importArtifactToRepository(versionNode);

		session.save();
	}

	private Node createArtifactIdLayout(Node groupId_NodeTail, String artifactId)
			throws RepositoryException {
		Node artifactIdNode;
		if (!groupId_NodeTail.hasNode(artifactId)) {
			artifactIdNode = groupId_NodeTail.addNode(artifactId, "nt:folder");
			artifactIdNode.addMixin("exo:artifact");

			artifactIdNode.setProperty("exo:pathType",
					ImportArtifact.ARTIFACT_ID_TYPE);
			artifactIdNode.setProperty("exo:versionList", new String[] {
					ImportArtifact.STRING_TERMINATOR,
					ImportArtifact.STRING_TERMINATOR });
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
		for (Iterator<String> iterator = struct_groupId.iterator(); iterator
				.hasNext();) {
			String name = iterator.next();
			Node levelNode;
			if (!groupIdTail.hasNode(name)) { //Node do not has such child nodes
				levelNode = groupIdTail.addNode(iterator.next(), "nt:folder");
				levelNode.addMixin("exo:artifact");
				levelNode.setProperty("exo:pathType",
						ImportArtifact.GROUP_ID_TYPE);

			} else {
				levelNode = groupIdTail.getNode(name);
			}
			groupIdTail = levelNode;
		}

		return groupIdTail;
	}

	private void updateVersionList(Node artifactId, String version)
			throws RepositoryException {
		//Update version list
		Property property = artifactId.getProperty("exo:versionList");
		Value[] values = property.getValues();
		Vector<String> versions = new Vector<String>();
		// refactore it -
		for (Value ver : values) {
			String str = ver.getString();
			if (!str.equals(ImportArtifact.STRING_TERMINATOR)) {
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

	private Node createVersionLayout(Node artifactId, String version)
			throws RepositoryException {

		Node currentVersion = artifactId.addNode(version, "nt:folder");
		currentVersion.addMixin("exo:artifact");
		currentVersion.setProperty("exo:pathType",
				ImportArtifact.VERSION_ID_TYPE);
		currentVersion.setProperty("exo:version", version);

		updateVersionList(artifactId, version); //Add current version to version list

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
		//jarFile.setProperty("jcr:filename", "PUT_HERE_FILENAME");

		try {
			FileInputStream ios = null;
			try {
				ios = new FileInputStream(new File(artifactBean.getJar()
						.getPath()));
				jarFile.setProperty("jcr:data", ios);
			} finally {
				ios.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (SecurityException e) {
			e.printStackTrace();
		}

		Node pomNode = versionNode.addNode("pom", "nt:file");
		Node pomFile = pomNode.addNode("jcr:content", "nt:resource");
		mimeType = "plain/text";
		pomFile.setProperty("jcr:mimeType", mimeType);
		pomFile.setProperty("jcr:lastModified", Calendar.getInstance());
		//file.setProperty("jcr:filename", "PUT_HERE_FILENAME");
		pomFile.setProperty("jcr:data", "");

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

	}

}
