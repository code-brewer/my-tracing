package cn.sumpay.tracing.agent.core.plugin.match;

import net.bytebuddy.description.annotation.AnnotationDescription;
import net.bytebuddy.description.annotation.AnnotationList;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static net.bytebuddy.matcher.ElementMatchers.*;

/**
 * Match the class, which has methods with the certain annotations.
 * This is a very complex match.
 *
 * @author wusheng
 */
public class MethodAnnotationMatch implements IndirectMatch {
    private String[] annotations;

    private MethodAnnotationMatch(String[] annotations) {
        if (annotations == null || annotations.length == 0) {
            throw new IllegalArgumentException("annotations is null");
        }
        this.annotations = annotations;
    }

    @Override
    public ElementMatcher.Junction buildJunction() {
        ElementMatcher.Junction junction = null;
        for (String annotation : annotations) {
            if (junction == null) {
                junction = buildEachAnnotation(annotation);
            } else {
                junction = junction.and(buildEachAnnotation(annotation));
            }
        }
        junction = declaresMethod(junction).and(not(isInterface()));
        return junction;
    }

    @Override
    public boolean isMatch(TypeDescription typeDescription) {
        for (MethodDescription.InDefinedShape methodDescription : typeDescription.getDeclaredMethods()) {
            List<String> annotationList = new ArrayList<String>(Arrays.asList(annotations));

            AnnotationList declaredAnnotations = methodDescription.getDeclaredAnnotations();
            for (AnnotationDescription annotation : declaredAnnotations) {
                annotationList.remove(annotation.getAnnotationType().getActualName());
            }
            if (annotationList.isEmpty()) {
                return true;
            }
        }

        return false;
    }

    private ElementMatcher.Junction buildEachAnnotation(String annotationName) {
        return isAnnotatedWith(named(annotationName));
    }

    public static ClassMatch byMethodAnnotationMatch(String[] annotations) {
        return new MethodAnnotationMatch(annotations);
    }
}
