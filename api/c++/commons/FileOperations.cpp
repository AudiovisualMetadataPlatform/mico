
#include "FileOperations.h"

#include <string>
#include <vector>
#include <map>
#include <iostream>

#include <boost/filesystem/operations.hpp>
#include <boost/filesystem/fstream.hpp>
#include <boost/regex.hpp>
#define BOOST_FILESYSTEM_NO_DEPRECATED

namespace fs=boost::filesystem;

namespace mico {
namespace commons {

using namespace std;

bool FileOperations::findFile( const std::string & dir_path,
                               const std::string & file_name,
                               std::string& path_found )
{
  boost::regex expression(file_name);
  boost::system::error_code ec;

  if ( !exists( fs::path(dir_path) ) ) return false;
  fs::directory_iterator end_itr; // default construction yields past-the-end
  for ( fs::directory_iterator itr( dir_path, ec);
        itr != end_itr;
        itr.increment(ec) )
  {
    if (ec) {
      std::cerr << "directory not accessible." << std::endl;
      continue;
    }

    if ( fs::is_directory(itr->status(ec)) )
    {
      if (ec) {
        std::cerr << "directory not accessible." << std::endl;
        continue;
      }
      if ( findFile( itr->path().string(), file_name, path_found ) ) return true;
    }
    //else if ( itr->path().filename() == file_name )
    else if (boost::regex_match(itr->path().filename().string(), expression) )
    {
      path_found = itr->path().string();
      return true;
    }
  }
  return false;
}

std::map<std::string,std::string> FileOperations::findFiles(
      const std::vector<std::string>& fileList,
      const std::vector<std::string>& searchPathList)

{
  std::map<std::string,std::string> results;
  std::vector<std::string>::const_iterator fIt;
  std::vector<std::string>::const_iterator pIt;

  for (fIt = fileList.begin(); fIt != fileList.end(); fIt++)
  {
    for (pIt = searchPathList.begin(); pIt != searchPathList.end(); pIt++)
    {
      std::string result;

      if (findFile(*pIt,*fIt, result)) {
        results[*fIt] = result;
        break;
      }
    }
  }
  return results;
}

}
}

