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
class TransactionRollbackFrame : public TransactionFrame
{
public:
    /**
     *  Destructor
     */
    virtual ~TransactionRollbackFrame() {}

    /**
     *  Decode a transaction rollback frame from a received frame
     *
     *  @param   frame   received frame to decode
     */
    TransactionRollbackFrame(ReceivedFrame& frame) :
        TransactionFrame(frame)
    {}

    /**
     *  Construct a transaction rollback frame
     * 
     *  @param   channel     channel identifier
     *  @return  newly created transaction rollback frame
     */
    TransactionRollbackFrame(uint16_t channel) :
        TransactionFrame(channel, 0)
    {}

    /**
     *  return the method id
     *  @return uint16_t
     */
    virtual uint16_t methodID() const override
    {
        return 30;
    }
};

/**
 *  end namespace
 */
}

