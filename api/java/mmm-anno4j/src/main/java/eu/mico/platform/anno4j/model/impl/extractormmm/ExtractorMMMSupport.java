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

package eu.mico.platform.anno4j.model.impl.extractormmm;

import com.github.anno4j.annotations.Partial;
import com.github.anno4j.model.impl.ResourceObjectSupport;

import java.util.HashSet;

/**
 * Support class for the ExtractorMMM class, adding functionality.
 */
@Partial
public abstract class ExtractorMMMSupport extends ResourceObjectSupport implements ExtractorMMM {

    @Override
    public void addMode(ModeMMM mode) {
        if(this.getModes() == null) {
            this.setModes(new HashSet<ModeMMM>());
        }

        this.getModes().add(mode);
    };
}
