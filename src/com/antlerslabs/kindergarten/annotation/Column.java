package com.antlerslabs.kindergarten.annotation;

import java.lang.annotation.Target;
import java.lang.annotation.Retention;
import java.lang.annotation.ElementType;
import java.lang.annotation.RetentionPolicy;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Column {
	public String name() default "";
	public boolean allowChange() default true;
	public boolean primaryKey() default false;
	public boolean autoIncrement() default false;
}
