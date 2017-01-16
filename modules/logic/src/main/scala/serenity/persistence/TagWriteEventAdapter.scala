package serenity.persistence

import akka.persistence.journal.{Tagged, WriteEventAdapter}
import serenity.users.UserProtocol.write.{HospesUserImportEvt, UserRegisteredEvt}

class TagWriteEventAdapter extends WriteEventAdapter {
  override def manifest(event: Any): String = ""

  override def toJournal(event: Any): Any = event match {
    case evt@(
        _: HospesUserImportEvt |
        _: UserRegisteredEvt
        ) =>
      Tagged(evt, Set(Tags.USER_EMAIL))
    case evt@_ => evt
  }

}
