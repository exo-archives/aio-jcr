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
package org.exoplatform.services.jcr.ext.backup.server.bean.response;

import java.util.Calendar;

import org.exoplatform.services.jcr.config.WorkspaceEntry;
import org.exoplatform.services.jcr.ext.backup.BackupChainLog;

/**
 * Created by The eXo Platform SAS .
 * 
 * @author <a href="mailto:gavrikvetal@gmail.com">Vitaliy Gulyy</a>
 * @version $
 */

public class DetailedInfoEx extends DetailedInfo {
	
	protected WorkspaceEntry workspaceEntry;
	
	protected String failMessage;

	public DetailedInfoEx(int type, 
      BackupChainLog chainLog,
      Calendar startedTime,
      Calendar finishedTime,
      int state,
      String repositroryName,
      String workspaceName,
      WorkspaceEntry workspaceEntry,
      String failMessage) {
		super(type, chainLog, startedTime, finishedTime, state, repositroryName, workspaceName);

		this.workspaceEntry = workspaceEntry;
		this.failMessage = failMessage;
	}

	public String getFailMessage() {
		return failMessage;
	}

	public void setFailMessage(String failMessage) {
		this.failMessage = failMessage;
	}

	public WorkspaceEntry getWorkspaceEntry() {
		return workspaceEntry;
	}
	
	public void setWorkspaceEntry(WorkspaceEntry workspaceEntry) {
		this.workspaceEntry = workspaceEntry;
	}

}
