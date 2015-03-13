find_path(AMQPCPP_INCLUDE_DIR NAMES amqpcpp.h)
find_library(AMQPCPP_LIBRARY NAMES amqpcpp libamqpcpp)

set(AMQPCPP_INCLUDE_DIRS ${AMQPCPP_INCLUDE_DIR})
set(AMQPCPP_LIBRARIES ${AMQPCPP_LIBRARY})

include(FindPackageHandleStandardArgs)

find_package_handle_standard_args(AMQPCPP DEFAULT_MSG AMQPCPP_LIBRARY AMQPCPP_INCLUDE_DIR)

mark_as_advanced(AMQPCPP_INCLUDE_DIR AMQPCPP_LIBRARY)
