include_directories(../rdf ../persistence ../logging ${PROTOBUF_INCLUDE_DIRS})

find_package(Protobuf 2.6 REQUIRED)

#specify directoy with all protobuf input files here
get_filename_component(MICO_EVENT_PB_INPUT_PATH ${CMAKE_CURRENT_SOURCE_DIR}/../../shared/event/ ABSOLUTE)

#specify all protobuf input files without path here
set(MICO_EVENT_PB_INPUT_FILES
  Event.proto
)

################################################################################################


get_filename_component(MICO_EVENT_PB_OUT_DIR ${CMAKE_CURRENT_BINARY_DIR}/protobuf_out ABSOLUTE)
file(MAKE_DIRECTORY ${MICO_EVENT_PB_OUT_DIR})
set(MICO_EVENT_PB_OUTPUT_SRC_FILES)

foreach (f ${MICO_EVENT_PB_INPUT_FILES})
  get_filename_component(curr_basename ${f} NAME_WE)
  get_filename_component(curr_h_file ${MICO_EVENT_PB_OUT_DIR}/${curr_basename}.pb.h ABSOLUTE)
  get_filename_component(curr_cc_file ${MICO_EVENT_PB_OUT_DIR}/${curr_basename}.pb.cc ABSOLUTE)
  get_filename_component(curr_input_file ${MICO_EVENT_PB_INPUT_PATH}/${f} ABSOLUTE)  
  set(curr_tgt_name mico_event_generate_${f})
  add_custom_command(OUTPUT ${curr_h_file} ${curr_cc_file} COMMAND ${PROTOBUF_PROTOC_EXECUTABLE} ARGS --proto_path ${MICO_EVENT_PB_INPUT_PATH} --cpp_out ${MICO_EVENT_PB_OUT_DIR} ${curr_input_file})
  list(APPEND MICO_EVENT_PB_OUTPUT_SRC_FILES ${curr_h_file}) 
  list(APPEND MICO_EVENT_PB_OUTPUT_SRC_FILES ${curr_cc_file})
endforeach()

include_directories(
  ${MICO_EVENT_PB_OUT_DIR}
)

add_library(mico_event SHARED 
  ${MICO_EVENT_PB_OUTPUT_SRC_FILES}
  EventManager.cpp
)
target_link_libraries(mico_event mico_logging mico_rdf mico_persistence ${PROTOBUF_LIBRARIES} ${Boost_SYSTEM_LIBRARY} ${CMAKE_THREAD_LIBS_INIT} ${AMQPCPP_LIBRARIES})
set_target_properties(mico_event PROPERTIES VERSION "${MICO_PLATFORM_VERSION}" SOVERSION "${MICO_PLATFORM_SOVERSION}")

install (TARGETS mico_event DESTINATION lib)
install (FILES AnalysisService.hpp EventManager.hpp ${MICO_EVENT_PB_OUT_DIR}/Event.pb.h DESTINATION include)
