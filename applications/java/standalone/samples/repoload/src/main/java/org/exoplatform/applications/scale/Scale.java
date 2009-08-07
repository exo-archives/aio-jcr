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
package org.exoplatform.applications.scale;

import java.io.IOException;

import javax.jcr.RepositoryException;

import org.apache.commons.logging.Log;
import org.exoplatform.services.log.ExoLogger;

/**
 * Created by The eXo Platform SAS Author : Alex Reshetnyak alex.reshetnyak@exoplatform.org.ua
 * reshetnyak.alex@gmail.com 07.05.2007 15:51:14
 * 
 * @version $Id: Scale.java 07.05.2007 15:51:14 rainfox
 */
public class Scale {

  public static void main(String[] args) {

    Log log = ExoLogger.getLogger("repload.Scale");

    ScaleBase scaleBase = new ScaleBase(args);

    try {
      scaleBase.createFolder();
    } catch (RepositoryException e) {
      log.error("Error create folder", e);
    }

    try {
      scaleBase.deleteFolder();
    } catch (RepositoryException e) {
      log.error("Error delete folder", e);
    }

    try {
      scaleBase.uploadFile();
    } catch (RepositoryException e) {
      log.error("Error upload file", e);
    } catch (IOException e) {
      log.error("Error upload file", e);
    }

    try {
      scaleBase.downloadFile();
    } catch (RepositoryException e) {
      log.error("Error download file", e);
    } catch (IOException e) {
      log.error("Error download file", e);
    }
  }

}
