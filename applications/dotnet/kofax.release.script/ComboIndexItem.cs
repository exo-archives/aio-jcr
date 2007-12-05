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
