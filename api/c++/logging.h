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