package io.syndesis.qe.debug;

import lombok.extern.slf4j.Slf4j;
import net.bytebuddy.build.Plugin;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.DynamicType;

@Slf4j
public class AnnotationPlugin implements Plugin {

    @Override
    public DynamicType.Builder<?> apply(DynamicType.Builder<?> builder, TypeDescription typeDescription) {
        return null;
    }

    @Override
    public boolean matches(TypeDescription target) {
        log.info("Plugin target: {}", target);
        return false;
    }
}
