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
package org.exoplatform.services.ftp;

/**
 * Created by The eXo Platform SAS
 * Author : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
 * @version $Id: $
 */

public class FtpConst {
  
  public static final String FTP_PREFIX = "jcr.ftpservice.";
  
  public static final String FTP_COMMAND_CATALOG = "FTP";
  public static final String FTP_CACHEFILEEXTENTION = ".ftpcache";
  
  public static final int FTP_TIMESTAMPED_BLOCK_SIZE = 2048;

  public static final String []eXoLogo = {
    "220- ",
    "              _/_/_/_/  *** eXo Platform JCR FTP Server        _/_/_/_/",
    "            _/                                                      _/",
    "           _/                          _/                          _/",
    "          _/            _/_/_/_/      _/      _/_/_/              _/",
    "                       _/        _/  _/     _/     _/              ",
    "                      _/          _/_/     _/     _/              ",
    "                     _/_/_/        _/     _/     _/              ",
    "                    _/            _/_/   _/     _/              ",
    "                   _/_/_/_/      _/  _/   _/_/_/               ",
    "   _/                           _/                          _/",
    "  _/                           _/                          _/",
    " _/                                                       _/",
    "_/_/_/_/               http://eXoPlatform.org ***  _/_/_/_/",
    "220 - "
  };
  
  public static final String []eXoHelpInfo = {    
    "214-The following commands are recognized:",
    "",
    "             _/_/_/_/  *** eXo Platform JCR FTP Server               _/_/_/_/",
    "            _/                                                            _/ ",
    "           _/                 _/                  CDUP    CWD     DELE   _/  ",
    "          _/   _/_/_/_/      _/      _/_/_/      HELP    LIST    MKD    _/   ",
    "              _/        _/  _/     _/     _/    MODE    NLST    NOOP         ",
    "             _/          _/_/     _/     _/    PASS    PASV    PORT          ",
    "            _/_/_/        _/     _/     _/    PWD     QUIT    REST           ",
    "           _/            _/_/   _/     _/    RETR    RMD     RNFR            ",
    "          _/_/_/_/      _/  _/   _/_/_/     RNTO    SIZE    STAT             ",
    "   _/                  _/                  STOR    SYST    TYPE   _/         ",
    "  _/                  _/                          USER           _/          ",
    " _/                                                             _/           ",
    "_/_/_/_/                     http://eXoPlatform.org ***  _/_/_/_/            ",
    "",
    "214 http://eXoPlatForm.org"
  };
  
  public class Encoding {    
    public static final String WINDOWS_NT = "Windows_NT";
    public static final String UNIX = "UNIX";
    public static final String UNIX_L8 = "UNIX Type: L8";
  }
  
  public class Commands {
    public static final String CMD_USER =   "USER";
    public static final String CMD_PASS =   "PASS";
    public static final String CMD_TYPE =   "TYPE";
    public static final String CMD_CWD  =   "CWD";
    public static final String CMD_PWD  =   "PWD";
    public static final String CMD_QUIT =   "QUIT";
    public static final String CMD_HELP =   "HELP";
    public static final String CMD_SYST =   "SYST";
    public static final String CMD_PASV =   "PASV";
    public static final String CMD_NOOP =   "NOOP";
    public static final String CMD_LIST =   "LIST";
    public static final String CMD_CDUP =   "CDUP";
    public static final String CMD_MKD  =   "MKD";
    public static final String CMD_MODE =   "MODE";
    public static final String CMD_RMD  =   "RMD";
    public static final String CMD_RNFR =   "RNFR";
    public static final String CMD_RNTO =   "RNTO";
    public static final String CMD_STOR =   "STOR";
    public static final String CMD_RETR =   "RETR";
    public static final String CMD_DELE =   "DELE";
    public static final String CMD_REST =   "REST";
    public static final String CMD_NLST =   "NLST";
    public static final String CMD_PORT =   "PORT";
    public static final String CMD_SIZE =   "SIZE";
    public static final String CMD_STAT =   "STAT";
    public static final String CMD_STRU =   "STRU";
  }
  
  public class Replyes {    
    public static final String REPLY_125 = "125 Data connection already open; Transfer starting";

    public static final String REPLY_200 = "200 %s";
    public static final String REPLY_213 = "213 %s";
    public static final String REPLY_215 = "215 %s";
    public static final String REPLY_221 = "221 eXo JCR FTP Server. Goodbye :)";    
    public static final String REPLY_226 = "226 Transfer complete";
    public static final String REPLY_227 = "227 Entering Passive Mode (%s)";
    
    public static final String REPLY_230 = "230 %s user logged in";
    
    public static final String REPLY_250 = "250 %s command successful";
    public static final String REPLY_257 = "257 \"%s\" is current directory";
    public static final String REPLY_257_CREATED = "257 \"%s\" directory created";
    
    public static final String REPLY_331 = "331 Password required for %s";
    
    public static final String REPLY_350 = "350 File or directory exists, ready for destination name";
    public static final String REPLY_350_REST = "350 Restarting at %s. Send STORE or RETRIEVE to initiate transfer";
    
    public static final String REPLY_421 = "421 Idle Timeout (%d seconds): closing control connection";
    public static final String REPLY_421_DATA = "421 Service not available";
    
    public static final String REPLY_425 = "425 Unable to build data connection";
    public static final String REPLY_450 = "450 %s No such file or directory";
    public static final String REPLY_451 = "451 Transfer aborted";

    public static final String REPLY_500 = "500 %s not understood";
    public static final String REPLY_500_PARAMREQUIRED = "500 %s: command requires a parameter";
    public static final String REPLY_500_ILLEGAL = "500 Illegal %s command";
    
    public static final String REPLY_501_MODE = "501 '%s' unrecognized transfer mode";
    public static final String REPLY_501_STRU = "501 '%s' unrecognized structure type";
    
    public static final String REPLY_503 = "503 Bad sequence of commands";
    public static final String REPLY_503_PASS = "503 Login with USER first";
    
    public static final String REPLY_504 = "504 '%s' unsupported transfer mode";
    
    public static final String REPLY_530 = "530 Please login with USER and PASS";
    public static final String REPLY_550 = "550 %s: Permission denied";
    public static final String REPLY_550_SIZE = "550 %s: No such file";
    public static final String REPLY_550_RESTORE = "550 Restore value invalid";
    
    public static final String REPLY_553 = "553 %s: Unable to rename file or directory";
  }
  
  public class SystemTypes {
    public static final String WINDOWS_NT = "Windows_NT";
    public static final String UNIX_L8 = "UNIX Type: L8";
  }
  
  public class NodeTypes {
    public static final String NT_FOLDER = "nt:folder";
    public static final String NT_FILE = "nt:file";
    public static final String JCR_CONTENT = "jcr:content"; 
    public static final String JCR_DATA = "jcr:data";
    public static final String JCR_CREATED = "jcr:created";
    public static final String JCR_LASTMODIFIED = "jcr:lastModified";
    public static final String NT_RESOURCE = "nt:resource";
    public static final String JCR_MIMETYPE = "jcr:mimeType";
    public static final String MIX_VERSIONABLE = "mix:versionable";
  }
  
}
