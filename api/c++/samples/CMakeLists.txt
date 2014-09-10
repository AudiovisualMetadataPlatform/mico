include_directories(../rdf ../persistence ../event)

add_executable(mico_ocr_service mico_ocr_service.cpp)
target_link_libraries(mico_ocr_service mico_marmotta mico_rdf mico_persistence mico_event mico_amqp)