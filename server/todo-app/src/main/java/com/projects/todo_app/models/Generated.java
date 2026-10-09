package com.projects.todo_app.models;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

// Marks IDE-generated boilerplate (equals/hashCode) so JaCoCo leaves it out of coverage reports.
// JaCoCo skips any method whose annotation name contains "Generated", provided the annotation
// is kept in the .class file (CLASS or RUNTIME retention).
@Retention(RetentionPolicy.CLASS)
@Target(ElementType.METHOD)
public @interface Generated {
}
