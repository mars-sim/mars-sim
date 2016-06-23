/* Mars Simulation Project
 * ThymeleafConfiguration.java
 * @version 3.1.0 2016-06-22
 * @author Manny Kung
 * $LastChangedDate$
 * $LastChangedRevision$
 */
package org.mars_sim.msp.ui.springboot;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.thymeleaf.ThymeleafProperties;
import org.springframework.boot.autoconfigure.web.WebMvcAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.ViewResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.spring4.SpringTemplateEngine;
import org.thymeleaf.spring4.templateresolver.SpringResourceTemplateResolver;
import org.thymeleaf.spring4.view.ThymeleafViewResolver;
import org.thymeleaf.templateresolver.ITemplateResolver;
import org.thymeleaf.templateresolver.UrlTemplateResolver;
//import org.thymeleaf.templatemode.TemplateMode;

// see http://stackoverflow.com/questions/37439369/spring-boot-and-thymeleaf-3-0-0-release-integration

@Configuration
@EnableConfigurationProperties(ThymeleafProperties.class)
@ConditionalOnClass(SpringTemplateEngine.class)
@AutoConfigureAfter(WebMvcAutoConfiguration.class)

public class ThymeleafConfiguration extends WebMvcConfigurerAdapter implements ApplicationContextAware {

    private ApplicationContext applicationContext;

    @Autowired
    ThymeleafProperties properties;

    public void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Bean
    public ViewResolver viewResolver() {
        ThymeleafViewResolver resolver = new ThymeleafViewResolver();
        resolver.setTemplateEngine((SpringTemplateEngine) templateEngine());
        resolver.setCharacterEncoding("UTF-8");
        return resolver;
    }

    private TemplateEngine templateEngine() {
        SpringTemplateEngine engine = new SpringTemplateEngine();
        engine.addTemplateResolver(urlTemplateResolver());
        engine.addTemplateResolver(templateResolver());
        // pre-initialize the template engine by getting the configuration.  It's a side-effect.
        engine.getConfiguration();
        return engine;
    }

    private ITemplateResolver templateResolver() {
        SpringResourceTemplateResolver resolver = new SpringResourceTemplateResolver();
        resolver.setApplicationContext(applicationContext);
        //resolver.setPrefix("classpath:templates/AdminLTE-2.3.3/");
        resolver.setPrefix("classpath:/templates/");
        //resolver.setPrefix("/WEB-INF/templates/");
        //resolver.setSuffix(".html");
        //resolver.setTemplateMode(TemplateMode.HTML);
        resolver.setCacheable(properties.isCache());
        return resolver;
    }
    

    private UrlTemplateResolver urlTemplateResolver() {
        return new UrlTemplateResolver();
    }

}