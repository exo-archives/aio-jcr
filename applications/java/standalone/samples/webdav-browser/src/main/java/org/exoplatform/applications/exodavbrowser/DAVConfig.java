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
package org.exoplatform.applications.exodavbrowser;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.StringTokenizer;
import java.util.Vector;

/**
 * Created by The eXo Platform SAS
 * Author : Alex Reshetnyak
 *          alex_reshetnyak@yahoo.com
 * ${date}  
 */

public class DAVConfig{
 public Vector ReadConfig(String token, boolean multiline ){
   
   Vector info = new Vector();
   info.add("http://127.0.0.1:8080/webdav/repository");
   String userPath = System.getProperty( "user.home" );
   if (userPath == null)
       userPath = "";
   else
       userPath += File.separatorChar;
   String filePath = null;
   File theFile = new File(userPath + DAVConst.Config.sConfigFileName);
   if (theFile.exists())
       filePath = userPath + DAVConst.Config.sConfigFileName;
   if (filePath != null)
   {
       try
       {
           FileInputStream fin = new FileInputStream(filePath);
           BufferedReader in = new BufferedReader(new InputStreamReader(fin));
           boolean found = false;
           do
           {
               String line = in.readLine();
               if( line == null )
                   break;
               StringTokenizer filetokens = new StringTokenizer( line, "=" );
               if( (filetokens.nextToken()).equals(token) )
               {
                   String data = filetokens.nextToken(); 
                   info.addElement( data );
                   found = true;
               }
           }
           while( multiline || !found );
           in.close();
       }
       catch (Exception fileEx){}
   }
   return info;
 }
 
 public String ReadConfig(String token){
   String s = "";
   
   Vector<String> v = ReadConfig(token, false);
   
   if (v.size() > 0)
     return v.get(0);
   else
     return "";
 }
 
 public void WriteConfigEntry( String token, Vector data, boolean overwrite )
 {
     if( (data == null) || (data.size() == 0) )
         return;
     // this has the side effect of removing all old token entries
     WriteConfigEntry( token, (String)data.elementAt(0), overwrite );
     for( int i=1; i<data.size(); i++ )
     {
         // it doesn't make sense here to overwrite entries
         WriteConfigEntry( token, (String)data.elementAt(i), false );
     }
 }
 
 public void WriteConfigEntry( String token, String data, boolean overwrite )
 {
     String userPath = System.getProperty( "user.home" );
     if (userPath == null)
         userPath = "";
     else
         userPath += File.separatorChar;
     String filePath = userPath + DAVConst.Config.sConfigFileName;
     String tmpFilePath = userPath + DAVConst.Config.sTampFileName;
     try
     {
         FileOutputStream fout = new FileOutputStream( tmpFilePath );
         BufferedWriter out = new BufferedWriter(new OutputStreamWriter(fout));
         File theFile = new File(filePath);
         if ( theFile.exists() )
         {
             FileInputStream fin = new FileInputStream(filePath);
             BufferedReader in = new BufferedReader(new InputStreamReader(fin));
             String line = null;
             do
             {
                 line = in.readLine();
                 if( line != null )
                 {
                     StringTokenizer filetokens = new StringTokenizer( line, "=" );
                     if( !overwrite || !(filetokens.nextToken()).equals(token) )
                     {
                         // copy line to new file
                         out.write( line );
                         out.newLine();
                     }
                 }
             }
             while( line != null );
             in.close();
         }
         out.write( token );
         out.write( "=" );
         out.write( data );
         out.newLine();
         out.close();

         if( theFile.exists() )
             theFile.delete();
         File theNewFile = new File( tmpFilePath );
         theNewFile.renameTo( theFile );
     }
     catch (Exception fileEx)
     {
         System.out.println( fileEx.toString() );
     }
 }



 
 
 
}