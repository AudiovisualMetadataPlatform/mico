SET(RESOURCE_COMPILER xxd)
FILE(GLOB_RECURSE COMPILED_RESOURCES "../../shared/persistence/sparql/*.sparql")
FOREACH(INPUT_FILE ${COMPILED_RESOURCES})
    SET(OUTPUT_FILE ${INPUT_FILE}.cc)
    ADD_CUSTOM_COMMAND(
        OUTPUT ${OUTPUT_FILE}
        COMMAND ${RESOURCE_COMPILER} -i ${INPUT_FILE} ${OUTPUT_FILE}
        COMMENT "Compiling ${INPUT_FILE} to binary")
    LIST(APPEND COMPILED_RESOURCES ${OUTPUT_FILE})
ENDFOREACH()

include_directories(../rdf)
add_library(mico_persistence SHARED ${COMPILED_RESOURCES} SPARQLUtil.cpp Metadata.cpp Content.cpp ContentItem.cpp PersistenceService.cpp URLStream.cpp)
target_link_libraries(mico_persistence mico_marmotta mico_rdf)

install (TARGETS mico_persistence DESTINATION lib)
install (FILES Content.hpp ContentItem.hpp Metadata.hpp PersistenceService.hpp SPARQLUtil.hpp URLStream.hpp DESTINATION include)