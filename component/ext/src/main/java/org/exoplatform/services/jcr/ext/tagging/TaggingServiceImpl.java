/*
 * Copyright (C) 2003-2007 eXo Platform SAS.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
 */

package org.exoplatform.services.jcr.ext.tagging;

import java.net.URI;
import java.util.Collection;
import java.util.Comparator;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.exoplatform.container.xml.InitParams;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.config.RepositoryConfigurationException;
import org.exoplatform.services.jcr.core.ManageableRepository;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.jcr.ext.registry.RegistryEntry;
import org.exoplatform.services.jcr.ext.registry.RegistryService;
import org.exoplatform.services.security.ConversationState;

/**
 * Created by The eXo Platform SAS .
 * 
 * @author eXo Platform
 * @version $Id: $
 */

public class TaggingServiceImpl implements TaggingService {

  // private ConversationRegistry identityRegistry;
  private RepositoryService repoService;

  private RegistryService   registryService;

  public TaggingServiceImpl(InitParams params,
                            RegistryService registryService,
                            RepositoryService repoService) {

    // this.identityRegistry = registry;
    this.repoService = repoService;
    this.registryService = registryService;

  }

  public void addTag(String name, URI uri, String description, String repository, String workspace) throws RepositoryException,
                                                                                                   RepositoryConfigurationException {

    String user = ConversationState.getCurrent().getIdentity().getUserId();
    Node root = root(repository, workspace, ConversationState.getCurrent());

    Node tag;
    if (root.getNode("tags").hasNode(name))
      tag = root.getNode("tags").getNode(name);
    else
      tag = root.getNode("tags").addNode(name, "exo:tag");

    Node userTags;
    if (tag.hasNode(user))
      userTags = tag.getNode(user);
    else
      userTags = tag.addNode(user, "exo:userTags");

    Node userTag;
    if (userTags.hasNode(name))
      userTag = userTags.getNode(name);
    else {
      userTag = userTags.addNode(name, "exo:userTag");
    }
    userTag.setProperty("exo:uri", uri.toASCIIString());
    userTag.setProperty("exo:description", description);

    root.save();

  }

  // public void addTag(String name, UnifiedNodeReference unr, String userId,
  // String description) {
  // session(unr.getWorkpace(), unr.getRepository());
  //    
  // }

  public TagURICrossrate getCrossrate(String tagName, URI uri) {

    // TODO Auto-generated method stub
    return null;
  }

  public Collection<TagURICrossrate> getCrossrate(URI uri) {
    // TODO Auto-generated method stub
    return null;
  }

  public TagRate getRate(String tagName) {
    // TODO Auto-generated method stub
    return null;
  }

  public Collection<TagRate> getRates() {
    // TODO Auto-generated method stub
    return null;
  }

  public Collection<TagRate> getRates(Comparator comparator) {
    // TODO Auto-generated method stub
    return null;
  }

  public Collection<Tag> getTags(String userId) {
    // TODO Auto-generated method stub
    return null;
  }

  public Collection<Tag> getTags(URI uri) {
    // TODO Auto-generated method stub
    return null;
  }

  private Node root(String repoName, String workspaceName, ConversationState state) throws RepositoryException,
                                                                                   RepositoryConfigurationException {
    ManageableRepository repository = repoService.getRepository(repoName);
    // Identity id = this.identityRegistry.getIdentity();
    SessionProvider sp = (SessionProvider) state.getAttribute(SessionProvider.SESSION_PROVIDER);
    RegistryEntry re = registryService.getEntry(sp, RegistryService.EXO_SERVICES + "/tagging");
    //
    return sp.getSession(workspaceName, repository).getRootNode();
  }

}
