#ifndef HAVE_RDF_MODEL_H
#define HAVE_RDF_MODEL_H

#include <string>
#include <cstring>
#include <cstdint>

#include <boost/multiprecision/cpp_int.hpp>
#include <boost/multiprecision/cpp_dec_float.hpp> 

using std::string;
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

      public:
	
	URI(const string& uri);
	URI(const char* uri);
	~URI();
	
	/**
	 * Gets the local name of this URI.
	 */
	string getLocalName() const;

	/**
	 * Gets the namespace of this URI.
	 */
	string getNamespace() const;	


	const string& stringValue() const;
      };


      class BNode : public virtual Resource {

      private:
	string id;

      public:
	/**
	 * Create a new BNode with a random ID.
	 */
	BNode();

	/**
	 * Create a new BNode with the given ID
	 */
	BNode(const string& id);
	BNode(const char*   id);

	~BNode();

	/**
	 * retrieves this blank node's identifier.
	 */
	const string& getID() const;
	

	const string& stringValue() const;

      };

      /**
       * An RDF literal consisting of a label (the value) and optionally a language tag or a datatype (but not both).
       */
      class Literal : public virtual Value {
      protected:
	string value;

      public:

	Literal(const string& label);
	Literal(const char*   label);
	~Literal();

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
	const string& getLabel() const;	

	const string& stringValue() const;
      };

      /**
       * A literal with a language tag.
       */
      class LanguageLiteral : public virtual Literal {
      private:
	string lang;

      public:

	LanguageLiteral(const string& label, const string& language);
	LanguageLiteral(const char*   label, const char*   language);
	~LanguageLiteral();

	/**
	 * Gets the language tag for this literal, normalized to lower case.
	 */
	const string& getLanguage() const;

      };


      /**
       * A literal with a datatype.
       */
      class DatatypeLiteral : public virtual Literal {
      private:
	URI datatype;

      public:
	DatatypeLiteral(const string& label, const URI& datatype);
	DatatypeLiteral(const char*   label, const URI& datatype);
	~DatatypeLiteral();

	/**
	 * Gets the datatype for this literal.
	 */
	const URI& getDatatype() const;

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
