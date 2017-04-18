package repositories.eventsource

import models.Evt
import repositories.eventsource.protobuf.ProtobufWriteEventAdapter
import users.UserWriteProtocol.{BasicAuthEvt, HospesUserImportEvt, MembershipUpdateEvt, UserUpdatedEvt}

class DomainWriteEventAdapter extends ProtobufWriteEventAdapter {

  import DomainProtobufFormats._

  override def manifest(event: Any): String = event match {
    case m:Evt => m.getClass.getSimpleName
    case _ => ""
  }

  override def toJournal(event: Any): Any = event match {
    case evt:HospesUserImportEvt =>
      serializeTagged(evt, Set(Tags.USER_EMAIL))
    case evt:UserUpdatedEvt =>
      serializeTagged(evt, Set(Tags.USER_EMAIL))
    case evt:BasicAuthEvt =>
      serializeTagged(evt)
    case evt:MembershipUpdateEvt =>
      serializeTagged(evt)
    case evt@_ => evt
  }

}
