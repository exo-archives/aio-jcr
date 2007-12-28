/*
 * Copyright (C) 2003-2007 eXo Platform SAS.
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
package org.exoplatform.services.jcr.rmi.api.value;

import java.io.Serializable;

import org.exoplatform.services.jcr.core.value.ExtendedValue;

/**
 * The <code>StatefullValue</code> interface defines the API used for the
 * state classes used by the
 * {@link org.exoplatform.services.jcr.rmi.api.value.SerialValue} class.
 * <p>
 * This is a marker interface with two purposes; it separates the value state
 * classes from the more general value classes, and it forces the state classes
 * to be serializable. This interface is used only internally by the State
 * pattern implementation of the
 * {@link org.exoplatform.services.jcr.rmi.api.value.SerialValue} class.
 * <p>
 * This interface is not intended to be implemented by clients. Rather any of
 * the concrete implementations of this class should be used or overwritten as
 * appropriate.
 * 
 * @see org.exoplatform.services.jcr.rmi.api.value.SerialValue
 */
public interface StatefulValue extends ExtendedValue, Serializable {
}
