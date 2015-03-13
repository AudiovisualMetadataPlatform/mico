include_directories(../rdf ../persistence ../event ../daemon)

add_executable(mico_ocr_service mico_ocr_service.cpp)
target_link_libraries(mico_ocr_service mico_event mico_daemon ${TESSERACT_LIBRARIES} ${AMQPCPP_LIBRARIES})

install(TARGETS mico_ocr_service RUNTIME DESTINATION bin)
