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
/**
 *  Namespace
 */
namespace AMQP {

/**
 *  Class definition
 */
class MessageImpl : public Message
{
private:
    /**
     *  How many bytes have been received?
     *  @var uint64_t
     */
    uint64_t _received;

    /**
     *  Was the buffer allocated by us?
     *  @var bool
     */
    bool _selfAllocated;

protected:
    /**
     *  Constructor
     *  @param  exchange
     *  @param  routingKey
     */
    MessageImpl(const std::string &exchange, const std::string &routingKey) :
        Message(exchange, routingKey),
        _received(0), _selfAllocated(false)
        {}

public:
    /**
     *  Destructor
     */
    virtual ~MessageImpl()
    {
        // clear up memory if it was self allocated
        if (_selfAllocated) delete[] _body;
    }

    /**
     *  Set the body size
     *  This field is set when the header is received
     *  @param  uint64_t
     */
    void setBodySize(uint64_t size)
    {
        _bodySize = size;
    }

    /**
     *  Append data
     *  @param  buffer      incoming data
     *  @param  size        size of the data
     *  @return bool        true if the message is now complete
     */
    bool append(const char *buffer, uint64_t size)
    {
        // is this the only data, and also direct complete?
        if (_received == 0 && size >= _bodySize)
        {
            // we have everything
            _body = buffer;
            _received = _bodySize;

            // done
            return true;
        }
        else
        {
            // it does not yet fit, do we have to allocate?
            if (!_body) _body = new char[_bodySize];
            _selfAllocated = true;

            // prevent that size is too big
            if (size > _bodySize - _received) size = _bodySize - _received;

            // append data
            memcpy((char *)(_body + _received), buffer, size);

            // we have more data now
            _received += size;

            // done
            return _received >= _bodySize;
        }
    }
};

/**
 *  End of namespace
 */
}

