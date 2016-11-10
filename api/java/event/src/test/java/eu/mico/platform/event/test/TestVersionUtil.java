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

package eu.mico.platform.event.test;

import static org.junit.Assert.*;

import org.junit.Test;

import eu.mico.platform.event.api.VersionUtil;

public class TestVersionUtil {

    @Test
    public void testCheckVersion() {
        String requiredVersion = "1.2.3";
        String newVersion = "1.3.4";
        String toNewVersion = "2.3.4";
        String toOldVersion = "1.1.4";
        assertTrue(VersionUtil.checkVersion(requiredVersion, requiredVersion));
        assertTrue(VersionUtil.checkVersion(requiredVersion, newVersion));
        assertFalse(VersionUtil.checkVersion(requiredVersion, toNewVersion));
        assertFalse(VersionUtil.checkVersion(requiredVersion, toOldVersion));
    }

    @Test
    public void testStripPatchVersion() {
        String requiredVersion = "1.2.3";
        String newVersion = "1.3.4";
        String rcVersion = "1.3.4-RC1";
        assertEquals("1.2",VersionUtil.stripPatchVersion(requiredVersion));
        assertEquals("1.3",VersionUtil.stripPatchVersion(newVersion));
        assertEquals("1.3",VersionUtil.stripPatchVersion(rcVersion));
    }

    @Test
    public void testVersionParts(){
        String version = "1.2.3-RC4+meta-info123";
        assertEquals(1,VersionUtil.getMajorVersion(version));
        assertEquals(2,VersionUtil.getMinorVersion(version));
        assertEquals(3,VersionUtil.getPatchVersion(version));
        assertEquals("RC4",VersionUtil.getPreReleaseVersion(version));
        assertEquals("meta-info123",VersionUtil.getBuildMetadata(version));
        assertEquals("1.2.3",VersionUtil.getNormalVersion(version));
    }
}
