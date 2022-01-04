package com.aiskov.aws.products.commons;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.AbstractEnvironment;
import org.springframework.core.env.EnumerablePropertySource;
import org.springframework.core.env.Environment;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.stereotype.Component;

import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static java.lang.String.format;
import static java.lang.String.join;
import static java.util.Arrays.stream;

@Slf4j
@Component
public class EnvironmentLogger {

    @EventListener
    public void handleContextRefresh(ContextRefreshedEvent event) {
        Environment env = event.getApplicationContext().getEnvironment();
        StringBuilder message = new StringBuilder(format("%n------ Environment and configuration ------%n"));

        message.append(format("Active profiles: %s%n", join(", ", env.getActiveProfiles())));

        message.append(format("Properties values:%n"));

        this.propertyKeys(((AbstractEnvironment) env).getPropertySources())
                .filter(prop -> ! this.shouldNotBeVisible(prop))
                .forEach(prop -> message.append(format("    %s: %s%n", prop, env.getProperty(prop))));

        message.append("===========================================");

        log.info(message.toString());
    }

    private boolean shouldNotBeVisible(String key) {
        return key.contains("credentials") || key.contains("password");
    }

    private Stream<String> propertyKeys(MutablePropertySources sources ) {
        return StreamSupport.stream(sources.spliterator(), false)
                .filter(ps -> ps instanceof EnumerablePropertySource)
                .flatMap(ps -> stream(((EnumerablePropertySource) ps).getPropertyNames()));
    }
}