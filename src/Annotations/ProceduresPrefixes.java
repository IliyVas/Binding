package Annotations;

/**
 * Created by tt on 01.09.14.
 */
public @interface ProceduresPrefixes {
    String select() default "";
    String insert() default "";
    String update() default "";
    String delete() default "";
}
