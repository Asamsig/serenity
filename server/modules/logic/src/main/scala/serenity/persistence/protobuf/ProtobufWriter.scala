package serenity.persistence.protobuf

import com.google.protobuf.Message

trait ProtobufWriter[A] {
  def write(event: A): Message
}

trait ProtobufReader[A] {
  def read(proto: Message): A
}

trait ProtobufFormat[A] extends ProtobufWriter[A] with ProtobufReader[A]

