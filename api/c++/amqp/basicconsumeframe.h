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
class BasicConsumeFrame : public BasicFrame
{
private:
    /**
     *  Field that is no longer used
     *  @var uint16_t
     */
    uint16_t _deprecated = 0;
    
    /**
     *  specifies the name of the queue to consume from
     *  @var Table
     */
    ShortString _queueName;

     /**
     *  specifies the identifier for the consumer tag. 
     *  This tag is local to a channel so two clients can use the same consumer tags. 
     *  If empty, the server generates a tag.
     *  @var ShortString
     */
    ShortString _consumerTag;

    /**
     *  Booleans sent in frame
     *  0: noLocal
     *  1: noAck
     *  2: exclusive
     *  3: noWait
     *  @var BooleanSet
     */
    BooleanSet _bools;

     /**
      *  additional arguments, implementation dependent
      *  @var Table
      */
    Table _filter;

    

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
        
        // fill the buffer
        buffer.add((uint16_t) _deprecated);
        _queueName.fill(buffer);
        _consumerTag.fill(buffer);
        _bools.fill(buffer);
        _filter.fill(buffer);
    }

public:
    /**
     *  Construct a basic consume frame
     *
     *  @param  channel         channel we're working on
     *  @param  queueName       name of the queue to consume from
     *  @param  consumerTag     identifier for the consumer tag. 
     *  @param  noLocal         no-local
     *  @param  noAck           no acknowledgements
     *  @param  exclusive       exclusive channel
     *  @param  noWait          don't wait for a response
     *  @param  filter          additional arguments
     */
    BasicConsumeFrame(uint16_t channel, const std::string& queueName, const std::string& consumerTag, bool noLocal = false, bool noAck = false, bool exclusive = false, bool noWait = false, const Table& filter = {}) :
        BasicFrame(channel, (queueName.length() + consumerTag.length() + 5 + filter.size())), // size of vars, +1 for each shortstring size, +1 for bools, +2 for deprecated value
        _queueName(queueName),
        _consumerTag(consumerTag),
        _bools(noLocal, noAck, exclusive, noWait),
        _filter(filter)
    {}    

    /**
     *  Constructor based on incoming data
     *  @param  frame
     */
    BasicConsumeFrame(ReceivedFrame &frame) :
        BasicFrame(frame),
        _deprecated(frame.nextUint16()), // read deprecated info
        _queueName(frame),
        _consumerTag(frame),
        _bools(frame),
        _filter(frame)
    {}

    /**
     *  Destructor
     */
    virtual ~BasicConsumeFrame() {}

    /**
     *  Is this a synchronous frame?
     *
     *  After a synchronous frame no more frames may be
     *  sent until the accompanying -ok frame arrives
     */
    bool synchronous() const override
    {
        // we are synchronous when the nowait option is not set
        return !noWait();
    }

    /**
     *  Return the method ID
     *  @return uint16_t
     */
    virtual uint16_t methodID() const override
    {
        return 20;
    }

    /**
     *  Return the name of the queue to consume from
     *  @return string
     */
    const std::string& queueName() const
    {
        return _queueName;
    }

    /**
     *  return the identifier for the consumertag
     *  @return string
     */
    const std::string& consumerTag() const
    {
        return _consumerTag;
    }

    /**
     *  return the value of the noLocal bool
     *  @return bool
     */
    bool noLocal() const
    {
        return _bools.get(0);
    }

    /**
     *  return the value of the noAck bool
     *  @return bool
     */
    bool noAck() const
    {
        return _bools.get(1);
    }

    /**
     *  return whether the queue is exclusive
     *  @return bool
     */
    bool exclusive() const
    {
        return _bools.get(2);
    }

    /**
     *  return whether to wait for a response
     *  @return bool
     */
    bool noWait() const
    {
        return _bools.get(3);
    }

    /**
     *  return the additional filter arguments
     *  @return Table
     */
    const Table& filter() const
    {
        return _filter;
    }

};

/**
 *  End namespace
 */
}
