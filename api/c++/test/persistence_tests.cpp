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
