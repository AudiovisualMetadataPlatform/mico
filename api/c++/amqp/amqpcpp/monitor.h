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
class Monitor
{
private:
    /**
     *  The object being watched
     *  @var    Watchable
     */
    Watchable *_watchable;

    /**
     *  Invalidate the object
     */
    void invalidate()
    {
        _watchable = nullptr;
    }

public:
    /**
     *  Constructor
     *  @param  watchable
     */
    Monitor(Watchable *watchable) : _watchable(watchable)
    {
        // register with the watchable
        _watchable->add(this);
    }
    
    /**
     *  Destructor
     */
    virtual ~Monitor()
    {
        // remove from watchable
        if (_watchable) _watchable->remove(this);
    }
    
    /**
     *  Check if the object is valid
     *  @return bool
     */
    bool valid()
    {
        return _watchable != nullptr;
    }
    
    /**
     *  The watchable can access private data
     */
    friend class Watchable;
};
        


/**
 *  End of namespace
 */
}


 