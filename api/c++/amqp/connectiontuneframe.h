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
class ConnectionTuneFrame : public ConnectionFrame
{
private:
    /**
     *  Proposed maximum number of channels
     *  @var uint16_t
     */
    uint16_t _channels;

    /**
     *  Proposed maximum frame size
     *  @var uint32_t
     */
    uint32_t _frameMax;

    /**
     *  Desired heartbeat delay
     *  @var uint16_t
     */
    uint16_t _heartbeat;
    
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

        // add fields
        buffer.add(_channels);
        buffer.add(_frameMax);
        buffer.add(_heartbeat);
    }
public:
    /**
     *  Construct a connection tuning frame
     *
     *  @param  channels    proposed maximum number of channels
     *  @param  frameMax    proposed maximum frame size
     *  @param  heartbeat   desired heartbeat delay
     */
    ConnectionTuneFrame(uint16_t channels, uint32_t frameMax, uint16_t heartbeat) : 
        ConnectionFrame(8), // 2x uint16_t, 1x uint32_t
        _channels(channels),
        _frameMax(frameMax),
        _heartbeat(heartbeat)
    {}

    /**
     *  Construct a connection tune frame from a received frame
     *
     *  @param  frame   received frame
     */
    ConnectionTuneFrame(ReceivedFrame &frame) :
        ConnectionFrame(frame),
        _channels(frame.nextUint16()),
        _frameMax(frame.nextUint32()),
        _heartbeat(frame.nextUint16())
    {}

    /**
     *  Destructor
     */
    virtual ~ConnectionTuneFrame() {}

    /**
     *  Method id
     *  @return uint16_t
     */
    virtual uint16_t methodID() const override
    {
        return 30;
    }

    /**
     *  Proposed maximum number of channels
     *  @return _uint16_t
     */
    uint16_t channelMax() const
    {
        return _channels;
    }

    /**
     *  Proposed maximum frame size
     *  @return _uint32_t
     */
    uint32_t frameMax() const
    {
        return _frameMax;
    }

    /**
     *  Desired heartbeat delay
     *  @return uint16_t
     */
    uint16_t heartbeat() const
    {
        return _heartbeat;
    }

    /**
     *  Process the frame
     *  @param  connection      The connection over which it was received
     *  @return bool            Was it succesfully processed?
     */
    virtual bool process(ConnectionImpl *connection) override
    {
        // remember this in the connection
        connection->setCapacity(channelMax(), frameMax());
        
        // theoretically it is possible that the connection object gets destructed between sending the messages
        Monitor monitor(connection);
        
        // send it back
        connection->send(ConnectionTuneOKFrame(channelMax(), frameMax(), heartbeat()));
        
        // check if the connection object still exists
        if (!monitor.valid()) return true;
        
        // and finally we start to open the frame
        return connection->send(ConnectionOpenFrame(connection->vhost()));
    }
};

/**
 *  end namespace
 */
}

