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
class ConsumedMessage : public MessageImpl
{
private:
    /**
     *  The consumer tag
     *  @var string
     */
    std::string _consumerTag;

    /**
     *  The delivery tag
     *  @var uint64_t
     */
    uint64_t _deliveryTag;

    /**
     *  Is this a redelivered message?
     *  @var bool
     */
    bool _redelivered;


public:
    /**
     *  Constructor
     *  @param  frame
     */
    ConsumedMessage(const BasicDeliverFrame &frame) :
        MessageImpl(frame.exchange(), frame.routingKey()),
        _consumerTag(frame.consumerTag()), _deliveryTag(frame.deliveryTag()), _redelivered(frame.redelivered())
    {}

    /**
     *  Constructor
     *  @param  frame
     */
    ConsumedMessage(const BasicGetOKFrame &frame) :
        MessageImpl(frame.exchange(), frame.routingKey()),
        _deliveryTag(frame.deliveryTag()), _redelivered(frame.redelivered())
    {}


    /**
     *  Destructor
     */
    virtual ~ConsumedMessage() {}

    /**
     *  Retrieve the consumer tag
     *  @return std::string
     */
    const std::string &consumer() const
    {
        return _consumerTag;
    }
     
    /**
     *  Report to the handler
     *  @param  callback
     */
    void report(const MessageCallback &callback) const
    {
        // send ourselves to the consumer
        if (callback) callback(*this, _deliveryTag, _redelivered);
    }
};

/**
 *  End of namespace
 */
}

