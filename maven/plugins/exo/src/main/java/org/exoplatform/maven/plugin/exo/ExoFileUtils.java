/***************************************************************************
 * Copyright 2001-2003 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.maven.plugin.exo;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import org.codehaus.plexus.util.FileUtils;

/**
 * Created by The eXo Platform SARL 
 * Author : Phung Hai Nam 
 * phunghainam@gmail.com
 * Dec 6, 2005
 * Nguyen Quang Thang
 * nqthang.net@gmail.com
 * Feb 22, 2006
 * Nhu Dinh Thuan
 * nhudinhthuan@yahoo.com
 */
public class ExoFileUtils extends FileUtils {
  
  public static int NumofDir = 0;
  public static int copyDirectoryStructure(File sourceDirectory, File destinationDirectory,
                                           HashSet<Pattern> ignoreFiles) throws IOException {
    return copyDirectoryStructure(sourceDirectory, destinationDirectory, ignoreFiles, false);
  }

  public static int copyDirectoryStructure(File sourceDirectory, File destinationDirectory,
        HashSet<Pattern> ignoreFiles,boolean overwrite) throws IOException {
    if (!sourceDirectory.exists()) {
      throw new IOException("Source directory doesn't exists (" + sourceDirectory.getAbsolutePath() + ").");
    }
    int counter = 0;
    File[] files = sourceDirectory.listFiles();
    String sourcePath = sourceDirectory.getAbsolutePath();
    for (int i = 0; i < files.length; i++) {
      File file = files[i];
      String dest = file.getAbsolutePath();
      dest = dest.substring(sourcePath.length() + 1);
      File destination = new File(destinationDirectory, dest);
      if (file.isFile()) {
        if (!isIgnoredFile(ignoreFiles,file.getName())) {
          File fileInDst = new File(destination.getPath());
          if (fileInDst.exists()) {
            if (overwrite || file.lastModified() > fileInDst.lastModified()) {
              destination = destination.getParentFile();
              copyFileToDirectory(file, destination);
              counter++;
            }
          } else {
            destination = destination.getParentFile();
            copyFileToDirectory(file, destination);
            counter++;
          }
        }
      } else if (file.isDirectory()) {
        if (!isIgnoredFile(ignoreFiles,file.getName())) {
          if (!destination.exists() && !destination.mkdirs()) {
            throw new IOException("Could not create destination directory '"
                + destination.getAbsolutePath() + "'.");
          }
          counter += copyDirectoryStructure(file, destination, ignoreFiles, overwrite);
        }
      } else
        throw new IOException("Unknown file type: " + file.getAbsolutePath());
    }
    return counter;
  }
  
  public static boolean isIgnoredFile(HashSet<Pattern> set, String input) {
    for(Pattern p : set) {
      Matcher m = p.matcher(input) ;
      if(m.matches()) return true;
    }  
    return false ;
  }
  
  public static boolean containFile(File parentDir, String childName) {
    String[] childFiles = parentDir.list();
    for (int i = 0; i < childFiles.length; i++) {
      if (childFiles[i].equals(childName))
        return true;
    }
    return false;
  }
  
  public static void deleteContentInDirectory(File dir) throws Exception {
    File[] files = dir.listFiles();
    for(int i=0; i<files.length; i++) {
      if (files[i].isFile()) {files[i].delete();}
      else if (files[i].isDirectory()) deleteDirectory(files[i]);
    }
  }
  public static void addToArchive(File input,File output) throws Exception {
    HashSet<Pattern> ignoredFiles = new HashSet<Pattern>();
    addToArchive(input, output, ignoredFiles);
  }
  
  /*
   * Extract the content of a Zip file into the specified directory
   */
  public static void extractZip(String zipFile, String destDir)
    throws Exception {
      ZipFile zip = new java.util.jar.JarFile(zipFile);
      
      for(Enumeration entries = zip.entries() ; entries.hasMoreElements() ; ) {
        // Get the current entry, its destination on
        // the file system and the parent directory.
        ZipEntry entry = (ZipEntry) entries.nextElement();
        File dest = new java.io.File(destDir +
                                     File.separator +
                                     entry.getName());

        if (entry.isDirectory()) {
          // The entry is a directory. Create it.
          dest.mkdirs();
        }
        else {
          // The entry is a file. Ensure the parent directory exists. Indeed, in
          // some cases, file entries may be obtained before the directories
          // they are located in.
          File parent = dest.getParentFile();
          if(! parent.exists()) {
            parent.mkdirs();
          }
          
          // Persist the entry
          InputStream is = zip.getInputStream(entry);
          FileOutputStream fos = new java.io.FileOutputStream(dest);
          BufferedOutputStream bos = new BufferedOutputStream(fos);
          byte[] buffer = new byte[4096];
          int count;
          while ((count = is.read(buffer)) >= 0) {
            bos.write(buffer, 0, count);
          }
          
          // Close the various streams
          bos.close();
          is.close();
        }
      }
      zip.close();
  }
  
  public static void addToArchive(File input, File output,HashSet<Pattern> ignoredFiles) throws Exception {
    String path = input.getAbsolutePath();
    ZipOutputStream out = new ZipOutputStream(
    new BufferedOutputStream(new FileOutputStream(output)));
    final int BUFFER = 2048;
    byte data[] = new byte[BUFFER];
    BufferedInputStream origin = null;
    NumofDir = 0;
    System.out.print("[");
    List<File> files = getFiles(input,ignoredFiles);
    int NumofFile = 0;
    for (Iterator<File> iter = files.iterator(); iter.hasNext();) {
      File f = iter.next();
      String filePath = f.getAbsolutePath();
      if (filePath.startsWith(path))
        filePath = filePath.substring(path.length()-input.getName().length());
      filePath = filePath.replace("\\","/");
      if( ! (f.isDirectory())){
        FileInputStream fi = new FileInputStream(f);
        origin = new BufferedInputStream(fi, BUFFER);
      }else {   
        filePath +="/";
      }
      ZipEntry entry = new ZipEntry(filePath);      
      out.putNextEntry(entry);      
      if(f.isFile()){
        NumofFile++;
        int count;
        while ((count = origin.read(data, 0, BUFFER)) != -1)
          out.write(data, 0, count);
        origin.close();
      }
      out.closeEntry(); 
    }
    out.close();
    System.out.println("=>]");
    System.out.println("zipped "+NumofDir+" directories include "+NumofFile+" files");

  }
  private static List<File> getFiles(File dir,final HashSet<Pattern> ignoredFiles) {
    final List<File> files = new LinkedList<File>();
    dir.listFiles(new FileFilter() {
      public boolean accept(File f) {
        if (f.isDirectory()){return false;}
        files.add(f);
        return true;
      }
    });
    dir.listFiles(new FileFilter() {
      public boolean accept(File f) {
        if (f.isFile()) return false;
        if (f.isDirectory() && !isIgnoredFile(ignoredFiles,f.getName())){
          files.addAll(getFiles(f,ignoredFiles));
          NumofDir++;
          System.out.print("=");
        }
        return false;
      }
    });
    if(files.size() == 0){
      files.add(dir);
    }
    return files;
  }
}
