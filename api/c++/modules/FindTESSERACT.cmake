# --------------------------------------------------------
#  Copyright (C) 2004-2012 by EMGU. All rights reserved.
#
# - Try to find tesseract
# Once done, this will define
#
# TESSERACT_FOUND - system has tesseract
# TESSERACT_INCLUDE_DIRS - the tesseract include directories
# TESSERACT_LIBRARIES - link these to use tessereact
#
# --------------------------------------------------------

FIND_PATH(TESSERACT_INCLUDE_DIRS
  baseapi.h
  /usr/include/tesseract
  )

FIND_LIBRARY(TESSERACT_LIBRARY
  NAMES tesseract tesseract_api
  PATHS
  /usr/lib
  /usr/lib64
)
MESSAGE(STATUS "TESSERACT_LIBRARY ${TESSERACT_LIBRARY}")

FIND_LIBRARY(LEPTONICA_LIBRARY
  NAMES lept
  PATHS
  /usr/lib
  /usr/lib64
)
MESSAGE(STATUS "LEPTONICA_LIBRARY ${LEPTONICA_LIBRARY}")


# handle the QUIETLY and REQUIRED arguments and set TESSERACT_FOUND to TRUE if
# all listed variables are TRUE
INCLUDE(FindPackageHandleStandardArgs)
FIND_PACKAGE_HANDLE_STANDARD_ARGS(TESSERACT  DEFAULT_MSG TESSERACT_LIBRARY TESSERACT_INCLUDE_DIRS)

IF(TESSERACT_FOUND)
#  MESSAGE(STATUS "TESSERACT found")
  list(APPEND TESSERACT_LIBRARIES ${TESSERACT_LIBRARY})
  list(APPEND TESSERACT_LIBRARIES ${LEPTONICA_LIBRARY})
ENDIF(TESSERACT_FOUND)

MARK_AS_ADVANCED(TESSERACT_INCLUDE_DIR TESSERACT_LIBRARY)