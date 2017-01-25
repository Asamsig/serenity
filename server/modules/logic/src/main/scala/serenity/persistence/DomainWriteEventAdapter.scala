package serenity.persistence

import serenity.cqrs.Evt
import serenity.persistence.protobuf.ProtobufWriteEventAdapter
import serenity.users.UserWriteProtocol.{BasicAuthEvt, HospesUserImportEvt, UserRegisteredEvt}

class DomainWriteEventAdapter extends ProtobufWriteEventAdapter {

  import DomainProtobufFormats._

  override def manifest(event: Any): String = event match {
    case m:Evt => m.getClass.getSimpleName
    case _ => ""
  }

  override def toJournal(event: Any): Any = event match {
    case evt:HospesUserImportEvt =>
      serializeTagged(evt, Set(Tags.USER_EMAIL))
    case evt:UserRegisteredEvt =>
      serializeTagged(evt, Set(Tags.USER_EMAIL))
    case evt:BasicAuthEvt =>
      serializeTagged(evt)
    case evt@_ => evt
  }

}
