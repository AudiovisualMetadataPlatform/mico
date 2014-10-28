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
 *  AmqpFlags.h
 *
 *  The various flags that are supported
 *
 *  @copyright 2014 Copernica BV
 */
 
/**
 *  Set up namespace
 */
namespace AMQP {

/**
 *  All bit flags
 *  @var int
 */
extern const int durable;
extern const int autodelete;
extern const int active;
extern const int passive;
extern const int ifunused;
extern const int ifempty;
extern const int global;
extern const int nolocal;
extern const int noack;
extern const int exclusive;
extern const int mandatory;
extern const int immediate;
extern const int redelivered;
extern const int multiple;
extern const int requeue;

/**
 *  End of namespace
 */
}

