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
class QueueBindFrame : public QueueFrame
{
private:
    /**
     *  Deprecated field
     *  @var uint16_t
     */
    uint16_t _deprecated = 0;

    /**
     *  Queue name
     *  @var ShortString
     */
    ShortString _name;

    /**
     *  Exchange name
     *  @var ShortString
     */
    ShortString _exchange;

    /**
     *  Routing key
     *  @var ShortString
     */
    ShortString _routingKey;

    /**
     *  Do not wait on response
     *  @var BooleanSet
     */
    BooleanSet _noWait;

    /**
     *  Additional arguments. Implementation dependent.
     *  @var Table
     */
    Table _arguments;

protected:
    /**
     *  Encode the frame into a buffer
     *
     *  @params buffer  buffer to encode frame in to
     */ 
    virtual void fill(OutBuffer& buffer) const override
    {
        // call base
        QueueFrame::fill(buffer);

        // add fields
        buffer.add(_deprecated);
        _name.fill(buffer);
        _exchange.fill(buffer);
        _routingKey.fill(buffer);
        _noWait.fill(buffer);
        _arguments.fill(buffer);
    }
public:
    /**
     *  Destructor
     */
    virtual ~QueueBindFrame() {}

    /**
     *  Construct a queue bind frame
     *
     *  @param   channel             channel identifier
     *  @param   String name         name of the queue
     *  @param   String exchange     name of the exchange
     *  @param   String routingKey   the routingKey
     *  @param   Bool noWait         do not wait for a response
     *  @param   Table arguments     additional arguments
     */
    QueueBindFrame(uint16_t channel, const std::string& name, const std::string& exchange, const std::string& routingKey = "", bool noWait = false, const Table& arguments = {}) :
        QueueFrame(channel, (name.length() + exchange.length() + routingKey.length() + arguments.size() + 6) ), // 3 extra per string, 1 for bools, 2 for deprecated field
        _name(name),
        _exchange(exchange),
        _routingKey(routingKey),
        _noWait(noWait),
        _arguments(arguments)
    {}     
  

    /**
     *  Constructor based on incoming data
     *  @param  frame   received frame
     */
    QueueBindFrame(ReceivedFrame &frame) :
        QueueFrame(frame),
        _deprecated(frame.nextUint16()),
        _name(frame),
        _exchange(frame),
        _routingKey(frame),
        _noWait(frame),
        _arguments(frame)
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
     *  Returns the method id
     *  @return uint16_t
     */
    virtual uint16_t methodID() const override
    {
        return 20;
    }

    /**
     *  returns the queue name
     *  @return string
     */
    const std::string& name() const
    {
        return _name;
    }

    /**
     *  returns the exchange Name
     *  @return string
     */
    const std::string& exchange() const
    {
        return _exchange;
    }

    /**
     *  returns the routing key
     *  @return string
     */
    const std::string& routingKey() const
    {
        return _routingKey;
    }

    /**
     *  returns whether to wait on a response
     *  @return boolean
     */
    bool noWait() const
    {
        return _noWait.get(0);
    }

    /**
     *  returns the additional arguments. Implementation dependant.
     *  @return Table
     */
    const Table &arguments() const
    {
        return _arguments;
    }
};

/**
 *  end namespace
 */
}

