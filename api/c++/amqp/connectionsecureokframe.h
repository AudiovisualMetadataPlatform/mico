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
 *  Set up namespace
 */
namespace AMQP {

/**
 *  Class implementation
 */
class ConnectionSecureOKFrame : public ConnectionFrame
{
private:
    /**
     *  The security challenge response
     *  @var LongString
     */
    LongString _response;

protected:
    /**
     *  Encode a frame on a string buffer
     *
     *  @param  buffer  buffer to write frame to
     */
    virtual void fill(OutBuffer& buffer) const override
    {
        // call base
        ConnectionFrame::fill(buffer);

        // add fields
        _response.fill(buffer);
    }

public:
    /**
     *  Construct a connection security challenge response frame
     *
     *  @param  response    the challenge response
     */
    ConnectionSecureOKFrame(const std::string& response) :
        ConnectionFrame(response.length() + 4), //response length + uint32_t for encoding the length
        _response(response)
    {}

    /**
     *  Construct a connection security challenge response frame from a received frame
     *
     *  @param  frame   received frame
     */
    ConnectionSecureOKFrame(ReceivedFrame &frame) :
        ConnectionFrame(frame),
        _response(frame)
    {}

    /**
     *  Destructor
     */
    virtual ~ConnectionSecureOKFrame() {}

    /**
     *  Method id
     *  @return uint16_t
     */
    virtual uint16_t methodID() const override
    {
        return 21;
    }

    /**
     *  Get the challenge response
     *  @return string
     */
    const std::string& response() const
    {
        return _response;
    }
};

/**
 *  end namespace
 */
}

