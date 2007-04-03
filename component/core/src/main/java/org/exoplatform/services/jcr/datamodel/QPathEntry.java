/**
 * 
 */
package org.exoplatform.services.jcr.datamodel;

public class QPathEntry extends InternalQName {

    private final int index;
    
    public QPathEntry(InternalQName qName, int index){
      super(qName.getNamespace(), qName.getName());
      this.index = index > 0 ? index : 1;
    } 
    
    public QPathEntry(String namespace, String name, int index) {
      super(namespace, name);
      this.index = index > 0 ? index : 1;
    }

    public int getIndex() {
      return index;
    }
    
}