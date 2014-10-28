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
class Frame
{
protected:
    /**
     *  Protected constructor to ensure that no objects are created from
     *  outside the library
     */
    Frame() {}

public:
    /**
     *  Destructor
     */
    virtual ~Frame() {}

    /**
     *  return the total size of the frame
     *  @return uint32_t
     */
    virtual uint32_t totalSize() const = 0;

    /**
     *  Fill an output buffer
     *  @param  buffer
     */
    virtual void fill(OutBuffer &buffer) const = 0;

    /**
     *  Is this a frame that is part of the connection setup?
     *  @return bool
     */
    virtual bool partOfHandshake() const { return false; }

    /**
     *  Is this a frame that is part of the connection close operation?
     *  @return bool
     */
    virtual bool partOfShutdown() const { return false; }

    /**
     *  Does this frame need an end-of-frame seperator?
     *  @return bool
     */
    virtual bool needsSeparator() const { return true; }

    /**
     *  Is this a synchronous frame?
     *
     *  After a synchronous frame no more frames may be
     *  sent until the accompanying -ok frame arrives
     */
    virtual bool synchronous() const { return false; }

    /**
     *  Retrieve the buffer in AMQP wire-format for
     *  sending over the socket connection
     */
    OutBuffer buffer() const
    {
        // we need an output buffer
        OutBuffer buffer(totalSize());

        // fill the buffer
        fill(buffer);

        // append an end of frame byte (but not when still negotiating the protocol)
        if (needsSeparator()) buffer.add((uint8_t)206);

        // return the created buffer
        return buffer;
    }

    /**
     *  Process the frame
     *  @param  connection      The connection over which it was received
     *  @return bool            Was it succesfully processed?
     */
    virtual bool process(ConnectionImpl *connection)
    {
        // this is an exception
        throw ProtocolException("unimplemented frame");

        // unreachable
        return false;
    }
};

/**
 *  End of namespace
 */
}

