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
    /// The object added to the Index list item.
    /// Author: Brice Revenant
    /// </summary>
    class ComboIndexItem
    {
        // The index name
        private string name;

        // The index type
        private KfxLinkSourceType type;

        //**********************************************************************
        // Constructor
        //**********************************************************************
        public ComboIndexItem(string name, KfxLinkSourceType type)
        {
            this.name = name;
            this.type = type;
        }

        //**********************************************************************
        // Name accessor
        //**********************************************************************
        public string Name
        {
            get
            {
                return this.name;
            }

            set
            {
                this.name = value;
            }
        }

        //**********************************************************************
        // Returns the text to be displayed
        //**********************************************************************
        public override String ToString()
        {
            return Helper.KfxLinkSourceTypeToString(this.type)
                + " (" + name + ")";

        }

        //**********************************************************************
        // Type accessor
        //**********************************************************************
        public KfxLinkSourceType Type
        {
            get
            {
                return this.type;
            }

            set
            {
                this.type = value;
            }
        }
    }
}
