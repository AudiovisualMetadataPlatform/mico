add_library(mico_rdf SHARED rdf_model.cpp rdf_query_tuple.cpp rdf_query_bool.cpp)
add_library(mico_marmotta SHARED http_client.cpp sparql_client.cpp)

target_link_libraries(mico_rdf ${EXPAT_LIBRARIES} ${CURL_LIBRARIES} ${CMAKE_THREAD_LIBS_INIT})
target_link_libraries(mico_marmotta mico_rdf ${CMAKE_THREAD_LIBS_INIT})

set_target_properties(mico_rdf PROPERTIES SOVERSION "${MICO_PLATFORM_VERSION}")
set_target_properties(mico_marmotta PROPERTIES SOVERSION "${MICO_PLATFORM_VERSION}")

install (TARGETS mico_rdf mico_marmotta DESTINATION lib)
install (FILES vocabularies.hpp http_client.hpp rdf_model.hpp rdf_query.hpp sparql_client.hpp DESTINATION include)