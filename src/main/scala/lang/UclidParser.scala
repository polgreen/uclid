/**
 * First created by Rohit Sinha on 5/21/15.
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
    lazy val OpNeg = "!"
    lazy val OpMinus = "-"
    lazy val KwProcedure = "procedure"
    lazy val KwBool = "bool"
    lazy val KwInt = "int"
    lazy val KwEnum = "enum"
    lazy val KwRecord = "record"
    lazy val KwReturns = "returns"
    lazy val KwAssume = "assume"
    lazy val KwAssert = "assert"
    lazy val KwHavoc = "havoc"
    lazy val KwVar = "var"
    lazy val KwConst = "const"
    lazy val KwSkip = "skip"
    lazy val KwCall = "call"
    lazy val KwIf = "if"
    lazy val KwElse = "else"
    lazy val KwCase = "case"
    lazy val KwEsac = "esac"
    lazy val KwFor = "for"
    lazy val KwIn = "in"
    lazy val KwRange = "range"
    lazy val KwType = "type"
    lazy val KwInput = "input"
    lazy val KwOutput = "output"
    lazy val KwInit = "init"
    lazy val KwInitialize = "initialize"
    lazy val KwNext = "next"
    lazy val KwModule = "module"
    lazy val KwITE = "ITE"
    lazy val KwLambda = "Lambda"
    lazy val KwFunction = "function"
    lazy val KwControl = "control"
    lazy val KwSimulate = "simulate"
    lazy val KwUnroll = "unroll"
    lazy val KwDecide = "decide"
    lazy val KwDebug = "__uclid_debug"
    
    lazy val KwDefineProp = "property"
    lazy val TemporalOpGlobally = "G"
    lazy val TemporalOpFinally = "F"
    lazy val TemporalOpNext = "Next"
    lazy val TemporalOpUntil = "U"
    lazy val TemporalOpWUntil = "W"
    lazy val TemporalOpRelease = "R"
  
    lexical.delimiters ++= List("(", ")", ",", "[", "]", 
      "bv", "{", "}", ";", "=", ":=", ":", ".", "->", "*",
      OpAnd, OpOr, OpBvAnd, OpBvOr, OpBvXor, OpBvNot, OpAdd, OpSub, OpMul, 
      OpBiImpl, OpImpl, OpLT, OpGT, OpLE, OpGE, OpEQ, OpNE, OpConcat, 
      OpNeg, OpMinus)
    lexical.reserved += (OpAnd, OpOr, OpAdd, OpSub, OpMul, 
      OpBiImpl, OpImpl, OpLT, OpGT, OpLE, OpGE, OpEQ, OpNE, 
      OpBvAnd, OpBvOr, OpBvXor, OpBvNot, OpConcat, OpNeg, OpMinus,
      "false", "true", "bv", KwProcedure, KwBool, KwInt, KwReturns,
      KwAssume, KwAssert, KwVar, KwHavoc, KwCall, KwIf, KwElse,
      KwCase, KwEsac, KwFor, KwIn, KwRange, KwInput, KwOutput,
      KwModule, KwType, KwEnum, KwRecord, KwSkip, KwFunction, 
      KwInitialize, KwUnroll, KwSimulate, KwDecide, KwControl, KwDebug,
      KwInit, KwNext, KwITE, KwLambda, 
      KwDefineProp, TemporalOpGlobally, TemporalOpFinally, TemporalOpNext,
      TemporalOpUntil, TemporalOpWUntil, TemporalOpRelease)
  
    lazy val ast_binary: Expr ~ String ~ Expr => Expr = {
      case x ~ TemporalOpUntil   ~ y => OperatorApplication(UntilTemporalOp(), List(x, y))
      case x ~ TemporalOpWUntil  ~ y => OperatorApplication(WUntilTemporalOp(), List(x, y))
      case x ~ TemporalOpRelease ~ y => OperatorApplication(ReleaseTemporalOp(), List(x, y))
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
    lazy val UnOp: Parser[String] = OpNeg | OpMinus
    lazy val RecordSelectOp: Parser[Identifier] = positioned { ("." ~> Id) }
    lazy val ArraySelectOp: Parser[List[Expr]] =
      ("[" ~> Expr ~ rep("," ~> Expr) <~ "]") ^^ 
      {case e ~ es => (e :: es) }
    lazy val ArrayStoreOp: Parser[(List[Expr],Expr)] =
      ("[" ~> (Expr ~ rep("," ~> Expr) ~ (":=" ~> Expr)) <~ "]") ^^ 
      {case e ~ es ~ r => (e :: es, r)}
    lazy val ConstBitVectorSlice: Parser[lang.ConstBitVectorSlice] =
      positioned { ("[" ~> Integer ~ ":" ~ Integer <~ "]") ^^ { case x ~ ":" ~ y => lang.ConstBitVectorSlice(x.value.toInt, y.value.toInt) } }
    lazy val VarBitVectorSlice: Parser[lang.VarBitVectorSlice] = 
      positioned { ("[" ~> Expr ~ ":" ~ Expr <~ "]") ^^ { case x ~ ":" ~ y => lang.VarBitVectorSlice(x, y) } }
    lazy val ExtractOp: Parser[lang.ExtractOp] =
      ("[" ~> Integer ~ ":" ~ Integer <~ "]") ^^ { case x ~ ":" ~ y => lang.ExtractOp(lang.ConstBitVectorSlice(x.value.toInt, y.value.toInt)) }
    lazy val Id: PackratParser[Identifier] = positioned { ident ^^ {case i => Identifier(i)} }
    lazy val Bool: PackratParser[BoolLit] =
      positioned { "false" ^^ { _ => BoolLit(false) } | "true" ^^ { _ => BoolLit(true) } }
    lazy val Integer: PackratParser[lang.IntLit] = 
      positioned { integerLit ^^ { case intLit => IntLit(BigInt(intLit.chars, intLit.base)) } }
    lazy val BitVector: PackratParser[lang.BitVectorLit] = 
      positioned { bitvectorLit ^^ { case bvLit => lang.BitVectorLit(bvLit.intValue, bvLit.width) } }
    lazy val Number : PackratParser[lang.NumericLit] = positioned (Integer | BitVector)
    lazy val TemporalExpr0: PackratParser[Expr] = 
        positioned { TemporalExpr1 ~ TemporalOpUntil  ~ TemporalExpr0 ^^ ast_binary | TemporalExpr1 } 
    lazy val TemporalExpr1: PackratParser[Expr] =
      positioned { TemporalExpr2 ~ TemporalOpWUntil  ~ TemporalExpr1 ^^ ast_binary | TemporalExpr2 }
    lazy val TemporalExpr2: PackratParser[Expr] =
      positioned { TemporalExpr3 ~ TemporalOpRelease  ~ TemporalExpr2 ^^ ast_binary | TemporalExpr3 }
    lazy val TemporalExpr3: PackratParser[Expr] = 
      positioned { TemporalOpFinally ~> TemporalExpr4 ^^ { case expr => OperatorApplication(FinallyTemporalOp(), List(expr)) } | TemporalExpr4 }
    lazy val TemporalExpr4: PackratParser[Expr] = 
      positioned { TemporalOpGlobally ~> TemporalExpr5 ^^ { case expr => OperatorApplication(GloballyTemporalOp(), List(expr)) } | TemporalExpr5 }
    lazy val TemporalExpr5: PackratParser[Expr] = 
      positioned { TemporalOpNext ~> E0 ^^ { case expr => OperatorApplication(NextTemporalOp(), List(expr)) } | E0 }
      
    /** E0 := E1 OpEquiv E0 | E1  **/
    lazy val E0: PackratParser[Expr] = positioned { E1 ~ OpBiImpl ~ E0 ^^ ast_binary | E1 }
    /** E1 := E2 OpImpl E1 | E2  **/
    lazy val E1: PackratParser[Expr] = positioned { E2 ~ OpImpl ~ E1 ^^ ast_binary | E2 }
    /** E2 := E3 OpAnd E2 | E3 OpOr E2 | E3 **/
    lazy val E2: PackratParser[Expr] = positioned {
        E3 ~ OpAnd ~ E2 ^^ ast_binary   | 
        E3 ~ OpOr ~ E2 ^^ ast_binary    |
        E3 ~ OpBvAnd ~ E2 ^^ ast_binary |
        E3 ~ OpBvOr ~ E2 ^^ ast_binary  |
        E3 ~ OpBvXor ~ E2 ^^ ast_binary |
        E3
    }
    /** E3 := E4 OpRel E3 | E4  **/
    lazy val E3: PackratParser[Expr] = positioned ( E4 ~ RelOp ~ E4 ^^ ast_binary | E4 )
    /** E4 := E5 OpConcat E4 | E5 **/
    lazy val E4: PackratParser[Expr] = positioned ( E5 ~ OpConcat ~ E4 ^^ ast_binary | E5 )
    /** E5 := E6 OpAdd E5 | E6 **/
    lazy val E5: PackratParser[Expr] = positioned ( E6 ~ OpAdd ~ E5 ^^ ast_binary | E6 )
    /** E6 := E6 OpSub E6 | E7 **/
    lazy val E6: PackratParser[Expr] = positioned ( E7 ~ OpSub ~ E7 ^^ ast_binary | E7 )
    /** E6 := E7 OpMul E6 | E7 **/
    lazy val E7: PackratParser[Expr] = E8 ~ OpMul ~ E8 ^^ ast_binary | E8
    /** E8 := UnOp E9 | E9 **/
    lazy val E8: PackratParser[Expr] = positioned {
        OpNeg ~> E9 ^^ { case e => OperatorApplication(NegationOp(), List(e)) } |
        OpBvNot ~> E9 ^^ { case e => OperatorApplication(BVNotOp(0), List(e)) } |
        E9
    }
    /** E9 := E10 MapOp | E10 **/
    lazy val E9: PackratParser[Expr] = positioned {
        E10 ~ ExprList ^^ { case e ~ f => FuncApplication(e, f) } |
        E10 ~ RecordSelectOp ~ rep(RecordSelectOp) ^^ { 
          case e ~ r ~ rs =>
            (r :: rs).foldLeft(e){ 
              (acc, f) => OperatorApplication(RecordSelect(f), List(acc))
            }
        } |
        E10 ~ ArraySelectOp ^^ { case e ~ m => ArraySelectOperation(e, m) } |
        E10 ~ ArrayStoreOp ^^ { case e ~ m => ArrayStoreOperation(e, m._1, m._2) } |
        E10 ~ ExtractOp ^^ { case e ~ m => OperatorApplication(m, List(e)) } |
        E10
    }
    /** E10 := false | true | Number | Id FuncApplication | (Expr) **/
    lazy val E10: PackratParser[Expr] = positioned {
        Bool |
        Number |
        "{" ~> Expr ~ rep("," ~> Expr) <~ "}" ^^ {case e ~ es => Tuple(e::es)} |
        KwITE ~> ("(" ~> Expr ~ ("," ~> Expr) ~ ("," ~> Expr) <~ ")") ^^ { case e ~ t ~ f => ITE(e,t,f) } |
        KwLambda ~> (IdTypeList) ~ ("." ~> Expr) ^^ { case idtyps ~ expr => Lambda(idtyps, expr) } |
        "(" ~> Expr <~ ")" |
        Id
    }
    /** Expr := TemporalExpr0 **/
    lazy val Expr: PackratParser[Expr] = positioned ( TemporalExpr0 )
    lazy val ExprList: Parser[List[Expr]] =
      ("(" ~> Expr ~ rep("," ~> Expr) <~ ")") ^^ { case e ~ es => e::es } |
      "(" ~> ")" ^^ { case _ => List.empty[Expr] }
  
    /** Examples of allowed types are bool | int | [int,int,bool] int **/
    lazy val PrimitiveType : PackratParser[Type] = positioned {
      KwBool ^^ {case _ => BoolType()}   | 
      KwInt ^^ {case _ => IntType()}     |
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
      ("[") ~> PrimitiveType ~ (rep ("," ~> PrimitiveType) <~ "]") ~ Type ^^ { case t ~ ts ~ rt => lang.ArrayType(t :: ts, rt)}
    }
    lazy val SynonymType : PackratParser[lang.SynonymType] = positioned ( Id ^^ { case id => lang.SynonymType(id) } )
    lazy val Type : PackratParser[Type] = positioned {
      MapType | ArrayType | EnumType | TupleType | RecordType | PrimitiveType | SynonymType
    }
  
    lazy val IdType : PackratParser[(Identifier,Type)] =
      Id ~ (":" ~> Type) ^^ { case id ~ typ => (id,typ)}
  
    lazy val IdTypeList : PackratParser[List[(Identifier,Type)]] =
      "(" ~> IdType ~ (rep ("," ~> IdType) <~ ")") ^^ { case t ~ ts =>  t :: ts} |
      "(" ~ ")" ^^ { case _~_ => List.empty[(Identifier,Type)] }
  
    lazy val Lhs : PackratParser[lang.Lhs] = positioned {
      Id ~ ConstBitVectorSlice ^^ { case id ~ slice => lang.LhsSliceSelect(id, slice) }  |
      Id ~ ArraySelectOp ^^ { case id ~ mapOp => lang.LhsArraySelect(id, mapOp) }        |
      Id ~ RecordSelectOp ~ rep(RecordSelectOp) ^^ { case id ~ rOp ~ rOps => lang.LhsRecordSelect(id, rOp::rOps) }    |
      Id ^^ { case id => lang.LhsId(id) }
    }
  
    lazy val LhsList: PackratParser[List[Lhs]] =
      ("(" ~> Lhs ~ rep("," ~> Lhs) <~ ")") ^^ { case l ~ ls => l::ls } |
      "(" ~> ")" ^^ { case _ => List.empty[Lhs] }
  
    lazy val RangeExpr: PackratParser[(NumericLit,NumericLit)] =
      KwRange ~> ("(" ~> Number ~ ("," ~> Number) <~ ")") ^^ { case x ~ y => (x,y) }
  
    lazy val LocalVarDecl : PackratParser[lang.LocalVarDecl] = positioned {
      KwVar ~> IdType <~ ";" ^^ { case (id,typ) => lang.LocalVarDecl(id,typ)}
    }
      
    lazy val Statement: PackratParser[Statement] = positioned {
      KwSkip <~ ";" ^^ { case _ => SkipStmt() } |
      KwAssert ~> Expr <~ ";" ^^ { case e => AssertStmt(e, None) } |
      KwAssume ~> Expr <~ ";" ^^ { case e => AssumeStmt(e, None) } |
      KwHavoc ~> Id <~ ";" ^^ { case id => HavocStmt(id) } |
      Lhs ~ rep("," ~> Lhs) ~ ":=" ~ Expr ~ rep("," ~> Expr) <~ ";" ^^
        { case l ~ ls ~ ":=" ~ r ~ rs => AssignStmt(l::ls, r::rs) } |
      KwCall ~> LhsList ~ (":=" ~> Id) ~ ExprList <~ ";" ^^
        { case lhss ~ id ~ args => ProcedureCallStmt(id, lhss, args) } |
      KwIf ~> Expr ~ BlockStatement ~ (KwElse ~> BlockStatement) ^^
        { case e ~ f ~ g => IfElseStmt(e,f,g)} |
      KwIf ~> (Expr ~ BlockStatement) ^^
        { case e ~ f => IfElseStmt(e, f, List.empty[Statement]) } |
      KwCase ~> rep(CaseBlockStmt) <~ KwEsac ^^ 
        { case i => CaseStmt(i) } |
      KwFor ~> (Id ~ (KwIn ~> RangeExpr) ~ BlockStatement) ^^
        { case id ~ r ~ body => ForStmt(ConstIdentifier(id.name), r, body) }
    }
      
    lazy val CaseBlockStmt: PackratParser[(Expr, List[Statement])] =  
      Expr ~ (":" ~> BlockStatement) ^^ { case e ~ ss => (e,ss) }
    lazy val BlockStatement: PackratParser[List[Statement]] = 
      "{" ~> rep (Statement) <~ "}"
  
    lazy val ProcedureDecl : PackratParser[lang.ProcedureDecl] = positioned {
      KwProcedure ~> Id ~ IdTypeList ~ (KwReturns ~> IdTypeList) ~ 
        ("{" ~> rep(LocalVarDecl)) ~ (rep(Statement) <~ "}") ^^ 
        { case id ~ args ~ outs ~ decls ~ body =>  
          lang.ProcedureDecl(id, lang.ProcedureSig(args,outs), decls, body) } |
      KwProcedure ~> Id ~ IdTypeList ~ ("{" ~> rep(LocalVarDecl)) ~ (rep(Statement) <~ "}") ^^
        { case id ~ args ~ decls ~ body => 
          lang.ProcedureDecl(id, lang.ProcedureSig(args, List.empty[(Identifier,Type)]), decls, body) }
    }
  
    lazy val TypeDecl : PackratParser[lang.TypeDecl] = positioned {
      KwType ~> Id ~ ("=" ~> Type) <~ ";" ^^ { case id ~ t => lang.TypeDecl(id,t) }
    }
      
    lazy val VarDecl : PackratParser[lang.StateVarDecl] = positioned {
      keyword(KwVar) ~> IdType <~ ";" ^^ { case (id,typ) => lang.StateVarDecl(id,typ)}
    }
      
    lazy val InputDecl : PackratParser[lang.InputVarDecl] = positioned {
      KwInput ~> IdType <~ ";" ^^ { case (id,typ) => lang.InputVarDecl(id,typ)}
    }
      
    lazy val OutputDecl : PackratParser[lang.OutputVarDecl] = positioned {
      KwOutput ~> IdType <~ ";" ^^ { case (id,typ) => lang.OutputVarDecl(id,typ)}
    }
      
    lazy val ConstDecl : PackratParser[lang.ConstantDecl] = positioned {
      KwConst ~> IdType <~ ";" ^^ { case (id,typ) => lang.ConstantDecl(id,typ)}
    }
      
    lazy val FuncDecl : PackratParser[lang.FunctionDecl] = positioned {
      KwFunction ~> Id ~ IdTypeList ~ (":" ~> Type) <~ ";" ^^ 
      { case id ~ idtyps ~ rt => lang.FunctionDecl(id, lang.FunctionSig(idtyps, rt)) }
    }
      
    lazy val InitDecl : PackratParser[lang.InitDecl] = positioned { 
      KwInit ~> BlockStatement ^^ 
        { case b => lang.InitDecl(b) }
    }
    
    lazy val NextDecl : PackratParser[lang.NextDecl] = positioned {
      KwNext ~> BlockStatement ^^ 
        { case b => lang.NextDecl(b) }
    }
      
    lazy val Decl: PackratParser[Decl] = 
      positioned (TypeDecl | ConstDecl | FuncDecl | VarDecl | InputDecl | OutputDecl | ProcedureDecl | InitDecl | NextDecl | SpecDecl)
  
    // control commands.
    lazy val InitializeCmd : PackratParser[lang.InitializeCmd] = positioned { 
      KwInitialize <~ ";" ^^ { case _ => lang.InitializeCmd() }
    }
  
    lazy val DecideCmd : PackratParser[lang.DecideCmd] = positioned { 
      KwDecide <~ ";" ^^ { case _ => lang.DecideCmd() }
    }
    
    lazy val UnrollCmd : PackratParser[lang.UnrollCmd] = positioned { 
      KwUnroll ~ "(" ~> Integer <~ ")" ~ ";" ^^ { case num => lang.UnrollCmd(num) }
    }
  
    lazy val SimulateCmd : PackratParser[lang.SimulateCmd] = positioned {
      KwSimulate ~ "(" ~> Integer <~ ")" ~ ";" ^^ { case num => lang.SimulateCmd(num) }
    }
    
    // this command is for debugging.
    lazy val DebugCmd : PackratParser[lang.DebugCmd] = positioned {
      KwDebug ~> Id <~ ";" ^^ { case cmd => lang.DebugCmd(cmd, List.empty[lang.Expr]) }
    }


    lazy val Cmd : PackratParser[UclCmd] =
      ( InitializeCmd | UnrollCmd | SimulateCmd | DecideCmd | DebugCmd )
    
    lazy val BlockCmd : PackratParser[List[UclCmd]] = KwControl ~ "{" ~> rep(Cmd) <~ "}"
    
    lazy val Module: PackratParser[lang.Module] = positioned {
      KwModule ~> Id ~ ("{" ~> rep(Decl) ~ ( BlockCmd.? ) <~ "}") ^^ { 
        case id ~ (decls ~ Some(cs)) => lang.Module(id, decls, cs)
        case id ~ (decls ~ None) => lang.Module(id, decls, List[UclCmd]())
      }
    }
  
    lazy val SpecDecl: PackratParser[lang.SpecDecl] = positioned {
      KwDefineProp ~> Id ~ (":" ~> Expr) <~ ";" ^^ { case id ~ expr => lang.SpecDecl(id,expr) }
    }
  
    lazy val Model: PackratParser[List[Module]] = rep(Module) 
      
    def parseExpr(input: String): Expr = {
      val tokens = new PackratReader(new lexical.Scanner(input))
      phrase(Expr)(tokens) match {
        case Success(ast, _) => ast
        case e: NoSuccess => throw new IllegalArgumentException(e.toString)
      }
    }
  
    def parseModule(input: String): Module = {
      val tokens = new PackratReader(new lexical.Scanner(input))
      phrase(Module)(tokens) match {
        case Success(ast, _) => ast
        case e: NoSuccess => throw new IllegalArgumentException(e.toString)
      }
    }
    
    def parseModel(filename : String, text: String): List[Module] = {
      val tokens = new PackratReader(new lexical.Scanner(text))
      phrase(Model)(tokens) match {
        case Success(modules, _) => modules.map((m) => m.withFilename(filename))
        case e: NoSuccess => throw new IllegalArgumentException(e.toString)
      }
    }
  }
