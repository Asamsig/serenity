package repositories.eventsource.protobuf

import akka.persistence.journal.{Tagged, WriteEventAdapter}

import scala.reflect.ClassTag

trait ProtobufWriteEventAdapter extends WriteEventAdapter{

  protected def serializeTagged[A: ClassTag: ProtobufWriter](msg: A, tags: Set[String] = Set.empty): Tagged = {
    val proto = implicitly[ProtobufWriter[A]].write(msg)
    Tagged(proto, tags + implicitly[ClassTag[A]].runtimeClass.getSimpleName)
  }

}
