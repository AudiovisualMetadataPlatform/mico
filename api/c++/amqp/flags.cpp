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
#include "includes.h"
 
/**
 *  Set up namespace
 */
namespace AMQP {

/**
 *  All bit flags
 *  @var int
 */
const int durable      = 0x1;
const int autodelete   = 0x2;
const int active       = 0x4;
const int passive      = 0x8;
const int ifunused     = 0x10;
const int ifempty      = 0x20;
const int global       = 0x40;
const int nolocal      = 0x80;
const int noack        = 0x100;
const int exclusive    = 0x200;
const int nowait       = 0x400;
const int mandatory    = 0x800;
const int immediate    = 0x1000;
const int redelivered  = 0x2000;
const int multiple     = 0x4000;
const int requeue      = 0x8000;

/**
 *  End of namespace
 */
}

