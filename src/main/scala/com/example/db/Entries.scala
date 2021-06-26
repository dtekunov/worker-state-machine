package com.example.db

case class Entries(authEntry: String,
                   hostname: String,
                   isAdmin: Boolean,
                   actualQuota: Double)

object Entries {
  final val authEntryDbFieldName = "auth_entry"
  final val hostnameDbFieldName = "hostname"
  final val isAdminDbFieldName = "is_admin"
  final val actualQuotaDbFieldName = "actual_quota"
}