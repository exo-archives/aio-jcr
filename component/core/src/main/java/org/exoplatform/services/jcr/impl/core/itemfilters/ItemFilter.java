package org.exoplatform.services.jcr.impl.core.itemfilters;

import javax.jcr.Item;
import javax.jcr.RepositoryException;

public interface ItemFilter {

  /**
   * Returns <code>true</code> if the specified element is to be included in the set of child
   * elements returbned by
   * 
   * @param element
   *          The item to be tested for inclusion in the returned set.
   * @return a <code>boolean</code>.
   */
  public boolean accept(Item item) throws RepositoryException;
}
