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
 *  We extend from the default deferred and add extra functionality
 */
class DeferredCancel : public Deferred
{
private:
    /**
     *  Pointer to the channel
     *  @var    ChannelImpl
     */
    ChannelImpl *_channel;

    /**
     *  Callback to execute when the instruction is completed
     *  @var    CancelCallback
     */
    CancelCallback _cancelCallback;

    /**
     *  Report success for frames that report cancel operations
     *  @param  name            Consumer tag that is cancelled
     *  @return Deferred
     */
    virtual const std::shared_ptr<Deferred> &reportSuccess(const std::string &name) const override;

    /**
     *  The channel implementation may call our
     *  private members and construct us
     */
    friend class ChannelImpl;
    friend class ConsumedMessage;
    
public:
    /**
     *  Protected constructor that can only be called
     *  from within the channel implementation
     *
     *  Note: this constructor _should_ be protected, but because make_shared
     *  will then not work, we have decided to make it public after all,
     *  because the work-around would result in not-so-easy-to-read code.
     * 
     *  @param  channel     Pointer to the channel
     *  @param  failed      Are we already failed?
     */
    DeferredCancel(ChannelImpl *channel, bool failed = false) : 
        Deferred(failed), _channel(channel) {}

public:
    /**
     *  Register a function to be called when the cancel operation succeeded
     *
     *  Only one callback can be registered. Successive calls
     *  to this function will clear callbacks registered before.
     *
     *  @param  callback    the callback to execute
     */
    DeferredCancel &onSuccess(const CancelCallback &callback)
    {
        // store callback
        _cancelCallback = callback;
        
        // allow chaining
        return *this;
    }

    /**
     *  Register the function that is called when the cancel operation succeeded
     *  @param  callback
     */
    DeferredCancel &onSuccess(const SuccessCallback &callback)
    {
        // call base
        Deferred::onSuccess(callback);
        
        // allow chaining
        return *this;
    }
};

/**
 *  End namespace
 */
}
