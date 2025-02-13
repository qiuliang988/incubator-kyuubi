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

package org.apache.kyuubi.sql.zorder

import org.apache.spark.sql.catalyst.analysis.{UnresolvedAttribute, UnresolvedRelation}
import org.apache.spark.sql.catalyst.expressions.{Ascending, Attribute, Expression, NullsLast, SortOrder}
import org.apache.spark.sql.catalyst.plans.logical.{Filter, LogicalPlan, Sort, UnaryNode}

case class OptimizeZorderCommand(child: LogicalPlan) extends UnaryNode {
  override def output: Seq[Attribute] = child.output
}

object OptimizeZorderCommand {

  def apply(tableIdent: Seq[String],
            whereExp: Option[Expression],
            sortArr: Seq[UnresolvedAttribute]): OptimizeZorderCommand = {
    val table = UnresolvedRelation(tableIdent)
    val child = whereExp match {
      case Some(x) => Filter(x, table)
      case None => table
    }

    val sortOrder = SortOrder(Zorder(sortArr), Ascending, NullsLast, Seq.empty)
    val zorderSort = Sort(Seq(sortOrder), true, child)
    OptimizeZorderCommand(zorderSort)
  }
}
