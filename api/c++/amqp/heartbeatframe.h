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
class HeartbeatFrame : public ExtFrame 
{
public:
    /**
     *  Construct a heartbeat frame
     *
     *  @param  channel     channel identifier
     *  @param  payload     payload of the body
     */
    HeartbeatFrame() :
        ExtFrame(0, 0)
    {}

    /**
     *  Decode a received frame to a frame
     *
     *  @param  frame   received frame to decode
     *  @return shared pointer to newly created frame
     */
    HeartbeatFrame(ReceivedFrame& frame) :
        ExtFrame(frame)
    {}

    /**
     *  Destructor
     */
    virtual ~HeartbeatFrame() {}

    /**
     *  Return the type of frame
     *  @return     uint8_t
     */ 
    uint8_t type() const
    {
        // the documentation says 4, rabbitMQ sends 8
        return 8;
    }

    /**
     *  Process the frame
     *  @param  connection      The connection over which it was received
     *  @return bool            Was it succesfully processed?
     */
    virtual bool process(ConnectionImpl *connection) override
    {
        // send back the same frame
        connection->send(*this);

        // done
        return true;
    }
};

/**
 *  end namespace
 */
}
