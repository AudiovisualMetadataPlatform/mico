
#ifndef __FILE_OPERATIONS_H__
#define __FILE_OPERATIONS_H__

#include <string>
#include <vector>
#include <map>

namespace mico {
namespace commons {


class FileOperations {

public:

  /** @brief Searches for files in different directories
   *
   *  @param fileList List of file names to find
   *  @param searchPathList List of paths to check for each file
   *
   *  @return Map of first occurence of found files filename->location
   */
  static std::map<std::string,std::string> findFiles(
      const std::vector<std::string>& fileList,
      const std::vector<std::string>& searchPathList);

protected:

  static bool findFile( const std::string & dir_path,
                        const std::string & file_name,
                        std::string& path_found );
};

}
}

#endif
