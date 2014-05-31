package Annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by TTyLLIKa on 30.05.2014.
 */
@Retention(value= RetentionPolicy.RUNTIME)
@Target(value= ElementType.FIELD)
public @interface Column {
    String name() default "";
}
