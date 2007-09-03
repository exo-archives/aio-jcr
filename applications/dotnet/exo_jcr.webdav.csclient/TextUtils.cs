using System;
using System.Collections.Generic;
using System.Text;
using System.Collections;

using System.Windows.Forms;

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

        public static String convert(string sourceValue, Encoding source, Encoding target)
        {
            //MessageBox.Show("SOURCE: " + sourceValue);

            Decoder decoder = source.GetDecoder();
            byte[] bytes = target.GetBytes(sourceValue);
            int length = decoder.GetCharCount(bytes, 0, bytes.Length);
            char[] chars = new char[length];
            decoder.GetChars(bytes, 0, bytes.Length, chars, 0);

            //MessageBox.Show("CONVERTED: " + new String(chars));

            return new String(chars);
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

    }
}
