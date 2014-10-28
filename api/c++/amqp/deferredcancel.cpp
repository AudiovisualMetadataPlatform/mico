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
 *  Report success for frames that report cancel operations
 *  @param  name            Consumer tag that is cancelled
 *  @return Deferred
 */
const std::shared_ptr<Deferred> &DeferredCancel::reportSuccess(const std::string &name) const
{
    // in the channel, we should uninstall the consumer
    _channel->uninstall(name);
    
    // skip if no special callback was installed
    if (!_cancelCallback) return Deferred::reportSuccess();
    
    // call the callback
    _cancelCallback(name);
    
    // return next object
    return _next;
}

/**
 *  End namespace
 */
}

