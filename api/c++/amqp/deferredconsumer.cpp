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
#include "includes.h"

/**
 *  Namespace
 */
namespace AMQP {

/**
 *  Report success for frames that report start consumer operations
 *  @param  name            Consumer tag that is started
 *  @return Deferred
 */
const std::shared_ptr<Deferred> &DeferredConsumer::reportSuccess(const std::string &name) const
{
    // we now know the name, so we can install the message callback on the channel
    _channel->install(name, _messageCallback);
    
    // skip if no special callback was installed
    if (!_consumeCallback) return Deferred::reportSuccess();
    
    // call the callback
    _consumeCallback(name);
    
    // return next object
    return _next;
}

/**
 *  End namespace
 */
}
