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
#pragma once
/**
 *  Watchable.h
 *
 *  Every class that overrides from the Watchable class can be monitored for
 *  destruction by a Monitor object
 *
 *  @copyright 2014 Copernica BV
 */

/**
 *  Set up namespace
 */
namespace AMQP {

/**
 *  Class definition
 */
class Watchable
{
private:
    /**
     *  The monitors
     *  @var set
     */
    std::set<Monitor*> _monitors;

    /**
     *  Add a monitor
     *  @param  monitor
     */
    void add(Monitor *monitor)
    {
        _monitors.insert(monitor);
    }
    
    /**
     *  Remove a monitor
     *  @param  monitor
     */
    void remove(Monitor *monitor)
    {
        _monitors.erase(monitor);
    }

public:
    /**
     *  Destructor
     */
    virtual ~Watchable();
    
    /**
     *  Only a monitor has full access
     */
    friend class Monitor;
};     

/**
 *  End of namespace
 */
}
