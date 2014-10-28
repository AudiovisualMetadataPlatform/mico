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
class ConnectionOpenOKFrame : public ConnectionFrame
{
private:
    /**
     *  Deprecated field we need to read
     *  @var ShortString
     */
    ShortString _deprecatedKnownHosts;

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

        // add deprecaed field
        _deprecatedKnownHosts.fill(buffer);
    }
public:
    /**
     *  Construct a connectionopenokframe from a received frame
     *
     *  @param  frame   received frame
     */
    ConnectionOpenOKFrame(ReceivedFrame &frame) :
        ConnectionFrame(frame),
        _deprecatedKnownHosts(frame)
    {}

    /**
     *  Construct a connectionopenokframe
     *
     */
    ConnectionOpenOKFrame() :
        ConnectionFrame(1), // for the deprecated shortstring
        _deprecatedKnownHosts("")
    {}

    /**
     *  Destructor
     */
    virtual ~ConnectionOpenOKFrame() {}

    /**
     *  Method id
     *  @return uint16_t
     */
    virtual uint16_t methodID() const override
    {
        return 41;
    }

    /**
     *  Process the frame
     *  @param  connection      The connection over which it was received
     *  @return bool            Was it succesfully processed?
     */
    virtual bool process(ConnectionImpl *connection) override
    {
        // all is ok, mark the connection as connected
        connection->setConnected();
        
        // done
        return true;
    }
};

/**
 *  end namespace
 */
}

