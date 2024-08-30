package email.quass.qmail.service;

import email.quass.qmail.service.config.QMailServiceConfiguration;
import email.quass.qmail.service.filters.CorsFilter;
import email.quass.qmail.service.filters.SessionFilter;
import email.quass.qmail.service.resources.InboxResource;
import email.quass.qmail.service.resources.LoginResource;
import io.dropwizard.core.Application;
import io.dropwizard.core.setup.Environment;

public class QMailServiceApplication extends Application<QMailServiceConfiguration> {

    public static void main(String[] args) throws Exception {
        new QMailServiceApplication()
                .run(args);
    }

    @Override
    public void run(QMailServiceConfiguration configuration, Environment environment) {
        environment.jersey().setUrlPattern("/api");

        environment.jersey().register(CorsFilter.class);
        environment.jersey().register(SessionFilter.class);

        environment.jersey().register(new LoginResource());
        environment.jersey().register(new InboxResource());
    }
}
