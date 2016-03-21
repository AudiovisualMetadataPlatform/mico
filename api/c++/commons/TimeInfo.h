#ifndef __TIME_INFO_H__
#define __TIME_INFO_H__

#include <time.h>

namespace mico {
namespace commons {

class TimeInfo {

public:
    // helper function to get time stamp
    static std::string getTimestamp() {
      time_t now;
      time(&now);
      char buf[sizeof "2016-03-17T09:42:09Z"];
      strftime(buf, sizeof buf, "%FT%TZ", gmtime(&now));
      return std::string(buf);
    }
};

}}

#endif
