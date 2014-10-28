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
 *  Internal helper class that checks if there is enough room left in the frame
 */
class FrameCheck
{
private:
    /**
     *  The frame
     *  @var ReceivedFrame
     */
    ReceivedFrame *_frame;
    
    /**
     *  The size that is checked
     *  @var size_t
     */
    size_t _size;
    
public:
    /**
     *  Constructor
     *  @param  frame
     *  @param  size
     */
    FrameCheck(ReceivedFrame *frame, size_t size) : _frame(frame), _size(size)
    {
        // no problem is there are still enough bytes left
        if (frame->_buffer.size() - frame->_skip >= size) return;
        
        // frame buffer is too small
        throw ProtocolException("frame out of range");
    }
    
    /**
     *  Destructor
     */
    virtual ~FrameCheck()
    {
        // update the number of bytes to skip
        _frame->_skip += _size;
    }
};

/**
 *  End namespace
 */
}

