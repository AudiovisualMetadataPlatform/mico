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

package eu.mico.platform.anno4j.model.fam;

import java.util.HashSet;
import org.openrdf.model.Literal;
import org.openrdf.model.Resource;
import org.openrdf.repository.object.LangString;

import com.github.anno4j.annotations.Partial;

@Partial
public abstract class TopicBodySupport extends FAMBodySupport implements TopicBody {

    
    @Override
    public void addTopicLabel(LangString name) {
        if(name == null){
            return;
        }
        if(getTopicLabels() == null){
            setTopicLabels(new HashSet<LangString>());
        }
        getTopicLabels().add(name);
    }
    
    @Override
    public void addTopicLabel(Literal label) {
        if(label != null){
            addTopicLabel(new LangString(label.stringValue(), label.getLanguage()));
        }
        
    }

    @Override
    public void setTopicUri(String reference) {
        setTopic(reference == null ? null : new Reference(reference));
    }
    
    @Override
    public void setTopic(Resource reference) {
        setTopic(reference == null ? null : new Reference(reference));
    }

}
