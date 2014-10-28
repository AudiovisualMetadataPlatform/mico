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
 *  Class defintion
 */
class BasicNackFrame : public BasicFrame {
private:
    /**
     *  server-assigned and channel specific delivery tag
     *  @var    uint64_t
     */
    uint64_t _deliveryTag;

    /**
     *  The additional bits
     *  @var    BooleanSet
     */
    BooleanSet _bits;

protected:
    /**
     *  Encode a frame on a string buffer
     *
     *  @param   buffer  buffer to write frame to
     *  @return  pointer to object to allow for chaining
     */
    virtual void fill(OutBuffer& buffer) const override
    {
        // call base
        BasicFrame::fill(buffer);
        
        // add the delivery tag
        buffer.add(_deliveryTag);
        
        // add the booleans
        _bits.fill(buffer);
    }

public:
    /**
     *  Construct a basic negative-acknowledgement frame
     *
     *  @param  channel         Channel identifier
     *  @param  deliveryTag     server-assigned and channel specific delivery tag
     *  @param  multiple        nack mutiple messages
     *  @param  requeue         requeue the message
     */
    BasicNackFrame(uint16_t channel, uint64_t deliveryTag, bool multiple = false, bool requeue = false) : 
        BasicFrame(channel, 9),
        _deliveryTag(deliveryTag),
        _bits(multiple, requeue) {}
    
    /**
     *  Construct based on received frame
     *  @param  frame
     */
    BasicNackFrame(ReceivedFrame &frame) : 
        BasicFrame(frame),
        _deliveryTag(frame.nextUint64()),
        _bits(frame) {}
    
    /**
     *  Destructor
     */
    virtual ~BasicNackFrame() {}

    /**
     *  Return the method ID
     *  @return  uint16_t
     */
    virtual uint16_t methodID() const override
    {
        return 120;
    }

    /**
     *  Return the server-assigned and channel specific delivery tag
     *  @return  uint64_t
     */
    uint64_t deliveryTag() const
    {
        return _deliveryTag;
    }

    /**
     *  Return whether to acknowledgement multiple messages
     *  @return  bool
     */
    bool multiple()
    {
        return _bits.get(0);
    }

    /**
     *  Should the message be put back in the queue?
     *  @return  bool
     */
    bool requeue()
    {
        return _bits.get(1);
    }
};

/**
 *  end namespace
 */
}

