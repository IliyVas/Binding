package Annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by tt on 24.08.14.
 */
@Retention(value= RetentionPolicy.RUNTIME)
@Target(value= ElementType.FIELD)
public @interface ManyToOne {
    //TODO: получать эту информацию из метаинформации
    String associatedEntity() default "";
    String fkColumnName() default "";
}
