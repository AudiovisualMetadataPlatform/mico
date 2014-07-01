#include "rdf_model.hpp"


namespace org {
  namespace openrdf {
    namespace model {


      size_t URI::split() const {
	size_t found = uri.find('#');
	if(found != string::npos) {
	  return found + 1;
	}

	found = uri.rfind('/');
	if(found != string::npos) {
	  return found + 1;
	}

	found = uri.rfind(':');
	if(found != string::npos) {
	  return found + 1;
	}

	return string::npos;
      }
	

      /**
       * Returns the boolean value of this literal.
       */
      bool Literal::booleanValue() const {
	return (strncasecmp("true",label.c_str(),4) == 0) || (strncasecmp("yes",label.c_str(),3) == 0) || (strncasecmp("on",label.c_str(),2) == 0);
      }

      /**
       * Returns the byte value of this literal.
       */
      int8_t Literal::byteValue() const { 
	return (int8_t)std::stoi(label);
      };


      /**
       * Returns the decimal value of this literal.
       */
      cpp_dec_float_50 Literal::decimalValue() const {
	return cpp_dec_float_50(label.c_str()); // cpp_dec_float_50 has assignment constructor for strings
      }

      /**
       * Returns the double value of this literal.
       */
      double Literal::doubleValue() const {
	return std::stod(label);
      }

      /**
       * Returns the float value of this literal.
       */
      float Literal::floatValue() const {
	return std::stof(label);
      }

      /**
       * Returns the integer value of this literal.
       */
      cpp_int Literal::integerValue() const {
	return cpp_int(label.c_str()); // cpp_int has assignment constructor for strings
      }

      /**
       * Returns the 32 bit int value of this literal.
       */
      int32_t Literal::intValue() const {
	return (int32_t)std::stol(label);
      }

      /**
       * Returns the 64 bit long value of this literal.
       */
      int64_t Literal::longValue() const {
	return (int64_t)std::stoll(label);
      }

      /**
       * Returns the 16 bit short value of this literal.
       */
      int16_t Literal::shortValue() const {
	return (int16_t)std::stoi(label);
      }




    }
  }
}
