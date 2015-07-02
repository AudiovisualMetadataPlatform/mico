SET(RESOURCE_COMPILER "${CMAKE_SOURCE_DIR}/sparqlToC.sh")
FILE(GLOB COMPILED_RESOURCES "../../shared/persistence/sparql/*.sparql")
FOREACH(INPUT_FILE ${COMPILED_RESOURCES})
    get_filename_component(FILENAME ${INPUT_FILE} NAME_WE)
    SET(OUTPUT_FILE  ${CMAKE_CURRENT_BINARY_DIR}${CMAKE_FILES_DIRECTORY}/${FILENAME}.cc)
    ADD_CUSTOM_COMMAND(
        OUTPUT ${OUTPUT_FILE}
        COMMAND ${RESOURCE_COMPILER} ${INPUT_FILE} ${OUTPUT_FILE}
        COMMENT "Compiling SPARQL query ${INPUT_FILE} to C array")
    LIST(APPEND COMPILED_RESOURCES ${OUTPUT_FILE})
ENDFOREACH()

#set_source_files_properties(${COMPILED_RESOURCES} PROPERTIES GENERATED TRUE)

include_directories(../rdf ../logging ${HDFS_INCLUDE_DIRS})
add_library(mico_persistence SHARED ${COMPILED_RESOURCES} SPARQLUtil.cpp Metadata.cpp Content.cpp ContentItem.cpp PersistenceService.cpp URLStream.cpp FileStream.cpp WebStream.cpp HDFSStream.cpp)
target_link_libraries(mico_persistence mico_marmotta mico_rdf mico_logging ${CURL_LIBRARIES} ${HDFS_LIBRARIES} ${CMAKE_THREAD_LIBS_INIT} ${CMAKE_THREAD_LIBS_INIT})

set_target_properties(mico_persistence PROPERTIES SOVERSION "${MICO_PLATFORM_VERSION}")


install (TARGETS mico_persistence DESTINATION lib)
install (FILES Content.hpp ContentItem.hpp Metadata.hpp PersistenceService.hpp SPARQLUtil.hpp URLStream.hpp FileStream.hpp WebStream.hpp HDFSStream.hpp DESTINATION include)
