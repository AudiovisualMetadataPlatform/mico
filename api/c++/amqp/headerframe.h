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
class HeaderFrame : public ExtFrame
{
protected:
    /**
     *  Construct a header frame
     *  @param  channel     Channel ID
     *  @param  size        Payload size
     */
    HeaderFrame(uint16_t channel, uint32_t size) : ExtFrame(channel, size + 2) {}  // + size of classID (2bytes)

    /**
     *  Construct based on incoming data
     *  @param  frame       Incoming frame
     */
    HeaderFrame(ReceivedFrame &frame) : ExtFrame(frame) {}

    /**
     *  Encode a header frame to a string buffer
     *
     *  @param  buffer  buffer to write frame to
     */
    virtual void fill(OutBuffer& buffer) const
    {
        // call base
        ExtFrame::fill(buffer);

        // add type
        buffer.add(classID());
    }

public:
    /**
     *  Destructor
     */
    virtual ~HeaderFrame() {}

    /**
     *  Get the message type
     *  @return uint8_t
     */
    virtual uint8_t type() const override
    {
        return 2;
    }

    /**
     *  Class id
     *  @return uint16_t
     */
    virtual uint16_t classID() const = 0;
};

/**
 *  end namespace
 */
}

