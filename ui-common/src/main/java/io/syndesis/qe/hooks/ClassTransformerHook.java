package io.syndesis.qe.hooks;

import io.syndesis.qe.TestConfiguration;
import io.syndesis.qe.report.selector.ExcludeFromSelectorReports;

import io.cucumber.java.en.Given;
import io.cucumber.plugin.EventListener;
import io.cucumber.plugin.event.EventPublisher;
import lombok.extern.slf4j.Slf4j;
import net.bytebuddy.agent.ByteBuddyAgent;
import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.description.annotation.AnnotationDescription;
import net.bytebuddy.description.type.PackageDescription;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.implementation.SuperMethodCall;
import net.bytebuddy.matcher.ElementMatchers;

@Slf4j
public class ClassTransformerHook implements EventListener {

    private boolean shouldLoadAgent = true;

    public ClassTransformerHook() {
        transform();
    }

    private void transformSelectors() {
        lazyAgentInstall();
        /*
         The resulting agent annotates all methods loaded from apicurito testsuite with @ExcludeFromSelectorReports
         */
        new AgentBuilder.Default()
            .type(ElementMatchers.nameStartsWithIgnoreCase("apicurito.tests"))
            .transform((builder, typeDescription, classLoader, module) -> {
                if (ElementMatchers
                    .isAnnotatedWith(new TypeDescription.ForPackageDescription(new PackageDescription.ForLoadedPackage(Given.class.getPackage())))
                    .matches(typeDescription)) {
                    log.debug("Transforming {}", typeDescription.getDeclaredMethods());
                }
                return builder
                    .method(ElementMatchers.isAnnotatedWith(
                        new TypeDescription.ForPackageDescription(new PackageDescription.ForLoadedPackage(Given.class.getPackage()))))
                    .intercept(SuperMethodCall.INSTANCE)
                    .annotateMethod(AnnotationDescription.Builder.ofType(ExcludeFromSelectorReports.class).build());
            }).installOnByteBuddyAgent();
        /*
        The resulting agent changes methods annotated with @ExcludeFromSelectorReports to call ReporterPauseInterceptor#onEnter()
        And to call ReporterPauseInterceptor#onExit()
        TLDR: before the actual method is executed SelectorSnooper#pauseReporting() is called and SelectorSnooper#resumeReporting() is called after
         the method finishes
         */
    }

    private void transform() {
        if (TestConfiguration.snoopSelectors()) {
            transformSelectors();
        }
    }

    ///Install Bytebuddy agent only once
    private void lazyAgentInstall() {
        if (shouldLoadAgent) {
            ByteBuddyAgent.install();
            shouldLoadAgent = false;
        }
    }

    @Override
    public void setEventPublisher(EventPublisher publisher) {
        //NOOP all action is handled in constructor
    }
}
