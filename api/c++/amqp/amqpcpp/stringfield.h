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
#pragma once
/**
 *  String field types for amqp
 * 
 *  @copyright 2014 Copernica BV
 */

/**
 *  Set up namespace
 */
namespace AMQP {

/**
 *  Base class for string types
 */
template <typename T, char F>
class StringField : public Field
{
private:
    /**
     *  Pointer to string data
     *  @var string
     */
    std::string _data;

public:
    /**
     *  Initialize empty string
     */
    StringField() {}

    /**
     *  Construct based on a std::string
     *
     *  @param  value   string value
     */
    StringField(std::string value) : _data(value) {}

    /**
     *  Construct based on received data
     *  @param  frame
     */
    StringField(ReceivedFrame &frame)
    {
        // get the size
        T size(frame);
        
        // allocate string
        _data = std::string(frame.nextData(size.value()), (size_t) size.value());
    }

    /**
     *  Clean up memory used
     */
    virtual ~StringField() {}

    /**
     *  Create a new instance of this object
     *  @return Field*
     */
    virtual std::shared_ptr<Field> clone() const override
    {
        // create a new copy of ourselves and return it
        return std::make_shared<StringField>(_data);
    }

    /**
     *  Assign a new value
     *
     *  @param  value   new value
     */
    StringField& operator=(const std::string& value)
    {
        // overwrite data
        _data = value;

        // allow chaining
        return *this;
    }

    /**
     *  Get the size this field will take when
     *  encoded in the AMQP wire-frame format
     *  @return size_t
     */
    virtual size_t size() const override
    {
        // find out size of the size parameter
        T size(_data.size());
        
        // size of the uint8 or uint32 + the actual string size
        return size.size() + _data.size();
    }

    /**
     *  Get the value
     *  @return string
     */
    virtual operator const std::string& () const override
    {
        return _data;
    }

    /**
     *  Get the value
     *  @return string
     */
    const std::string& value() const
    {
        // get data
        return _data;
    }

    /**
     *  Get the maximum allowed string length for this field
     *  @return size_t
     */
    constexpr static size_t maxLength()
    {
        return T::max();
    }

    /**
     *  Write encoded payload to the given buffer.
     *  @param  buffer
     */
    virtual void fill(OutBuffer& buffer) const override
    {
        // create size
        T size(_data.size());
        
        // first, write down the size of the string
        size.fill(buffer);

        // write down the string content
        buffer.add(_data);
    }

    /**
     *  Get the type ID that is used to identify this type of
     *  field in a field table
     *  @return char
     */
    virtual char typeID() const override
    {
        return F;
    }

    /**
     *  Output the object to a stream
     *  @param std::ostream
     */
    virtual void output(std::ostream &stream) const override
    {
        // show
        stream << "string(" << value() << ")";
    }
};

/**
 *  Concrete string types for AMQP
 */
typedef StringField<UOctet, 's'>    ShortString;
typedef StringField<ULong, 'S'>     LongString;

/**
 *  end namespace
 */
}

