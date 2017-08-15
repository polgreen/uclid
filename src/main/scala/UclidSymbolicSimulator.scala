
/**
 * @author rohitsinha
 */

object UniqueIdGenerator {
  var i : Int = 0;
  def unique() : Int = {i = i + 1; return i}
}

object UclidSymbolicSimulator {
  var asserts : List[SMTExpr] = List.empty
  
  type SymbolTable = Map[UclIdentifier, SMTExpr];
  
  def newHavocSymbol(name: String, t: SMTType) = new SMTSymbol("_ucl_" + name + UniqueIdGenerator.unique(), t)
  def newInputSymbol(name: String, step: Int, t: SMTType) = new SMTSymbol("_ucl_" + step +"_" + name, t)
  def newConstantSymbol(name: String, t: SMTType) = new SMTSymbol(name,t)
  
  def simulate_steps(m: UclModule, number_of_steps: Int) : SymbolTable = {
    var st : SymbolTable = Map.empty;
    
    var c : Context = new Context();
    c.extractContext(m);
    st = c.constants.foldLeft(st)((acc,i) => st.updated(i._1, 
        newConstantSymbol(i._1.value, toSMT(c.constants(i._1)))));
    st = c.variables.foldLeft(st)((acc,i) => st.updated(i._1, 
        newHavocSymbol(i._1.value, toSMT(c.variables(i._1)))));
    st = c.outputs.foldLeft(st)((acc,i) => st.updated(i._1, 
        newHavocSymbol(i._1.value, toSMT(c.outputs(i._1)))));
    for (step <- 1 to number_of_steps) {
      st = c.inputs.foldLeft(st)((acc,i) => st.updated(i._1, 
          newInputSymbol(i._1.value, step, toSMT(c.inputs(i._1)))));
      st = simulate(m, st, c);
    }
    
    return st
  }
  
  def toSMT(t: UclType) : SMTType = {
    def dealWithFunc(inTypes: List[UclType], outType: UclType) : Unit = {
      if (inTypes.filter { x => !(x.isInstanceOf[UclBoolType] || x.isInstanceOf[UclIntType]) }.size > 0 ||
          !(outType.isInstanceOf[UclBoolType] || outType.isInstanceOf[UclIntType])
      ) {
        throw new UclidUtils.UnimplementedException("Primitive map types implemented thus far")
      }
    }
    t match {
      case UclIntType() => return SMTIntType()
      case UclBoolType() => return SMTBoolType()
      case UclMapType(inTypes,outType) => 
        dealWithFunc(inTypes, outType); 
        return SMTMapType(inTypes.map(t => toSMT(t)), toSMT(outType))
      case UclArrayType(inTypes,outType) => 
        dealWithFunc(inTypes, outType); 
        return SMTArrayType(inTypes.map(t => toSMT(t)), toSMT(outType))
    }
  }
  
  case class SMTIntLTOperator() extends SMTOperator { override def toString = "<" }
case class SMTIntLEOperator() extends SMTOperator { override def toString = "<=" }
case class SMTIntGTOperator() extends SMTOperator { override def toString = ">" }
case class SMTIntGEOperator() extends SMTOperator { override def toString = ">=" }
case class SMTIntAddOperator() extends SMTOperator { override def toString = "+" }
case class SMTIntSubOperator() extends SMTOperator { override def toString = "-" }
case class SMTIntMulOperator() extends SMTOperator { override def toString = "*" }
  
  def toSMT(op: UclOperator) : SMTOperator = {
    op match {
      case UclLTOperator() => return SMTIntLTOperator()
      case UclLEOperator() => return SMTIntLEOperator()
      case UclGTOperator() => return SMTIntGTOperator()
      case UclGEOperator() => return SMTIntGEOperator()
      case UclAddOperator () => return SMTIntAddOperator()
      case UclMulOperator () => return SMTIntMulOperator()
    }
  }
  
  def simulate(stmts: List[UclStatement], symbolTable: SymbolTable, c: Context) : SymbolTable = {
    return stmts.foldLeft(symbolTable)((acc,i) => simulate(i, acc, c));
  }
  
  def simulate(m: UclModule, symbolTable: SymbolTable, c: Context) : SymbolTable = {
    return simulate(c.next, symbolTable, c)
  }
  
  /*
  def simulate(p: UclProcedureDecl, symbolTable: SymbolTable, c: Context) : SymbolTable = {
    var st: SymbolTable = symbolTable;
    var c2: Context = c.copyContext()
    c2.inputs = c.inputs ++ (p.sig.inParams.map(i => i._1 -> i._2).toMap)
    c2.variables = c.variables ++ (p.sig.outParams.map(i => i._1 -> i._2).toMap)
    c2.variables = c2.variables ++ (p.decls.map(i => i.id -> i.typ).toMap)
    st = p.decls.foldLeft(st)((acc,i) => st.updated(i.id, newHavocSymbol(i.id.value, toSMT(i.typ))));
    return simulate(p.body, symbolTable, c2)
  }
  * */
  
  def simulate(s: UclStatement, symbolTable: SymbolTable, c: Context) : SymbolTable = {
    def simulateAssign(lhss: List[UclLhs], args: List[SMTExpr], input: SymbolTable) : SymbolTable = {
      var st : SymbolTable = input;
      def lhs(i: (UclLhs,SMTExpr)) = { i._1 }
      def rhs(i: (UclLhs,SMTExpr)) = { i._2 }
      (lhss zip args).foreach { x =>
        var arrayId: UclIdentifier = lhs(x).id;
        x._1.arraySelect match {
          case Some(as) => throw new UclidUtils.UnimplementedException("Unimplemented arrays in LHS")
          case None => st = st.updated(lhs(x).id, rhs(x))
        }
        x._1.recordSelect match {
          case Some(rs) => throw new UclidUtils.UnimplementedException("Unimplemented records in LHS")
          case None => st = st.updated(lhs(x).id, rhs(x))
        }
      }
      return st
    }
    s match {
      case UclSkipStmt() => return symbolTable
      case UclAssertStmt(e) => throw new UclidUtils.UnimplementedException("err");
      case UclAssumeStmt(e) => throw new UclidUtils.UnimplementedException("err");
      case UclHavocStmt(id) => 
        return symbolTable.updated(id, newHavocSymbol(id.value, toSMT(c.variables(id))))
      case UclAssignStmt(lhss,rhss) =>
        val es = rhss.map(i => evaluate(i, symbolTable, c));
        return simulateAssign(lhss, es, symbolTable)
      case UclIfElseStmt(e,then_branch,else_branch) =>
        var then_modifies : Set[UclIdentifier] = writeSet(then_branch,c)
        var else_modifies : Set[UclIdentifier] = writeSet(else_branch,c)
        //compute in parallel
        var then_st : SymbolTable = simulate(then_branch, symbolTable, c)
        var else_st : SymbolTable = simulate(else_branch, symbolTable, c)
        return symbolTable.keys.filter { id => then_modifies.contains(id) || else_modifies.contains(id) }.
          foldLeft(symbolTable){ (acc,id) => 
            acc.updated(id, SMTITE(evaluate(e, symbolTable,c), then_st(id), else_st(id)))
          }
      case UclForStmt(id, range, body) => throw new UclidUtils.UnimplementedException("Cannot symbolically execute For loop")
      case UclCaseStmt(body) => throw new UclidUtils.UnimplementedException("Cannot symbolically execute Case stmt")
      case UclProcedureCallStmt(id,lhss,args) =>
        var st : SymbolTable = (c.procedures(id).sig.inParams zip args).
          foldLeft(symbolTable){(acc,x) => acc.updated(x._1._1, evaluate(x._2, symbolTable, c) )}
        var c2: Context = c.copyContext()
        val proc: UclProcedureDecl = c.procedures(id)
        c2.inputs = c.inputs ++ (proc.sig.inParams.map(i => i._1 -> i._2).toMap)
        c2.variables = c.variables ++ (proc.sig.outParams.map(i => i._1 -> i._2).toMap)
        c2.variables = c2.variables ++ (proc.decls.map(i => i.id -> i.typ).toMap)
        st = proc.decls.foldLeft(st)((acc,i) => acc.updated(i.id, newHavocSymbol(i.id.value, toSMT(i.typ))));
        st = simulate(proc.body, st, c2)
        return simulateAssign(lhss, proc.sig.outParams.map(i => st(i._1)), symbolTable)
      case _ => return symbolTable
    }
  }
  
  def writeSet(stmts: List[UclStatement], c: Context) : Set[UclIdentifier] = {
    def stmtWriteSet(stmt: UclStatement, c: Context) : Set[UclIdentifier] = stmt match {
      case UclSkipStmt() => Set.empty
      case UclAssertStmt(e) => Set.empty
      case UclAssumeStmt(e) => Set.empty
      case UclHavocStmt(id) => Set(id)
      case UclAssignStmt(lhss,rhss) => 
        return lhss.map { lhs => 
          var lhs_id : String = lhs.id.value;
          lhs.recordSelect match {
            case Some(rs) => throw new UclidUtils.UnimplementedException("Unimplemented records in LHS")
            case None => UclIdentifier(lhs_id)
          }
        }.toSet
      case UclIfElseStmt(e,then_branch,else_branch) => 
        return writeSet(then_branch,c) ++ writeSet(else_branch,c)
      case UclForStmt(id, range, body) => return writeSet(body,c)
      case UclCaseStmt(body) => 
        return body.foldLeft(Set.empty[UclIdentifier]) { (acc,i) => acc ++ writeSet(i._2,c) }
      case UclProcedureCallStmt(id,lhss,args) => return writeSet(c.procedures(id).body,c)
    }
    return stmts.foldLeft(Set.empty[UclIdentifier]){(acc,s) => acc ++ stmtWriteSet(s,c)}
  }

  def substitute(e: UclExpr, id: UclIdentifier, arg: UclExpr) : UclExpr = {
     e match {
       case UclBiImplication(l,r) => 
         return UclBiImplication(substitute(l,id,arg), substitute(r,id,arg))
       case UclImplication(l,r) =>
         return UclImplication(substitute(l,id,arg), substitute(r,id,arg))
       case UclConjunction(l,r) => 
         return UclConjunction(substitute(l,id,arg), substitute(r,id,arg))
       case UclDisjunction(l,r) => 
         return UclDisjunction(substitute(l,id,arg), substitute(r,id,arg))
       case UclNegation(l) => return UclNegation(substitute(l,id,arg))
       case UclEquality(l,r) => 
         return UclEquality(substitute(l,id,arg), substitute(r,id,arg))
       case UclIFuncApplication(op,args) =>
         return UclIFuncApplication(op, args.map(x => substitute(x, id, arg)))
       case UclArraySelectOperation(a,index) => 
         return UclArraySelectOperation(a, index.map(x => substitute(x, id, arg)))
       case UclArrayStoreOperation(a,index,value) => 
         return UclArrayStoreOperation(a, index.map(x => substitute(x, id, arg)), substitute(value, id, arg))
       case UclFuncApplication(f,args) => 
         return UclFuncApplication(substitute(f,id,arg), args.map(x => substitute(x,id,arg)))
       case UclITE(cond,t,f) =>
         return UclITE(substitute(cond,id,arg), substitute(t,id,arg), substitute(f,id,arg))
       case UclLambda(idtypes, le) =>
         UclidUtils.assert(idtypes.exists(x => x._1.value == id.value), "Lambda arguments of the same name")
         return UclLambda(idtypes, substitute(le, id, arg))
       case UclNumber(n) => return e
       case UclBoolean(b) => return e
       case UclIdentifier(i) => return (if (id.value == i) arg else e)
       case _ => throw new UclidUtils.UnimplementedException("Should not get here")
     }
  }
  
  def evaluate(e: UclExpr, symbolTable: SymbolTable, c: Context) : SMTExpr = {
     e match { //check that all identifiers in e have been declared
       case UclBiImplication(l,r) => 
         return SMTBiImplication(evaluate(l,symbolTable,c), evaluate(r,symbolTable,c))
       case UclImplication(l,r) =>
         return SMTImplication(evaluate(l,symbolTable,c), evaluate(r,symbolTable,c))
       case UclConjunction(l,r) => 
         return SMTConjunction(evaluate(l,symbolTable,c), evaluate(r,symbolTable,c))
       case UclDisjunction(l,r) => 
         return SMTDisjunction(evaluate(l,symbolTable,c), evaluate(r,symbolTable,c))
       case UclNegation(l) => return SMTNegation(evaluate(l,symbolTable,c))
       case UclEquality(l,r) => 
         return SMTEquality(evaluate(l,symbolTable,c), evaluate(r,symbolTable,c))
       case UclIFuncApplication(op,args) =>
         return SMTIFuncApplication(toSMT(op), args.map(i => evaluate(i, symbolTable, c)))
       case UclArraySelectOperation(a,index) => 
         if (index.size > 1) throw new UclidUtils.UnimplementedException("Not handling multiple array indices")
         var index0 = evaluate(index(0), symbolTable, c);
         return SMTArraySelectOperation(evaluate(a, symbolTable, c), List(index0))
       case UclArrayStoreOperation(a,index,value) => 
         if (index.size > 1) throw new UclidUtils.UnimplementedException("Not handling multiple array indices")
         var index0 = evaluate(index(0), symbolTable, c);
         return SMTArrayStoreOperation(evaluate(a, symbolTable, c), List(index0), evaluate(value, symbolTable,c))
       case UclFuncApplication(f,args) => f match {
         case UclIdentifier(id) => 
           return SMTFuncApplication(evaluate(f, symbolTable,c), args.map(i => evaluate(i,symbolTable,c)))
         case UclLambda(idtypes,le) => //do beta sub
           var le_sub = (idtypes.map(x => x._1) zip args).foldLeft(le){(acc,x) => substitute(acc, x._1, x._2)}
           return evaluate(le_sub, symbolTable, c)
       }
       case UclITE(cond,t,f) =>
         return SMTITE(evaluate(cond,symbolTable,c), evaluate(t,symbolTable,c), evaluate(f,symbolTable,c))
       case UclLambda(ids,le) => 
         return SMTLambda(ids.map(i => SMTSymbol(i._1.value, toSMT(i._2))), evaluate(le,symbolTable,c))
       case UclNumber(n) => SMTNumber(n)
       case UclBoolean(b) => SMTBoolean(b)
       case UclIdentifier(id) => symbolTable(UclIdentifier(id))
       case _ => throw new UclidUtils.UnimplementedException("Should not get here")
    }
  }
  
}
