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
using System.Net;
using System.Net.Sockets;
using System.IO;
using System.Xml;
using System.Collections;
using System.Threading;
using System.Windows.Forms;
using exo_jcr.webdav.csclient.Request;

/**
 * Created by The eXo Platform SARL
 * Authors : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
 *         : Max Shaposhnik <uy7c@yahoo.com>
 * @version $Id:
 */

namespace exo_jcr.webdav.csclient.Commands
{
    
    public abstract class WebDavCommand {

        protected DavContext context;

        private string commandName = "GET";

        private String resourcePath = "/";
        protected bool isNeedXmlRequest = false;

        private Hashtable requestHeaders = new Hashtable();
        Hashtable responseHeaders = new Hashtable();

        protected byte[] requestBytes = null;
        protected byte[] responseBytes = null;
        private int status = 100;

        public WebDavCommand(DavContext context) {
            this.context = context;
            addRequestHeader(HttpHeaders.HOST, context.Host + ":" + context.Port);
            addRequestHeader(HttpHeaders.USERAGENT, "eXo Platform WebDav Client 1.0 alpha.");
        }

        public string CommandName {
            get {
                return commandName;
            }
            set {
                commandName = value;
            }
        }

        private void addResponseHeader(String headerName, String headerValue)
        {
            responseHeaders.Add(headerName, headerValue);
        }

        public String getResponseHeader(String headerName)
        {
            foreach (DictionaryEntry key in responseHeaders)
            {
                if (key.Key.Equals(headerName)) return (String)key.Value;
            }
            return null;
        }


        public void addRequestHeader(String headerName, String headerValue) {
            if (requestHeaders.Contains(headerName)) {
                requestHeaders.Remove(headerName);
            }

            requestHeaders.Add(headerName, headerValue);
        }

        public void setResourcePath(String resourcePath)
        {
            this.resourcePath = resourcePath;
        }

        public String getRequestHeader(String headerName) {
            return "";
        }

        public int getStatus()
        {
            return status;
        }
        

        public byte[] getResponseBody() {
            return responseBytes;
        }

        public byte[] getBytes(String value) {
            char[] data1 = value.ToCharArray();
            byte[] data2 = new byte[data1.Length];
            for (int i = 0; i < data1.Length; i++) {
                data2[i] = (byte)data1[i];
            }
            return data2;
        }

        public virtual byte[] generateXmlRequest() {
            return null;
        }

        public virtual void finalizeExecuting()
        {
        }

        public static string ReadLine(NetworkStream stream, int maxLen, int idleTimeOut) {
            long lastDataTime = DateTime.Now.Ticks;
            ArrayList lineBuf = new ArrayList();
            byte prevByte = 0;
            while (true) {
                if (stream.DataAvailable) {
                    byte[] currByte = new byte[1];
                    int countRecieved = stream.Read(currByte, 0, 1);
                    if (countRecieved == 1) {
                        lineBuf.Add(currByte[0]);
                        if ((prevByte == (byte)'\r' && currByte[0] == (byte)'\n')) {
                            byte[] retVal = new byte[lineBuf.Count - 2];
                            lineBuf.CopyTo(0, retVal, 0, lineBuf.Count - 2);
                            return System.Text.Encoding.Default.GetString(retVal).Trim();
                        }
                        prevByte = currByte[0];
                        if (lineBuf.Count > maxLen) {
                            throw new Exception("Maximum line length exceeded");
                        }
                        lastDataTime = DateTime.Now.Ticks;
                    }
                }
                else
                {
                    if (DateTime.Now.Ticks > lastDataTime + ((long)(idleTimeOut)) * 10000)
                    {
                        throw new Exception("Read timeout");
                    }
                    System.Threading.Thread.Sleep(100);
                }
            }
        }

        public virtual int execute() {
            TcpClient comm = new TcpClient();
            comm.Connect(context.Host, context.Port);

            NetworkStream stream = comm.GetStream();

            byte[] request = requestBytes;
            if (isNeedXmlRequest) {
                request = generateXmlRequest();
            }
            if (request == null) {
                request = new byte[0];
            }

            if (!context.User.Equals(""))
            {
                String t_usr = context.User;
                String t_pass = context.Pass;
                String t_decode = t_usr + ":" + t_pass;
                byte[] t_bytes = getBytes(t_decode);

                String t_decoded = System.Convert.ToBase64String(t_bytes);
                addRequestHeader(HttpHeaders.AUTHORIZATION, "Basic " + t_decoded);                
            }

            String main = "";

            String convertedPath = TextUtils.convert(resourcePath, Encoding.Default, Encoding.UTF8);

            resourcePath = TextUtils.escape(convertedPath);

            main += commandName + " " + context.ServletPath + resourcePath + " HTTP/1.1\r\n";

            addRequestHeader(HttpHeaders.CONTENTLENGTH, request.Length.ToString());

            foreach (DictionaryEntry de in requestHeaders)
            {
                main += de.Key + ": " + de.Value + "\r\n";
            }

            main += "\r\n";

            byte[] mainBytes = getBytes(main);

            stream.Write(mainBytes, 0, mainBytes.Length);
            stream.Write(request, 0, request.Length);

            String reply = ReadLine(stream, 1024, 60000);

            reply = reply.Substring(reply.IndexOf(" ")).Trim();
            status = Convert.ToInt32(reply.Substring(0, reply.IndexOf(" ")));            

            while (true) {
                String one = ReadLine(stream, 1024, 60000);                
                if (one.Equals("")) break;
                String[]  arr = one.Split(": ".ToCharArray());
                addResponseHeader(arr[0], arr[2]);                
            }

            if (commandName != DavCommands.HEAD) {
                try
                {
                    String transferEncoding = getResponseHeader(HttpHeaders.TRANSFER_ENCODING);
                    if (transferEncoding == "chunked")
                    {
                        MemoryStream outStream = new MemoryStream();

                        while (true) {
                            String nextLengthValue = ReadLine(stream, 100, 60000);

                            uint needsToRead = System.UInt32.Parse(nextLengthValue, System.Globalization.NumberStyles.AllowHexSpecifier);

                            if (needsToRead == 0)
                            {
                                responseBytes = outStream.ToArray();
                                break;
                            }

                            byte[] buffer = new byte[needsToRead];

                            while (true)
                            {
                                int readed = stream.Read(buffer, 0, buffer.Length);

                                outStream.Write(buffer, 0, readed);
                                if (readed == needsToRead)
                                {
                                    break;
                                }
                                if (readed < 0) {
                                    Thread.Sleep(100);
                                    continue;
                                }

                                needsToRead -= (uint)readed;
                                buffer = new byte[needsToRead];
                            }

                            String zeroLine = ReadLine(stream, 100, 60000);
                        }
                    }
                    else
                    {
                        String responseHeader = getResponseHeader(HttpHeaders.CONTENTLENGTH);
                        int contentLenght = Convert.ToInt32(getResponseHeader(HttpHeaders.CONTENTLENGTH));

                        responseBytes = new byte[contentLenght];
                        int readed = 0;
                        while (readed < contentLenght)
                        {
                            int curreaded = stream.Read(responseBytes, readed, contentLenght - readed);
                            if (curreaded <= 0) break;
                            readed = readed + curreaded;
                            if (readed == contentLenght)
                            {
                                break;
                            }
                            Thread.Sleep(100);
                        }
                    }
                }
                catch (Exception exc)
                {
                    MessageBox.Show("!!!!!!!!! " + exc.Message + " - " + exc.StackTrace);
                }
            }
            
            finalizeExecuting();
            return status;
        }

    }

}
