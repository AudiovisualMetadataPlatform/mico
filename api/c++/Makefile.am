AUTOMAKE_OPTIONS = foreign
SUBDIRS = rdf persistence amqp event test tools samples

include $(top_srcdir)/m4/doxygen.am

docdir=$(prefix)/share/doc/libmico-api
doc_DATA=doc/mico-platform.pdf

htmldocdir=$(prefix)/share/doc/libmico-api/html
htmldoc_DATA=doc/html/*.html doc/html/*.css doc/html/*.js doc/html/*.png

htmlsearchdir=$(prefix)/share/doc/libmico-api/html/search
htmlsearch_DATA=doc/html/search/*.html doc/html/search/*.css doc/html/search/*.js doc/html/search/*.png
