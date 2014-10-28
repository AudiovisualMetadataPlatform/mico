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
class QueueFrame : public MethodFrame
{
protected:
    /**
     *  Construct a queueframe
     *  @param  channel  channel identifier
     *  @param   size     size of the frame
     */
    QueueFrame(uint16_t channel, uint32_t size) : MethodFrame(channel, size) {}

    /**
     *  Constructor based on incoming data
     *  @param  frame   received frame
     */
    QueueFrame(ReceivedFrame &frame) : MethodFrame(frame) {}

public:
    /**
     *  Destructor
     */
    virtual ~QueueFrame() {}
    
    /**
     *  returns the class id
     *  @return uint16_t
     */
    virtual uint16_t classID() const override
    {
        return 50;
    }
};

/**
 *  end namespace
 */
}

