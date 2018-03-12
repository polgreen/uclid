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

 * Utilities for UCLID5.
 *
 */
package uclid

import scala.util.parsing.input.Position

object Utils {
  def assert(b: Boolean, err: => String /* error may be lazily computed. */) : Unit = {
    if (!b) {
      throw new AssertionError(err)
    }
  }
  def raiseParsingError(err: => String, pos : => Position, fileName : => Option[String]) : Unit = {
    throw new ParserError(err, Some(pos), fileName)
  }
  def checkParsingError(b : Boolean, err: => String, pos : => Position, fileName : => Option[String]) : Unit = {
    if (!b) { raiseParsingError(err, pos, fileName) }
  }
  def checkError(b : Boolean, err: => String) : Unit = {
    if (!b) { throw new ParserError(err, None, None) }
  }
  class UnimplementedException (msg:String=null, cause:Throwable=null) extends java.lang.UnsupportedOperationException (msg, cause)
  class RuntimeError (msg:String = null, cause: Throwable=null) extends java.lang.RuntimeException(msg, cause)
  class EvaluationError(msg : String, cause: Throwable = null) extends RuntimeError(msg, cause)
  class AssertionError(msg:String = null, cause: Throwable=null) extends java.lang.RuntimeException(msg, cause)
  class ParserError(val msg:String, val pos : Option[Position], val filename: Option[String]) extends java.lang.RuntimeException(msg, null) {
    override def hashCode : Int = {
      msg.hashCode() + pos.hashCode() + filename.hashCode()
    }
    def errorName = "Parser"
    lazy val positionStr = (filename, pos) match {
      case (Some(f), Some(p)) => f.toString + ", line " + p.line.toString
      case (None, Some(p)) => "line " + p.line.toString
      case _ => ""
    }
    lazy val fullStr = pos match {
      case Some(p) => p.longString
      case None => ""
    }
  }
  class TypeError(msg: String, pos: Option[Position], filename: Option[String]) extends ParserError(msg, pos, filename) {
    override def equals(that: Any) : Boolean = {
      that match {
        case that : TypeError =>
          (msg == that.msg) && (pos == that.pos) && filename == that.filename
        case _ =>
          false
      }
    }
    override def errorName = "Type"
  }
  class SyntaxError(msg: String, pos: Option[Position], filename: Option[String]) extends ParserError(msg,  pos, filename) {
    override def equals(that: Any) : Boolean = {
      that match {
        case that : SyntaxError =>
          (msg == that.msg) && (pos == that.pos) && filename == that.filename
        case _ => false
      }
    }
    override def errorName = "Syntax"
  }
  class TypeErrorList(val errors: List[TypeError]) extends java.lang.RuntimeException("Type errors.", null)
  class ParserErrorList(val errors : List[(String, lang.ASTPosition)]) extends java.lang.RuntimeException("Parser Errors", null)
  class UnknownIdentifierException(val ident : lang.Identifier) extends java.lang.RuntimeException("Unknown identifier:  " + ident.toString)

  def existsOnce(a: List[lang.Identifier], b: lang.Identifier) : Boolean = existsNTimes(a,b,1)
  def existsNone(a: List[lang.Identifier], b: lang.Identifier) : Boolean = existsNTimes(a,b,0)
  def existsNTimes(a: List[lang.Identifier], b: lang.Identifier, n: Int) : Boolean =
    a.count { x => x.name == b.name } == n

  def allUnique(a: List[lang.Identifier]) : Boolean = a.distinct.size == a.size

  def join(things: List[String], sep: String) = {
    things match {
      case Nil => ""
      case head :: tail => head + tail.foldLeft(""){(acc,i) => acc + sep + i}
    }
  }

  def topoSort[T](roots : List[T], graph: Map[T, Set[T]]) : List[T] = {
    def visit(node : T, visitOrder : Map[T, Int]) : Map[T, Int] = {
      if (visitOrder.contains(node)) {
        visitOrder
      } else {
        val visitOrderP = visitOrder + (node -> visitOrder.size)
        graph.get(node) match {
          case Some(nodes) => nodes.foldLeft(visitOrderP)((acc, m) => visit(m, acc))
          case None => visitOrderP
        }
      }
    }
    // now walk through the dep graph
    val order : List[(T, Int)] = roots.foldLeft(Map.empty[T, Int])((acc, r) => visit(r, acc)).toList
    order.sortWith((x, y) => x._2 < y._2).map(p => p._1)
  }

  def findCyclicDependencies[U, V](graph : Map[U, Set[U]], roots : List[U], errorFn : ((U, List[U]) => V)) : List[V] = {
    def visit(node : U, stack : List[U], errorsIn : List[V]) : List[V] = {
      if (stack contains node) {
        val cycleError = errorFn(node, stack)
        cycleError :: errorsIn
      } else {
        graph.get(node) match {
          case Some(nodes) =>
            nodes.foldLeft(errorsIn)((acc, n) => visit(n, node :: stack, acc))
          case None =>
            errorsIn
        }
      }
    }
    roots.foldLeft(List.empty[V])((acc, r) => visit(r, List.empty[U], acc))
  }
}

class UniqueNamer(val prefix : String) {
  var count = 0
  def newName() : String = {
    val name : String = prefix + "_" + count.toString
    count = count + 1
    return name
  }
}

class Memo[I, O](f : I => O) {
  var memoHashMap = new scala.collection.mutable.HashMap[I, O]
  val fun = f
  def apply(key : I) : O = {
    memoHashMap.get(key) match {
      case Some(v) => v
      case None    => {
        val item = fun(key)
        memoHashMap.put(key, item)
        item
      }
    }
  }
}
