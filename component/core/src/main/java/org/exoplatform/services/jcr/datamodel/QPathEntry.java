/**
 * 
 */
package org.exoplatform.services.jcr.datamodel;

public class QPathEntry extends InternalQName implements Comparable<QPathEntry> {

  private final int index;

  public QPathEntry(InternalQName qName, int index) {
    super(qName.getNamespace(), qName.getName());
    this.index = index > 0 ? index : 1;
    
    // must be same as for InternalQName   
    //this.hashCode = 31 * this.hashCode + this.index;
  }

  public QPathEntry(String namespace, String name, int index) {
    super(namespace, name);
    this.index = index > 0 ? index : 1;

    // must be same as for InternalQName    
    //this.hashCode = 31 * this.hashCode + this.index;
  }

  public int getIndex() {
    return index;
  }

  public boolean isSame(QPathEntry obj) {
    if (super.equals(obj))
      return index == obj.getIndex();

    return false;
  }

  @Override
  public String getAsString() {
    return getAsString(false);
  }
  
  /**
   * @return - if showIndex=false it's a string without index
   */
  public String getAsString(boolean showIndex) {
    return super.getAsString() + (showIndex ? QPath.PREFIX_DELIMITER + this.index : "");
  }

  public int compareTo(QPathEntry compare) {
    int result = 0;

    if (this.isSame(compare))
      return result;
    result = namespace.compareTo(compare.namespace);
    if (result == 0) {
      result = name.compareTo(compare.name);
      if (result == 0)
        result = index - compare.index;
    }
    return result;
  }

  protected String asString() {
    return getAsString(true);
  }
  
}