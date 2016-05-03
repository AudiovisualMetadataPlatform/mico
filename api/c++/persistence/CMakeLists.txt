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

include_directories(../rdf ../logging ../commons ${HDFS_INCLUDE_DIRS})

# ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++

message(STATUS "Using [${ANNODEPS_JAR}], version ${ANNO4JDEPENDENCIES_VERSION} based on anno4j ${ANNO4JDEPENDENCIES_ANNO4J_VERSION}.")

add_definitions(-DANNO4JDEPENDENCIES_PATH="${ANNO4CPPDEPS_ROOT}/usr/share/mico")


add_library(mico_persistence SHARED
  JnippExceptionHandling
  Item
  Part
  Asset
  Resource
  Uri
  AssetAnno4cpp
  ItemAnno4cpp
  PartAnno4cpp
  ResourceAnno4cpp
  ${COMPILED_RESOURCES} SPARQLUtil.cpp Metadata.cpp Content.cpp ContentItem.cpp PersistenceService.cpp URLStream.cpp FileStream.cpp WebStream.cpp HDFSStream.cpp
)
target_link_libraries(mico_persistence
  micoapicommons
  anno4cpp
  mico_marmotta mico_rdf mico_logging ${CURL_LIBRARIES} ${HDFS_LIBRARIES} ${CMAKE_THREAD_LIBS_INIT} ${CMAKE_THREAD_LIBS_INIT} ${JNI_LIBRARIES} ${JAVA_AWT_LIBRARY}
)

add_dependencies(mico_persistence
    anno4cpp_ext
)

set_target_properties(mico_persistence PROPERTIES VERSION "${MICO_PLATFORM_VERSION}" SOVERSION "${MICO_PLATFORM_SOVERSION}")
set_target_properties(mico_persistence PROPERTIES COMPILE_DEFINITIONS "JNIPP_USE_TYPE_TRAITS")
set_target_properties(mico_persistence PROPERTIES COMPILE_OPTIONS "-O3;-std=c++11;-ffunction-sections;-fdata-sections")


install (TARGETS mico_persistence DESTINATION lib)
install (FILES
    Uri.hpp
    Metadata.hpp
    Item.hpp
    Part.hpp
    Resource.hpp
    Asset.hpp
    PersistenceService.hpp
    SPARQLUtil.hpp
    URLStream.hpp
    FileStream.hpp
    WebStream.hpp
    HDFSStream.hpp
    DESTINATION include)

install(DIRECTORY ${ANNO4CPPDEPS_ROOT}/include/ DESTINATION include)
install(DIRECTORY ${ANNO4CPPDEPS_ROOT}/lib/ DESTINATION lib)
install(DIRECTORY ${ANNO4CPPDEPS_ROOT}/usr/ DESTINATION usr)
