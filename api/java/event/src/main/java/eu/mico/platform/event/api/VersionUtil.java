/*
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

package eu.mico.platform.event.api;

import com.github.zafarkhaja.semver.UnexpectedCharacterException;
import com.github.zafarkhaja.semver.Version;

public class VersionUtil {

    /** check if available version fits to required version, based on semantic version string: <br>
     * major version must match <br>
     * minor version must be greater or equal <br>
     * patch version is ignored
     * @param requiredVersion
     * @param availableVersion
     * @return
     */
    public static boolean checkVersion(String requiredVersion, String availableVersion) {
        Version v1 =  buildVersion(requiredVersion);
        Version v2 =  buildVersion(availableVersion);
        if (v2.getMajorVersion()== v1.getMajorVersion()){
            return v2.greaterThanOrEqualTo(v1);
        }
        return false;
    };
    
    public static String stripPatchVersion(String version){
        Version v = buildVersion(version);
        return v.getMajorVersion()+"."+v.getMinorVersion();
    }

    public static int getMajorVersion(String version){
        Version v = buildVersion(version);
        return v.getMajorVersion();
    }
    public static int getMinorVersion(String version){
        Version v = buildVersion(version);
        return v.getMinorVersion();
    }
    public static int getPatchVersion(String version){
        Version v = buildVersion(version);
        return v.getPatchVersion();
    }
    public static String getPreReleaseVersion(String version){
        Version v = buildVersion(version);
        return v.getPreReleaseVersion();
    }

    public static String getBuildMetadata(String version){
        Version v = buildVersion(version);
        return v.getBuildMetadata();
    }

    public static String getNormalVersion(String version){
        Version v = buildVersion(version);
        return v.getNormalVersion();
    }
    private static Version buildVersion(String version) {
        try{
            return Version.valueOf(version);
        } catch (UnexpectedCharacterException e) {
            throw new IllegalArgumentException("Version string [" + version
                    + "] does not fit to semantic versioning");
        }
    }

    
}
