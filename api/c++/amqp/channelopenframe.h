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
class ChannelOpenFrame : public ChannelFrame
{
protected:
    /**
     *  Encode a frame on a string buffer
     *
     *  @param  buffer  buffer to write frame to
     */
    virtual void fill(OutBuffer& buffer) const
    {
        // call base
        ChannelFrame::fill(buffer);
        
        // add deprecated data
        ShortString unused;
        
        // add to the buffer
        unused.fill(buffer);
    }

public:
    /**
     *  Constructor to create a channelOpenFrame
     *
     *  @param  channel     channel we're working on
     */
    ChannelOpenFrame(uint16_t channel) : ChannelFrame(channel, 1) {}    // 1 for the deprecated shortstring size

    /**
     *  Construct to parse a received frame
     *  @param  frame
     */
    ChannelOpenFrame(ReceivedFrame &frame) : ChannelFrame(frame)
    {
        // deprecated argument
        ShortString unused(frame);
    }

    /**
     *  Destructor
     */
    virtual ~ChannelOpenFrame() {}

    /**
     *  Method id
     *  @return uint16_t
     */
    virtual uint16_t methodID() const override
    {
        return 10;
    }
};

/**
 *  end namespace
 */
}

