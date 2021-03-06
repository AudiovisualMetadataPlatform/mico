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

package eu.mico.platform.anno4j.model;


import com.github.anno4j.annotations.Partial;
import com.github.anno4j.model.impl.ResourceObjectSupport;

/**
 * This class implements the ItemMMM interface and adds additional functionality.
 */
@Partial
public abstract class ItemMMMSupport extends ResourceObjectSupport implements ItemMMM {

    @Override
    /**
     * {@inheritDoc}
     */
    public void addPart(PartMMM part) {
        getParts().add(part);
    }

    @Override
    /**
     * {@inheritDoc}
     */
    public void setSerializedAt(int year, int month, int day, int hours, int minutes, int seconds) {

        StringBuilder builder = new StringBuilder();
        builder.append(Integer.toString(year)).append("-").
                append(Integer.toString(month)).append("-").
                append(Integer.toString(day)).append("T");

        if(hours < 10) {
            builder.append(0);
        }
        builder.append(Integer.toString(hours));

        builder.append(":");

        if(minutes < 10) {
            builder.append(0);
        }
        builder.append(Integer.toString(minutes));

        builder.append(":");

        if(seconds < 10) {
            builder.append(0);
        }
        builder.append(Integer.toString(seconds));

        builder.append("Z");

        this.setSerializedAt(builder.toString());
    }
}
