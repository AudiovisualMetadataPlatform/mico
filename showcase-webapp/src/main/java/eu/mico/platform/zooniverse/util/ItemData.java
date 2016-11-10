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

package eu.mico.platform.zooniverse.util;

import java.util.Collections;
import java.util.Map;

/**
 * @author Horst Stadler (horst.stadler@salzburgresearch.at)
 * @since 17.05.2016
 */

public class ItemData {

    private final Map<String, Object> rawItemData;

    public ItemData(Map<String, Object> rawItemData) {
        this.rawItemData = rawItemData;
    }

    public boolean hasFinished() {
        if (((String)this.rawItemData.get("finished")).trim().equalsIgnoreCase("false"))
            return false;
        return true;
    }

    public boolean hasErrors() {
        if (((String)this.rawItemData.get("hasError")).trim().equalsIgnoreCase("false"))
            return false;
        return true;
    }

    public Map<String, Object> getData() {
        return Collections.unmodifiableMap(this.rawItemData);
    }

}
