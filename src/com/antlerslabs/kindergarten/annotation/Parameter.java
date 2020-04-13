package com.antlerslabs.kindergarten.annotation;

import java.lang.annotation.Target;
import java.lang.annotation.Retention;
import java.lang.annotation.ElementType;
import java.lang.annotation.RetentionPolicy;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Parameter {
	public String name() default "";
	public boolean download() default false;
	public String format() default "";
	public boolean isCharacter() default false;
}
