package repositories.eventsource

import akka.persistence.journal.EventSeq
import com.google.protobuf.Message
import models.Evt
import repositories.eventsource.protobuf.ProtobufReadEventAdapter
import users.UserWriteProtocol.{
  BasicAuthEvt,
  HospesUserImportEvt,
  MembershipUpdateEvt,
  UserUpdatedEvt
}

class DomainReadEventAdapter extends ProtobufReadEventAdapter {

  import DomainProtobufFormats._

  override def fromJournal(event: Any, manifest: String): EventSeq = event match {
    case proto: Message if manifest == classOf[UserUpdatedEvt].getSimpleName =>
      deserialize[UserUpdatedEvt](proto)
    case proto: Message if manifest == classOf[HospesUserImportEvt].getSimpleName =>
      deserialize[HospesUserImportEvt](proto)
    case proto: Message if manifest == classOf[BasicAuthEvt].getSimpleName =>
      deserialize[BasicAuthEvt](proto)
    case proto: Message if manifest == classOf[MembershipUpdateEvt].getSimpleName =>
      deserialize[MembershipUpdateEvt](proto)
    case evt: Evt =>
      EventSeq.single(evt)
    case _ =>
      throw new IllegalStateException(
        s"class [${event.getClass.getName}] with '$manifest' can't be deserialize by protobuf"
      )
  }

  def fromMessage(message: Any): Any = {
    fromJournal(message, message.getClass.getSimpleName.replace("Message", "Evt")).events.head
  }

}
