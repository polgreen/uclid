/**
 * First created by Rohit Sinha on 5/21/15.
 */

package uclid {
  package lang {
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
  //  implicit def keyword(chars: String): Parser[String] = accept(Keyword(chars)) ^^ (_.chars)
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

      lazy val OpAnd = "&&"
      lazy val OpOr = "||"
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
      lazy val KwLocalVar = "localvar"
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
      
      lazy val KwDefineProp = "property"
      lazy val TemporalOpGlobally = "G"
      lazy val TemporalOpFinally = "F"
      lazy val TemporalOpNext = "Next"
      lazy val TemporalOpUntil = "U"
      lazy val TemporalOpWUntil = "W"
      lazy val TemporalOpRelease = "R"
    
      lexical.delimiters ++= List("(", ")", ",", "[", "]", 
        "bv", "{", "}", ";", "=", ":=", ":", ".", "->", "*",
        OpAnd, OpOr, OpAdd, OpSub, OpMul, OpBiImpl, OpImpl,
        OpLT, OpGT, OpLE, OpGE, OpEQ, OpNE, OpConcat, OpNeg, OpMinus)
      lexical.reserved += (OpAnd, OpOr, OpAdd, OpSub, OpMul, OpBiImpl, OpImpl,
        OpLT, OpGT, OpLE, OpGE, OpEQ, OpNE, OpConcat, OpNeg, OpMinus,
        "false", "true", "bv", KwProcedure, KwBool, KwInt, KwReturns,
        KwAssume, KwAssert, KwVar, KwLocalVar, KwHavoc, KwCall, KwIf, KwElse,
        KwCase, KwEsac, KwFor, KwIn, KwRange, KwLocalVar, KwInput, KwOutput,
        KwModule, KwType, KwEnum, KwRecord, KwSkip, KwFunction, 
        KwInitialize, KwUnroll, KwSimulate, KwControl,
        KwInit, KwNext, KwITE, KwLambda, 
        KwDefineProp, TemporalOpGlobally, TemporalOpFinally, TemporalOpNext,
        TemporalOpUntil, TemporalOpWUntil, TemporalOpRelease)
    
      lazy val ast_binary: Expr ~ String ~ Expr => Expr = {
        case x ~ TemporalOpUntil   ~ y => UclTemporalOpUntil(x, y)
        case x ~ TemporalOpWUntil  ~ y => UclTemporalOpWUntil(x, y)
        case x ~ TemporalOpRelease ~ y => UclTemporalOpRelease(x, y)
        case x ~ OpBiImpl ~ y => UclOperatorApplication(IffOp(), List(x, y))
        case x ~ OpImpl ~ y => UclOperatorApplication(ImplicationOp(), List(x, y))
        case x ~ OpAnd ~ y => UclOperatorApplication(ConjunctionOp(), List(x, y))
        case x ~ OpOr ~ y => UclOperatorApplication(DisjunctionOp(), List(x, y))
        case x ~ OpLT ~ y => UclOperatorApplication(LTOp(), List(x,y))
        case x ~ OpGT ~ y => UclOperatorApplication(GTOp(), List(x,y))
        case x ~ OpLE ~ y => UclOperatorApplication(LEOp(), List(x,y))
        case x ~ OpGE ~ y => UclOperatorApplication(GEOp(), List(x,y))
        case x ~ OpEQ ~ y => UclOperatorApplication(EqualityOp(), List(x, y))
        case x ~ OpNE ~ y => UclOperatorApplication(InequalityOp(), List(x, y))
        case x ~ OpConcat ~ y => UclOperatorApplication(ConcatOp(), List(x,y))
        case x ~ OpAdd ~ y => UclOperatorApplication(AddOp(), List(x,y))
        case x ~ OpSub ~ y => UclOperatorApplication(SubOp(), List(x,y))
        case x ~ OpMul ~ y => UclOperatorApplication(MulOp(), List(x,y))
      }
    
      lazy val RelOp: Parser[String] = OpGT | OpLT | OpEQ | OpNE | OpGE | OpLE
      lazy val UnOp: Parser[String] = OpNeg | OpMinus
      lazy val RecordSelectOp: Parser[Identifier] = ("." ~> Id)
      lazy val ArraySelectOp: Parser[List[Expr]] =
        ("[" ~> Expr ~ rep("," ~> Expr) <~ "]") ^^ 
        {case e ~ es => (e :: es)}
      lazy val ArrayStoreOp: Parser[(List[Expr],Expr)] =
        ("[" ~> (Expr ~ rep("," ~> Expr) ~ (":=" ~> Expr)) <~ "]") ^^ 
        {case e ~ es ~ r => (e :: es, r)}
      lazy val ExtractOp: Parser[ExtractOp] =
        ("[" ~> Number ~ ":" ~ Number <~ "]") ^^ { case x ~ ":" ~ y => lang.ExtractOp(x, y) }
      lazy val Id: PackratParser[Identifier] = ident ^^ {case i => Identifier(i)}
      lazy val Bool: PackratParser[BoolLit] =
        "false" ^^ { _ => BoolLit(false) } | "true" ^^ { _ => BoolLit(true) }
      lazy val Number: PackratParser[IntLit] = integerLit ^^ { case intLit => IntLit(BigInt(intLit.chars, intLit.base)) }
      lazy val BitVector: PackratParser[BitVectorLit] = bitvectorLit ^^ { case bvLit => lang.BitVectorLit(bvLit.intValue, bvLit.width) }
    
      lazy val TemporalExpr0: PackratParser[Expr] = 
          TemporalExpr1 ~ TemporalOpUntil  ~ TemporalExpr0 ^^ ast_binary | TemporalExpr1 
      lazy val TemporalExpr1: PackratParser[Expr] =
        TemporalExpr2 ~ TemporalOpWUntil  ~ TemporalExpr1 ^^ ast_binary | TemporalExpr2
      lazy val TemporalExpr2: PackratParser[Expr] =
        TemporalExpr3 ~ TemporalOpRelease  ~ TemporalExpr2 ^^ ast_binary | TemporalExpr3
      lazy val TemporalExpr3: PackratParser[Expr] = 
        TemporalOpFinally ~> TemporalExpr4 ^^ { case expr => UclTemporalOpFinally(expr) } | TemporalExpr4
      lazy val TemporalExpr4: PackratParser[Expr] = 
        TemporalOpGlobally ~> TemporalExpr5 ^^ { case expr => UclTemporalOpGlobally(expr) } | TemporalExpr5
      lazy val TemporalExpr5: PackratParser[Expr] = 
        TemporalOpNext ~> E0 ^^ { case expr => UclTemporalOpNext(expr) } | E0
        
      /** E0 := E1 OpEquiv E0 | E1  **/
      lazy val E0: PackratParser[Expr] = E1 ~ OpBiImpl ~ E0 ^^ ast_binary | E1
      /** E1 := E2 OpImpl E1 | E2  **/
      lazy val E1: PackratParser[Expr] = E2 ~ OpImpl ~ E1 ^^ ast_binary | E2
      /** E2 := E3 OpAnd E2 | E3 OpOr E2 | E3 **/
      lazy val E2: PackratParser[Expr] = E3 ~ OpAnd ~ E2 ^^ ast_binary | E3 ~ OpOr ~ E2 ^^ ast_binary | E3
      /** E3 := E4 OpRel E3 | E4  **/
      lazy val E3: PackratParser[Expr] = E4 ~ RelOp ~ E4 ^^ ast_binary | E4
      /** E4 := E5 OpConcat E4 | E5 **/
      lazy val E4: PackratParser[Expr] = E5 ~ OpConcat ~ E4 ^^ ast_binary | E5
      /** E5 := E6 OpAdd E5 | E6 **/
      lazy val E5: PackratParser[Expr] = E6 ~ OpAdd ~ E5 ^^ ast_binary | E6
      /** E6 := E6 OpSub E6 | E7 **/
      lazy val E6: PackratParser[Expr] = E7 ~ OpSub ~ E7 ^^ ast_binary | E7
      /** E6 := E7 OpMul E6 | E7 **/
      lazy val E7: PackratParser[Expr] = E8 ~ OpMul ~ E8 ^^ ast_binary | E8
      /** E8 := UnOp E9 | E9 **/
      lazy val E8: PackratParser[Expr] = OpNeg ~> E9 ^^ { case e => UclOperatorApplication(NegationOp(), List(e)) } | E9
      /** E9 := E10 MapOp | E10 **/
      lazy val E9: PackratParser[Expr] =
          E10 ~ ExprList ^^ { case e ~ f => UclFuncApplication(e, f) } |
          E10 ~ ArraySelectOp ^^ { case e ~ m => UclArraySelectOperation(e, m) } |
          E10 ~ ArrayStoreOp ^^ { case e ~ m => UclArrayStoreOperation(e, m._1, m._2) } |
          E10 ~ ExtractOp ^^ { case e ~ m => UclOperatorApplication(m, List(e)) } |
          E10
      /** E10 := false | true | Number | Bitvector | Id FuncApplication | (Expr) **/
      lazy val E10: PackratParser[Expr] =
          Bool |
          Number |
          BitVector |
          "{" ~> Expr ~ rep("," ~> Expr) <~ "}" ^^ {case e ~ es => Record(e::es)} |
          KwITE ~> ("(" ~> Expr ~ ("," ~> Expr) ~ ("," ~> Expr) <~ ")") ^^ { case e ~ t ~ f => UclITE(e,t,f) } |
          KwLambda ~> (IdTypeList) ~ ("." ~> Expr) ^^ { case idtyps ~ expr => UclLambda(idtyps, expr) } |
          "(" ~> Expr <~ ")" |
          Id
      /** Expr := TemporalExpr0 **/
      lazy val Expr: PackratParser[Expr] = TemporalExpr0
      lazy val ExprList: Parser[List[Expr]] =
        ("(" ~> Expr ~ rep("," ~> Expr) <~ ")") ^^ { case e ~ es => e::es } |
        "(" ~> ")" ^^ { case _ => List.empty[Expr] }
    
      /** Examples of allowed types are bool | int | [int,int,bool] int **/
      lazy val PrimitiveType : PackratParser[UclType] =
        KwBool ^^ {case _ => UclBoolType()}   | 
        KwInt ^^ {case _ => UclIntType()}     |
        bitVectorType ^^ {case bvType => UclBitVectorType(bvType.width)}
        
      lazy val EnumType : PackratParser[UclEnumType] =
        KwEnum ~> ("{" ~> Id) ~ rep("," ~> Id) <~ "}" ^^ { case id ~ ids => UclEnumType(id::ids) }
      lazy val RecordType : PackratParser[UclRecordType] =
        KwRecord ~> ("{" ~> IdType) ~ rep("," ~> IdType) <~ "}" ^^ 
        { case id ~ ids => UclRecordType(id::ids) }
      lazy val MapType : PackratParser[UclMapType] =
        PrimitiveType ~ rep ("*" ~> PrimitiveType) ~ ("->" ~> Type) ^^
          { case t ~ ts ~ rt => UclMapType(t :: ts, rt)}
      lazy val ArrayType : PackratParser[UclArrayType] =
        ("[") ~> PrimitiveType ~ (rep ("," ~> PrimitiveType) <~ "]") ~ Type ^^
          { case t ~ ts ~ rt => UclArrayType(t :: ts, rt)}
      lazy val SynonymType : PackratParser[UclSynonymType] = Id ^^ { case id => UclSynonymType(id) }
      lazy val Type : PackratParser[UclType] = 
        MapType | ArrayType | EnumType | RecordType | PrimitiveType | SynonymType
    
      lazy val IdType : PackratParser[(Identifier,UclType)] =
        Id ~ (":" ~> Type) ^^ { case id ~ typ => (id,typ)}
    
      lazy val IdTypeList : PackratParser[List[(Identifier,UclType)]] =
        "(" ~> IdType ~ (rep ("," ~> IdType) <~ ")") ^^ { case t ~ ts =>  t :: ts} |
        "(" ~ ")" ^^ { case _~_ => List.empty[(Identifier,UclType)] }
    
      lazy val Lhs : PackratParser[UclLhs] =
        Id ~ ArraySelectOp ~ RecordSelectOp ~ rep(RecordSelectOp) ^^ 
          { case id ~ mapOp ~ rOp ~ rOps => UclLhs(id, Some(mapOp), Some(rOp::rOps))} |
        Id ~ ArraySelectOp ^^ { case id ~ op => UclLhs(id, Some(op), None) } |
        Id ~ RecordSelectOp ~ rep(RecordSelectOp) ^^ { case id ~ rOp ~ rOps => UclLhs(id, None, Some(rOp::rOps)) } |
        Id ^^ { case id => UclLhs(id, None, None) }
    
      lazy val LhsList: PackratParser[List[UclLhs]] =
        ("(" ~> Lhs ~ rep("," ~> Lhs) <~ ")") ^^ { case l ~ ls => l::ls } |
        "(" ~> ")" ^^ { case _ => List.empty[UclLhs] }
    
      lazy val RangeExpr: PackratParser[(IntLit,IntLit)] =
        KwRange ~> ("(" ~> Number ~ ("," ~> Number) <~ ")") ^^ { case x ~ y => (x,y) }
    
      lazy val LocalVarDecl : PackratParser[UclLocalVarDecl] =
        KwLocalVar ~> IdType <~ ";" ^^ { case (id,typ) => UclLocalVarDecl(id,typ)}
        
      lazy val Statement: PackratParser[UclStatement] =
        KwSkip <~ ";" ^^ { case _ => UclSkipStmt() } |
        KwAssert ~> Expr <~ ";" ^^ { case e => UclAssertStmt(e) } |
        KwAssume ~> Expr <~ ";" ^^ { case e => UclAssumeStmt(e) } |
        KwHavoc ~> Id <~ ";" ^^ { case id => UclHavocStmt(id) } |
        Lhs ~ rep("," ~> Lhs) ~ ":=" ~ Expr ~ rep("," ~> Expr) <~ ";" ^^
          { case l ~ ls ~ ":=" ~ r ~ rs => UclAssignStmt(l::ls, r::rs) } |
        KwCall ~> LhsList ~ (":=" ~> Id) ~ ExprList <~ ";" ^^
          { case lhss ~ id ~ args => UclProcedureCallStmt(id, lhss, args) } |
        KwIf ~> Expr ~ BlockStatement ~ (KwElse ~> BlockStatement) ^^
          { case e ~ f ~ g => UclIfElseStmt(e,f,g)} |
        KwCase ~> rep(CaseBlockStmt) <~ KwEsac ^^ 
          { case i => UclCaseStmt(i) } |
        KwFor ~> (Id ~ (KwIn ~> RangeExpr) ~ BlockStatement) ^^
          { case id ~ r ~ body => UclForStmt(id, r, body) }
        
      lazy val CaseBlockStmt: PackratParser[(Expr, List[UclStatement])] = 
        Expr ~ (":" ~> BlockStatement) ^^ { case e ~ ss => (e,ss) }
      lazy val BlockStatement: PackratParser[List[UclStatement]] = "{" ~> rep (Statement) <~ "}"
    
      lazy val ProcedureDecl : PackratParser[UclProcedureDecl] =
        KwProcedure ~> Id ~ IdTypeList ~ (KwReturns ~> IdTypeList) ~ 
          ("{" ~> rep(LocalVarDecl)) ~ (rep(Statement) <~ "}") ^^ 
          { case id ~ args ~ outs ~ decls ~ body =>  
            UclProcedureDecl(id, UclProcedureSig(args,outs), decls, body) } |
        KwProcedure ~> Id ~ IdTypeList ~ ("{" ~> rep(LocalVarDecl)) ~ (rep(Statement) <~ "}") ^^
          { case id ~ args ~ decls ~ body => 
            UclProcedureDecl(id, UclProcedureSig(args, List.empty[(Identifier,UclType)]), decls, body) }
    
      lazy val TypeDecl : PackratParser[UclTypeDecl] =
        KwType ~> Id ~ ("=" ~> Type) <~ ";" ^^ { case id ~ t => UclTypeDecl(id,t) }
        
      lazy val VarDecl : PackratParser[UclStateVarDecl] =
        KwVar ~> IdType <~ ";" ^^ { case (id,typ) => UclStateVarDecl(id,typ)}
        
      lazy val InputDecl : PackratParser[UclInputVarDecl] =
        KwInput ~> IdType <~ ";" ^^ { case (id,typ) => UclInputVarDecl(id,typ)}
        
      lazy val OutputDecl : PackratParser[UclOutputVarDecl] =
        KwOutput ~> IdType <~ ";" ^^ { case (id,typ) => UclOutputVarDecl(id,typ)}
        
      lazy val ConstDecl : PackratParser[UclConstantDecl] =
        KwConst ~> IdType <~ ";" ^^ { case (id,typ) => UclConstantDecl(id,typ)}
        
      lazy val FuncDecl : PackratParser[UclFunctionDecl] =
        KwFunction ~> Id ~ IdTypeList ~ (":" ~> Type) <~ ";" ^^ 
        { case id ~ idtyps ~ rt => UclFunctionDecl(id, UclFunctionSig(idtyps, rt)) }
        
      lazy val InitDecl : PackratParser[UclInitDecl] = KwInit ~> BlockStatement ^^ 
        { case b => UclInitDecl(b) }
      
      lazy val NextDecl : PackratParser[UclNextDecl] = KwNext ~> BlockStatement ^^ 
        { case b => UclNextDecl(b) }
        
      lazy val Decl: PackratParser[UclDecl] = 
        (TypeDecl | ConstDecl | FuncDecl | VarDecl | InputDecl | OutputDecl | ProcedureDecl | InitDecl | NextDecl | SpecDecl)
    
      // control commands.
      lazy val InitializeCmd : PackratParser[UclInitializeCmd] = 
        KwInitialize <~ ";" ^^ { case _ => UclInitializeCmd() }
      
      lazy val UnrollCmd : PackratParser[UclUnrollCmd] = 
        KwUnroll ~ "(" ~> Number <~ ")" ~ ";" ^^ { case num => UclUnrollCmd(num) }
    
      lazy val SimulateCmd : PackratParser[UclSimulateCmd] =
        KwSimulate ~ "(" ~> Number <~ ")" ~ ";" ^^ { case num => UclSimulateCmd(num) }
      
      lazy val Cmd : PackratParser[UclCmd] =
        ( InitializeCmd | UnrollCmd | SimulateCmd )
      
      lazy val BlockCmd : PackratParser[List[UclCmd]] = KwControl ~ "{" ~> rep(Cmd) <~ "}"
      
      lazy val Module: PackratParser[UclModule] =
        KwModule ~> Id ~ ("{" ~> rep(Decl) ~ ( BlockCmd.? ) <~ "}") ^^ { 
          case id ~ (decls ~ Some(cs)) => UclModule(id, decls, cs)
          case id ~ (decls ~ None) => UclModule(id, decls, List[UclCmd]())
        }
    
      lazy val SpecDecl: PackratParser[UclSpecDecl] =
        KwDefineProp ~> Id ~ (":" ~> Expr) <~ ";" ^^ { case id ~ expr => UclSpecDecl(id,expr) }
    
      lazy val Model: PackratParser[List[UclModule]] = rep(Module) 
        
      def parseExpr(input: String): Expr = {
        val tokens = new PackratReader(new lexical.Scanner(input))
        phrase(Expr)(tokens) match {
          case Success(ast, _) => ast
          case e: NoSuccess => throw new IllegalArgumentException(e.toString)
        }
      }
    
      def parseModule(input: String): UclModule = {
        val tokens = new PackratReader(new lexical.Scanner(input))
        phrase(Module)(tokens) match {
          case Success(ast, _) => ast
          case e: NoSuccess => throw new IllegalArgumentException(e.toString)
        }
      }
      
      def parseModel(text: String): List[UclModule] = {
        val tokens = new PackratReader(new lexical.Scanner(text))
        phrase(Model)(tokens) match {
          case Success(ast, _) => ast
          case e: NoSuccess => throw new IllegalArgumentException(e.toString)
        }
      }
    }
  }
}
