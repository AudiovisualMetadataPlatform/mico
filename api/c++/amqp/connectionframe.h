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
 *  Class definition
 */
namespace AMQP {

/**
 *  Class implementation
 */
class ConnectionFrame : public MethodFrame
{
protected:
    /**
     *  Constructor for a connectionFrame
     *  
     *  A connection frame never has a channel identifier, so this is passed
     *  as zero to the base constructor
     * 
     *  @param  size        size of the frame
     */
    ConnectionFrame(uint32_t size) : MethodFrame(0, size) {}

    /**
     *  Constructor based on a received frame
     *  @param  frame
     */
    ConnectionFrame(ReceivedFrame &frame) : MethodFrame(frame) {}

public:
    /**
     *  Destructor
     */
    virtual ~ConnectionFrame() {}

    /**
     *  Class id
     *  @return uint16_t
     */
    virtual uint16_t classID() const override
    {
        return 10;
    }
};

/**
 *  end namespace
 */
}

