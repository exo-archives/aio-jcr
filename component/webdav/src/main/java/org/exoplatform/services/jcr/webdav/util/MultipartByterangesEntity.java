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

package org.exoplatform.services.jcr.webdav.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import org.exoplatform.services.jcr.webdav.Range;
import org.exoplatform.services.jcr.webdav.WebDavConst;
import org.exoplatform.services.jcr.webdav.WebDavHeaders;
import org.exoplatform.services.jcr.webdav.resource.FileResource;
import org.exoplatform.services.jcr.webdav.resource.Resource;
import org.exoplatform.services.jcr.webdav.resource.VersionResource;
import org.exoplatform.services.rest.transformer.SerializableEntity;

/**
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id: $
 */
public class MultipartByterangesEntity implements SerializableEntity {

  private final Resource resource_;
  private final List<Range> ranges_;
  private final long contentLength_;
  private final String contentType_;
  
  public MultipartByterangesEntity(Resource resource, List<Range> ranges,
      String contentType, long contentLength) {
    resource_ = resource;
    ranges_ = ranges;
    contentLength_ = contentLength;
    contentType_ = contentType;
  }
  
  /* (non-Javadoc)
   * @see org.exoplatform.services.rest.transformer.SerializableEntity#writeObject(java.io.OutputStream)
   */
  public void writeObject(OutputStream ostream) throws IOException {
    try {
    for (Range range : ranges_) {
      InputStream istream = null;
      if (resource_ instanceof VersionResource) 
        istream = ((VersionResource) resource_).getContentAsStream();
      else 
        istream = ((FileResource) resource_).getContentAsStream();

      println(ostream);
      // boundary
      print("--" + WebDavConst.BOUNDARY, ostream);
      println(ostream);
      // content-type
      print(WebDavHeaders.CONTENTTYPE + ": " + contentType_, ostream);
      println(ostream);
      // current range
      print(WebDavHeaders.CONTENTRANGE + ": bytes " + range.getStart()
          + "-" + range.getEnd() + "/" + contentLength_, ostream);
      println(ostream);
      println(ostream);
      // range data
      RangedInputStream rangedInputStream =
        new RangedInputStream(istream, range.getStart(), range.getEnd());
      
      byte buff[] = new byte[0x1000];
      int rd = -1;
      while ((rd = rangedInputStream.read(buff)) != -1)
        ostream.write(buff, 0, rd);
      rangedInputStream.close();
    }
    println(ostream);
    print("--" + WebDavConst.BOUNDARY + "--", ostream);
    println(ostream);
    } catch (Exception e) {
      e.printStackTrace();
      throw new IOException("Can't write to stream, caused " + e);
    }
  }
  
  private void print(String s, OutputStream ostream) throws IOException {
    int length = s.length();
    for (int i = 0; i < length; i++) {
      char c = s.charAt(i);
      ostream.write(c);
    }
  }

  private void println(OutputStream ostream) throws IOException {
    ostream.write('\r');
    ostream.write('\n');
  }

}
