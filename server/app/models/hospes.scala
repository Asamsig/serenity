package models

object hospes {

  case class PersonJson(
      id: Int,
      email: String,
      firstname: Option[String],
      lastname: Option[String],
      address: Option[String],
      phonenumber: Option[String],
      locale: String,
      password_pw: String,
      password_slt: String,
      uniquieid: Option[String],
      validated: Boolean,
      timezone: String,
      superuser: Boolean,
      openidkey: Option[BigInt]
  )

  case class MembershipJson(
      id: Int,
      year: Int,
      boughtdate: String,
      bought_by_person_id: Int,
      member_person_id: Option[Int]
  )

}
