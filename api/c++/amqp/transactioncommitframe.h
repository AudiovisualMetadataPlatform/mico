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
class TransactionCommitFrame : public TransactionFrame
{
public:
    /**
     *  Destructor
     */
    virtual ~TransactionCommitFrame() {}

    /**
     * Construct a transaction commit frame
     * 
     * @param   channel     channel identifier
     * @return  newly created transaction commit frame
     */
    TransactionCommitFrame(uint16_t channel) : 
        TransactionFrame(channel, 0)
    {}

    /**
     *  Constructor based on incoming data
     *  @param  frame   received frame
     */
    TransactionCommitFrame(ReceivedFrame &frame) :
        TransactionFrame(frame)
    {}

    /**
     *  return the method id
     *  @return uint16_t
     */
    virtual uint16_t methodID() const override
    {
        return 20;
    }  
};

/**
 *  end namespace
 */
}

