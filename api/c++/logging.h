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
#ifndef HAVE_LOGGING_H
#define HAVE_LOGGING_H

#define BOOST_LOG_DYN_LINK 1

#include <boost/log/core.hpp>
#include <boost/log/trivial.hpp>
#include <boost/log/expressions.hpp>

#define LOG_DEBUG BOOST_LOG_TRIVIAL(debug)
#define LOG_INFO  BOOST_LOG_TRIVIAL(info)
#define LOG_WARN  BOOST_LOG_TRIVIAL(warn)
#define LOG_ERROR BOOST_LOG_TRIVIAL(error)

#endif