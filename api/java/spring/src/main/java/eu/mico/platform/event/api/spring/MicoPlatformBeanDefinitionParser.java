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
package eu.mico.platform.event.api.spring;

import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.AbstractSingleBeanDefinitionParser;
import org.springframework.util.StringUtils;
import org.w3c.dom.Element;

/**
 * Parser for custom spring XML settings
 *
 * @author Kai Schlegel (kai.schlegel@googlemail.com)
 */
public class MicoPlatformBeanDefinitionParser extends AbstractSingleBeanDefinitionParser {

    @Override
    protected Class getBeanClass(Element element) {
        return PlatformConfiguration.class;
    }

    @Override
    protected void doParse(Element element, BeanDefinitionBuilder bean) {
        String host = element.getAttribute("host");
        if (StringUtils.hasText(host)) {
            bean.addPropertyValue("host", host);
        }

        // start the bean at startup
        bean.setLazyInit(false);
        bean.setScope("singleton");
        bean.setInitMethodName("init");
        bean.setDestroyMethodName("disconnect");
    }
}
