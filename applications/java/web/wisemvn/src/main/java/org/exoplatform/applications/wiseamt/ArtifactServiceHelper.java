package org.exoplatform.applications.wiseamt;

import java.io.File;
import java.io.OutputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Hashtable;

/*14.08.2007-12:45:05 Volodymyr*/
public class ArtifactServiceHelper {
  private static ArtifactServiceHelper instance = null;
  public static int GROUP_ID_TYPE = 0;
  public static int ARTIFACT_ID_TYPE = 1;
  public static int VERSION_ID_TYPE = 2;
  public static int METADATA_NOT_DEFINED = 3;
  public static int METADATA_DEFINED = 4;
  
  public static ArtifactServiceHelper getInstance(){
    if( instance == null)
      return instance = new ArtifactServiceHelper();
    return instance;
  }
  private ArtifactServiceHelper(){
    // default constructor
  }
 
  private Hashtable<String, Object> map = new Hashtable<String, Object>();
  
  public void setProperty(String key, Object value){
    map.put(key, value);
  }
  
  public Object getProperty(String key){
    return map.get(key);
  }
  
  public static String convertNavType(String groupIdPath){
    String result="";
    for(String subString: groupIdPath.split("/")){
      result = result.concat(subString).concat(".");
    }
    return result.substring(0,result.length()-1); //do not include last dot
  }
  
  public static String getFileName(URL filepath) throws URISyntaxException{
    File file = new File(filepath.toURI());
    String name = file.getName();
    int lastDotSeperator = name.lastIndexOf(".");
    return name.substring(0, lastDotSeperator - 1); //without file extansion and previous dot.
  }
  
  // creates maven-metadata.xml with such DTD:
  /*
   * 
   * */
  public static OutputStream toXML(ArtifactBean artifact){
    return null;
  }

}
 