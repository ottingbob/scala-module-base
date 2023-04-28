package caskServer.api

import munit.FunSuite

class CaskServerSpec extends FunSuite {

  test("should run") {
    val obtained = "works!"
    val expected = "does it?"

    assertNotEquals(obtained, expected)
    assertEquals(1, 1)
  }
}
