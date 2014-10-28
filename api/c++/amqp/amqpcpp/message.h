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
#pragma once
/**
 *  Message.h
 *
 *  An incoming message has the same sort of information as an outgoing
 *  message, plus some additional information.
 *
 *  Message objects can not be constructed by end users, they are only constructed
 *  by the AMQP library, and passed to the ChannelHandler::onDelivered() method
 *
 *  @copyright 2014 Copernica BV
 */

/**
 *  Set up namespace
 */
namespace AMQP { 

/**
 *  Class definition
 */
class Message : public Envelope
{
protected:
    /**
     *  The exchange to which it was originally published
     *  @var    string
     */
    std::string _exchange;
    
    /**
     *  The routing key that was originally used
     *  @var    string
     */
    std::string _routingKey;
    
protected:
    /**
     *  The constructor is protected to ensure that endusers can not
     *  instantiate a message
     *  @param  exchange
     *  @param  routingKey
     */
    Message(const std::string &exchange, const std::string &routingKey) :
        Envelope(nullptr, 0), _exchange(exchange), _routingKey(routingKey)
    {}
    
public:
    /**
     *  Destructor
     */
    virtual ~Message() {}

    /**
     *  The exchange to which it was originally published
     *  @var    string
     */
    const std::string &exchange() const
    {
        return _exchange;
    }
    
    /**
     *  The routing key that was originally used
     *  @var    string
     */
    const std::string &routingKey() const
    {
        return _routingKey;
    }
};

/**
 *  End of namespace
 */
}

