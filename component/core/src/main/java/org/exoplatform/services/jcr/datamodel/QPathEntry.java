/**
 * 
 */
package org.exoplatform.services.jcr.datamodel;

public class QPathEntry extends InternalQName {

    private final int index;

    public QPathEntry(String namespace, String name, int index) {
      super(namespace, name);
      this.index = index > 0 ? index : 1;
    }

    public int getIndex() {
      return index;
    }
  }