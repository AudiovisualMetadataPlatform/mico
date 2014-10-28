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
#include "includes.h"

/**
 *  Set up namespace
 */
namespace AMQP {

/**
 *  Decode a field by fetching a type and full field from a frame
 *  The returned field is allocated on the heap!
 *  @param  frame
 *  @return Field*
 */
Field *Field::decode(ReceivedFrame &frame)
{
    // get the type
    uint8_t type = frame.nextUint8();
    
    // create field based on type
    switch (type)
    {
        case 't':   return new BooleanSet(frame);
        case 'b':   return new Octet(frame);
        case 'B':   return new UOctet(frame);
        case 'U':   return new Short(frame);
        case 'u':   return new UShort(frame);
        case 'I':   return new Long(frame);
        case 'i':   return new ULong(frame);
        case 'L':   return new LongLong(frame);
        case 'l':   return new ULongLong(frame);
        case 'f':   return new Float(frame);
        case 'd':   return new Double(frame);
        case 'D':   return new DecimalField(frame);
        case 's':   return new ShortString(frame);
        case 'S':   return new LongString(frame);
        case 'A':   return new Array(frame);
        case 'T':   return new Timestamp(frame);
        case 'F':   return new Table(frame);
        default:    return nullptr;
    }
}

/**
 *  Cast to string
 *  @return std::string
 */
Field::operator const std::string& () const
{
    // static empty string
    static std::string empty;
    
    // return it
    return empty;
}

/**
 *  Cast to array
 *  @return Array
 */
Field::operator const Array& () const
{
    // static empty array
    static Array empty;
    
    // return it
    return empty;
}

/**
 *  Cast to object
 *  @return Array
 */
Field::operator const Table& () const
{
    // static empty table
    static Table empty;
    
    // return it
    return empty;
}

/**
 *  End of namespace
 */
}

