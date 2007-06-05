/*
 * Copyright (C) 2005 Alfresco, Inc.
 *
 * Licensed under the Mozilla Public License version 1.1 
 * with a permitted attribution clause. You may obtain a
 * copy of the License at
 *
 *   http://www.alfresco.org/legal/license.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.exoplatform.services.cifs.smb;

/**
 * Find First/Next Flags
 */
public class FindFirstNext {
	// Find first/find next flags

	public static final int CloseSearch = 0x01;

	public static final int CloseAtEnd = 0x02;

	public static final int ReturnResumeKey = 0x04;

	public static final int ResumePrevious = 0x08;

	public static final int BackupIntent = 0x10;
}
