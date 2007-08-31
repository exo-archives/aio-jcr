/**
 * Copyright 2001-2007 The eXo Platform SASL   All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 */

package org.exoplatform.services.jcr.ext.mvnadmin.service;

/**
 * Created by The eXo Platform SARL        .
 * @author Volodymyr Krasnikov
 * @version $Id: ArtifactBrowser.java 11:16:02
 */

import java.util.ArrayList;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.apache.commons.logging.Log;
import org.exoplatform.services.log.ExoLogger;

public class ArtifactBrowser {
	private Session session;
	private Node rootNode;

	private static Log log = ExoLogger.getLogger(ArtifactBrowser.class);

	public ArtifactBrowser(Session session) throws RepositoryException {
		this.session = session;
		rootNode = session.getRootNode(); // root Node is already been initialized! 
	}

	public List<Node> getChildLevel(Node parentNode) throws RepositoryException {
		ArrayList<Node> nodeList = new ArrayList<Node>();
		if (parentNode.hasNodes()) {
			for (NodeIterator iterator = parentNode.getNodes(); iterator
					.hasNext();) {
				nodeList.add(iterator.nextNode());
			}
		} else
			return null;
		return nodeList;
	}

}
