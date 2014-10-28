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
class BasicQosFrame : public BasicFrame
{
private:
    /**
     *  specifies the size of the prefetch window in octets
     *  @var int32_t
     */
    int32_t _prefetchSize;

    /**
     *  specifies a prefetch window in terms of whole messages
     *  @var int16_t
     */
    int16_t _prefetchCount;

    /**
     *  apply QoS settings to entire connection
     *  @var BooleanSet
     */
    BooleanSet _global;

protected:
    /**
     *  Encode a frame on a string buffer
     *  @param   buffer  buffer to write frame to
     */
    virtual void fill(OutBuffer& buffer) const override
    {
        // call base
        BasicFrame::fill(buffer);

        // add fields
        buffer.add(_prefetchSize);
        buffer.add(_prefetchCount);
        _global.fill(buffer);
    }

public:
    /**
     * Construct a basic qos frame
     *
     * @param   channel         channel we're working on
     * @param   prefetchCount   specifies a prefetch window in terms of whole messages
     * @param   global          share prefetch count with all consumers on the same channel
     * @default false
     */
    BasicQosFrame(uint16_t channel, int16_t prefetchCount = 0, bool global = false) :
        BasicFrame(channel, 7), // 4 (int32) + 2 (int16) + 1 (bool)
        _prefetchSize(0),
        _prefetchCount(prefetchCount),
        _global(global)
    {}

    /**
     *  Constructor based on incoming frame
     *  @param  frame
     */
    BasicQosFrame(ReceivedFrame &frame) :
        BasicFrame(frame),
        _prefetchSize(frame.nextInt32()),
        _prefetchCount(frame.nextInt16()),
        _global(frame)
    {}

    /**
     *  Destructor
     */
    virtual ~BasicQosFrame() {}

    /**
     *  Return the method ID
     *  @return  uint16_t
     */
    virtual uint16_t methodID() const override
    {
        return 10;
    }

    /**
     *  Return the prefetch count
     *  @return int16_t
     */
    int16_t prefetchCount() const
    {
        return _prefetchCount;
    }

    /**
     *  returns the value of global
     *  @return  boolean
     */
    bool global() const
    {
        return _global.get(0);
    }

    /**
     *  returns the prefetch size
     *  @return int32_t
     */
    int32_t prefetchSize() const
    {
        return _prefetchSize;
    }
};

/**
 *  End namespace
 */
}

