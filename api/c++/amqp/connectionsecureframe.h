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
class ConnectionSecureFrame : public ConnectionFrame
{
private:
    /**
     *  The security challenge
     *  @var LongString
     */
    LongString _challenge;

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

        // encode fields
        _challenge.fill(buffer);
    }

public:
    /**
     *  Construct a connection security challenge frame
     *
     *  @param  challenge   the challenge
     */
    ConnectionSecureFrame(const std::string& challenge) :
        ConnectionFrame(challenge.length() + 4), // 4 for the length of the challenge (uint32_t)
        _challenge(challenge)
    {}

    /**
     *  Construct a connection secure frame from a received frame
     *
     *  @param  frame   received frame
     */
    ConnectionSecureFrame(ReceivedFrame &frame) :
        ConnectionFrame(frame),
        _challenge(frame)
    {}

    /**
     *  Destructor
     */
    virtual ~ConnectionSecureFrame() {}

    /**
     *  Method id
     *  @return uint16_t
     */
    virtual uint16_t methodID() const override
    {
        return 20;
    }

    /**
     *  Get the challenge
     *  @return string
     */
    const std::string& challenge() const 
    {
        return _challenge;
    }
};

/**
 *  end namespace
 */
}

