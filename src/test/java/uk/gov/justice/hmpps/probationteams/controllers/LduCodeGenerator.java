package uk.gov.justice.hmpps.probationteams.controllers;


import java.util.concurrent.atomic.AtomicInteger;

class LduCodeGenerator {
    private static final String LDU_CODE_PREFIX = "ABC_X_";
    private static final AtomicInteger counter = new AtomicInteger();

    String lduCode() {
        return LDU_CODE_PREFIX + counter.incrementAndGet();
    }
}
