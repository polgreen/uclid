/*
 * UCLID5 Verification and Synthesis Engine
 *
 * Copyright (c) 2017.
 * Sanjit A. Seshia, Rohit Sinha and Pramod Subramanyan.
 *
 * All Rights Reserved.
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 * 1. Redistributions of source code must retain the above copyright notice,
 *
 * this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 *
 * documentation and/or other materials provided with the distribution.
 * 3. Neither the name of the copyright holder nor the names of its
 * contributors may be used to endorse or promote products derived from this
 * software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS
 * IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * Authors: Rohit Sinha, Pramod Subramanyan

 * Statement scheduler.
 *
 */

package uclid
package lang

import com.typesafe.scalalogging.Logger

object StatementScheduler {
  lazy val logger = Logger(classOf[StatementScheduler])

  def extractId(n : Expr) : Identifier = {
    n match {
      case OperatorApplication(GetNextValueOp(), List(id : Identifier)) => id
      case _ => throw new Utils.AssertionError("Unexpected value where primed identifier was expected.")
    }
  }

  def writeSet(st : Statement, context : Scope) : Set[Identifier] = {
    st match {
      case SkipStmt() => Set.empty
      case AssertStmt(e, _) => Set.empty
      case AssumeStmt(e, _) => Set.empty
      case HavocStmt(h) => 
        h match {
          case HavocableId(id) => Set(id)
          case HavocableFreshLit(f) =>
            throw new Utils.AssertionError("Fresh literals must have been eliminated by now.")
        }
      case AssignStmt(lhss, rhss) => lhss.map(lhs => lhs.ident).toSet
      case IfElseStmt(cond, ifblock, elseblock) =>
        writeSets(ifblock, context) ++ writeSets(elseblock, context)
      case ForStmt(_, _, body) =>
        writeSets(body, context)
      case CaseStmt(bodies) =>
        bodies.flatMap(b => writeSets(b._2, context)).toSet
      case ProcedureCallStmt(id, callLhss, args) => callLhss.map(_.ident).toSet
      case ModuleCallStmt(id) =>
        val namedExprOpt = context.map.get(id)
        Utils.assert(namedExprOpt.isDefined, "Must not haven an unknown instance here: " + id.toString())
        val namedExpr = namedExprOpt.get
        Utils.assert(namedExpr.isInstanceOf[Scope.Instance], "Must be a module instance: " + id.toString())
        val instD = namedExpr.asInstanceOf[Scope.Instance].instD
        val moduleType : ModuleType = instD.modType.get.asInstanceOf[ModuleType]
        instD.outputMap.map(p => extractId(p._3)).toSet
    }
  }

  def writeSets(stmts: List[Statement], context : Scope) : (Set[Identifier]) = {
    stmts.foldLeft(Set.empty[Identifier])((acc, st) => acc ++ writeSet(st, context))
  }

  def readSet(st : Statement, context : Scope) : Set[Identifier] = {
    st match {
      case SkipStmt() => Set.empty
      case AssertStmt(e, _) => readSet(e)
      case AssumeStmt(e, _) => readSet(e)
      case HavocStmt(h) => Set.empty
      case AssignStmt(lhss, rhss) => readSets(rhss)
      case IfElseStmt(cond, ifblock, elseblock) =>
        readSet(cond) ++ readSets(ifblock, context) ++ readSets(elseblock, context)
      case ForStmt(_, _, body) =>
        readSets(body, context)
      case CaseStmt(bodies) =>
        bodies.flatMap(b => readSet(b._1) ++ readSets(b._2, context)).toSet
      case ProcedureCallStmt(_, lhss, args) =>
        readSets(args)
      case ModuleCallStmt(id) =>
        val namedExprOpt = context.map.get(id)
        Utils.assert(namedExprOpt.isDefined, "Must not haven an unknown instance here: " + id.toString())
        val namedExpr = namedExprOpt.get
        Utils.assert(namedExpr.isInstanceOf[Scope.Instance], "Must be a module instance: " + id.toString())
        val instD = namedExpr.asInstanceOf[Scope.Instance].instD
        val moduleType : ModuleType = instD.modType.get.asInstanceOf[ModuleType]
        readSets(instD.inputMap.map(p => p._3)) ++ readSets(instD.sharedVarMap.map(p => p._3))
    }
  }

  def readSets(stmts : List[Statement], context : Scope) : (Set[Identifier]) = {
    stmts.foldLeft(Set.empty[Identifier])((acc, st) => acc ++ readSet(st, context))
  }

  def readSets(es : List[Expr]) : Set[Identifier] = {
    es.foldLeft(Set.empty[Identifier])((acc, e) => acc ++ readSet(e))
  }

  def readSet(e : Expr) : Set[Identifier] = {
    e match {
      case Identifier(_) => Set.empty
      case ExternalIdentifier(_, _) => Set.empty
      case lit : Literal => Set.empty
      case Tuple(values) => readSets(values)
      case OperatorApplication(GetNextValueOp(), List(id : Identifier)) => Set(id)
      case OperatorApplication(_, es) => readSets(es)
      case ArraySelectOperation(e, index) => readSet(e) ++ readSets(index)
      case ArrayStoreOperation(e, index, value) => readSet(e) ++ readSets(index) ++ readSet(value)
      case FuncApplication(e, args) => readSet(e) ++ readSets(args)
      case Lambda(ids, expr) => readSet(expr)
    }
  }
  type StmtDepGraph = Map[Identifier, Set[Identifier]]
  def getReadWriteSets(statements : List[Statement], context : Scope) : List[(Set[Identifier], Set[Identifier])] = {
    statements.map {
      st => {
        val ins = readSet(st, context)
        val outs = writeSet(st, context)
        logger.debug("Statement: {}", st.toString())
        logger.debug("Input Dependencies: {}", ins.toString())
        logger.debug("Output Dependencies: {}", outs.toString())
        (ins, outs)
      }
    }
  }
  def addEdges(graph : StmtDepGraph, deps : List[(Set[Identifier], Set[Identifier])]) : StmtDepGraph = {
    deps.foldLeft(graph) {
      (accSt, dep) => {
        val ins = dep._1
        val outs = dep._2
        outs.foldLeft(accSt) {
          (accId, out) => {
            accId.get(out) match {
              case Some(deps) => accId + (out -> (deps ++ ins))
              case None => accId + (out -> ins)
            }
          }
        }
      }
    }
  }
}

class VariableDependencyFinderPass extends ReadOnlyPass[List[ModuleError]] {
  lazy val logger = Logger(classOf[VariableDependencyFinder])

  type T = List[ModuleError]
  override def applyOnInit(d : TraversalDirection.T, init : InitDecl, in : T, context : Scope) : T = {
    if (d == TraversalDirection.Up) { checkBlock(init.body, in, context) }
    else { in }
  }
  override def applyOnNext(d : TraversalDirection.T, next : NextDecl, in : T, context : Scope) : T = {
    if (d == TraversalDirection.Up) { checkBlock(next.body, in, context) }
    else { in }
  }
  override def applyOnIfElse(d : TraversalDirection.T, ifelse : IfElseStmt, in : T, context : Scope) : T = {
    if (d == TraversalDirection.Up && context.procedure.isEmpty) {
      checkBlock(ifelse.ifblock, in, context) ++ checkBlock(ifelse.elseblock, in, context)
    } else {
      in
    }
  }
  override def applyOnFor(d : TraversalDirection.T, forLoop : ForStmt, in : T, context : Scope) : T = {
    if (d == TraversalDirection.Up && context.procedure.isEmpty) {
      checkBlock(forLoop.body, in, context)
    } else {
      in
    }
  }
  override def applyOnCase(d : TraversalDirection.T, caseStmt : CaseStmt, in : T, context : Scope) : T = {
    if (d == TraversalDirection.Up && context.procedure.isEmpty) {
      caseStmt.body.foldLeft(in)((acc, b) => checkBlock(b._2, acc, context))
    } else {
      in
    }
  }
  def checkBlock(stmts : List[Statement], in : T, context : Scope) : T = {
    val deps = StatementScheduler.getReadWriteSets(stmts, context)
    val graph = StatementScheduler.addEdges(Map.empty, deps)
    val (writeSet, errors) = deps.foldLeft((Set.empty[Identifier], in)) {
      (acc, dep) => {
        val repeatVars = dep._2.intersect(acc._1)
        val errorsP = if (repeatVars.size > 0) {
          val repeatVarsList = repeatVars.toList
          val msg = "Multiple updates to identifier(s): " + Utils.join(repeatVarsList.map(_.toString()), ", ")
          ModuleError(msg, repeatVarsList(0).position) :: acc._2
        } else {
          acc._2
        }
        val vars = acc._1 ++ dep._2
        (vars, errorsP)
      }
    }
    isCyclic(graph, writeSet.toSeq, errors)
  }
  def isCyclic(graph : StatementScheduler.StmtDepGraph, roots : Seq[Identifier], in : T) : T = {
    def cyclicModuleError(node : Identifier, stack : List[Identifier]) : ModuleError = {
      val msg = "Cyclical dependency involving variable(s): " + Utils.join(stack.map(_.toString).toList, ", ")
      ModuleError(msg, node.position)
    }
    val errors = Utils.findCyclicDependencies(graph, roots, cyclicModuleError)
    in ++ errors
  }
}

class VariableDependencyFinder() extends ASTAnalyzer(
    "VariableDependencyFinder", new VariableDependencyFinderPass())
{
  lazy val logger = Logger(classOf[VariableDependencyFinder])

  var cyclicalDependency : Option[Boolean] = None
  override def reset() {
    in = Some(List.empty)
  }

  override def finish() {
    val errors = out.get
    if (errors.size > 0) {
      throw new Utils.ParserErrorList(errors.map(e => (e.msg, e.position)))
    }
  }
}

class StatementSchedulerPass extends RewritePass {
  lazy val logger = Logger(classOf[StatementSchedulerPass])
  override def rewriteNext(next : NextDecl, context : Scope) : Option[NextDecl] = {
    val stmts = next.body
    val deps = StatementScheduler.getReadWriteSets(stmts, context)
    val graph = StatementScheduler.addEdges(Map.empty, deps)
    val module = context.module.get
    val roots = module.vars.map(v => v._1) ++ module.outputs.map(v => v._1)
    val order = Utils.topoSort(roots, graph)
    logger.debug("Order: {}", order.toString())
    Some(next)
  }
}

class StatementScheduler extends ASTRewriter("StatementScheduler", new StatementSchedulerPass())