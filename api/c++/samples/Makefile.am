LIBRDF=../rdf/libmico_rdf.la
LIBMARMOTTA=../rdf/libmico_marmotta.la
LIBPERSISTENCE=../persistence/libmico_persistence.la
LIBAMQP=../amqp/libmico_amqp.la
LIBEVENT=../event/libmico_event.la

AM_CXXFLAGS=-I../rdf -I../persistence -I../event -I../amqp

bin_PROGRAMS=

if SAMPLE_OCR
bin_PROGRAMS += mico_ocr_service
mico_ocr_service_SOURCES = mico_ocr_service.cpp
mico_ocr_service_LDADD = $(LIBPERSISTENCE) $(LIBRDF) $(LIBEVENT) @tesseract_libs@ -lboost_system -lboost_log -lboost_log_setup
endif
