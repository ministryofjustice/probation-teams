package uk.gov.justice.hmpps.probationteams.utils

import java.util.concurrent.atomic.AtomicInteger

private val counter = AtomicInteger()

fun uniqueLduCode(): String = "ABC_X_${counter.incrementAndGet()}"
