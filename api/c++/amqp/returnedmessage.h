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
class ReturnedMessage : public MessageImpl
{
private:
    /**
     *  The reply code
     *  @var    int16_t
     */
    int16_t _replyCode;

    /**
     *  The reply message
     *  @var    string
     */
    std::string _replyText;


public:
    /**
     *  Constructor
     *  @param  frame
     */
    ReturnedMessage(const BasicReturnFrame &frame) :
        MessageImpl(frame.exchange(), frame.routingKey()),
        _replyCode(frame.replyCode()), _replyText(frame.replyText()) {}

    /**
     *  Destructor
     */
    virtual ~ReturnedMessage() {}
};

/**
 *  End of namespace
 */
}

