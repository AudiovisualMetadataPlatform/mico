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
class ChannelCloseOKFrame : public ChannelFrame
{
protected:
    /**
     *  Encode a frame on a string buffer
     *
     *  @param  buffer  buffer to write frame to
     */
    virtual void fill(OutBuffer& buffer) const override
    {
        // call base
        ChannelFrame::fill(buffer);
    }

public:
    /**
     *  Construct a channel close ok  frame
     *  @param  frame
     */
    ChannelCloseOKFrame(ReceivedFrame &frame) :
        ChannelFrame(frame)
    {}

    /**
     *  Construct a channel close ok  frame
     *
     *  @param  channel     channel we're working on
     */
    ChannelCloseOKFrame(uint16_t channel) :
        ChannelFrame(channel, 0)
    {}

    /**
     *  Destructor
     */
    virtual ~ChannelCloseOKFrame() {}

    /**
     *  Method id
     *  @return uint16_t
     */
    virtual uint16_t methodID() const override
    {
        return 41;
    }

    /**
     *  Process the frame
     *  @param  connection      The connection over which it was received
     *  @return bool            Was it succesfully processed?
     */
    virtual bool process(ConnectionImpl *connection) override
    {
        // we need the appropriate channel
        auto channel = connection->channel(this->channel());

        // channel does not exist
        if (!channel) return false;

        // report that the channel is closed
        if (channel->reportClosed()) channel->onSynchronized();

        // done
        return true;
    }
};

/**
 *  end namespace
 */
}

