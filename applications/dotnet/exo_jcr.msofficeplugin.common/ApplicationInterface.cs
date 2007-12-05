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

using System;
using System.Collections.Generic;
using System.Text;

using exo_jcr.webdav.csclient.Request;

namespace exo_jcr.msofficeplugin.common
{
    public interface ApplicationInterface
    {
        DavContext getContext();

        String getCacheFolder();

        String getWorkspaceName();

        void setFileNameForOpen(String fileName);

        void needsCompare(Boolean isNeedsCompare);

        String getActiveDocumentName();

        String getActiveDocumentFullName();

        void saveDocumentWithFormat(String path, String contentType);

    }
}
