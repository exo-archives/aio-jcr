package org.exoplatform.services.jcr.ext.mvnadmin;

import java.io.File;
import java.net.URL;
import java.util.List;
/*10.08.2007-16:22:52 Volodymyr*/

import javax.jcr.Node;
import javax.jcr.RepositoryException;

/*
 * Shows main functionality of Exo Artifact Managment Tool.
 * Available functions:
 *
 * There are many (may be more then one) REPOs under AMT, so user must be able to :
 *  - browse repositories
 *  - select repository : select current repository? with this repo will assign all artifact operations 
 * 
 * For Single artifact
 *  - import : add artifact to current repo
 *  - remove : delete artifact from current repo
 *  - search : search artifact in repo (repos) - If there are repos, search fires on a list of repos
 *  
 * For existing artifact repository (correct maven repository)
 *  - attach : attach ext repository list (add to root node)
 *  - import : imports in current repository
 *  
 * For current repository
 *  - export current repository to local folder
 * 
 * */

/*
 * There is very raw api spec for user's functionality 
 * */
public interface ArtifactService {
  
  public List<Node> broseRepositories(Node parentNode);
  public void importArtifact(ArtifactBean artifactBean) throws RepositoryException ;
  public void removeArtifact(ArtifactBean artifactBean);
  public void searchArtifact(String query);
  
  public void importRepository(URL externalRepository);
  public void exportRepository(File exportPath);

}
 