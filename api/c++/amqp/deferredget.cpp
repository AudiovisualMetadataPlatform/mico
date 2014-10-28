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
 *  Dependencies
 */
#include "includes.h"

/**
 *  Set up namespace
 */
namespace AMQP {

/**
 *  Report success, a get message succeeded and the message is expected soon
 *  @param  messageCount    Message count
 *  @return Deferred
 */
const std::shared_ptr<Deferred> &DeferredGet::reportSuccess(uint32_t messageCount) const
{
    // we grab a self pointer to ensure that the deferred object stays alive
    auto self = shared_from_this();

    // we now know the name, so we can install the message callback on the channel, the self
    // pointer is also captured, which ensures that 'this' is not destructed, all members stay
    // accessible, and that the onFinalize() function will only be called after the message
    // is reported (onFinalize() is called from the destructor of this DeferredGet object)
    _channel->install("", [self, this](const Message &message, uint64_t deliveryTag, bool redelivered) {

        // install a monitor to deal with the case that the channel is removed
        Monitor monitor(_channel);

        // call the callbacks
        if (_messageCallback) _messageCallback(message, deliveryTag, redelivered);
        
        // we can remove the callback now from the channel
        if (monitor.valid()) _channel->uninstall("");
    });

    // report the size (note that this is the size _minus_ the message that is retrieved
    // (and for which the callback will be called later), so it could be zero)
    if (_sizeCallback) _sizeCallback(messageCount);
    
    // return next object
    return _next;
}

/**
 *  Report success, although no message could be get
 *  @return Deferred
 */
const std::shared_ptr<Deferred> &DeferredGet::reportSuccess() const
{
    // report the size
    if (_sizeCallback) _sizeCallback(0);

    // check if a callback was set
    if (_emptyCallback) _emptyCallback();
    
    // return next object
    return _next;
}

/**
 *  End of namespace
 */
}

