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
 * Class implementation
 */
class BasicFrame : public MethodFrame
{
protected:
    /**
     *  Constructor
     *  @param  channel     The channel ID
     *  @param  size        Payload size
     */
    BasicFrame(uint16_t channel, uint32_t size) : MethodFrame(channel, size) {}

    /**
     *  Constructor based on a received frame
     *  @param  frame
     */
    BasicFrame(ReceivedFrame &frame) : MethodFrame(frame) {}


public:
    /**
     *  Destructor
     */
    virtual ~BasicFrame() {}

    /** 
     *  Class id
     *  @return uint16_t
     */
    virtual uint16_t classID() const override
    {
        return 60;
    }
};

/**
 *  end namespace
 */
}

