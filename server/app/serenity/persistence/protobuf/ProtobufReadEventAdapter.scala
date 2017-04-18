package serenity.persistence.protobuf

import akka.persistence.journal.{EventSeq, ReadEventAdapter}
import com.google.protobuf.Message

trait ProtobufReadEventAdapter extends ReadEventAdapter {

  protected def deserialize[A: ProtobufReader](proto: Message): EventSeq = {
    EventSeq.single(implicitly[ProtobufReader[A]].read(proto))
  }

}
