/******************************************************************************
 * Copyright(C) 2001-2007 eXo Platform SAS. All rights reserved.              *
 * Please look at licensing information for more detail.                      *
 ******************************************************************************/

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
