package com.example.db

case class Entries(authEntry: String,
                   hostname: String,
                   isAdmin: Boolean,
                   actualQuota: Double)

object Entries {
  final val authEntryDb = "auth_entry"
  final val hostnameDb = "hostname"
  final val isAdminDb = "is_admin"
  final val actualQuotaDb = "actual_quota"
}