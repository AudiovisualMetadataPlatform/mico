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
 *  Include guard
 */
#pragma once

/**
 *  Namespace
 */
namespace AMQP {

/**
 *  Class definition
 */
class Buffer
{
public:
    /**
     *  Total size of the buffer
     *  @return size_t
     */
    virtual size_t size() const = 0;

    /**
     *  Get access to a single byte
     * 
     *  No safety checks are necessary: this method will only be called
     *  for bytes that actually exist
     * 
     *  @param  pos         position in the buffer
     *  @return char        value of the byte in the buffer
     */
    virtual char byte(size_t pos) const = 0;
    
    /**
     *  Get access to the raw data
     *  @param  pos         position in the buffer
     *  @param  size        number of continuous bytes
     *  @return char*
     */
    virtual const char *data(size_t pos, size_t size) const = 0;
    
    /**
     *  Copy bytes to a buffer
     * 
     *  No safety checks are necessary: this method will only be called
     *  for bytes that actually exist
     * 
     *  @param  pos         position in the buffer
     *  @param  size        number of bytes to copy
     *  @param  buffer      buffer to copy into
     *  @return void*       pointer to buffer
     */
    virtual void *copy(size_t pos, size_t size, void *buffer) const = 0;

};

/**
 *  End of namespace
 */
}


