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

 * Parser for UCLID5.
 *
 */

package uclid
package lang

import scala.util.parsing.input.Positional
import scala.util.parsing.combinator.token._
import scala.util.parsing.combinator.syntactical._
import scala.util.parsing.combinator.PackratParsers

import scala.language.implicitConversions
import scala.collection.mutable

  /** This is a re-implementation of the Scala libraries StdTokenParsers with StdToken replaced by UclidToken. */
trait UclidTokenParsers extends TokenParsers {
  type Tokens <: UclidTokens
  import lexical.{Keyword, IntegerLit, BitVectorTypeLit, BitVectorLit, StringLit, Identifier}

  protected val keywordCache = mutable.HashMap[String, Parser[String]]()

  /** A parser which matches a single keyword token.
   *
   * @param chars    The character string making up the matched keyword.
   * @return a `Parser` that matches the given string
   */
  implicit def keyword(chars: String): Parser[String] =
    keywordCache.getOrElseUpdate(chars, accept(Keyword(chars)) ^^ (_.chars))

  /** A parser which matches an integer literal */
  def integerLit: Parser[IntegerLit] =
    elem("integer", _.isInstanceOf[IntegerLit]) ^^ (_.asInstanceOf[IntegerLit])

  /** A parser which matches a bitvector type */
  def bitVectorType: Parser[BitVectorTypeLit] =
    elem("bitvector type", _.isInstanceOf[BitVectorTypeLit]) ^^ {_.asInstanceOf[BitVectorTypeLit]}

  /** A parser which matches a bitvector literal */
  def bitvectorLit: Parser[BitVectorLit] =
    elem("bitvector", _.isInstanceOf[BitVectorLit]) ^^ (_.asInstanceOf[BitVectorLit])

  /** A parser which matches a string literal */
  def stringLit: Parser[String] =
    elem("string literal", _.isInstanceOf[StringLit]) ^^ (_.chars)

  /** A parser which matches an identifier */
  def ident: Parser[String] =
    elem("identifier", _.isInstanceOf[Identifier]) ^^ (_.chars)
}

object UclidParser extends UclidTokenParsers with PackratParsers {
    type Tokens = UclidTokens
    val lexical = new UclidLexical

    // an implicit keyword function that gives a warning when a given word is not in the reserved/delimiters list
    override implicit def keyword(chars : String): Parser[String] = {
      if(lexical.reserved.contains(chars) || lexical.delimiters.contains(chars)) super.keyword(chars)
      else failure("You are trying to parse \""+chars+"\", but it is neither contained in the delimiters list, nor in the reserved keyword list of your lexical object")
    }

    sealed class PositionedString(val str : String) extends Positional

    lazy val OpAnd = "&&"
    lazy val OpOr = "||"
    lazy val OpBvAnd = "&"
    lazy val OpBvOr = "|"
    lazy val OpBvXor = "^"
    lazy val OpBvNot = "~"
    lazy val OpAdd = "+"
    lazy val OpSub = "-"
    lazy val OpMul = "*"
    lazy val OpUMul = "*_u"
    lazy val OpBiImpl = "<==>"
    lazy val OpImpl = "==>"
    lazy val OpLT = "<"
    lazy val OpULT = "<_u"
    lazy val OpGT = ">"
    lazy val OpUGT = ">_u"
    lazy val OpLE = "<="
    lazy val OpULE = "<=_u"
    lazy val OpGE = ">="
    lazy val OpUGE = ">=_u"
    lazy val OpEQ = "=="
    lazy val OpNE = "!="
    lazy val OpConcat = "++"
    lazy val OpNeg = "-"
    lazy val OpNot = "!"
    lazy val OpMinus = "-"
    lazy val OpPrime = "'"
    lazy val OpSelectFromInstance = "->"
    lazy val KwProcedure = "procedure"
    lazy val KwBoolean = "boolean"
    lazy val KwInteger = "integer"
    lazy val KwEnum = "enum"
    lazy val KwRecord = "record"
    lazy val KwReturns = "returns"
    lazy val KwAssume = "assume"
    lazy val KwAssert = "assert"
    lazy val KwHavoc = "havoc"
    lazy val KwVar = "var"
    lazy val KwSharedVar = "sharedvar"
    lazy val KwConst = "const"
    lazy val KwSkip = "skip"
    lazy val KwCall = "call"
    lazy val KwIf = "if"
    lazy val KwThen = "then"
    lazy val KwElse = "else"
    lazy val KwCase = "case"
    lazy val KwEsac = "esac"
    lazy val KwFor = "for"
    lazy val KwIn = "in"
    lazy val KwRange = "range"
    lazy val KwInstance = "instance"
    lazy val KwType = "type"
    lazy val KwInput = "input"
    lazy val KwOutput = "output"
    lazy val KwInit = "init"
    lazy val KwNext = "next"
    lazy val KwModule = "module"
    lazy val KwLambda = "lambda"
    lazy val KwFunction = "function"
    lazy val KwControl = "control"
    lazy val KwForall = "forall"
    lazy val KwExists = "exists"
    lazy val KwDefault = "default"
    lazy val KwSynthesis = "synthesis"
    lazy val KwGrammar = "grammar"
    lazy val KwRequires = "requires"
    lazy val KwDefine = "define"
    lazy val KwEnsures = "ensures"
    lazy val KwInvariant = "invariant"
    lazy val KwDefineProp = "property"
    lazy val KwDefineAxiom = "axiom"
    lazy val KwModifies = "modifies"
    lazy val KwParameter = "parameter"

    // lazy val TemporalOpGlobally = "G"
    // lazy val TemporalOpFinally = "F"
    // lazy val TemporalOpNext = "Next"
    // lazy val TemporalOpUntil = "U"
    // lazy val TemporalOpWUntil = "W"
    // lazy val TemporalOpRelease = "R"

    lexical.delimiters ++= List("(", ")", ",", "[", "]",
      "bv", "{", "}", ";", "=", ":", "::", ".", "->", "*", "::=",
      OpAnd, OpOr, OpBvAnd, OpBvOr, OpBvXor, OpBvNot, OpAdd, OpSub, OpMul,
      OpBiImpl, OpImpl, OpLT, OpGT, OpLE, OpGE, OpEQ, OpNE, OpConcat,
      OpNot, OpMinus, OpSelectFromInstance, OpPrime)
    lexical.reserved += (OpAnd, OpOr, OpAdd, OpSub, OpMul,
      OpBiImpl, OpImpl, OpLT, OpGT, OpLE, OpGE, OpEQ, OpNE,
      OpBvAnd, OpBvOr, OpBvXor, OpBvNot, OpConcat, OpNot, OpMinus, OpPrime,
      "false", "true", "bv", KwProcedure, KwBoolean, KwInteger, KwReturns,
      KwAssume, KwAssert, KwSharedVar, KwVar, KwHavoc, KwCall,
      KwIf, KwThen, KwElse, KwCase, KwEsac, KwFor, KwIn, KwRange,
      KwInstance, KwInput, KwOutput, KwConst, KwModule, KwType, KwEnum,
      KwRecord, KwSkip, KwDefine, KwFunction, KwControl, KwInit,
      KwNext, KwLambda, KwModifies, KwDefineProp, KwDefineAxiom,
      KwForall, KwExists, KwDefault, KwSynthesis, KwGrammar, KwRequires,
      KwEnsures, KwInvariant, KwParameter)

    lazy val ast_binary: Expr ~ String ~ Expr => Expr = {
      case x ~ OpBiImpl ~ y => OperatorApplication(IffOp(), List(x, y))
      case x ~ OpImpl ~ y => OperatorApplication(ImplicationOp(), List(x, y))
      case x ~ OpAnd ~ y => OperatorApplication(ConjunctionOp(), List(x, y))
      case x ~ OpOr ~ y => OperatorApplication(DisjunctionOp(), List(x, y))
      case x ~ OpBvAnd ~ y => OperatorApplication(BVAndOp(0), List(x, y))
      case x ~ OpBvOr ~ y => OperatorApplication(BVOrOp(0), List(x, y))
      case x ~ OpBvXor ~ y => OperatorApplication(BVXorOp(0), List(x, y))
      case x ~ OpLT ~ y => OperatorApplication(LTOp(), List(x,y))
      case x ~ OpGT ~ y => OperatorApplication(GTOp(), List(x,y))
      case x ~ OpLE ~ y => OperatorApplication(LEOp(), List(x,y))
      case x ~ OpGE ~ y => OperatorApplication(GEOp(), List(x,y))
      case x ~ OpEQ ~ y => OperatorApplication(EqualityOp(), List(x, y))
      case x ~ OpNE ~ y => OperatorApplication(InequalityOp(), List(x, y))
      case x ~ OpConcat ~ y => OperatorApplication(ConcatOp(), List(x,y))
      case x ~ OpAdd ~ y => OperatorApplication(AddOp(), List(x,y))
      case x ~ OpSub ~ y => OperatorApplication(SubOp(), List(x,y))
      case x ~ OpMul ~ y => OperatorApplication(MulOp(), List(x,y))
    }

    lazy val RelOp: Parser[String] = OpGT | OpLT | OpEQ | OpNE | OpGE | OpLE
    lazy val UnOp: Parser[String] = OpNot | OpMinus
    lazy val RecordSelectOp: Parser[Identifier] = positioned { ("." ~> Id) }
    lazy val SelectFromInstanceOp : Parser[Identifier] = positioned { (OpSelectFromInstance ~> Id) }
    lazy val SelectFromModuleOp : Parser[(Identifier, Identifier)] = {
      Id ~ ("::" ~> Id) ^^ { case modId ~ id => (modId, id) }
    }
    lazy val ArraySelectOp: Parser[List[Expr]] =
      ("[" ~> Expr ~ rep("," ~> Expr) <~ "]") ^^
      {case e ~ es => (e :: es) }
    lazy val ArrayStoreOp: Parser[(List[Expr],Expr)] =
      ("[" ~> (Expr ~ rep("," ~> Expr) ~ ("=" ~> Expr)) <~ "]") ^^
      {case e ~ es ~ r => (e :: es, r)}
    lazy val ConstBitVectorSlice: Parser[lang.ConstBitVectorSlice] =
      positioned { ("[" ~> Integer ~ ":" ~ Integer <~ "]") ^^ { case x ~ ":" ~ y => lang.ConstBitVectorSlice(x.value.toInt, y.value.toInt) } }
    lazy val VarBitVectorSlice: Parser[lang.VarBitVectorSlice] =
      positioned { ("[" ~> Expr ~ ":" ~ Expr <~ "]") ^^ { case x ~ ":" ~ y => lang.VarBitVectorSlice(x, y) } }
    lazy val ConstExtractOp : Parser[lang.ConstExtractOp] =
      ("[" ~> Integer ~ ":" ~ Integer <~ "]") ^^ { case x ~ ":" ~ y => lang.ConstExtractOp(lang.ConstBitVectorSlice(x.value.toInt, y.value.toInt)) }
    lazy val VarExtractOp : Parser[lang.VarExtractOp] =
      positioned { ("[" ~> Expr ~ ":" ~ Expr <~ "]") ^^ { case x ~ ":" ~ y => lang.VarExtractOp(lang.VarBitVectorSlice(x, y)) } }
    lazy val ExtractOp : Parser[lang.ExtractOp] = positioned { ConstExtractOp | VarExtractOp }
    lazy val Id: PackratParser[Identifier] = positioned { ident ^^ {case i => Identifier(i)} }
    lazy val Bool: PackratParser[BoolLit] =
      positioned { "false" ^^ { _ => BoolLit(false) } | "true" ^^ { _ => BoolLit(true) } }
    lazy val Integer: PackratParser[lang.IntLit] =
      positioned { integerLit ^^ { case intLit => IntLit(BigInt(intLit.chars, intLit.base)) } }
    lazy val BitVector: PackratParser[lang.BitVectorLit] =
      positioned { bitvectorLit ^^ { case bvLit => lang.BitVectorLit(bvLit.intValue, bvLit.width) } }
    lazy val Number : PackratParser[lang.NumericLit] = positioned (Integer | BitVector)
    lazy val E1: PackratParser[Expr] =
      KwForall ~> IdTypeList ~ ("::" ~> E1) ^^ { case ids ~ expr => OperatorApplication(ForallOp(ids), List(expr)) } |
      KwExists ~> IdTypeList ~ ("::" ~> E1) ^^ { case ids ~ expr => OperatorApplication(ExistsOp(ids), List(expr)) } |
      E3

    /** E3 = E4 OpEquiv E3 | E4  **/
    lazy val E3: PackratParser[Expr] = positioned { E4 ~ OpBiImpl ~ E3 ^^ ast_binary | E4 }
    /** E4 = E5 OpImpl E4 | E5  **/
    lazy val E4: PackratParser[Expr] = positioned { E5 ~ OpImpl ~ E4 ^^ ast_binary | E5 }
    /** E5 = E6 <Bool_Or_Bv_Op> E5 | E6 **/
    lazy val E5: PackratParser[Expr] = positioned {
        E6 ~ OpAnd ~ E5 ^^ ast_binary   |
        E6 ~ OpOr ~ E5 ^^ ast_binary    |
        E6 ~ OpBvAnd ~ E5 ^^ ast_binary |
        E6 ~ OpBvOr ~ E5 ^^ ast_binary  |
        E6 ~ OpBvXor ~ E5 ^^ ast_binary |
        E6
    }
    /** E6 = E7 OpRel E7 | E7  **/
    lazy val E6: PackratParser[Expr] = positioned ( E7 ~ RelOp ~ E7 ^^ ast_binary | E7 )
    /** E7 = E8 OpConcat E7 | E8 **/
    lazy val E7: PackratParser[Expr] = positioned ( E8 ~ OpConcat ~ E7 ^^ ast_binary | E8 )
    /** E8 = E9 OpAdd E8 | E9 **/
    lazy val E8: PackratParser[Expr] = positioned ( E9 ~ OpAdd ~ E8 ^^ ast_binary | E9 )
    /** E9 = E9 OpSub E10 | E10 **/
    lazy val E9: PackratParser[Expr] = positioned ( E10 ~ OpSub ~ E10 ^^ ast_binary | E10 )
    /** E10 = E11 OpMul E11 | E11 **/
    lazy val E10: PackratParser[Expr] = E11 ~ OpMul ~ E11 ^^ ast_binary | E11
    /** E11 = UnOp E12 | E12 **/
    lazy val E11: PackratParser[Expr] = positioned {
        OpNeg ~> E12 ^^ { case e => OperatorApplication(UnaryMinusOp(), List(e)) } |
        OpNot ~> E12 ^^ { case e => OperatorApplication(NegationOp(), List(e)) } |
        OpBvNot ~> E12 ^^ { case e => OperatorApplication(BVNotOp(0), List(e)) } |
        E12
    }
    /** E12 = E13 MapOp | E13 **/
    lazy val E12: PackratParser[Expr] = positioned {
        E13 ~ ArraySelectOp ^^ { case e ~ m => ArraySelectOperation(e, m) } |
        E13 ~ ArrayStoreOp ^^ { case e ~ m => ArrayStoreOperation(e, m._1, m._2) } |
        E13 ~ ExtractOp ^^ { case e ~ m => OperatorApplication(m, List(e)) } |
        E13
    }
    /** E14 = E14 (ExprList) | E14 (.RecordSelect)* | E14 (->InstanceField)* | E14 */
    lazy val E13: PackratParser[Expr] = positioned {
        E14 ~ ExprList ^^ { case e ~ f => FuncApplication(e, f) } |
        E14 ~ RecordSelectOp ~ rep(RecordSelectOp) ^^ {
          case e ~ r ~ rs =>
            (r :: rs).foldLeft(e){
              (acc, f) => OperatorApplication(RecordSelect(f), List(acc))
            }
        } |
        E14 ~ SelectFromInstanceOp ~ rep(SelectFromInstanceOp) ^^ {
          case e ~ f ~ fs =>
            (f :: fs).foldLeft(e) {
              (acc, f) => OperatorApplication(SelectFromInstance(f), List(acc))
            }
        } |
        E14
    }
    /** E14 = false | true | Number | Id FuncApplication | (Expr) **/
    lazy val E14: PackratParser[Expr] = positioned {
        Bool |
        Number |
        "{" ~> Expr ~ rep("," ~> Expr) <~ "}" ^^ {case e ~ es => Tuple(e::es)} |
        KwIf ~> ("(" ~> E3 <~ ")") ~ (KwThen ~> E3) ~ (KwElse ~> Expr) ^^ {
          case expr ~ thenExpr ~ elseExpr => lang.OperatorApplication(lang.ITEOp(), List(expr, thenExpr, elseExpr))
        } |
        KwLambda ~> (IdTypeList) ~ ("." ~> Expr) ^^ { case idtyps ~ expr => Lambda(idtyps, expr) } |
        "(" ~> Expr <~ ")" |
        SelectFromModuleOp  ^^ {
          case (moduleId, varId) => lang.ExternalIdentifier(moduleId, varId)
        } |
        Id <~ OpPrime ^^ { case id => lang.OperatorApplication(GetNextValueOp(), List(id)) } |
        Id
    }

    /** Expr = E1 (Used to be TemporalExpr0) **/
    lazy val Expr: PackratParser[Expr] = positioned ( E1 ) // Used to be TemporalExpr0
    lazy val ExprList: Parser[List[Expr]] =
      ("(" ~> Expr ~ rep("," ~> Expr) <~ ")") ^^ { case e ~ es => e::es } |
      "(" ~> ")" ^^ { case _ => List.empty[Expr] }

    /** Examples of allowed types are bool | int | [int,int,bool] int **/
    lazy val PrimitiveType : PackratParser[Type] = positioned {
      KwBoolean ^^ {case _ => BooleanType()}   |
      KwInteger ^^ {case _ => IntegerType()}     |
      bitVectorType ^^ {case bvType => BitVectorType(bvType.width)}
    }

    lazy val EnumType : PackratParser[lang.EnumType] = positioned {
      KwEnum ~> ("{" ~> Id) ~ rep("," ~> Id) <~ "}" ^^ { case id ~ ids => lang.EnumType(id::ids) }
    }
    lazy val TupleType : PackratParser[lang.TupleType] = positioned {
      ("{" ~> Type ~ rep("," ~> Type) <~ "}") ^^ { case t ~ ts => lang.TupleType(t :: ts) }
    }
    lazy val RecordType : PackratParser[lang.RecordType] = positioned {
      KwRecord ~> ("{" ~> IdType) ~ rep("," ~> IdType) <~ "}" ^^ { case id ~ ids => lang.RecordType(id::ids) }
    }
    lazy val MapType : PackratParser[lang.MapType] = positioned {
      PrimitiveType ~ rep ("*" ~> PrimitiveType) ~ ("->" ~> Type) ^^ { case t ~ ts ~ rt => lang.MapType(t :: ts, rt)}
    }
    lazy val ArrayType : PackratParser[lang.ArrayType] = positioned {
      ("[") ~> Type ~ (rep ("," ~> Type) <~ "]") ~ Type ^^ { case t ~ ts ~ rt => lang.ArrayType(t :: ts, rt)}
    }
    lazy val SynonymType : PackratParser[lang.SynonymType] = positioned ( Id ^^ { case id => lang.SynonymType(id) } )
    lazy val ExternalType : PackratParser[lang.ExternalType] = positioned {
      Id ~ ("." ~> Id) ^^ { case moduleId ~ typeId => lang.ExternalType(moduleId, typeId) }
    }
    lazy val Type : PackratParser[Type] = positioned {
      MapType | ArrayType | EnumType | TupleType | RecordType | ExternalType | SynonymType | PrimitiveType
    }

    lazy val IdType : PackratParser[(Identifier,Type)] =
      Id ~ (":" ~> Type) ^^ { case id ~ typ => (id,typ)}

    lazy val IdTypeList : PackratParser[List[(Identifier,Type)]] =
      "(" ~> IdType ~ (rep ("," ~> IdType) <~ ")") ^^ { case t ~ ts =>  t :: ts} |
      "(" ~ ")" ^^ { case _~_ => List.empty[(Identifier,Type)] }

    lazy val Lhs : PackratParser[lang.Lhs] = positioned {
      Id ~ VarBitVectorSlice ^^ { case id ~ slice => lang.LhsVarSliceSelect(id, slice) } |
      Id ~ ArraySelectOp ^^ { case id ~ mapOp => lang.LhsArraySelect(id, mapOp) } |
      Id ~ RecordSelectOp ~ rep(RecordSelectOp) ^^ { case id ~ rOp ~ rOps => lang.LhsRecordSelect(id, rOp::rOps) }  |
      Id <~ OpPrime ^^ { case id => LhsNextId(id) } |
      Id ^^ { case id => lang.LhsId(id) }
    }

    lazy val LhsList: PackratParser[List[Lhs]] =
      ("(" ~> Lhs ~ rep("," ~> Lhs) <~ ")") ^^ { case l ~ ls => l::ls } |
      "(" ~> ")" ^^ { case _ => List.empty[Lhs] }

    lazy val RangeExpr: PackratParser[(NumericLit,NumericLit)] =
      KwRange ~> ("(" ~> Number ~ ("," ~> Number) <~ ")") ^^ { case x ~ y => (x,y) }

    lazy val IdList : PackratParser[List[lang.Identifier]] =
      Id ~ rep("," ~> Id) ^^ { case id ~ ids => id :: ids }

    lazy val LocalVarDecl : PackratParser[List[lang.LocalVarDecl]] = {
      KwVar ~> IdType <~ ";" ^^ { case (id,typ) => List(lang.LocalVarDecl(id,typ)) } |
      KwVar ~> (Id ~ rep("," ~> Id)) ~ (":" ~> Type) <~ ";" ^^ { 
        case id ~ ids ~ typ => (id :: ids).map(lang.LocalVarDecl(_, typ))
      }
    }

    lazy val Statement: PackratParser[Statement] = positioned {
      KwSkip <~ ";" ^^ { case _ => SkipStmt() } |
      KwAssert ~> Expr <~ ";" ^^ { case e => AssertStmt(e, None) } |
      KwAssume ~> Expr <~ ";" ^^ { case e => AssumeStmt(e, None) } |
      KwHavoc ~> Id <~ ";" ^^ { case id => HavocStmt(HavocableId(id)) } |
      Lhs ~ rep("," ~> Lhs) ~ "=" ~ Expr ~ rep("," ~> Expr) <~ ";" ^^
        { case l ~ ls ~ "=" ~ r ~ rs => AssignStmt(l::ls, r::rs) } |
      KwCall ~> Id ~ ExprList <~ ";" ^^
        { case id ~ args => ProcedureCallStmt(id, List.empty, args) } |
      KwCall ~> LhsList ~ ("=" ~> Id) ~ ExprList <~ ";" ^^
        { case lhss ~ id ~ args => ProcedureCallStmt(id, lhss, args) } |
      KwNext ~ "(" ~> Id <~ ")" ~ ";" ^^
        { case id => lang.ModuleCallStmt(id) } |
      KwIf ~ "(" ~ "*" ~ ")" ~> (BlockStatement <~ KwElse) ~ BlockStatement ^^
        { case tblk ~ fblk => lang.IfElseStmt(lang.FreshLit(lang.BooleanType()), tblk, fblk) } |
      KwIf ~ "(" ~ "*" ~ ")" ~> BlockStatement ^^
        { case blk => IfElseStmt(lang.FreshLit(lang.BooleanType()), blk, List.empty) } |
      KwIf ~ "(" ~> (Expr <~ ")") ~ BlockStatement ~ (KwElse ~> BlockStatement) ^^
        { case e ~ f ~ g => IfElseStmt(e,f,g)} |
      KwIf ~> (Expr ~ BlockStatement) ^^
        { case e ~ f => IfElseStmt(e, f, List.empty[Statement]) } |
      KwCase ~> rep(CaseBlockStmt) <~ KwEsac ^^
        { case i => CaseStmt(i) } |
      KwFor ~> (Id ~ (KwIn ~> RangeExpr) ~ BlockStatement) ^^
        { case id ~ r ~ body => ForStmt(Identifier(id.name), r, body) } |
      ";" ^^ { case _ => SkipStmt() }
    }

    lazy val CaseBlockStmt: PackratParser[(Expr, List[Statement])] =
      (Expr ~ ":" ~ BlockStatement) ^^ { case e ~ ":" ~ ss => (e,ss) } |
      (KwDefault ~ ":" ~> BlockStatement) ^^ { case ss => (BoolLit(true), ss) }

    lazy val BlockStatement: PackratParser[List[Statement]] =
      "{" ~> rep (Statement) <~ "}"

    lazy val OptionalExpr : PackratParser[Option[lang.Expr]] =
      "(" ~ ")" ^^ { case _ => None } |
      "(" ~> Expr <~ ")" ^^ { case expr => Some(expr) }
    lazy val ArgMap : PackratParser[(lang.Identifier, Option[lang.Expr])] =
      Id ~ ":" ~ OptionalExpr ^^ { case id ~ ":" ~ optExpr => (id, optExpr) }
    lazy val ArgMapList : PackratParser[List[(lang.Identifier, Option[lang.Expr])]] =
      "(" ~ ")" ^^ { case _ => List.empty } |
      "(" ~> ArgMap ~ rep("," ~> ArgMap) <~ ")" ^^ { case arg ~ args => arg :: args }

    lazy val InstanceDecl : PackratParser[lang.InstanceDecl] = positioned {
      KwInstance ~> Id ~ ":" ~ Id ~ ArgMapList <~ ";" ^^ { case instId ~ ":" ~ moduleId ~ args => lang.InstanceDecl(instId, moduleId, args, None, None) }
    }
    lazy val RequireExpr : PackratParser[lang.Expr] = positioned {
      KwRequires ~> Expr <~ ";"
    }
    lazy val EnsureExpr : PackratParser[lang.Expr] = positioned {
      KwEnsures ~> Expr <~ ";"
    }
    lazy val ModifiesExprs : PackratParser[List[lang.Identifier]] = {
      KwModifies ~> Id ~ rep("," ~> Id) <~ ";" ^^ { case id ~ ids => id :: ids }
    }
    lazy val ProcedureDecl : PackratParser[lang.ProcedureDecl] = positioned {
      KwProcedure ~> Id ~ IdTypeList ~ (KwReturns ~> IdTypeList) ~
      rep(RequireExpr) ~ rep(EnsureExpr) ~ rep(ModifiesExprs) ~
        ("{" ~> rep(LocalVarDecl)) ~ (rep(Statement) <~ "}") ^^
        { case id ~ args ~ outs ~ requires ~ ensures ~ modifies ~ decls ~ body =>
          lang.ProcedureDecl(id, lang.ProcedureSig(args,outs), 
                             decls.flatMap(d => d), body,
                             requires, ensures, (modifies.flatMap(m => m)).toSet) } |
      // procedure with no return value
      KwProcedure ~> Id ~ IdTypeList ~ rep(RequireExpr) ~ rep(EnsureExpr) ~ rep(ModifiesExprs) ~
      ("{" ~> rep(LocalVarDecl)) ~ (rep(Statement) <~ "}") ^^
        { case id ~ args ~ requires ~ ensures ~ modifies ~ decls ~ body =>
          lang.ProcedureDecl(id, lang.ProcedureSig(args, List.empty[(Identifier,Type)]), 
                             decls.flatMap(d => d), body,
                             requires, ensures, (modifies.flatMap(m => m)).toSet) }
    }

    lazy val TypeDecl : PackratParser[lang.TypeDecl] = positioned {
      KwType ~> Id ~ ("=" ~> Type) <~ ";" ^^ { case id ~ t => lang.TypeDecl(id,t) } |
      KwType ~> Id <~ ";" ^^ { case id => lang.TypeDecl(id, lang.UninterpretedType(id)) }
    }

    lazy val VarsDecl : PackratParser[lang.StateVarsDecl] = positioned {
      KwVar ~> IdList ~ ":" ~ Type <~ ";" ^^ { case ids ~ ":" ~ typ => lang.StateVarsDecl(ids, typ) }
    }
    lazy val InputsDecl : PackratParser[lang.InputVarsDecl] = positioned {
      KwInput ~> IdList ~ ":" ~ Type <~ ";" ^^ { case ids ~ ":" ~ typ => lang.InputVarsDecl(ids, typ) }
    }
    lazy val OutputsDecl : PackratParser[lang.OutputVarsDecl] = positioned {
      KwOutput ~> IdList ~ ":" ~ Type <~ ";" ^^ { case ids ~ ":" ~ typ => lang.OutputVarsDecl(ids, typ) }
    }
    lazy val SharedVarsDecl : PackratParser[lang.SharedVarsDecl] = positioned {
      KwSharedVar ~> IdList ~ ":" ~ Type <~ ";" ^^ { case ids ~ ":" ~ typ => lang.SharedVarsDecl(ids, typ) }
    }

    lazy val ConstDecl : PackratParser[lang.ConstantsDecl] = positioned {
      KwConst ~> IdList ~ ":" ~ Type <~ ";" ^^ { case ids ~ ":" ~ typ => lang.ConstantsDecl(ids,typ)}
    }

    lazy val FuncDecl : PackratParser[lang.FunctionDecl] = positioned {
      KwFunction ~> Id ~ IdTypeList ~ (":" ~> Type) <~ ";" ^^
      { case id ~ idtyps ~ rt => lang.FunctionDecl(id, lang.FunctionSig(idtyps, rt)) }
    }

    // Grammar parsing begins here. //
    lazy val LiteralTerm : PackratParser[lang.LiteralTerm] = positioned {
      Bool ^^ { case b => lang.LiteralTerm(b) } |
      Number ^^ { case num => lang.LiteralTerm(num) }
    }

    lazy val SymbolTerm: PackratParser[lang.SymbolTerm] = positioned {
      Id ^^ { case id => lang.SymbolTerm(id) }
    }

    lazy val GrammarInfixBinaryOp: PackratParser[lang.Operator] = positioned {
      OpBiImpl ^^ { case _ => IffOp() } |
      OpImpl ^^ { case _ => ImplicationOp() } |
      OpAnd ^^ { case _ => ConjunctionOp() } |
      OpOr ^^ { case _ => DisjunctionOp() } |
      OpBvAnd ^^ { case _ => BVAndOp(0) } |
      OpBvOr ^^ { case _ => BVOrOp(0) } |
      OpBvXor ^^ { case _ => BVXorOp(0) } |
      OpLT ^^ { case _ => LTOp() } |
      OpGT ^^ { case _ => GTOp() } |
      OpLE ^^ { case _ => LEOp() } |
      OpGE ^^ { case _ => GEOp() } |
      OpEQ ^^ { case _ => EqualityOp() } |
      OpNE ^^ { case _ => InequalityOp() } |
      OpConcat ^^ { case _ => ConcatOp() } |
      OpAdd ^^ { case _ => AddOp() } |
      OpSub ^^ { case _ => SubOp() } |
      OpMul ^^ { case _ => MulOp() }
    }

    lazy val UnaryOpAppTerm: PackratParser[lang.OpAppTerm] = positioned {
      "(" ~ OpNeg ~> GrammarTerm <~ ")" ^^ { case t => lang.OpAppTerm(UnaryMinusOp(), List(t)) } |
      "(" ~ OpNot ~> GrammarTerm <~ ")" ^^ { case t => lang.OpAppTerm(NegationOp(), List(t)) } |
      "(" ~ OpBvNot ~> GrammarTerm <~ ")" ^^ { case t => lang.OpAppTerm(BVNotOp(0), List(t)) }
    }

    lazy val BinaryOpAppTerm: PackratParser[lang.OpAppTerm] = positioned {
      "(" ~> GrammarTerm ~ GrammarInfixBinaryOp ~ GrammarTerm <~ ")" ^^ {
        case t1 ~ op ~ t2 => lang.OpAppTerm(op, List(t1, t2))
      }
    }

    lazy val ITETerm: PackratParser[lang.OpAppTerm] = positioned {
      "(" ~ KwIf ~ "(" ~> (GrammarTerm <~ ")") ~ (KwThen ~> GrammarTerm) ~ (KwElse ~> GrammarTerm) <~ ")" ^^ {
        case c ~ t ~ f => lang.OpAppTerm(ITEOp(), List(c, t, f))
      }
    }

    lazy val ConstantTerm: PackratParser[lang.ConstantTerm] = positioned {
      KwConst ~> Type ^^ { case typ => lang.ConstantTerm(typ) }
    }

    lazy val ParameterTerm: PackratParser[lang.ParameterTerm] = positioned {
      KwParameter ~> Type ^^ { case typ => lang.ParameterTerm(typ) }
    }

    lazy val GrammarTerm : PackratParser[lang.GrammarTerm] = positioned {
      UnaryOpAppTerm | BinaryOpAppTerm | ITETerm | LiteralTerm | SymbolTerm | ConstantTerm | ParameterTerm
    }

    lazy val GrammarTermList : PackratParser[List[lang.GrammarTerm]] = {
      GrammarTerm ~ rep("|" ~> GrammarTerm) ^^ {
        case term ~ terms => term :: terms
      }
    }
    lazy val NonTerminal : PackratParser[lang.NonTerminal] = positioned {
      "(" ~> Id ~ (":" ~> Type <~ ")") ~ ("::=" ~> GrammarTermList) <~ ";" ^^ {
        case id ~ typ ~ terms => lang.NonTerminal(id, typ, terms)
      }
    }
    lazy val GrammarDecl : PackratParser[lang.GrammarDecl] = positioned {
      KwGrammar ~> Id ~ IdTypeList ~ (":" ~> Type) ~ ("=" ~ "{" ~> rep(NonTerminal) <~ "}") ^^ {
        case id ~ argTypes ~ retType ~ nonterminals => lang.GrammarDecl(id, lang.FunctionSig(argTypes, retType), nonterminals)
      }

    }
    lazy val SynthFuncDecl : PackratParser[lang.SynthesisFunctionDecl] = positioned {
      KwSynthesis ~ KwFunction ~> Id ~ IdTypeList ~ (":" ~> Type) ~
        (KwGrammar ~> Id ) ~ ("(" ~> IdList <~ ")") ~
        rep(KwEnsures ~> Expr) <~ ";" ^^
      { case id ~ idtyps ~ rt ~ gId ~ gArgs ~ eArgs =>
          lang.SynthesisFunctionDecl(id, lang.FunctionSig(idtyps, rt), gId, gArgs, eArgs)
      }
    }
    lazy val DefineDecl : PackratParser[lang.DefineDecl] = positioned {
      KwDefine ~> Id ~ IdTypeList ~ (":" ~> Type) ~ ("=" ~> Expr) <~ ";" ^^
      {
        case id ~ idTypeList ~ retType ~ expr => {
          lang.DefineDecl(id, FunctionSig(idTypeList, retType), expr)
        }
      }
    }
    lazy val InitDecl : PackratParser[lang.InitDecl] = positioned {
      KwInit ~> BlockStatement ^^
        { case b => lang.InitDecl(b) }
    }

    lazy val NextDecl : PackratParser[lang.NextDecl] = positioned {
      KwNext ~> BlockStatement ^^
        { case b => lang.NextDecl(b) }
    }

    lazy val SpecDecl: PackratParser[lang.SpecDecl] = positioned {
      (KwInvariant | KwDefineProp) ~> ("[" ~> rep(Expr) <~ "]").? ~ Id ~  (":" ~> Expr) <~ ";" ^^
         { case decOption ~ id ~ expr => decOption match {
           case None => lang.SpecDecl(id, expr, List.empty)
           case Some(dec) => lang.SpecDecl(id, expr, dec.map(ExprDecorator.parse(_))) }
         }
    }

    lazy val AxiomDecl: PackratParser[lang.AxiomDecl] = positioned {
      (KwAssume | KwDefineAxiom) ~> Id ~ (":" ~> Expr) <~ ";" ^^ { case id ~ expr => lang.AxiomDecl(Some(id), expr) } |
      (KwAssume | KwDefineAxiom) ~> Expr <~ ";" ^^ { case expr => lang.AxiomDecl(None, expr) }
    }

    lazy val Decl: PackratParser[Decl] =
      positioned (InstanceDecl | TypeDecl | ConstDecl | FuncDecl |
                  SynthFuncDecl | DefineDecl | GrammarDecl |
                  VarsDecl | InputsDecl | OutputsDecl | SharedVarsDecl |
                  ConstDecl | ProcedureDecl | InitDecl | NextDecl | SpecDecl | AxiomDecl)

    // control commands.
    lazy val IdParamList : PackratParser[List[Identifier]] =
      "[" ~> Id ~ (rep ("," ~> Id) <~ "]") ^^ { case t ~ ts =>  t :: ts} |
      "[" <~ "]" ^^ { case _ => List.empty }


    lazy val Cmd : PackratParser[lang.GenericProofCommand] = positioned {
      (Id <~ "=").? ~ (Id <~ "->").? ~ Id <~ ";" ^^
        { case rId ~ oId ~ id => lang.GenericProofCommand(id, List.empty, List.empty, rId, oId) } |
      (Id <~ "=").? ~ (Id <~ "->").? ~ Id ~ IdParamList <~ ";" ^^
        { case rId ~ oId ~ id ~ idparams => lang.GenericProofCommand(id, idparams, List.empty, rId, oId) } |
      (Id <~ "=").? ~ (Id <~ "->").? ~ Id ~ ExprList <~ ";" ^^
        { case rId ~ oId ~ id ~ es => lang.GenericProofCommand(id, List.empty, es, rId, oId) } |
      (Id <~ "=").? ~ (Id <~ "->").? ~ Id ~ IdParamList ~ ExprList <~ ";" ^^
        { case rId ~ oId ~ id ~ idparams ~ es => lang.GenericProofCommand(id, idparams, es, rId, oId) }
    }

    lazy val CmdBlock : PackratParser[List[GenericProofCommand]] = KwControl ~ "{" ~> rep(Cmd) <~ "}"

    lazy val Module: PackratParser[lang.Module] = positioned {
      KwModule ~> Id ~ ("{" ~> rep(Decl) ~ ( CmdBlock.? ) <~ "}") ^^ {
        case id ~ (decls ~ Some(cs)) => lang.Module(id, decls, cs, Annotation.default)
        case id ~ (decls ~ None) => lang.Module(id, decls, List.empty, Annotation.default)
      }
    }

    lazy val Model: PackratParser[List[Module]] = rep(Module)

    def parseModel(filename : String, text: String): List[Module] = {
      val tokens = new PackratReader(new lexical.Scanner(text))
      phrase(Model)(tokens) match {
        case Success(modules, _) => modules.map((m) => m.withFilename(filename))
        case NoSuccess(msg, next) => throw new Utils.SyntaxError(msg, Some(next.pos), Some(filename))
      }
    }
  }
