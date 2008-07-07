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
using System.Collections;

using System.Windows.Forms;
using System.Text.RegularExpressions;

namespace exo_jcr.webdav.csclient
{
    public class TextUtils
    {
        public static char ESCAPE_CHAR = '%';

        private static byte getDecValue(char hexValue)
        {
            if (hexValue >= '0' && hexValue <= '9')
            {
                return (byte)(hexValue - '0');
            }

            return (byte)((hexValue - 'A') + 10);
        }

        public static String unEscape(String sourceString)
        {
            String resultString = "";

            for (int i = 0; i < sourceString.Length; i++)
            {
                char curChar = sourceString[i];
                if (curChar != ESCAPE_CHAR) {
                    resultString += curChar;
                    continue;
                }

                String hexValue = "" + sourceString[i + 1] + sourceString[i + 2];
                hexValue = hexValue.ToUpper();
                char high = hexValue[0];
                char low = hexValue[1];
                char charValue = (char)((getDecValue(high) << 4) + getDecValue(low));
                resultString += charValue;
                i += 2;
            }

            return resultString;
        }

        private static String escapeMask = "0123456789abcdef";

        private static char[] enabledChars = {'-', '_', '.', '!', '~', '*', '\\', '(', ')', '/'};

        public static String convert(string sourceValue, Encoding source, Encoding target){
        
            Encoder encoder = source.GetEncoder();
            Decoder decoder = target.GetDecoder();
                    
            byte[] cpBytes = source.GetBytes(sourceValue);
            int bytesCount = source.GetByteCount(sourceValue);
            byte[] utfBytes = Encoding.Convert(source, target, cpBytes);
            char[] utfChars = new char[bytesCount];
            decoder.GetChars(utfBytes, 0, utfBytes.Length, utfChars, 0);

            return new String(utfChars);
        }



        public static String escape(String sourceString)
        {
            if (true) {
                return Uri.EscapeUriString(sourceString);
            }

            String resultString = "";

            for (int i = 0; i < sourceString.Length; i++ )
            {
                char curChar = sourceString[i];

                if ((curChar >= 'a' && curChar <= 'z') || (curChar >= 'A' && curChar <= 'Z') || (curChar >= '0' && curChar <= '9')) {
                    resultString += curChar;
                    continue;
                }

                bool finded = false;
                for (int c = 0; c < enabledChars.Length; c++ )
                {
                    if (curChar == enabledChars[c]) {
                        finded = true;
                        break;
                    }
                }

                if (finded) {
                    resultString += curChar;
                    continue;
                }

                byte high = (byte)(((byte)curChar & 0xF0) >> 4);
                byte low = (byte)((byte)curChar & 0x0F);

                resultString += ESCAPE_CHAR;
                resultString += escapeMask[high];
                resultString += escapeMask[low];
            }

            MessageBox.Show("MY METHOD: " + resultString + "\r\n" + "OWN METHOD: " + Uri.EscapeUriString(sourceString));

            return resultString;
        }

        private static String regexpCyrilicString = "^.*[à-ÿÀ-ß]{1,}.*$";

        public static Boolean sheckStringIsCyrilic(String str)
        {
            return Regex.IsMatch(str, regexpCyrilicString);
        }

    }
}
