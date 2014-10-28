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
 *  Class definition
 */
class QueueDeleteOKFrame : public QueueFrame
{
private:
    /**
     *  The message count
     *  @var int32_t
     */
    int32_t _messageCount;

protected:
    /**
     *  Encode a frame on a string buffer
     *
     *  @param  buffer  buffer to write frame to
     */
    virtual void fill(OutBuffer& buffer) const override
    {
        // call base
        QueueFrame::fill(buffer);

        // add field
        buffer.add(_messageCount);
    }

public:
    /**
     *  Construct a queuedeleteokframe
     *
     *  @param  channel         channel identifier
     *  @param  messageCount    number of messages
     */
    QueueDeleteOKFrame(uint16_t channel, int32_t messageCount) :
        QueueFrame(channel, 4), // 4 for int32_t size
        _messageCount(messageCount)
    {}

    /**
     *  Constructor based on received data
     *  @param  frame   received frame
     */
    QueueDeleteOKFrame(ReceivedFrame &frame) :
        QueueFrame(frame),
        _messageCount(frame.nextInt32())
    {}

    /**
     *  Destructor
     */
    virtual ~QueueDeleteOKFrame() {}

    /**
     *  return the method id
     *  @return uint16_t
     */
    virtual uint16_t methodID() const override
    {
        return 41;
    }

    /**
     *  returns the number of messages
     *  @return uint32_t
     */
    uint32_t messageCount() const
    {
        return _messageCount;
    }

    /**
     *  Process the frame
     *  @param  connection      The connection over which it was received
     *  @return bool            Was it succesfully processed?
     */
    virtual bool process(ConnectionImpl *connection) override
    {
        // check if we have a channel
        auto channel = connection->channel(this->channel());

        // channel does not exist
        if(!channel) return false;

        // report queue deletion success
        if (channel->reportSuccess(this->messageCount())) channel->onSynchronized();

        // done
        return true;
    }
};

/**
 *  end namespace
 */
}

