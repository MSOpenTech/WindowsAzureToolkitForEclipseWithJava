/**
* Copyright 2015 Microsoft Open Technologies, Inc.
*
* Licensed under the Apache License, Version 2.0 (the "License");
*  you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*	 http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
*  distributed under the License is distributed on an "AS IS" BASIS,
*  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
*  See the License for the specific language governing permissions and
*  limitations under the License.
*/
package com.gigaspaces.azure.util;

import java.io.File;
import java.util.Collection;
import java.util.List;

import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import com.gigaspaces.azure.propertypage.Messages;
import com.gigaspaces.azure.runnable.CacheAccountWithProgressWindow;
import com.gigaspaces.azure.runnable.LoadAccountWithProgressWindow;
import com.gigaspaces.azure.wizards.WizardCacheManager;
import com.microsoftopentechnologies.azurecommons.deploy.util.PublishData;
import com.microsoftopentechnologies.azurecommons.storageregistry.StorageAccount;
import com.microsoftopentechnologies.azurecommons.storageregistry.StorageAccountRegistry;
import com.microsoftopentechnologies.wacommon.storageregistry.PreferenceUtilStrg;
/**
 * Class has common methods which
 * handles publish settings file and extract data.
 * Methods get called whenever user clicks
 * "Import from publish settings file..." button
 * on publish wizard or preference page dialog.
 */
public class MethodUtils {
	/**
	 * Method checks file selected by user is valid
	 * and call method which extracts data from it.
	 * @param fileName
	 */
	public static void handleFile(String fileName, TableViewer tableViewer) {
		if (fileName != null && !fileName.isEmpty()) {
			File file = new File(fileName);
			PublishData publishDataToCache = null;
			//TODO: Check if this is publish settings
			publishDataToCache = handlePublishSettings(file);
			

			if (publishDataToCache == null) {
				return;
			}
			WizardCacheManager.
			setCurrentPublishData(publishDataToCache);
			// Make centralized storage registry.
			prepareListFromPublishData();
			tableViewer.refresh();
		}
	}

	/**
	 * Method extracts data from publish settings file.
	 * @param file
	 * @return
	 */
	public static PublishData handlePublishSettings(File file) {
		PublishData data = UIUtils.createPublishDataObj(file);
		/*
		 * If data is equal to null,
		 * then publish settings file already exists.
		 * So don't load information again.
		 */
		if (data != null) {
			Display.getDefault().syncExec(new
					CacheAccountWithProgressWindow(file, data, new Shell(),
							Messages.loadingCred));
			PreferenceUtil.save();
		}
		return data;
	}

	/**
	 * Method prepares storage account list.
	 * Adds data from publish settings file.
	 */
	public static void prepareListFromPublishData() {
		List<StorageAccount> strgList = StorageAccountRegistry.getStrgList();
		Collection<PublishData> publishDatas = WizardCacheManager.getPublishDatas();
		strgList = com.microsoftopentechnologies.azurecommons.deploy.util.MethodUtils.
				prepareListFromPublishData(strgList, publishDatas);
		PreferenceUtilStrg.save();
	}

	/**
	 * When we start new eclipse session,
	 * reload the subscription and storage account
	 * registry information just for once.
	 * @param tableViewer
	 */
	public static void loadSubInfoFirstTime(TableViewer tableViewer) {
		if (!PreferenceUtil.isLoaded()) {
			Display.getDefault().syncExec(new
					LoadAccountWithProgressWindow(null, new Shell()));
			PreferenceUtilStrg.load();
			prepareListFromPublishData();
		}
		PreferenceUtil.setLoaded(true);
		tableViewer.refresh();
	}
}
