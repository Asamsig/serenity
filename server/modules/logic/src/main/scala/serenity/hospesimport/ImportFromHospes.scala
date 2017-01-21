package serenity.hospesimport

import serenity.hospesimport.model.{MembershipJson, PersonJson}
import serenity.users.UserProtocol.write.{HospesImportCmd, HospesMembership, HospesUser}
import serenity.users.domain.Email


object ImportFromHospes {

  def apply(mJson: List[MembershipJson], pJson: List[PersonJson]): List[HospesImportCmd] = {
    val fm = mJson
        .filter(_.member_person_id.isDefined)
        .groupBy(_.member_person_id.getOrElse(0))
        .mapValues(_.map(m => HospesMembership(m.id, m.year)))

    val mId = fm.keys.toSet

    val fp = pJson
        //      .filter(_.validated)
        .filter(p => mId.contains(p.id))

    def cleanupPhone(v: Option[String]): Option[String] = v.map(_.replaceAll(" ", "").replace("+47", ""))

    def nonEmpty(v: Option[String]): Boolean = v match {
      case Some("") => false
      case None => false
      case _ => true
    }

    def toPerson(ps: List[PersonJson]): HospesImportCmd = {
      val sp: List[PersonJson] = ps.sortBy(_.id).reverse
      val ids = sp.map(_.id).toSet
      val memberships = fm.filter(m => ids.contains(m._1)).flatMap(_._2).toSet

      HospesImportCmd(
        HospesUser(
          sp.map(_.id),
          Email(sp.head.email, sp.head.validated) :: sp.tail.map(m => Email(m.email, m.validated)),
          sp.head.firstname,
          sp.head.lastname,
          sp.head.address,
          sp.head.phonenumber,
          sp.head.password_pw,
          sp.head.password_slt,
          memberships
        )
      )
    }

    val groupedByName = fp
        .filter(p => nonEmpty(p.firstname) || nonEmpty(p.lastname))
        .groupBy(p => s"${p.firstname.getOrElse("").toLowerCase} : ${p.lastname.getOrElse("").toLowerCase}")
        .filter(_._2.size > 1)

    val (identifiedPhone, others) =
      groupedByName.partition(_._2.map(p => cleanupPhone(p.phonenumber)).toSet.size == 1)

    val resId: Set[Int] = groupedByName.values.flatMap(_.map(_.id)).toSet

    fp.filter(p => !resId.contains(p.id)).map(f => toPerson(List(f))) :::
        identifiedPhone.map(p => toPerson(p._2)).toList :::
        others.map(p => toPerson(p._2)).toList
  }
}
