#ifndef HAVE_RDF_MODEL_H
#define HAVE_RDF_MODEL_H 1

#include <string>
#include <cstring>
#include <cstdint>
#include <cstdlib>

#include <boost/multiprecision/cpp_int.hpp>
#include <boost/multiprecision/cpp_dec_float.hpp> 

using std::string;
using std::size_t;
using std::ostream;
using namespace boost::multiprecision;

/*
 * This module contains an implementation of the Sesame data model in C++ for use in C++ components
 * of the MICO platform
 */

namespace mico {
  namespace rdf {
    namespace model {

      enum ValueTypes { TYPE_URI, TYPE_BNODE, TYPE_PLAIN_LITERAL, TYPE_LANGUAGE_LITERAL, TYPE_TYPED_LITERAL };

      /**
       * The supertype of all RDF model objects (URIs, blank nodes and literals).
       */
      class Value {

	friend bool operator==(const Value& l, const Value& r);
	friend bool operator!=(const Value& l, const Value& r);
	friend std::ostream& operator<<(std::ostream&, Value&);
	friend std::ostream& operator<<(std::ostream&, Value*);

      protected:
	/**
	 * Internal polymorphic implementation of equals and print.
	 */
	virtual bool equals(const Value& other) const = 0;

	virtual ostream& print(ostream& os) const = 0;	

      public:

	virtual ~Value() {};

	/**
	 * Returns the String-value of a Value object. This returns either a Literal's label, a URI's URI or a BNode's ID.
	 */
	virtual const string& stringValue() const = 0;

	/**
	 * Return type information (to avoid dynamic casts if possible)
	 */
	virtual const ValueTypes getType() const = 0;
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

	/**
	 * Internal polymorphic implementation of equals.
	 */
	bool equals(const Value& other) const;

	ostream& print(ostream& os) const { os << "URI("<<uri<<")"; return os; } ;

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


	/**
	 * Returns the String-value of a Value object. This returns either a Literal's label, a URI's URI or a BNode's ID.
	 */
	inline const string& stringValue() const { return uri; } ;


	inline const ValueTypes getType() const { return TYPE_URI; };


	inline bool operator==(const string& s) const { return uri == s; };
	inline bool operator==(const char* s) const { return uri == s; };
	inline bool operator!=(const string& s) const { return uri != s; };
	inline bool operator!=(const char* s) const { return uri != s; };
      };


      /**
       * RDF blank node, represented with an internal string identifier.
       */
      class BNode : public virtual Resource {

      private:
	string id;

	/**
	 * Internal polymorphic implementation of equals.
	 */
	bool equals(const Value& other) const;

	ostream& print(ostream& os) const { os << "BNode("<<id<<")"; return os; } ;
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
	

	/**
	 * Returns the String-value of a Value object. This returns either a Literal's label, a URI's URI or a BNode's ID.
	 */
	inline const string& stringValue() const { return id; };

	inline const ValueTypes getType() const { return TYPE_BNODE; };

	inline bool operator==(const string& s) const { return id == s; };
	inline bool operator==(const char* s) const { return id == s; };
	inline bool operator!=(const string& s) const { return id != s; };
	inline bool operator!=(const char* s) const { return id != s; };
      };

      /**
       * An RDF literal consisting of a label (the value) and optionally a language tag or a datatype (but not both).
       */
      class Literal : public virtual Value {
      protected:
	string label;

	/**
	 * Internal polymorphic implementation of equals.
	 */
	virtual bool equals(const Value& other) const;

	virtual ostream& print(ostream& os) const { os << "Literal("<<label<<")"; return os; };
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

	virtual ~Literal() {};

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

	/**
	 * Returns the String-value of a Value object. This returns either a Literal's label, a URI's URI or a BNode's ID.
	 */
	inline const string& stringValue() const { return label; };


	inline const ValueTypes getType() const { return TYPE_PLAIN_LITERAL; };



	inline bool operator==(const string& s) const { return label == s; };
	inline bool operator==(const char* s) const { return label == s; };
	inline bool operator!=(const string& s) const { return label != s; };
	inline bool operator!=(const char* s) const { return label != s; };


	inline operator bool() const { return booleanValue(); }
	inline operator int() const { return intValue(); }
	inline operator long int() const { return longValue(); }
	inline operator long long int() const { return longValue(); }
	inline operator float() const { return floatValue(); }
	inline operator double() const { return doubleValue(); }

      };

      /**
       * A literal with a language tag.
       */
      class LanguageLiteral : public virtual Literal {
      private:
	string lang;

	/**
	 * Internal polymorphic implementation of equals.
	 */
	bool equals(const Value& other) const;

	ostream& print(ostream& os) const { os << "LanguageLiteral("<<label<<","<<lang<<")"; return os; } ;
      public:

	LanguageLiteral(const string& label, const string& language) : Literal(label), lang(language) {};
	LanguageLiteral(const char*   label, const char*   language) : Literal(label), lang(language) {};

	/**
	 * Gets the language tag for this literal, normalized to lower case.
	 */
	inline const string& getLanguage() const { return lang; };

	inline const ValueTypes getType() const { return TYPE_LANGUAGE_LITERAL; };

      };


      /**
       * A literal with a datatype.
       */
      class DatatypeLiteral : public virtual Literal {
      private:
	URI datatype;	

	/**
	 * Internal polymorphic implementation of equals.
	 */
	bool equals(const Value& other) const;

	ostream& print(ostream& os) const { os << "DatatypeLiteral("<<label<<","<<datatype.stringValue()<<")"; return os; } ;

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


	inline const ValueTypes getType() const { return TYPE_TYPED_LITERAL; };
      };


      /**
       * Representation of an RDF triple, consisting of subject, predicate and object.
       */
      class Statement {
      private:
	Resource& subject;
	URI&      predicate;
	Value&    object;

      public:
	// provide a copy constructor for every possible combination (for convenience)
	Statement(Resource& s, URI& p, Value& o) : subject(s), predicate(p), object(o) {};
	
	/**
	 * Gets the subject of this statement.
	 */
	inline const Resource& getSubject() const { return subject; };

	/**
	 * Gets the predicate of this statement.
	 */
	inline const URI& getPredicate() const { return predicate; };

	
	/**
	 * Gets the object of this statement.
	 */
	inline const Value& getObject() const { return object; };

      };



      inline bool operator==(const string& l,const URI& r) {
	return r == l;
      }

      inline bool operator!=(const string& l,const URI& r) {
	return r != l;
      }

      inline bool operator==(const string& l,const BNode& r) {
	return r == l;
      }

      inline bool operator!=(const string& l,const BNode& r) {
	return r != l;
      }

      inline bool operator==(const string& l,const Literal& r) {
	return r == l;
      }

      inline bool operator!=(const string& l,const Literal& r) {
	return r != l;
      }


      // convenience: test double value of a literal
      inline bool operator==(const double d,const Literal& l) {
	return l.doubleValue() == d;
      }

      inline bool operator==(const Literal& l,const double d) {
	return l.doubleValue() == d;
      }

      inline bool operator!=(const double d,const Literal& l) {
	return l.doubleValue() != d;
      }

      inline bool operator!=(const Literal& l,const double d) {
	return l.doubleValue() != d;
      }




      inline bool operator==(const Value& l,const Value& r) {
	return l.equals(r);
      }

      inline bool operator!=(const Value& l,const Value& r) {
	return !l.equals(r);
      }


      inline std::ostream& operator<<(std::ostream& os, Value& v) {
	return v.print(os);
      }

      inline std::ostream& operator<<(std::ostream& os, Value* v) {
	return v->print(os);
      }

    }
  }
}

#endif
