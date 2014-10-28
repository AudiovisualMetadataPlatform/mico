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
class ExchangeFrame : public MethodFrame
{
protected:
    /**
     *  Constructor based on incoming data
     *
     *  @param  frame   received frame to decode
     */
    ExchangeFrame(ReceivedFrame &frame) : MethodFrame(frame) {}

    /**
     *  Constructor for an exchange frame
     *
     *  @param  channel     channel we're working on
     *  @param  size        size of the payload
     */
    ExchangeFrame(uint16_t channel, uint32_t size) : MethodFrame(channel, size) {}

public:
    /**
     *  Destructor
     */
    virtual ~ExchangeFrame() {}

    /**
     *  Class id
     *  @return uint16_t
     */
    virtual uint16_t classID() const override
    {
        return 40;
    }
};

/**
 *  end namespace
 */
}

