find_path(HDFS_INCLUDE_DIR NAMES hdfs/hdfs.h)
find_library(HDFS_LIBRARY NAMES hdfs3 libhdfs3)

set(HDFS_INCLUDE_DIRS ${HDFS_INCLUDE_DIR})
set(HDFS_LIBRARIES ${HDFS_LIBRARY})

include(FindPackageHandleStandardArgs)

find_package_handle_standard_args(HDFS DEFAULT_MSG HDFS_LIBRARY HDFS_INCLUDE_DIR)

mark_as_advanced(HDFS_INCLUDE_DIR HDFS_LIBRARY)