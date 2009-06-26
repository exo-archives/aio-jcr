/**
 * Copyright (C) 2003-2008 eXo Platform SAS.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
 */

package org.exoplatform.services.jcr.ext.script.groovy;

/**
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id$
 */
// Need this for back compatibility, see @deprecated methods in
// GroovyScript2RestLoader.
public class SimpleScriptKey implements ScriptKey {

  protected final String key;

  public SimpleScriptKey(String key) {
    this.key = key;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean equals(Object obj) {
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    return key.equals(((SimpleScriptKey) obj).key);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public int hashCode() {
    return key.hashCode();
  }

  /**
   * {@inheritDoc}
   */
  public String toString() {
    return key;
  }

}
