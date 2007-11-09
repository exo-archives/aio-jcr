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
