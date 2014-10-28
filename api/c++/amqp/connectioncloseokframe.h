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
class ConnectionCloseOKFrame : public ConnectionFrame
{
protected:
    /**
     *  Encode a frame on a string buffer
     *
     *  @param  buffer  buffer to write frame to
     */
    virtual void fill(OutBuffer& buffer) const override
    {
        // call base
        ConnectionFrame::fill(buffer);
    }
public:
    /**
     *  Constructor based on a received frame
     *
     *  @param frame    received frame
     */
    ConnectionCloseOKFrame(ReceivedFrame &frame) :
        ConnectionFrame(frame)
    {}

    /**
     *  construct a channelcloseokframe object
     */
    ConnectionCloseOKFrame() :
        ConnectionFrame(0)
    {}

    /**
     *  Destructor
     */
    virtual ~ConnectionCloseOKFrame() {}

    /**
     *  Method id
     */
    virtual uint16_t methodID() const override
    {
        return 51;
    }
    
    /**
     *  Process the frame
     *  @param  connection
     */
    virtual bool process(ConnectionImpl *connection) override
    {
        // report that it is closed
        connection->reportClosed();
        
        // done
        return true;
    }
};

/**
 *  end namespace
 */
}

