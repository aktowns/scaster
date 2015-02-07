package scaster.utils

import com.typesafe.scalalogging.Logger
import org.slf4j.LoggerFactory

object Log {
  val shared = Logger(LoggerFactory.getLogger("scaster"))
}
