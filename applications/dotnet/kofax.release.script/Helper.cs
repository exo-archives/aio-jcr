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

using Kofax.ReleaseLib;
using System;
using System.Collections.Generic;
using System.Text;
using System.Windows.Forms;

namespace Exo.KfxReleaseScript
{
    /// <summary>
    /// Contains utility methods and constants
    /// Author: Brice Revenant
    /// </summary>
    class Helper
    {
        public static readonly string CUSTOM_PROP_DESTINATION_PATH =
            "CUSTOM_PROP_DESTINATION_PATH";
        public static readonly string CUSTOM_PROP_DESTINATION_TYPE =
            "CUSTOM_PROP_DESTINATION_TYPE";
        public static readonly string DOCUMENT_NAME_DESTINATION =
            "^document_name^";

        //**********************************************************************
        // Retrieves the value of a custom property
        //**********************************************************************
        public static string GetCustomProperty(CustomProperties properties,
                                               String name)
        {
            try
            {
                // Try to retrieve the Property
                object oName = (object)name;
                return properties.get_Item(ref oName).Value;
            }
            catch
            {
                // An exception is thrown if the Property is not found
                return null;
            }
        }

        //**********************************************************************
        // Converts a KfxLinkSourceType enum element to a human readable string.
        //**********************************************************************
        public static string KfxLinkSourceTypeToString(KfxLinkSourceType type)
        {
            switch (type)
            {
                case KfxLinkSourceType.KFX_REL_INDEXFIELD:
                    return "index_field";

                case KfxLinkSourceType.KFX_REL_BATCHFIELD:
                    return "batch_field";
                
                default:
                    return "unknown";
            }
        }

        //**********************************************************************
        // Sets the value of a custom property
        //**********************************************************************
        public static void SetCustomProperty(CustomProperties properties,
                                             String name,
                                             String value)
        {
            object oName = (object)name;
            try
            {
                // Try to retrieve and set the Property
                properties.get_Item(ref oName).Value = value;
            }
            catch
            {
                // An exception is found if the Property is not found
                properties.Add(name, value);
            }
        }

        //**********************************************************************
        // Converts a WebDAV status to a human readable string
        //**********************************************************************
        public static string WebDAVStatusToString(int status)
        {
            switch (status)
            {
                case 100:
                    return "CONTINUE";
                case 101:
                    return "SWITCHING_PROTOCOLS";
                case 200:
                    return "OK";
                case 201:
                    return "CREATED";
                case 202:
                    return "ACCEPTED";
                case 203:
                    return "NON_AUTHORITATIVE_INFORMATION";
                case 204:
                    return "NO_CONTENT";
                case 205:
                    return "RESET_CONTENT";
                case 206:
                    return "PARTIAL_CONTENT";
                case 207:
                    return "MULTISTATUS";
                case 300:
                    return "MULTIPLE_CHOICES";
                case 301:
                    return "MOVED_PERMANENTLY";
                case 302:
                    return "FOUND";
                case 303:
                    return "SEE_OTHER";
                case 304:
                    return "NOT_MODIFIED";
                case 305:
                    return "USE_PROXY";
                case 307:
                    return "TEMPORARY_REDIRECT";
                case 400:
                    return "BAD_REQUEST";
                case 401:
                    return "UNAUTHORIZED";
                case 402:
                    return "PAYMENT_REQUIRED";
                case 403:
                    return "FORBIDDEN";
                case 404:
                    return "NOT_FOUND";
                case 405:
                    return "METHOD_NOT_ALLOWED";
                case 406:
                    return "NOT_ACCEPTABLE";
                case 407:
                    return "PROXY_AUTHENTICATION_REQUIRED";
                case 408:
                    return "REQUEST_TIMEOUT";
                case 409:
                    return "REQUEST_TIMEOUT";
                case 410:
                    return "GONE";
                case 411:
                    return "LENGTH_REQUIRED";
                case 412:
                    return "PRECONDITION_FAILED";
                case 413:
                    return "REQUEST_ENTITY_TOO_LARGE";
                case 414:
                    return "REQUEST_URI_TOO_LONG";
                case 415:
                    return "UNSUPPORTED_MEDIA_TYPE";
                case 416:
                    return "REQUESTED_RANGE_NOT_SATISFIABLE";
                case 417:
                    return "EXPECTATION_FAILED";
                case 500:
                    return "INTERNAL_SERVER_ERROR";
                case 501:
                    return "NOT_IMPLEMENTED";
                case 502:
                    return "BAD_GATEWAY";
                case 503:
                    return "SERVICE_UNAVAILABLE";
                case 504:
                    return "GATEWAY_TIMEOUT";
                case 505:
                    return "HTTP_VERSION_NOT_SUPPORTED";
                default:
                    return "UNKNOWN";
            }
        }
    }
}
