package org.exoplatform.services.jcr.ext.mvnadmin.service;

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
import org.exoplatform.services.jcr.ext.mvnadmin.ArtifactServiceImpl;

/*27.08.2007-11:55:09 Volodymyr*/
public class ImportArtifact {
  public static int ARTIFACT_ID_TYPE     = 1;
  
  public static int GROUP_ID_TYPE        = 0;
  
  public static int METADATA_DEFINED     = 4;
  
  public static int METADATA_NOT_DEFINED = 3;

  public static int ROOT_ID_TYPE        = -1;

  public static String STRING_TERMINATOR = "*";

  public static int VERSION_ID_TYPE      = 2;
  
  private Session session;
  //Put Session object in constructor - 
  //This operation -ImportArtifact- works in SESSION, opens and closes it!
  public ImportArtifact(Session session){
    this.session = session;
  }
  public void addToRepository(ArtifactBean artifactBean) throws RepositoryException{
    addToRepository(artifactBean.getGroupId(), artifactBean.getArtifactId(), 
        artifactBean.getVersion(), artifactBean.getFile());
  }
  private void addToRepository(String groupId, String artifactId, String version, URL artifactUrl) throws RepositoryException{
    Node rootNode;
    if(session.getRootNode().hasNode("ExoArtifactRepository"))
      rootNode = session.getRootNode().getNode("ExoArtifactRepository");
    else
      rootNode = session.getRootNode().addNode("ExoArtifactRepository");
    
    rootNode.addMixin("exo:artifact"); //add mix type
    rootNode.setProperty("exo:pathType",ImportArtifact.ROOT_ID_TYPE);
    
    Node groupIdTailNode = createGroupIdLayout(rootNode, groupId);
    Node artifactIdNode = createArtifactIdLayout(groupIdTailNode, artifactId);
    createVersionLayout(artifactIdNode, version);
        
    //updateArtifactIdMetadata(artifactIdNode, artifact); // currentNode points to
    //addArtifactToRepository(artifact);
    
    session.save();
  }
  
  private Node createArtifactIdLayout(Node groupId_NodeTail, String artifactId) throws RepositoryException {
    Node artifactIdNode;
    if(!groupId_NodeTail.hasNode(artifactId)){
      artifactIdNode = groupId_NodeTail.addNode(artifactId, "nt:folder");
      artifactIdNode.addMixin("exo:artifact");
      
      artifactIdNode.setProperty("exo:pathType", ImportArtifact.ARTIFACT_ID_TYPE);
      artifactIdNode.setProperty("exo:versionList", 
          new String[]{ImportArtifact.STRING_TERMINATOR,ImportArtifact.STRING_TERMINATOR} );
    }else{
      artifactIdNode = groupId_NodeTail.getNode(artifactId);
    }
    return artifactIdNode;
  }
  
  //this function creates hierarchy in JCR storage acording to groupID
  // parameter : com.google.code...
  private Node createGroupIdLayout(Node rootNode, String groupId) throws RepositoryException {
    groupId = groupId.replace('.',':');
    Vector<String> struct_groupId = new Vector<String>();
    String[] items = groupId.split(":");
    for (String subString : items ) {
      struct_groupId.add(subString);
    }
    Node groupIdTail = rootNode;
    for(Iterator<String> iterator = struct_groupId.iterator(); iterator.hasNext(); ){
      String name = iterator.next();
      Node levelNode;
      if( !groupIdTail.hasNode(name) ){ //Node do not has such child nodes
        levelNode = groupIdTail.addNode( iterator.next(), "nt:folder");
        levelNode.addMixin("exo:artifact");
        levelNode.setProperty("exo:pathType", ImportArtifact.GROUP_ID_TYPE);
        
      }else{
        levelNode = groupIdTail.getNode(name);
      }
      groupIdTail = levelNode;
    }
    
    return groupIdTail;
  }
  
  private Node createVersionLayout(Node artifactId, String version) throws RepositoryException {
    Node currentVersion = artifactId.addNode(version, "nt:folder");
    currentVersion.addMixin("exo:artifact");
    currentVersion.setProperty("exo:pathType", ImportArtifact.VERSION_ID_TYPE);
    currentVersion.setProperty("exo:version", version);
    
    Property property = artifactId.getProperty("exo:versionList");
    Value[] values = property.getValues();
    Vector<String> versions = new Vector<String>();
   
    // refactore it -
    for(Value ver: values){
      String str = ver.getString();
      if( !str.equals(ImportArtifact.STRING_TERMINATOR) ){
        versions.addElement(str);
      }
    }
    versions.addElement( version );
    String[] newValues = new String[versions.capacity()];
    Iterator i = versions.iterator();
    int index = 0;
    while(i.hasNext()){
      newValues[index++] = (String)i.next();
    }
    artifactId.setProperty("exo:versionList", newValues );
    
    Node jarNode = currentVersion.addNode("jar", "nt:file");
    Node file = jarNode.addNode("jcr:content","nt:resource");
    String mimeType = "application/zip";
    file.setProperty("jcr:mimeType", mimeType);
    file.setProperty("jcr:lastModified", Calendar.getInstance());
    //file.setProperty("jcr:filename", "PUT_HERE_FILENAME");
    
    file.setProperty("jcr:data", "");
    
    return currentVersion;
  }

}
 