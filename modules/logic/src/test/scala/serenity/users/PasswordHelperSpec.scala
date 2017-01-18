package serenity.users

import org.scalatest.{FunSpec, Matchers}
import serenity.users.domain.HospesAuth

class PasswordHelperSpec extends FunSpec with Matchers {

  describe("HospesPassword") {
    it("should validate") {
      val plan_pwd = "thi$Is@wsome"
      val (pwd, slt) = HospesPassword.createPasswordAndSalt(plan_pwd)

      HospesPassword.validate(HospesAuth(pwd, slt), plan_pwd) should be(true)
    }

    it("should not validate") {
      val plan_pwd = "thi$Is@wsome"
      val (pwd, slt) = HospesPassword.createPasswordAndSalt(plan_pwd)

      HospesPassword.validate(HospesAuth(pwd, slt), "NotTh@SamePwd") should be(false)
    }
  }

}
