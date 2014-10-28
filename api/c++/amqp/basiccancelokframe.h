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
namespace AMQP{

/**
 *  Class implementation
 */
class BasicCancelOKFrame : public BasicFrame
{
private:
    /**
     *  Holds the consumer tag specified by the client or provided by the server.
     *  @var ShortString
     */
    ShortString _consumerTag;

protected:
    /**
     * Encode a frame on a string buffer
     *
     * @param   buffer  buffer to write frame to
     */
    virtual void fill(OutBuffer& buffer) const override
    {
        // call base
        BasicFrame::fill(buffer);

        // add own information
        _consumerTag.fill(buffer);
    }

public:
    /**
     *  Construct a basic cancel ok frame
     *
     *  @param  frame   received frame
     */
    BasicCancelOKFrame(ReceivedFrame &frame) :
        BasicFrame(frame),
        _consumerTag(frame)
    {}

    /**
     *  Construct a basic cancel ok frame (client-side)
     *  @param  channel     channel identifier
     *  @param  consumerTag holds the consumertag specified by client or server
     */
    BasicCancelOKFrame(uint16_t channel, std::string& consumerTag) :
        BasicFrame(channel, consumerTag.length() + 1),    // add 1 byte for encoding the size of consumer tag
        _consumerTag(consumerTag)
    {}

    /**
     *  Destructor
     */
    virtual ~BasicCancelOKFrame() {}

    /**
     *  Return the consumertag, which is specified by the client or provided by the server
     *  @return string
     */
    const std::string& consumerTag() const
    {
        return _consumerTag;
    }

    /**
     *  Return the method ID
     *  @return uint16_t
     */
    virtual uint16_t methodID() const override
    {
        return 31;
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
        if (channel->reportSuccess<const std::string&>(consumerTag())) channel->onSynchronized();

        // done
        return true;
    }
};

/**
 *  End of namespace
 */
}

