/*
 * Copyright 2019 DiffPlug
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.diffplug.gradle.oomph;

import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;

/** @see SaveWorkspace */
class SaveWorkspaceInternal extends SetupAction.Internal<SaveWorkspace> {
	public SaveWorkspaceInternal(SaveWorkspace host) {
		super(host);
	}

	@Override
	public void runWithinEclipse() throws CoreException {
		save();
	}

	static void save() throws CoreException {
		// save the workspace
		IWorkspace workspace = ResourcesPlugin.getWorkspace();

		boolean full = false;
		IProgressMonitor monitor = null;
		IStatus status = workspace.save(full, monitor);
		if (!status.isOK()) {
			throw new IllegalStateException(status.getMessage(), status.getException());
		}
	}
}
