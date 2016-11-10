/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package eu.mico.platform.storage.impl;

import eu.mico.platform.storage.api.StorageService;
import eu.mico.platform.storage.util.VFSUtils;

import java.net.URI;

/**
 * Builds proper StorageService depending on the URL.
 *
 * @author Horst Stadler
 */
public class StorageServiceBuilder {

    public static StorageService buildStorageService(URI url) {
        String userInfo = url.getUserInfo();
        String username = null;
        String password = null;
        if (userInfo != null) {
            username = userInfo.split(":", 2)[0];
            if (userInfo.contains(":")) {
                password = userInfo.split(":", 2)[1];
            }
        }

        switch(url.getScheme().toLowerCase()) {
            case "ftp":
                VFSUtils.configure();
                return new StorageServiceFTP(url.getHost(), url.getPort(), url.getPath(), username, password);
            case "hdfs":
                return new StorageServiceHDFS(url.getHost(), url.getPort(), url.getPath());
            case "file":
                return new StorageServiceLocalFS(url);
        }

        return null;
    }
}
