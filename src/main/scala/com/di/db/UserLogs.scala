package com.di.db

import java.time.LocalDateTime

case class UserLogs(id: String,
                    hostname: String,
                    addedTime: LocalDateTime)

object UserLogs {
  val idDb = "id"
  val hostnameDb = "hostname"
  val addedTimeDb = "added_time"
  val quotaReservedDb = "quota_reserved"
}
