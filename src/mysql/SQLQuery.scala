// This file is part of the "SQLTap" project
//   (c) 2011-2013 Paul Asmuth <paul@paulasmuth.com>
//
// Licensed under the MIT License (the "License"); you may not use this
// file except in compliance with the License. You may obtain a copy of
// the License at: http://opensource.org/licenses/MIT

package com.paulasmuth.sqltap.mysql

import com.paulasmuth.sqltap.{SQLTap,ReadyCallback}
import scala.collection.mutable.ListBuffer

class SQLQuery(query_str: String) {

  val query    : String = query_str
  var columns  = new ListBuffer[String]()
  var rows     = new ListBuffer[ListBuffer[String]]()
  var num_cols : Int  = 0
  var qtime    : Long = 0
  var callback : ReadyCallback[SQLQuery] = null

  private var tik : Long = 0
  private var tok : Long = 0

  def start() : Unit = {
    tik = System.nanoTime

    SQLTap.log_debug("Execute: " + query)
  }

  def ready() : Unit = {
    if (callback != null)
      callback.ready(this)

    tok = System.nanoTime
    qtime = tok - tik

    SQLTap.log_debug("Finished (" + (qtime / 1000000.0) + "ms): " + query)
  }

  def attach(_callback: ReadyCallback[SQLQuery]) : Unit =
    callback = _callback

}
