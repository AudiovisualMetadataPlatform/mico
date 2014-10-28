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
class QueuePurgeFrame : public QueueFrame{
private:
    /**
     *  Field that is no longer in use
     *  @var int16_t
     */
    int16_t _deprecated = 0;

    /**
     *  Name of the queue
     *  @var ShortString
     */
    ShortString _name;

    /**
     *  Do not wait on response
     *  @var BooleanSet
     */
    BooleanSet _noWait;

protected:
    /**
     *  Encode the frame into a buffer
     *
     *  @param   buffer  buffer to write frame to
     */
    virtual void fill(OutBuffer& buffer) const override
    {
        // call base
        QueueFrame::fill(buffer);

        // add fields
        buffer.add(_deprecated);
        _name.fill(buffer);
        _noWait.fill(buffer);
    }

public:
    /**
     *  Destructor
     */
    virtual ~QueuePurgeFrame() {}

    /**
     *  Construct a QueuePurgeFrame
     *
     *  @param   channel channel identifier
     *  @param   name    name of the queue
     *  @param   noWait  Do not wait on response
     *
     *  @return  newly created Queuepurgeframe
     */
    QueuePurgeFrame(uint16_t channel, const std::string& name, bool noWait = false) :
        QueueFrame(channel, name.length() + 4), // 1 extra for string length, 1 for bool, 2 for deprecated field
        _name(name),
        _noWait(noWait)
    {}

    /**
     *  Constructor based on received data
     *  @param frame    received frame
     */
    QueuePurgeFrame(ReceivedFrame &frame) :
        QueueFrame(frame),
        _deprecated(frame.nextInt16()),
        _name(frame),
        _noWait(frame)
    {}

    /**
     *  Is this a synchronous frame?
     *
     *  After a synchronous frame no more frames may be
     *  sent until the accompanying -ok frame arrives
     */
    bool synchronous() const override
    {
        // we are synchronous without the nowait option
        return !noWait();
    }

    /**
     *  The method ID
     *  @return method id
     */
    virtual uint16_t methodID() const override
    {
        return 30;
    }

    /**
     *  The queue name
     *  @return the queue name
     */
    const std::string& name() const
    {
        return _name;
    }

    /**
     *  The nowait option
     *  @return the value of bool noWait
     */
    bool noWait() const
    {
        return _noWait.get(0);
    }
};

/**
 *  end namespace
 */
}

