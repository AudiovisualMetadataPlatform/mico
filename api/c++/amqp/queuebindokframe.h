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
class QueueBindOKFrame : public QueueFrame
{
protected:
    /**
     *  Fill output buffer
     *  @param  buffer
     */
    virtual void fill(OutBuffer& buffer) const override
    {
        // call base
        QueueFrame::fill(buffer);
    }

public:
    /**
     *  Construct a queuebindokframe
     *
     *  @param  channel     channel identifier
     */
    QueueBindOKFrame(uint16_t channel) : QueueFrame(channel, 0) {}

    /**
     *  Constructor based on incoming data
     *  @param  frame   received frame
     */
    QueueBindOKFrame(ReceivedFrame &frame) :
        QueueFrame(frame)
    {}

    /**
     *  Destructor
     */
    virtual ~QueueBindOKFrame() {}

    /**
     *  returns the method id
     *  @return uint16_t
     */
    virtual uint16_t methodID() const override
    {
        return 21;
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

        // report to handler
        if (channel->reportSuccess()) channel->onSynchronized();

        // done
        return true;
    }
};

/**
 *  end namespace
 */
}

