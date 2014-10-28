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
class ChannelFlowFrame : public ChannelFrame
{
private:
    /**
     *  Enable or disable the channel flow
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
     *  @param  frame   received frame
     */
    ChannelFlowFrame(ReceivedFrame &frame) :    
        ChannelFrame(frame),
        _active(frame)
    {}

    /**
     *  Construct a channel flow frame
     *
     *  @param  channel     channel we're working on
     *  @param  active      enable or disable channel flow
     */
    ChannelFlowFrame(uint16_t channel, bool active) : 
        ChannelFrame(channel, 1), //sizeof bool
        _active(active)
    {}

    /**
     *  Destructor
     */
    virtual ~ChannelFlowFrame() {}

    /**
     *  Method id
     *  @return uint16_t
     */
    virtual uint16_t methodID() const override
    {
        return 20;
    }
    
    /**
     *  Is channel flow active or not?
     *  @return bool
     */
    bool active() const
    {
        return _active.get(0);
    }
};

/**
 *  end namespace
 */
}

