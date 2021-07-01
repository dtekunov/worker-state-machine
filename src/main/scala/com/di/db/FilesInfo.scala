package com.di.db

case class FilesInfo(filename: String, isDeleted: Boolean)

object FilesInfo {
  final val filenameDbFieldName: String = "filename"
  final val isDeletedDbFieldName: String = "is_deleted"
}
