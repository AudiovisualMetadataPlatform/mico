/**
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
#include "rdf_model.hpp"

using namespace std;

namespace mico {
    namespace rdf {
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
            * Internal polymorphic implementation of equals.
            */
            bool URI::equals(const Value& v) const {
                const URI* vp = dynamic_cast<const URI*>(&v);
                return vp != NULL && vp->uri == this->uri;
            }


            /**
            * Internal polymorphic implementation of equals.
            */
            bool BNode::equals(const Value& v) const {
                const BNode* vp = dynamic_cast<const BNode*>(&v);
                return vp != NULL && vp->id == this->id;
            }


            /**
            * Internal polymorphic implementation of equals.
            */
            bool Literal::equals(const Value& v) const {
                const Literal* vp = dynamic_cast<const Literal*>(&v);
                return vp != NULL && vp->label == this->label;
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


            /**
            * Internal polymorphic implementation of equals.
            */
            bool LanguageLiteral::equals(const Value& v) const {
                const LanguageLiteral* vp = dynamic_cast<const LanguageLiteral*>(&v);
                return vp != NULL && vp->label == label && vp->lang == this->lang;
            }

            /**
            * Internal polymorphic implementation of equals.
            */
            bool DatatypeLiteral::equals(const Value& v) const {
                const DatatypeLiteral* vp = dynamic_cast<const DatatypeLiteral*>(&v);
                return vp != NULL && vp->label == this->label && vp->datatype == this->datatype;
            }



        }
    }
}
