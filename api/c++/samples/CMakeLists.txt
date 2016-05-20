find_package( Boost QUIET REQUIRED COMPONENTS program_options)
include_directories(../rdf ../persistence ../event ${CMAKE_CURRENT_BINARY_DIR}/../event/protobuf_out ../daemon ../logging ${Boost_INCLUDE_DIRS})
add_executable(mico_ocr_service mico_ocr_service.cpp)


link_directories(${JAVA_AWT_PATH}/xawt/)
message(STATUS "-----------------------------------${JAVA_AWT_PATH}/xawt/=" ${JAVA_AWT_PATH}/xawt/)
message(STATUS "-----------------------------------JNI_LIBRARIES         =" ${JNI_LIBRARIES})
message(STATUS "-----------------------------------JAVA_AWT_LIBRARY      =" ${JAVA_AWT_LIBRARY})

target_link_libraries(mico_ocr_service mico_event mico_daemon ${TESSERACT_LIBRARIES} ${AMQPCPP_LIBRARIES} ${Boost_LIBRARIES} ${JNI_LIBRARIES} ${JAVA_AWT_LIBRARY})

install(TARGETS mico_ocr_service RUNTIME DESTINATION bin)
