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

        //configBuilder.setPassiveMode(opts, true);
        configBuilder.setUserDirIsRoot(opts, true);
        configBuilder.setUserDirIsRoot(opts, true);

        return opts;
    }

}
