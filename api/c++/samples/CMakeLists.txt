include_directories(../rdf ../persistence ../event ../daemon)

if("${CMAKE_CURRENT_SOURCE_DIR}" MATCHES "[0123456789]+")
  message( FATAL_ERROR "The path of the source MUST NOT contain numbers! Otherwise you get an linking error, as some script messes it up." )
endif()

add_executable(mico_ocr_service mico_ocr_service.cpp)
target_link_libraries(mico_ocr_service mico_event mico_daemon ${TESSERACT_LIBRARIES} ${AMQPCPP_LIBRARIES})

install(TARGETS mico_ocr_service RUNTIME DESTINATION bin)
