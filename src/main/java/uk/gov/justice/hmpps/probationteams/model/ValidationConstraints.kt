package uk.gov.justice.hmpps.probationteams.model

import javax.validation.Constraint
import javax.validation.ReportAsSingleViolation
import javax.validation.constraints.Email
import javax.validation.constraints.NotBlank
import javax.validation.constraints.Pattern
import kotlin.annotation.AnnotationRetention.RUNTIME
import kotlin.annotation.AnnotationTarget.*
import kotlin.reflect.KClass

@NotBlank()
@Pattern(regexp = "^[A-Z0-9_]+$")
@ReportAsSingleViolation
@Retention(RUNTIME)
@Target(VALUE_PARAMETER, PROPERTY, ANNOTATION_CLASS )
@Constraint(validatedBy = [])
@MustBeDocumented
annotation class ProbationAreaCode(
        val message: String = "Must be a valid Probation Area Code",
        val groups: Array<KClass<out Any>> = [],
        val payload: Array<KClass<out Any>> = []
)

@NotBlank()
@Pattern(regexp = "^[A-Z0-9_]+$")
@ReportAsSingleViolation
@Retention(RUNTIME)
@Target(VALUE_PARAMETER, PROPERTY, ANNOTATION_CLASS )
@Constraint(validatedBy = [])
@MustBeDocumented
annotation class LduCode(
        val message: String = "Must be a valid Local Delivery Unit Code",
        val groups: Array<KClass<out Any>> = [],
        val payload: Array<KClass<out Any>> = []
)

@NotBlank()
@Pattern(regexp = "^[A-Z0-9_]+$")
@ReportAsSingleViolation
@Retention(RUNTIME)
@Target(VALUE_PARAMETER, PROPERTY, ANNOTATION_CLASS )
@Constraint(validatedBy = [])
@MustBeDocumented
annotation class TeamCode(
        val message: String = "Must be a valid Probation Team Code",
        val groups: Array<KClass<out Any>> = [],
        val payload: Array<KClass<out Any>> = []
)

@NotBlank()
@Email
@ReportAsSingleViolation
@Retention(RUNTIME)
@Target(VALUE_PARAMETER, PROPERTY, ANNOTATION_CLASS )
@Constraint(validatedBy = [])
@MustBeDocumented
annotation class Email(
        val message: String = "Must be a valid email address",
        val groups: Array<KClass<out Any>> = [],
        val payload: Array<KClass<out Any>> = []
)
