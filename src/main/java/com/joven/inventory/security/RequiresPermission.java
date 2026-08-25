package com.joven.inventory.security;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to enforce bitwise permission checks on controller methods or classes.
 * When applied, the current user's access rights are verified against the specified
 * permission constants before the method is executed.
 *
 * <p>By default, the user needs at least one of the specified permissions (OR logic).
 * Set {@code requireAll = true} to require all specified permissions (AND logic).
 *
 * <p>Usage example:
 * <pre>
 *     &#64;RequiresPermission(Permission.MANAGE_ITEMS)
 *     public ResponseEntity&lt;...&gt; createItem(...) { ... }
 *
 *     &#64;RequiresPermission(value = {Permission.VIEW_ITEMS, Permission.VIEW_STOCK}, requireAll = true)
 *     public ResponseEntity&lt;...&gt; getItemWithStock(...) { ... }
 * </pre>
 *
 * @author Joven Q. Divinagracia Jr.
 * @see PermissionAspect
 * @see Permission
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface RequiresPermission {

    /**
     * One or more permission constants to check against the user's access rights.
     *
     * @return the required permission values
     */
    long[] value();

    /**
     * Whether all specified permissions are required (AND logic).
     * Defaults to false, meaning any single permission grants access (OR logic).
     *
     * @return true if all permissions are required, false if any suffices
     */
    boolean requireAll() default false;
}
