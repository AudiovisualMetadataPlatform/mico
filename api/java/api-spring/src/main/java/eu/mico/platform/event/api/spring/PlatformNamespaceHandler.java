package eu.mico.platform.event.api.spring;

import org.springframework.beans.factory.xml.NamespaceHandlerSupport;

public class PlatformNamespaceHandler extends NamespaceHandlerSupport {

    @Override
    public void init() {
        registerBeanDefinitionParser("mico-platform", new MicoPlatformBeanDefinitionParser());
    }
}

