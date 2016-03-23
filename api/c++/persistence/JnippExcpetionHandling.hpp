#ifndef JNI_EXCEPTION_HANDLING_H
#define JNI_EXCEPTION_HANDLING_H

#include <exception>
#include <vector>
#include <anno4cpp.h>

namespace mico {
    namespace persistence {

    static bool checkJavaExcpetionNoThrow(std::vector<std::string> exceptionNames, std::string& error_msg)
    {
      bool failure = false;
      error_msg.clear();
      while (jnipp::Env::hasException()) {
        jnipp::LocalRef<JavaLangException> ex =  jnipp::Env::getException();
        ex->printStackTrace();
        for(auto exceptionName = exceptionNames.begin();exceptionName != exceptionNames.end(); exceptionName++)
          if (ex->getClass()->getName()->std_str().compare(exceptionName->c_str()) == 0) {
            error_msg += ex->getClass()->getName()->std_str() + "(msg: " + ex->getMessage()->std_str() + "), ";
            failure = true;
          }
      }
      return failure;
    }

    static bool checkJavaExcpetionNoThrow(std::string& error_msg)
    {
        bool failure = false;
        error_msg.clear();
        while (jnipp::Env::hasException()) {
            failure = true;
            jnipp::LocalRef<JavaLangException> ex =  jnipp::Env::getException();
            ex->printStackTrace();
            error_msg += ex->getClass()->getName()->std_str() + "(msg: " + ex->getMessage()->std_str();
            error_msg += "), ";
        }       
        return failure;
    }

    static void checkJavaExcpetionThrow() {
        std::string msg;

        if (checkJavaExcpetionNoThrow(msg))
            throw std::runtime_error(msg);
    }

    static bool checkJavaExcpetionThrow(std::vector<std::string> exceptionNames)
    {
      std::string msg;

      if (checkJavaExcpetionNoThrow(exceptionNames, msg))
        throw std::runtime_error(msg);
    }


}}


#endif
