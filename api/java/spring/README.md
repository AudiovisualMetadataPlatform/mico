# MICO Platform Spring Bindings

The MICO Platform Spring Bindings support a convenient access to the MICO platform. It contains annotations for automatically (un)registering new analysis services and a custom namepsace for the Spring configuration.

## Dependencies
You only need the Spring Bindings to communicate with the MICO platform.

    <dependency>
        <groupId>eu.mico-project.platform</groupId>
        <artifactId>api-spring</artifactId>
        <version>1.0.0-SNAPSHOT</version>
    </dependency>
    
## Configuration
You can configure the MICO platform connection in your Spring Application context. At startup, all correctly annotated analyser in your component scan are automatically registered at the MICO platform. 

    <?xml version="1.0" encoding="UTF-8"?>
    <beans xmlns="http://www.springframework.org/schema/beans"
        xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
        xmlns:context="http://www.springframework.org/schema/context"
        xmlns:mico="http://www.mico-project.eu/schema/spring/platform"
        xsi:schemaLocation="http://www.springframework.org/schema/context http://www.springframework.org/schema/context/spring-context-3.2.xsd
           				http://www.mico-project.eu/schema/spring/platform http://www.mico-project.eu/schema/spring/platform-1.0.xsd">

        <context:component-scan base-package="org.mico.project.uop.playground.service" />
        <mico:mico-platform id="mico" host="192.168.56.102" />
    </beans>
    
## Analyser
An analyser has to be annotated with the @AnalysisService tag and implement the Analyser interface.

    @AnalysisService(id = "http://www.mico-project.eu/playground/newAnalyser", provides = "text/xml", requires = "image/jpeg")
    public class NewAnalyser implements Analyser {

        @Override
        public void call(AnalysisResponse analysisResponse, ContentItem contentItem, URI uri) {
            //... analyser implementation
        }
    }

## Access to EventManager and PersistenceService
To get access to the EventManager or PersistenceService, just @Autowire your spring bean with PlatformConfiguration. The PlatformConfiguration has methods to get the EventManager or PersistenceService. 

    public class MyBean {
        @Autowired
        private PlatformConfiguration platformConfiguration;

        public void doSomething() {
            EventManager eventManager = platformConfiguration.getEventManager();
            PersistenceService persistenceService = platformConfiguration.getPersistenceService();
            // ...
        }
    
        public PlatformConfiguration getPlatformConfiguration() {
            return platformConfiguration;
        }

        public void setPlatformConfiguration(PlatformConfiguration platformConfiguration) {
            this.platformConfiguration = platformConfiguration;
        }
    }
