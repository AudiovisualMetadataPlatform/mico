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
class BasicRejectFrame : public BasicFrame 
{
private:
    /**
     *  server-assigned and channel specific delivery tag
     *  @var int64_t
     */
    int64_t _deliveryTag;

    /**
     *  Server will try to requeue messages. If requeue is false or requeue attempt fails, messages are discarded or dead-lettered
     *  @var BooleanSet
     */
    BooleanSet _requeue;

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

        // encode fields
        buffer.add(_deliveryTag);
        _requeue.fill(buffer);
    }

public:
    /**
     *  construct a basic reject frame from a received frame
     *
     *  @param  frame   received frame
     */
    BasicRejectFrame(ReceivedFrame &frame) :
        BasicFrame(frame),
        _deliveryTag(frame.nextInt64()),
        _requeue(frame)
    {}

    /**
     *  Construct a basic reject frame
     *
     *  @param  channel         channel id
     *  @param  deliveryTag     server-assigned and channel specific delivery tag
     *  @param  requeue         whether to attempt to requeue messages
     */
    BasicRejectFrame(uint16_t channel, int64_t deliveryTag, bool requeue = true) :
        BasicFrame(channel, 9), // 8 for uint64_t, 1 for bool
        _deliveryTag(deliveryTag),
        _requeue(requeue)
    {}

    /**
     *  Destructor
     */ 
    virtual ~BasicRejectFrame() {}

    /**
     *  Is this a synchronous frame?
     *
     *  After a synchronous frame no more frames may be
     *  sent until the accompanying -ok frame arrives
     */
    bool synchronous() const override
    {
        return false;
    }

    /**
     * Return the method ID
     * @return  uint16_t
     */
    virtual uint16_t methodID() const override
    {
        return 90;
    }

    /**
     *  Return the server-assigned and channel specific delivery tag
     *  @return  uint64_t
     */
    int64_t deliveryTag() const
    {
        return _deliveryTag;
    }

    /**
     *  Return whether the server will try to requeue
     *  @return bool
     */
     bool requeue() const
     {
        return _requeue.get(0);
     }
};

/**
 *  end namespace
 */
}

