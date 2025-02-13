/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.kyuubi.sql.sqlclassification

import java.io.File

import com.fasterxml.jackson.databind.{JsonNode, ObjectMapper}
import org.apache.spark.sql.internal.SQLConf

import org.apache.kyuubi.sql.KyuubiSQLConf._

/**
 * This object is used for getting sql_classification by the logicalPlan's simpleName.
 * When the configuration item: SQL_CLASSIFICATION_ENABLED is on,
 * we will load the rule from sql-classification-default.json.
 */
object KyuubiGetSqlClassification {

  private val jsonNode: Option[JsonNode] = {
    SQLConf.get.getConf(SQL_CLASSIFICATION_ENABLED) match {
      case true =>
        try {
          val defaultSqlClassificationFile =
            Thread.currentThread().getContextClassLoader
              .getResource("sql-classification-default.json").getPath
          val objectMapper = new ObjectMapper
          Some(objectMapper.readTree(new File(defaultSqlClassificationFile)))
        } catch {
          case e: Exception =>
            throw new IllegalArgumentException("sql-classification-default.json is not exist.", e)
        }
      case false =>
        None
    }
  }

  /**
   * Notice:
   *    You need to make sure that the configuration item: kyuubi.spark.sql.classification.enabled
    *   is true
   * @param simpleName: the analyzied_logical_plan's getSimpleName
   * @return: This sql's classification
   */
  def getSqlClassification(simpleName: String): String = {
    jsonNode.map { json =>
      val sqlClassififation = json.get(simpleName)
      if (sqlClassififation == null) {
        "undefined"
      } else {
        sqlClassififation.asText()
      }
    }.getOrElse(
      throw new IllegalArgumentException(
        "The configuration item: kyuubi.spark.sql.classification.enabled is false")
    )
  }
}
