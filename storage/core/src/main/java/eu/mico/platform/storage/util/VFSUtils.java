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

package eu.mico.platform.storage.util;

import org.apache.commons.vfs2.FileSystemOptions;
import org.apache.commons.vfs2.provider.ftp.FtpFileSystemConfigBuilder;

/**
 * Basic VFS utilities
 *
 * @author Sergio Fernández
 */
public class VFSUtils {

    /**
     * Default VFS configuration
     *
     * @return file system options
     */
    public static FileSystemOptions configure() {
        FileSystemOptions opts = new FileSystemOptions();
        final FtpFileSystemConfigBuilder configBuilder = FtpFileSystemConfigBuilder.getInstance();

        configBuilder.setPassiveMode(opts, true);
        configBuilder.setUserDirIsRoot(opts, true);

        return opts;
    }

}
