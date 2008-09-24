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

package org.exoplatform.services.jcr.webdav.command;

import java.io.IOException;
import java.io.OutputStream;

import javax.jcr.AccessDeniedException;
import javax.jcr.Node;
import javax.jcr.PathNotFoundException;
import javax.jcr.Session;
import javax.jcr.lock.Lock;
import javax.jcr.lock.LockException;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamWriter;

import org.exoplatform.common.util.HierarchicalProperty;
import org.exoplatform.services.jcr.impl.Constants;
import org.exoplatform.services.jcr.webdav.Depth;
import org.exoplatform.services.jcr.webdav.WebDavStatus;
import org.exoplatform.services.jcr.webdav.command.lock.LockRequestEntity;
import org.exoplatform.services.jcr.webdav.lock.NullResourceLocksHolder;
import org.exoplatform.services.jcr.webdav.resource.GenericResource;
import org.exoplatform.services.jcr.webdav.xml.PropertyWriteUtil;
import org.exoplatform.services.jcr.webdav.xml.WebDavNamespaceContext;
import org.exoplatform.services.rest.Response;
import org.exoplatform.services.rest.transformer.SerializableEntity;

/**
 * Created by The eXo Platform SAS .<br/>
 * 
 * @author Gennady Azarenkov
 * @version $Id: $
 */

public class LockCommand {

  private final NullResourceLocksHolder nullResourceLocks;

  public LockCommand(final NullResourceLocksHolder nullResourceLocks) {
    this.nullResourceLocks = nullResourceLocks;
  }

  public Response lock(Session session,
                       String path,
                       HierarchicalProperty body,
                       Depth depth,
                       String timeout) {

    String lockToken;
    try {
      WebDavNamespaceContext nsContext = new WebDavNamespaceContext(session);
      try {
        Node node = (Node) session.getItem(path);

        if (!node.isNodeType("mix:lockable")) {
          if (node.canAddMixin("mix:lockable")) {
            node.addMixin("mix:lockable");
            session.save();
          }
        }

        Lock lock = node.lock((depth.getIntValue() != 1), false);
        lockToken = lock.getLockToken();
      } catch (PathNotFoundException pexc) {
        lockToken = nullResourceLocks.addLock(session, path);
      }

      LockRequestEntity requestEntity = new LockRequestEntity(body);

      return Response.Builder.ok(body(nsContext,
                                      requestEntity,
                                      depth,
                                      lockToken,
                                      requestEntity.getOwner(),
                                      timeout),
                                 "text/xml").header("Lock-Token", "<" + lockToken + ">").build();

      // TODO 412 Precondition Failed ?
    } catch (LockException exc) {
      return Response.Builder.withStatus(WebDavStatus.LOCKED).build();
    } catch (AccessDeniedException e) {
      return Response.Builder.withStatus(WebDavStatus.FORBIDDEN).build();
    } catch (Exception exc) {
      exc.printStackTrace();
      return Response.Builder.serverError().errorMessage(exc.getMessage()).build();
    }

  }

  private final SerializableEntity body(WebDavNamespaceContext nsContext,
                                        LockRequestEntity input,
                                        Depth depth,
                                        String lockToken,
                                        String lockOwner,
                                        String timeout) {
    return new LockResultResponseEntity(nsContext, lockToken, lockOwner, timeout);
  }

  public class LockResultResponseEntity implements SerializableEntity {

    protected WebDavNamespaceContext nsContext;

    protected String                 lockToken;

    protected String                 lockOwner;

    protected String                 timeOut;

    public LockResultResponseEntity(WebDavNamespaceContext nsContext,
                                    String lockToken,
                                    String lockOwner,
                                    String timeOut) {
      this.nsContext = nsContext;
      this.lockToken = lockToken;
      this.lockOwner = lockOwner;
      this.timeOut = timeOut;
    }

    public void writeObject(OutputStream stream) throws IOException {
      try {
        XMLStreamWriter xmlStreamWriter = XMLOutputFactory.newInstance()
                                                          .createXMLStreamWriter(stream,
                                                                                 Constants.DEFAULT_ENCODING);
        xmlStreamWriter.setNamespaceContext(nsContext);
        xmlStreamWriter.setDefaultNamespace("DAV:");

        xmlStreamWriter.writeStartDocument();

        xmlStreamWriter.writeStartElement("D", "prop", "DAV:");
        xmlStreamWriter.writeNamespace("D", "DAV:");

        HierarchicalProperty lockDiscovery = GenericResource.lockDiscovery(lockToken,
                                                                           lockOwner,
                                                                           timeOut);
        PropertyWriteUtil.writeProperty(xmlStreamWriter, lockDiscovery);

        xmlStreamWriter.writeEndElement();

        xmlStreamWriter.writeEndDocument();
      } catch (Exception e) {
        e.printStackTrace();
        throw new IOException(e.getMessage());
      }
    }

  }

}
