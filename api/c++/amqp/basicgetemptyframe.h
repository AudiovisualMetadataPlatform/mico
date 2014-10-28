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
class BasicGetEmptyFrame : public BasicFrame 
{
private:
    /**
     *  Field that is no longer used
     *  @var ShortString
     */
    ShortString _deprecated;

protected:
    /**
     *  Encode a frame on a string buffer
     *
     *  @param  buffer  buffer to write frame to
     */
    virtual void fill(OutBuffer& buffer) const override
    {
        // call base
        BasicFrame::fill(buffer);

        // recreate deprecated field and encode
        _deprecated.fill(buffer);
    }


public:
    /**
     *  Construct a basic get empty frame
     *
     *  @param  channel     channel we're working on
     */
    BasicGetEmptyFrame(uint16_t channel) :
        BasicFrame(channel, 1)  // 1 for encoding the deprecated cluster id (shortstring)
    {}

    /**
     *  Constructor for incoming data
     *  @param  frame   received frame
     */
    BasicGetEmptyFrame(ReceivedFrame &frame) :
        BasicFrame(frame),
        _deprecated(frame)
    {}

    /**
     *  Destructor
     */
    virtual ~BasicGetEmptyFrame() {}

    /**
     * Return the method ID
     * @return  uint16_t
     */
    virtual uint16_t methodID() const override
    {
        return 72;
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

        // report
        if (channel->reportSuccess()) channel->onSynchronized();

        // done
        return true;
    }

};

/**
 *  end namespace
 */
}

