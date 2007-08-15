package org.exoplatform.applications.wiseamt;

import java.io.File;
import java.net.URL;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import javax.jcr.Node;
import javax.jcr.PathNotFoundException;
import javax.jcr.Property;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.SimpleCredentials;
import javax.jcr.Value;

import org.exoplatform.commons.utils.MimeTypeResolver;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.config.RepositoryConfigurationException;
import org.exoplatform.services.jcr.core.ManageableRepository;
import org.htmlparser.tags.MetaTag;

import com.amazon.thirdparty.Base64.InputStream;

import sun.security.krb5.Credentials;

/*13.08.2007-15:10:05 Volodymyr*/
public class ArtifactServiceImpl implements ArtifactService {

  private final RepositoryService repositoryService;
  private ManageableRepository manageableRepository;
  private Session session;
  private ArtifactServiceHelper helper = ArtifactServiceHelper.getInstance();
  private String WORKSPACE_NAME = "ws1";
  
  public ArtifactServiceImpl(RepositoryService repositoryService){
    this.repositoryService = repositoryService;
    try{
      manageableRepository = repositoryService.getRepository("MavenArtifacts");
    }catch(RepositoryConfigurationException e){
      e.printStackTrace();
    }
    catch(RepositoryException e){
      e.printStackTrace();
    }
    
    try{
      //helper.setProperty("CURRENT_SESSION", getSystemSession(WORKSPACE_NAME) );  //name of workspace must be decrared early
      helper.setProperty("CURRENT_SESSION", getSession() );  //name of workspace must be decrared early
    }catch(RepositoryException ex){
      ex.printStackTrace();
    }
  }
  
  public Session getSession() throws RepositoryException{
    return session = manageableRepository.login( new SimpleCredentials("admin","admin".toCharArray()), 
                                                 WORKSPACE_NAME );
  }
  
  public Session getSystemSession(String workspaceName) throws RepositoryException{
    return session = manageableRepository.getSystemSession(workspaceName); 
  }
  
  public List broseRepositories() {
    // TODO Auto-generated method stub
    return null;
  }

  public void exportRepository(File exportPath) {
    // TODO Auto-generated method stub

  }


    // TODO Auto-generated method stub
    // groupId parameter (string indeed) relies on type of maven repositore navigation
    // flat nav : groupId is if folder. name of folder contains dot delimiters ex: com.myapp.sometype
    // deep nav : groupId is a hierarchy made up with substring of "groupId" string ex:
    //  |
    //  com/
    //     |-myapp/
    //            |-sometype/
    
    //***
    
    // !! NOTA BENE !! Prepeare groupId parameter to the next structure.
    // groupId - string that has next format [str1].[str2].[str3]...[strN]
    // !! Needed dot demiliters format
    // full path (real path in FS) consists with $REPO_ROOT/../../../$artifactId/$version/some_file.jar
    //                                                      ^^^^^^^^^
    //                                                      groupId
    
    // Note that session object is object's variable (initialized already)!!
    // Why do I use sigleton here. Can session break ?
  
  public void importArtifact(ArtifactBean artifact){
  }
  
  public void importArtifact(String groupId, String artifactId, String version, URL artifactUrl) {    
  
    session = (Session)helper.getProperty("CURRENT_SESSION");
    
    ArtifactBean artifact = new ArtifactBean(groupId, artifactId,version, artifactUrl);
    try{
      Node root = session.getRootNode();
      
      Node groupIdTailNode = createGroupIdLayout(root, groupId);
      Node artifactIdNode = createArtifactIdLayout(groupIdTailNode, artifactId);
      createArtifactMetadataLayout(artifactIdNode);
      createVersionLayout(artifactIdNode, version);
     
      updateArtifactIdMetadata(artifactIdNode, artifact); //currentNode points to artifactId node
      addArtifactToRepository( artifact );
      
    }
    catch(RepositoryException ex){
      ex.printStackTrace();
    }

  }
  
  // this function creates hierarchy in JCR storage acording to groupID parameter : com.google.code... 
  private Node createGroupIdLayout(Node rootNode, String groupId) throws RepositoryException{
    Vector<String> struct_groupId = new Vector<String>();
    for(String subString : groupId.split(".")){
      struct_groupId.add(subString);
    }
    Iterator<String> iterator = struct_groupId.iterator();
    Node groupIdTail = rootNode;
    while(iterator.hasNext()){
      groupIdTail = groupIdTail.addNode(iterator.next(), "nt:folder");
      // add property for define type of folder - [groupId or artifactId]
      //property "type" means what part of real path does this Substring make up ?
      groupIdTail.setProperty("path:type", ArtifactServiceHelper.GROUP_ID_TYPE);
    }
    return groupIdTail;
  }
  
  //creates a artifact layout that includes a versions node 
  //
  //returns: points to artifactId nodes
  private Node createArtifactIdLayout(Node groupId_NodeTail, String artifactId) throws RepositoryException{
    Node currentNode = groupId_NodeTail;
    Node artifactIdNode = currentNode.addNode(artifactId, "nt:folder");  //append artifactId to folder hierarchy
    artifactIdNode.setProperty("path:type", ArtifactServiceHelper.ARTIFACT_ID_TYPE);
    Node artifactVersions = artifactIdNode.addNode("versions");
    return artifactIdNode;
  }
  
  // creates a version layout under $versionRoot node, also creates a $jar, $pom, $xml nodes, that
  // hold appropriate data: jar->binary data & checksums; pom->pom file, checksums; xml->metadata, checksums 
  // returns: points to version just created
  private Node createVersionLayout(Node artifactId, String version) throws RepositoryException{
    Node versionRoot = artifactId.getNode("versions");
    Node currentVersion = versionRoot.addNode(version,"nt:folder");
    
    Node jarNode = currentVersion.addNode("jar");
    Node pomNode = currentVersion.addNode("pom");
    Node metadataNode = currentVersion.addNode("metadata");
    
    return currentVersion;
  }

  private void createArtifactMetadataLayout(Node artifactId) throws RepositoryException{
    Node metadata = artifactId.addNode("metadata");
    
    createContentTextData(metadata, "maven-metadata.xml", null);
    createContentBinaryData(metadata, "maven-metadata.xml.md5", null);
    createContentBinaryData(metadata, "maven-metadata.xml.sha1", null);
   
  }
  
  private boolean checkMetadataExists(Node artifactId) throws RepositoryException{
    long status = 0;
    try{
      Property isMetadataSets = artifactId.getProperty("set-metadata");
      Value value = isMetadataSets.getValue();
      status = value.getLong();
    }
    catch(PathNotFoundException e){
      e.printStackTrace();
    }
    return (status == ArtifactServiceHelper.METADATA_DEFINED)?true:false;
  }
  
  private void updateArtifactIdMetadata(Node artifactId, ArtifactBean artifact) throws RepositoryException {
    if( ! checkMetadataExists(artifactId) )
      createArtifactMetadataLayout(artifactId);
   
    
  }
  private void addArtifactToRepository(ArtifactBean artifact){
    
  }
  
  
  
  private void createContentTextData(Node node, String caption, InputStream stream) throws RepositoryException{
    Node xml = node.addNode(caption, "nt:file");
      xml.setProperty("jcr:filename", caption);
      xml.setProperty("jcr:lastModified", Calendar.getInstance());
    Node xmlData = xml.addNode("jcr:content", "nt:resource");
    MimeTypeResolver mimetypeResolver = new MimeTypeResolver();
    mimetypeResolver.setDefaultMimeType("text/xml");
    String mimeType = mimetypeResolver.getDefaultMimeType();
      xmlData.setProperty("jcr:mimeType", mimeType);
    //loads data through InputStream
    //!! Create xml data as stream !! and load it into jcr
      xmlData.setProperty("jcr:data", stream /* PLACE for loading */);
    
  }
  private void createContentBinaryData(Node node, String caption, InputStream stream) throws RepositoryException{
    Node cheksum = node.addNode(caption, "nt:file");
      cheksum.setProperty("jcr:filename", caption);
      cheksum.setProperty("jcr:lastModified", Calendar.getInstance());
    Node data = cheksum.addNode("jcr:content", "nt:resource");
    MimeTypeResolver mimetypeResolver = new MimeTypeResolver();
    mimetypeResolver.setDefaultMimeType("application/zip");
    String mimeType = mimetypeResolver.getDefaultMimeType();
      data.setProperty("jcr:mimeType", mimeType);
    //loads data through InputStream
    //!! Create binary data as stream !! and load it into jcr
      data.setProperty("jcr:data", stream /* PLACE for loading */);
  }
  

  
  public void importRepository(URL externalRepository) {
    // TODO Auto-generated method stub

  }
  
  public void removeArtifact(String groupId, String artifactId, String version) {
    // TODO Auto-generated method stub

  }

  public void searchArtifact(String artifactLikes) {
    // TODO Auto-generated method stub

  }

  public void selectRepository(String repository) {
    // TODO Auto-generated method stub

  }

}
 