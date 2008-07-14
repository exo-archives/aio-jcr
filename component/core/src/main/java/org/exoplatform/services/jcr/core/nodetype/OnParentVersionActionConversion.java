package org.exoplatform.services.jcr.core.nodetype;

import javax.jcr.version.OnParentVersionAction;

/**
 * Serialization/Deserialization for OnParentVersionAction beans.
 * For JiBX binding process only. 
 * @author <a href="mailto:peterit@rambler.ru">Petro Nedonosko</a>
 */
public class OnParentVersionActionConversion {

  public static String serializeType(int propertyType) {
    
    String r = OnParentVersionAction.ACTIONNAME_IGNORE; // default
    try {
      r = OnParentVersionAction.nameFromValue(propertyType);
    } catch(IllegalArgumentException e) {
      //r = PropertyType.TYPENAME_UNDEFINED;
    } catch(Exception e) {
      //r = PropertyType.TYPENAME_UNDEFINED;
    }    
    return r;
  }
  
  public static int deserializeType(String propertyTypeString) {
    
    int r = OnParentVersionAction.IGNORE;  // default
    try {
      r = OnParentVersionAction.valueFromName(propertyTypeString);
    } catch(IllegalArgumentException e) {
      //r = PropertyType.TYPENAME_UNDEFINED;
    } catch(Exception e) {
      //r = PropertyType.TYPENAME_UNDEFINED;
    }    
    return r;
  }
  
}
