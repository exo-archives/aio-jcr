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

namespace Exo.KfxReleaseScript
{
    /// <summary>
    /// The object added to the Links list item.
    /// Author: Brice Revenant
    /// </summary>
    class ListLinkItem
    {
        // The link destination
        private string destination;

        // The link source name
        private string sourceName;

        // The link source type
        private KfxLinkSourceType sourceType;

        //**********************************************************************
        // Destination accessor
        //**********************************************************************
        public string Destination
        {
            get
            {
                return this.destination;
            }

            set
            {
                this.destination = value;
            }
        }


        //**********************************************************************
        // Constructor
        //**********************************************************************
        public ListLinkItem(string sourceName,
                            KfxLinkSourceType sourceType,
                            string destination)
        {
            this.sourceName  = sourceName;
            this.sourceType  = sourceType;
            this.destination = destination;
        }

        //**********************************************************************
        // Constructor
        //**********************************************************************
        public ListLinkItem(Link link)
        {
            this.sourceName  = link.Source;
            this.sourceType  = link.SourceType;
            this.destination = link.Destination;
        }

        //**********************************************************************
        // Source name accessor
        //**********************************************************************
        public string SourceName
        {
            get
            {
                return this.sourceName;
            }

            set
            {
                this.sourceName = value;
            }
        }

        //**********************************************************************
        // Source type accessor
        //**********************************************************************
        public KfxLinkSourceType SourceType
        {
            get
            {
                return this.sourceType;
            }

            set
            {
                this.sourceType = value;
            }
        }

        //**********************************************************************
        // Returns the text to be displayed
        //**********************************************************************
        public override String ToString()
        {
            return Helper.KfxLinkSourceTypeToString(sourceType)
                + " (" + sourceName + ") -> " + destination;
        }
    }
}
