#ifndef HAVE_RDF_MODEL_H
#define HAVE_RDF_MODEL_H

#include <string>
#include <cstring>
#include <cstdint>
#include <cstdlib>

#include <boost/multiprecision/cpp_int.hpp>
#include <boost/multiprecision/cpp_dec_float.hpp> 

using std::string;
using std::size_t;
using namespace boost::multiprecision;

/**
 * This module contains an implementation of the Sesame data model in C++ for use in C++ components
 * of the MICO platform
 */

namespace org {
  namespace openrdf {
    namespace model {

      /**
       * The supertype of all RDF model objects (URIs, blank nodes and literals).
       */
      class Value {

      public:
	  
	/**
	 * Returns the String-value of a Value object. This returns either a Literal's label, a URI's URI or a BNode's ID.
	 */
	virtual const string& stringValue() const = 0;

      };

      /**
       * The supertype of all RDF resources (URIs and blank nodes).
       */
      class Resource : public virtual Value {
      };

      /**
       * A URI. A URI consists of a namespace and a local name, which are derived from a URI string
       * by splitting it in two using the following algorithm: 
       * - Split after the first occurrence of the '#' character,
       * - If this fails, split after the last occurrence of the '/' character,
       * - If this fails, split after the last occurrence of the ':' character. 
       *
       * The last step should never fail as every legal (full) URI contains at least one ':'
       * character to seperate the scheme from the rest of the URI. The implementation should check
       * this upon object creation.
       */
      class URI : public virtual Resource {

      private:
	string uri;

	// find split position according to algorithm in class description
	size_t split() const;

      public:
	
	URI(const string& uri) : uri(uri) {};
	URI(const char* uri)   : uri(uri) {};
	URI(const URI& uri)    : uri(uri.uri) {};
	
	/**
	 * Gets the local name of this URI.
	 */
	string getLocalName() const { return uri.substr(split()); };

	/**
	 * Gets the namespace of this URI.
	 */
	string getNamespace() const { return uri.substr(0,split()); };	


	inline const string& stringValue() const { return uri; } ;
      };


      class BNode : public virtual Resource {

      private:
	string id;

      public:
	/**
	 * Create a new BNode with a random ID.
	 */
	BNode() : BNode(std::to_string(rand())) {};

	/**
	 * Create a new BNode with the given ID
	 */
	BNode(const string& id) : id(id) {};
	BNode(const char*   id) : id(id) {};
	BNode(const BNode&  id) : id(id.id) {};


	/**
	 * retrieves this blank node's identifier.
	 */
	inline const string& getID() const { return id; };
	

	inline const string& stringValue() const { return id; };

      };

      /**
       * An RDF literal consisting of a label (the value) and optionally a language tag or a datatype (but not both).
       */
      class Literal : public virtual Value {
      protected:
	string label;

      public:

	Literal(const string& label) : label(label) {};
	Literal(const char*   label) : label(label) {};
	Literal(int8_t  i) : label(std::to_string((int)i)) {};
	Literal(int16_t i) : label(std::to_string((int)i)) {};
	Literal(int32_t i) : label(std::to_string((long int)i)) {};
	Literal(int64_t i) : label(std::to_string((long long)i)) {};
	Literal(double d) : label(std::to_string(d)) {};
	Literal(float  d) : label(std::to_string(d)) {};
	Literal(bool b) : label(b ? "true" : "false") {};

	/**
	 * Returns the boolean value of this literal.
	 */
	bool booleanValue() const;

	/**
	 * Returns the byte value of this literal.
	 */
	int8_t byteValue() const;


	/**
	 * Returns the decimal value of this literal.
	 */
	cpp_dec_float_50 decimalValue() const; 

	/**
	 * Returns the double value of this literal.
	 */
	double doubleValue() const;

	/**
	 * Returns the float value of this literal.
	 */
	float floatValue() const;

	/**
	 * Returns the integer value of this literal.
	 */
	cpp_int integerValue() const;

	/**
	 * Returns the 32 bit int value of this literal.
	 */
	int32_t intValue() const;

	/**
	 * Returns the 64 bit long value of this literal.
	 */
	int64_t longValue() const;

	/**
	 * Returns the 16 bit short value of this literal.
	 */
	int16_t shortValue() const;

	/**
	 * Gets the label of this literal.
	 */
	inline const string& getLabel() const { return label; };

	inline const string& stringValue() const { return label; };
      };

      /**
       * A literal with a language tag.
       */
      class LanguageLiteral : public virtual Literal {
      private:
	string lang;

      public:

	LanguageLiteral(const string& label, const string& language) : Literal(label), lang(language) {};
	LanguageLiteral(const char*   label, const char*   language) : Literal(label), lang(language) {};

	/**
	 * Gets the language tag for this literal, normalized to lower case.
	 */
	inline const string& getLanguage() const { return lang; };

      };


      /**
       * A literal with a datatype.
       */
      class DatatypeLiteral : public virtual Literal {
      private:
	URI datatype;

      public:
	DatatypeLiteral(const string& label, const URI& datatype) : Literal(label), datatype(datatype) {};
	DatatypeLiteral(const char*   label, const URI& datatype) : Literal(label), datatype(datatype) {};
	DatatypeLiteral(int8_t  i) : Literal(std::to_string((int)i)), datatype("http://www.w3.org/2001/XMLSchema#byte") {};
	DatatypeLiteral(int16_t i) : Literal(std::to_string((int)i)), datatype("http://www.w3.org/2001/XMLSchema#short") {};
	DatatypeLiteral(int32_t i) : Literal(std::to_string((long int)i)), datatype("http://www.w3.org/2001/XMLSchema#int") {};
	DatatypeLiteral(int64_t i) : Literal(std::to_string((long long)i)), datatype("http://www.w3.org/2001/XMLSchema#long") {};
	DatatypeLiteral(double d) : Literal(std::to_string(d)), datatype("http://www.w3.org/2001/XMLSchema#double") {};
	DatatypeLiteral(float  d) : Literal(std::to_string(d)), datatype("http://www.w3.org/2001/XMLSchema#float") {};
	DatatypeLiteral(bool b) : Literal(b ? "true" : "false"), datatype("http://www.w3.org/2001/XMLSchema#boolean") {};

	/**
	 * Gets the datatype for this literal.
	 */
	const URI& getDatatype() const { return datatype; };

      };


      class Statement {
      };


	bool operator==(URI,URI);
	bool operator==(BNode,BNode);
	bool operator==(Literal,Literal);
	bool operator==(LanguageLiteral,LanguageLiteral);

    }
  }
}

#endif
