package eu.mico.platform.persistence.util;

import org.apache.commons.vfs2.FileSystemOptions;
import org.apache.commons.vfs2.provider.ftp.FtpFileSystemConfigBuilder;

/**
 * Basic VFS utilities
 *
 */
public class VFSUtils {

    public static void configure() {
        FileSystemOptions opts = new FileSystemOptions();
        //FtpFileSystemConfigBuilder.getInstance().setPassiveMode(opts, true);
        FtpFileSystemConfigBuilder.getInstance().setUserDirIsRoot(opts, true);
    }
}
