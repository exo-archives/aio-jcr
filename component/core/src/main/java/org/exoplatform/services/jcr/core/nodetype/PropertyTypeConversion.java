package org.exoplatform.services.jcr.core.nodetype;

import javax.jcr.PropertyType;

import org.exoplatform.services.jcr.core.ExtendedPropertyType;

/**
 * Serialization/Deserialization for PropertyType beans. For JiBX binding process only.
 * 
 * @author <a href="mailto:peterit@rambler.ru">Petro Nedonosko</a>
 */
public class PropertyTypeConversion {

  public static String serializeType(int propertyType) {

    String r = PropertyType.TYPENAME_UNDEFINED;
    try {
      r = ExtendedPropertyType.nameFromValue(propertyType);
    } catch (IllegalArgumentException e) {
      // r = PropertyType.TYPENAME_UNDEFINED;
    } catch (Exception e) {
      // r = PropertyType.TYPENAME_UNDEFINED;
    }
    return r;
  }

  public static int deserializeType(String propertyTypeString) {

    int r = PropertyType.UNDEFINED;
    try {
      r = ExtendedPropertyType.valueFromName(propertyTypeString);
    } catch (IllegalArgumentException e) {
      // r = PropertyType.TYPENAME_UNDEFINED;
    } catch (Exception e) {
      // r = PropertyType.TYPENAME_UNDEFINED;
    }
    return r;
  }

}
