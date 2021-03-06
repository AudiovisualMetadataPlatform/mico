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

import org.openrdf.annotations.Iri;

import com.github.anno4j.model.namespaces.DCTERMS;

import eu.mico.platform.anno4j.model.namespaces.FAM;

@Iri(FAM.LANGUAGE_ANNOTATION)
public interface LanguageBody extends FAMBody {

    @Iri(DCTERMS.NS + "language")
    public String getLanguage();
    
    @Iri(DCTERMS.NS + "language")
    public void setLanguage(String language);
}
