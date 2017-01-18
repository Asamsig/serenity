package auth

import org.scalatest.{FunSpec, Matchers}

class HospesPasswordHasherTest extends FunSpec with Matchers {

  describe("HospesPassword") {
    val hasher = new HospesPasswordHasher()
    it("should validate") {
      val plan_pwd = "thi$Is@wsome"
      val pwdInfo = hasher.hash(plan_pwd)

      hasher.matches(pwdInfo, plan_pwd) should be(true)
    }

    it("should not validate") {
      val plan_pwd = "thi$Is@wsome"
      val pwdInfo = hasher.hash(plan_pwd)

      hasher.matches(pwdInfo, "NotTh@SamePwd") should be(false)
    }
  }
}
