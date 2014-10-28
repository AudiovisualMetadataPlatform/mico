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
class ChannelFlowOKFrame : public ChannelFrame
{
private:
    /**
     *  Is the channel flow currently active?
     *  @var BooleanSet
     */
    BooleanSet _active;

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

        // add fields
        _active.fill(buffer);
    }
public:
    /**
     *  Construct a channel flow frame
     *
     *  @param  frame   received frame to decode
     */
    ChannelFlowOKFrame(ReceivedFrame &frame) :
        ChannelFrame(frame),
        _active(frame)
    {}

    /**
     *  Construct a channel flow frame
     *
     *  @param  channel channel we're working on
     *  @param  active  enable or disable channel flow
     */
    ChannelFlowOKFrame(uint16_t channel, bool active) :
        ChannelFrame(channel, 1), //sizeof bool
        _active(active)
    {}

    /**
     *  Destructor
     */
    virtual ~ChannelFlowOKFrame() {}

    /**
     *  Method id
     */
    virtual uint16_t methodID() const override
    {
        return 21;
    }

    /**
     *  Is channel flow active?
     *  @return bool
     */
    bool active() const
    {
        return _active.get(0);
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

        // report success for the call
        if (channel->reportSuccess()) channel->onSynchronized();

        // done
        return true;
    }
};

/**
 *  end namespace
 */
}

