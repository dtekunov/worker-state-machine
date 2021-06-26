package com.di.fileReaders

abstract class AbstractFileReader(filename: String) {

  def readOne: String

  def readMany(num: String): Vector[String]

}
