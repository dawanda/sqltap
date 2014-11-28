// This file is part of the "SQLTap" project
//   (c) 2011-2013 Paul Asmuth <paul@paulasmuth.com>
//
// Licensed under the MIT License (the "License"); you may not use this
// file except in compliance with the License. You may obtain a copy of
// the License at: http://opensource.org/licenses/MIT

package com.paulasmuth.sqltap.mysql

import com.paulasmuth.sqltap.callbackhell.Worker
import com.paulasmuth.sqltap.ctree.CTreeCache
import com.paulasmuth.sqltap._

import com.typesafe.scalalogging.StrictLogging

/**
 * The ReplicationFeed thread is responsible only for a single SQL Connection
 * that pulls the binlog event stream from MySQL.
 */
object ReplicationFeed extends Worker with AbstractSQLConnectionPool with StrictLogging {
  //val loop = SelectorProvider.provider().openSelector()
  private val query     = new SQLQuery("SHOW BINARY LOGS;")
  //private val worker    = new Worker()

  logger.info("replication feed starting...")

  def binlog(event: BinlogEvent) : Unit = event match {
    case evt: UpdateRowsBinlogEvent => {
      if (sqlmapping.Manifest.has_table(evt.table_name)) {
        logger.debug("[Expire] table: " + evt.table_name + ", id: " + evt.primary_key)

        CTreeCache.expire(this,
          sqlmapping.Manifest.resource_name_for_table(evt.table_name),
          evt.primary_key.toInt)
      }
    }

    case _ => ()
  }

  def ready(conn: SQLConnection) : Unit = {
    if (query.running) {
      conn.execute(query)
    } else {
      val row = query.rows.last
      val position = row.last.toInt
      val filename = row.head

      conn.start_binlog(filename, position)
    }
  }

  def close(conn: SQLConnection) : Unit = {
    throw new Exception("SQL connection closed")
  }

  def busy(conn: SQLConnection) : Unit = ()

  override def run : Unit = try {
    val conn = new SQLConnection(this)
    conn.configure(Config.get() + (('binlog, "enable")))
    conn.connect()
    super.run()
  } catch {
    case e: Exception => {
      logger.error("[SQL] [Replication] exception: " + e.toString, e)
      System.exit(1)
      //conn.close(e) // FIXPAUL: reconnect
    }
  }

}
