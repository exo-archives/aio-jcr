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
package org.exoplatform.services.jcr.ext.registry.transformer;

import java.io.IOException;
import java.io.OutputStream;

import javax.xml.transform.TransformerException;

import org.exoplatform.services.jcr.ext.registry.RegistryEntry;
import org.exoplatform.services.rest.transformer.OutputEntityTransformer;
import org.exoplatform.services.rest.transformer.PassthroughOutputTransformer;

/**
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id: $
 */
public class RegistryEntryOutputTransformer extends OutputEntityTransformer {

  @Override
  public void writeTo(Object entity, OutputStream entityDataStream) throws IOException {

    RegistryEntry regEntry = (RegistryEntry) entity;
    PassthroughOutputTransformer transformer = new PassthroughOutputTransformer();
    try {
      transformer.writeTo(regEntry.getAsInputStream(), entityDataStream);
    } catch (TransformerException tre) {
      throw new IOException("Can't get RegistryEntry as stream " + tre);
    }
  }

}
