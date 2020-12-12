// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0.
package software.amazon.awssdk.crt.annotations;

import java.lang.String;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marker interface for generated source code. Generated source code should not be edited directly.
 */
@Documented
@Retention(RetentionPolicy.SOURCE)
@Target({ElementType.PACKAGE, ElementType.TYPE, ElementType.ANNOTATION_TYPE, ElementType.METHOD, ElementType.CONSTRUCTOR, ElementType.FIELD, ElementType.LOCAL_VARIABLE, ElementType.PARAMETER,})
public @interface Generated {
    /**
     * The value element MUST have the name of the code generator.
     * The recommended convention is to use the fully qualified name of the
     * code generator. For example: com.acme.generator.CodeGen.
     */
    String value();

    /**
     * Date when the source was generated.
     */
    String date() default "";

    /**
     * A place holder for any comments that the code generator may want to
     * include in the generated code.
     */
    String comments() default "";
}
