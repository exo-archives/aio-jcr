package org.exoplatform.services.jcr.core.nodetype;

import java.util.ArrayList;

/**
 * Serialization/Deserialization for simple java types and Strings.
 * For JiBX binding process only. 
 * @author <a href="mailto:peterit@rambler.ru">Petro Nedonosko</a>
 */
public class DefaulStringConversion {
	
  public static String serializeString(String stringValue) {
    
    String r = ""; 
    try {
      r = stringValue != null ? stringValue : "";
    } catch(Exception e) {
      System.err.println("serializeString.Exception: " + e);
    }
    return r;
  }
  
  public static String deserializeString(String stringString) {
    
    String r = "";
    try {
      r = stringString != null ? stringString : "";
    } catch(Exception e) {
      System.err.println("deserializeString.Exception: " + e);
    }   
    return r;
  }
  
	public static String serializeInt(int intValue) {
		
		String r = ""; 
		try {
			r = String.valueOf(intValue);
		} catch(Exception e) {
		}		
		return r;
	}
  
  public static int deserializeInt(String intString) {
    
    int r = 0; 
    try {
      r = Integer.parseInt(intString);
    } catch(Exception e) {
    }   
    return r;
  }
  
	public static String serializeLong(long longValue) {
    
    String r = ""; 
    try {
      r = String.valueOf(longValue);
    } catch(Exception e) {
    }   
    return r;
  }
	
  public static long deserializeLong(String longString) {
    
    long r = 0; 
    try {
      r = Long.parseLong(longString);
    } catch(Exception e) {
    }   
    return r;
  }
  
  public static String serializeLong(int longValue) {
    
    String r = ""; 
    try {
      r = String.valueOf(longValue);
    } catch(Exception e) {
    }   
    return r;
  }   

  public static boolean deserializeBoolean(String boolString) {
    
    boolean r = false; 
    try {
      System.err.println("deserializeBoolean: " + boolString);
      r = Boolean.parseBoolean(boolString);
      System.err.println("deserializeBoolean: res: " + r);
    } catch(Exception e) {
      System.err.println("deserializeBoolean.Exception: " + e);
    }   
    return r;
  }
  
  public static String serializeArrayList(ArrayList arrayList) {
    
    String r = ""; // default
    try {
      if (arrayList != null) {
        
      }
    } catch(IllegalArgumentException e) {
      //r = PropertyType.TYPENAME_UNDEFINED;
    } catch(Exception e) {
      //r = PropertyType.TYPENAME_UNDEFINED;
    }   
    return r;
  }
}
