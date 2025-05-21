package uk.gov.justice.hmpps.probationteams

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.security.SecureRandom

class CodeQLTest {

  @Test
  fun `Should cause CodeQL to find an issue`() {
    val badPrng = SecureRandom()


    // BAD: Using a constant value as a seed for a random number generator means all numbers it generates are predictable.
    badPrng.setSeed(12345L)
    var randomData = badPrng.nextInt(32)
    // Just to stop warning message
    assertThat(randomData).isNotNull()

    // BAD: System.currentTimeMillis() returns the system time which is predictable.
    badPrng.setSeed(System.currentTimeMillis())
    randomData = badPrng.nextInt(32)
    // Just to stop warning message
    assertThat(randomData).isNotNull()

    // GOOD: SecureRandom implementations seed themselves securely by default.
    val goodPrng = SecureRandom()
    randomData = goodPrng.nextInt(32)

    // Just to stop warning message
    assertThat(randomData).isNotNull()
  }
}