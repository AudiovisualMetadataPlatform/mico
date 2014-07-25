AC_INIT(mico-platform,1.0,sschaffert@apache.org)
AM_INIT_AUTOMAKE([subdir-objects])
AC_CONFIG_MACRO_DIR(m4)
AC_CONFIG_HEADERS([config.h])


LT_INIT
AC_PROG_LIBTOOL
AM_PROG_LIBTOOL

AC_PROG_CC_C99
AC_PROG_CXX

DX_INIT_DOXYGEN([Mico Platform], Doxyfile, doc)
DX_DOXYGEN_FEATURE(ON)
DX_DOT_FEATURE(ON)
DX_HTML_FEATURE(ON)
DX_PDF_FEATURE(ON)

AC_PATH_PROG([XXD],[xxd])

# C system headers and libraries
AC_CHECK_HEADERS([stdio.h stdarg.h time.h])

AC_CHECK_LIB(m,log, , AC_MSG_ERROR("required math library is missing"))
AC_CHECK_HEADER(math.h, , AC_MSG_ERROR("required math headers are missing"))

AC_CHECK_LIB(pthread,pthread_create, , [AC_CHECK_LIB(c_r,pthread_create, , AC_MSG_ERROR("threading not supported on platform"))])
AC_CHECK_HEADER(pthread.h, , AC_MSG_ERROR("required pthread headers are missing"))
AC_DEFINE(USE_THREADS, 1, [use threads])
CFLAGS="$CFLAGS -pthread"
CXXFLAGS="$CXXFLAGS -pthread"


AC_CHECK_LIB(expat,XML_ParserCreate, xmllibs="-lexpat", AC_MSG_ERROR("required expat XML library is missing"))
AC_CHECK_HEADER(expat.h, , AC_MSG_ERROR("required expat XML headers are missing"))
AC_SUBST(xmllibs)

# check for libcurl, we use it to access the Marmotta webservice API
LIBCURL_CHECK_CONFIG(yes, ,  curllibs="-lcurl", AC_MSG_ERROR("required cURL HTTP library is missing"))
AC_SUBST(curllibs)


# c++ headers and libraries
AC_LANG_PUSH([C++])

# check for C++11
AX_CHECK_COMPILE_FLAG([-std=c++11], [CXXFLAGS="$CXXFLAGS -std=c++11"],
  AX_CHECK_COMPILE_FLAG([-std=c++0x], [CXXFLAGS="$CXXFLAGS -std=c++0x"], AC_MSG_ERROR("C++11 not supported by compiler"))
)

# check for some Boost header files, we use them in the data model
AC_CHECK_HEADERS([boost/multiprecision/cpp_int.hpp boost/multiprecision/cpp_dec_float.hpp boost/uuid/uuid.hpp boost/uuid/uuid_generators.hpp boost/algorithm/string.hpp boost/xpressive/xpressive.hpp boost/iostreams/stream.hpp boost/iostreams/stream_buffer.hpp boost/iostreams/concepts.hpp], , [AC_MSG_ERROR([required Boost headers missing])])


AC_LANG_POP([C++])



# GTest
AC_ARG_ENABLE(testing,AS_HELP_STRING(--enable-testing,[enable C++ unit testing (with Google Test); optional argument is ip address of host running the MICO platform]), 
[
  if test "x$enableval" = "xyes" ; then
     AC_DEFINE(TEST_HOST,"192.168.56.102", [host to connect to in unit tests])
  else
     AC_DEFINE_UNQUOTED(TEST_HOST,"$enableval", [host to connect to in unit tests])
  fi  
]
, with_gtest=no)
AS_IF([test "x$with_gtest" != xno],
  [
  AM_CONDITIONAL(GTEST,true)
  ], [
  AM_CONDITIONAL(GTEST,false)
  ])


AC_ARG_ENABLE(profiling,AS_HELP_STRING(--enable-profiling,enable GNU gprof profiling support),[
CFLAGS="$CFLAGS -g -pg"
CXXFLAGS="$CXXFLAGS -g -pg"
AC_DEFINE(PROFILING,1,[profiling enabled])
])

AC_ARG_ENABLE(warnings,AS_HELP_STRING(--enable-warnings,enable compile-time warnings),[
CFLAGS="$CFLAGS -Wall -Wextra"
CXXFLAGS="$CXXFLAGS -Wall -Wextra"
])


AC_OUTPUT([Makefile rdf/Makefile persistence/Makefile amqp/Makefile event/Makefile test/Makefile])
