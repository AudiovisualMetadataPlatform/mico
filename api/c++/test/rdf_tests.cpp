#include "gtest.h"

std::string mico_host;
std::string mico_user;
std::string mico_pass;

int main(int argc, char **argv) {
    ::testing::InitGoogleTest(&argc, argv);
    if(argc == 4) {
        mico_host = argv[1];
        mico_user = argv[2];
        mico_pass = argv[3];

        std::cout << "running tests on " << mico_user << "@" << mico_host << std::endl;

        return RUN_ALL_TESTS();
    } else {
        std::cerr << "usage: <testcmd> <host> <user> <password>" << std::endl;
    }

}
