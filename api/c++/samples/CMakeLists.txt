find_package( Boost QUIET REQUIRED COMPONENTS program_options)
include_directories(../rdf ../persistence ../event ../daemon ${Boost_INCLUDE_DIRS})

add_executable(mico_ocr_service mico_ocr_service.cpp)
target_link_libraries(mico_ocr_service mico_event mico_daemon ${TESSERACT_LIBRARIES} ${AMQPCPP_LIBRARIES} ${Boost_LIBRARIES})

install(TARGETS mico_ocr_service RUNTIME DESTINATION bin)
