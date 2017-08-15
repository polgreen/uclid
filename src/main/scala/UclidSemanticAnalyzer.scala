 /* Created by Rohit Sinha on 5/28/15.
 */

class Context {
  var procedures: Map[UclIdentifier, UclProcedureDecl] = _
  var functions: Map[UclIdentifier, UclFunctionSig] = _
  var variables: Map[UclIdentifier, UclType] = _
  var inputs: Map[UclIdentifier, UclType] = _
  var outputs: Map[UclIdentifier, UclType] = _
  var constants: Map[UclIdentifier, UclType] = _
  var types : Map[UclIdentifier, UclType] = _
  var next: List[UclStatement] = _
  var init: List[UclStatement] = _
  
  override def toString = variables.toString
  
  def extractContext(m: UclModule) = {    
    type T1 = UclProcedureDecl
    val m_procs = m.decls.filter { x => x.isInstanceOf[T1] }
    UclidUtils.assert(m_procs.map(i => i.asInstanceOf[T1].id).distinct.size == 
      m_procs.map(i => i.asInstanceOf[T1].id).size, "Multiple procedures with identical names")
    procedures = m_procs.map(x => x.asInstanceOf[T1].id -> x.asInstanceOf[T1]).toMap
    
    type T2 = UclFunctionDecl
    val m_funcs = m.decls.filter { x => x.isInstanceOf[T2] }
    UclidUtils.assert(m_funcs.map(i => i.asInstanceOf[T2].id).distinct.size == 
      m_funcs.map(i => i.asInstanceOf[T2].id).size, "Multiple functions with identical names")
    functions = m_funcs.map(x => x.asInstanceOf[T2].id -> x.asInstanceOf[T2].sig).toMap
    
    type T3 = UclStateVarDecl
    val m_vars = m.decls.filter { x => x.isInstanceOf[T3] }
    UclidUtils.assert(m_vars.map(i => i.asInstanceOf[T3].id).distinct.size == 
      m_vars.map(i => i.asInstanceOf[T3].id).size, "Multiple variables with identical names")
    variables = m_vars.map(x => x.asInstanceOf[T3].id -> x.asInstanceOf[T3].typ).toMap
    
    type T4 = UclInputVarDecl
    val m_inputs = m.decls.filter { x => x.isInstanceOf[T4] }
    UclidUtils.assert(m_inputs.map(i => i.asInstanceOf[T4].id).distinct.size == 
      m_inputs.map(i => i.asInstanceOf[T4].id).size, "Multiple inputs with identical names")
    inputs = m_inputs.map(x => x.asInstanceOf[T4].id -> x.asInstanceOf[T4].typ).toMap
    
    type T5 = UclOutputVarDecl
    val m_outputs = m.decls.filter { x => x.isInstanceOf[T5] }
    UclidUtils.assert(m_outputs.map(i => i.asInstanceOf[T5].id).distinct.size == 
      m_outputs.map(i => i.asInstanceOf[T5].id).size, "Multiple outputs with identical names")
    outputs = m_outputs.map(x => x.asInstanceOf[T5].id -> x.asInstanceOf[T5].typ).toMap
    
    type T6 = UclConstantDecl
    val m_consts = m.decls.filter { x => x.isInstanceOf[T6] }
    UclidUtils.assert(m_consts.map(i => i.asInstanceOf[T6].id).distinct.size == 
      m_consts.map(i => i.asInstanceOf[T6].id).size, "Multiple constants with identical names")
    constants = m_consts.map(x => x.asInstanceOf[T6].id -> x.asInstanceOf[T6].typ).toMap

    type T7 = UclTypeDecl
    val m_typedecls = m.decls.filter { x => x.isInstanceOf[T7] }
    UclidUtils.assert(m_typedecls.map(i => i.asInstanceOf[T7].id).distinct.size == 
      m_typedecls.map(i => i.asInstanceOf[T7].id).size, "Multiple typedecls with identical names")
    types = m_typedecls.map(x => x.asInstanceOf[T7].id -> x.asInstanceOf[T7].typ).toMap
    
    val m_nextdecl = m.decls.filter { x => x.isInstanceOf[UclNextDecl] }
    UclidUtils.assert(m_nextdecl.size == 1, "Need exactly one next decl");
    next = (m_nextdecl(0)).asInstanceOf[UclNextDecl].body
    
    val m_initdecl = m.decls.filter { x => x.isInstanceOf[UclInitDecl] }
    UclidUtils.assert(m_initdecl.size == 1, "Need exactly one init decl");
    init = (m_initdecl(0)).asInstanceOf[UclInitDecl].body
  }
  
  def copyContext() : Context = {
    var copy: Context = new Context()
    copy.constants = this.constants
    copy.functions = this.functions
    copy.inputs = this.inputs
    copy.outputs = this.outputs
    copy.procedures = this.procedures
    copy.types = this.types
    copy.variables = this.variables
    copy.init = this.init
    copy.next = this.next
    return copy
  }
  
  def externalDecls() : List[UclIdentifier] = {
    return this.constants.keys.toList ++ this.functions.keys.toList ++ this.inputs.keys.toList ++ 
      this.outputs.keys.toList ++ this.types.keys.toList ++ this.variables.keys.toList ++ 
      this.procedures.keys.toList
  }
}

object UclidSemanticAnalyzer {
  def checkSemantics(m: UclModule) : Unit = {
    var c: Context = new Context()
    c.extractContext(m)
    check(m,c)
  }
  
  def check(m: UclModule, c: Context) : Unit = {
    //check module level stuff
    def checkRedeclarationAndTypes(decls: Map[UclIdentifier, UclType]) : Unit = {
      decls.foreach(i => { UclidUtils.assert(UclidUtils.existsOnce(c.externalDecls(), i._1), 
          "Declaration " + i + " has conflicting name") })
    }
    checkRedeclarationAndTypes(c.constants)
    checkRedeclarationAndTypes(c.inputs)
    checkRedeclarationAndTypes(c.outputs)
    checkRedeclarationAndTypes(c.variables)
    checkRedeclarationAndTypes(c.types)
    
    m.decls.foreach { x => check(x,c) }
  }
  
  def check(d: UclDecl, c: Context) : Unit = {
    val externalDecls : List[UclIdentifier] = c.externalDecls()
    d match {
      case UclProcedureDecl(id,sig,decls,body) =>
        UclidUtils.assert(UclidUtils.hasDuplicates(sig.inParams.map(i => i._1) ++ sig.outParams.map(i => i._1)),
          "Signature of procedure " + id + " contains arguments of the same name")
        (sig.inParams ++ sig.outParams).foreach(i => { 
          UclidUtils.assert(UclidUtils.existsOnce(
              (externalDecls ++ sig.inParams.map(j => j._1) ++ sig.outParams.map(j => j._1)), i._1),
              "Signature of procedure " + id + " contains redeclaration: " + i);
          check(i._2, c)
        })
     
        decls.foreach { i => {
          //check that name is not reused
          UclidUtils.assert(UclidUtils.existsNone(
              externalDecls ++ sig.inParams.map(x => x._1) ++ sig.outParams.map(x => x._1), i.id), 
              "Local variable " + i + " redeclared")
          c.procedures.keys.
            filter{j => id.value != j.value}.
            foreach{ j => UclidUtils.assert(UclidUtils.existsNone(c.procedures(j).decls.map(k => k.id), i.id), 
                "Local variable " + i + " redeclared as a local variable of procedure " + j ) }
          check(i.typ,c)
        } }
        
        var c2: Context = c.copyContext()
        c2.inputs = c.inputs ++ (sig.inParams.map(i => i._1 -> i._2).toMap)
        c2.variables = c.variables ++ (sig.outParams.map(i => i._1 -> i._2).toMap)
        c2.variables = c2.variables ++ (decls.map(i => i.id -> i.typ).toMap)
        body.foreach { x => check(x,c2) }
        
      case UclFunctionDecl(id,sig) =>
        UclidUtils.assert(UclidUtils.hasDuplicates(sig.args.map(i => i._1)), 
            "Signature of function " + id + " contains arguments of the same name")
        sig.args.foreach(i => { 
          //check that name is not reused
          UclidUtils.assert(UclidUtils.existsNone(externalDecls, i._1), 
              "Signature of function " + id + " contains redeclaration: " + i)
          check(i._2, c)
        })
      case UclTypeDecl(id,typ) => check(typ, c)
      case UclStateVarDecl(id, typ) => check(typ, c)
      case UclInputVarDecl(id,typ) => check(typ, c)
      case UclOutputVarDecl(id, typ) => check(typ, c)
      case UclConstantDecl(id, typ) => check(typ, c)
      case UclInitDecl(body) => body.foreach{x => check(x,c)}
      case UclNextDecl(body) => body.foreach{x => check(x,c)}
    }
  }
  
  def transitiveType(typ: UclType, c: Context) : UclType = {
    val externalDecls : List[UclIdentifier] = c.externalDecls()
    if (typ.isInstanceOf[UclEnumType]) {
      return typ
    } else if (typ.isInstanceOf[UclRecordType]) {
      val t = typ.asInstanceOf[UclRecordType]
      return UclRecordType(t.fields.map(i => (i._1, transitiveType(i._2,c))))
    } else if (typ.isInstanceOf[UclMapType]) {
      val t = typ.asInstanceOf[UclMapType]
      return UclMapType(t.inTypes.map(i => transitiveType(i,c)), transitiveType(t.outType,c))
    } else if (typ.isInstanceOf[UclArrayType]) {
      val t = typ.asInstanceOf[UclArrayType]
      return UclArrayType(t.inTypes.map(i => transitiveType(i,c)), 
          transitiveType(t.outType,c))
    } else if (typ.isInstanceOf[UclSynonymType]) {
      val t = typ.asInstanceOf[UclSynonymType]
      return transitiveType(c.types(t.id),c)
    } else { return typ }
  }
  
  def check(typ: UclType, c: Context) : Unit = {
    val externalDecls : List[UclIdentifier] = c.externalDecls()
    if (typ.isInstanceOf[UclEnumType]) {
      typ.asInstanceOf[UclEnumType].ids.foreach { i => 
        UclidUtils.assert(UclidUtils.existsNone(externalDecls, i), "Enum " + typ + " has a redeclaration")
      }
    } else if (typ.isInstanceOf[UclRecordType]) {
      val t = typ.asInstanceOf[UclRecordType]
      t.fields.foreach { i => 
        //assert no maps
        UclidUtils.assert(!(transitiveType(i._2,c).isInstanceOf[UclMapType] || 
            transitiveType(i._2,c).isInstanceOf[UclArrayType]), 
            "Records cannot contain maps or arrays")
        check(i._2, c)
      }
    } else if (typ.isInstanceOf[UclMapType]) {
      typ.asInstanceOf[UclMapType].inTypes.foreach { i => 
        UclidUtils.assert(!(transitiveType(i,c).isInstanceOf[UclMapType]), 
            "Map types cannot be indexed by maps: " + typ);
        check(i,c) 
        }
      UclidUtils.assert(!(transitiveType(typ.asInstanceOf[UclMapType].outType,c).isInstanceOf[UclMapType]), 
          "Map types cannot produce maps: " + typ)
      check(typ.asInstanceOf[UclMapType].outType, c)
    } else if (typ.isInstanceOf[UclArrayType]) {
      typ.asInstanceOf[UclArrayType].inTypes.foreach { i => 
        UclidUtils.assert(!(transitiveType(i,c).isInstanceOf[UclArrayType]), 
            "Array types cannot be indexed by arrays: " + typ);
        check(i,c) 
        }
      UclidUtils.assert(!(transitiveType(typ.asInstanceOf[UclArrayType].outType,c).isInstanceOf[UclArrayType]), 
          "Array types cannot produce arrays: " + typ)
      check(typ.asInstanceOf[UclArrayType].outType, c)
    } else if (typ.isInstanceOf[UclSynonymType]) {
      UclidUtils.assert(c.types.keys.exists { x => x.value == typ.asInstanceOf[UclSynonymType].id.value }, 
          "Synonym Type " + typ + " does not exist.")
    }
  }
  
  def typeOf(lhs: UclLhs, c: Context) : UclType = {
    var intermediateType : UclType = (c.outputs ++ c.variables)(lhs.id)
    lhs.arraySelect match {
      case Some(as) => 
        UclidUtils.assert(transitiveType(intermediateType,c).isInstanceOf[UclArrayType],
            "Cannot use select on non-array " + lhs.id)
        intermediateType = transitiveType(intermediateType,c).asInstanceOf[UclArrayType].outType
      case None => ()
    }
    lhs.recordSelect match {
      case Some(rs) => 
        UclidUtils.assert(transitiveType(intermediateType,c).isInstanceOf[UclRecordType], 
            "Expected record type: " + intermediateType)
        rs.foreach { field =>
          transitiveType(intermediateType,c).asInstanceOf[UclRecordType].fields.find(i => i._1.value == field.value) 
              match { case Some(field_type) => intermediateType = field_type._2
                      case None => UclidUtils.assert(false, "Should not get here") }
        }
        return intermediateType
      case None => return intermediateType
    }
  }
  
  def check(lhs: UclLhs, c: Context) : Unit = {
    UclidUtils.assert((c.outputs.keys ++ c.variables.keys).exists { x => x.value == lhs.id.value }, 
        "LHS variable " + lhs.id + " does not exist")
    var intermediateType = transitiveType((c.outputs ++ c.variables)(lhs.id),c)
    lhs.arraySelect match {
      case Some(index) => 
        //assert that lhs.id is a map or array
        UclidUtils.assert(intermediateType.isInstanceOf[UclArrayType],
            "Cannot use select on non-array " + lhs.id)
        intermediateType = transitiveType(intermediateType.asInstanceOf[UclArrayType].outType,c)
        index.foreach { x => check(x,c) }
      case None => ()
    }
    lhs.recordSelect match {
      case Some(rs) => 
        UclidUtils.assert(intermediateType.isInstanceOf[UclRecordType], "Expected record type: " + intermediateType)
        rs.foreach { field => 
          UclidUtils.assert(intermediateType.asInstanceOf[UclRecordType].fields.
              exists { i => i._1.value == field.value }, "Field " + field + " not found")
          intermediateType.asInstanceOf[UclRecordType].fields.find(i => i._1.value == field.value) 
              match { case Some(y) => intermediateType = transitiveType(y._2,c)
                      case None => UclidUtils.assert(false, "Should not get here") }
        }
      case None => ()
    }
  }
  
  def check(s: UclStatement, c: Context) : Unit = {
    val externalDecls : List[UclIdentifier] = c.externalDecls()
    s match {
      case UclAssertStmt(e) => check(e,c)
      case UclAssumeStmt(e) => check(e,c)
      case UclHavocStmt(id) => 
        UclidUtils.assert((c.variables.keys ++ c.outputs.keys).exists { x => x.value == id.value }, 
            "Statement " + s + " updates unknown variable")
      case UclAssignStmt(lhss, rhss) => 
        UclidUtils.assert(lhss.size == rhss.size, "LHSS and RHSS of different size: " + s);
        lhss.foreach{ x => check(x,c) }; rhss.foreach { x => check(x,c) };
        UclidUtils.assert((lhss zip rhss).forall { i => typeOf(i._1, c) == typeOf(i._2, c) }, 
            "LHSS and RHSS have conflicting types: " + s);
        UclidUtils.assert(lhss.distinct.size == lhss.size, "LHSS contains identical variables: " + s)
      case UclIfElseStmt(e, t, f) => 
        check(e,c); 
        UclidUtils.assert(typeOf(e,c) == UclBoolType(), "Expected boolean conditional");
        (t ++ f).foreach { x => check(x,c) };
      case UclForStmt(id,_,body) => 
        UclidUtils.assert(!(externalDecls.exists { x => x.value == id.value }), 
            "For Loop counter " + id + " redeclared");
        var c2: Context = c.copyContext();
        c2.inputs = c.inputs ++ Map(id -> UclIntType());
        body.foreach{x => check(x,c2)}
      case UclCaseStmt(body) => body.foreach { x =>
        check(x._1,c);
        UclidUtils.assert(typeOf(x._1,c) == UclBoolType(), 
            "Expected boolean conditional within case statement guard");
        x._2.foreach { y => check(y,c) } 
        }
      case UclProcedureCallStmt(id, lhss, args) =>
        UclidUtils.assert(UclidUtils.existsOnce(c.procedures.keys.toList, id), 
            "Calling unknown procedure " + id)
        lhss.foreach{ x => check(x,c) }
        args.foreach{ x => check(x,c) }
        UclidUtils.assert(lhss.size == c.procedures(id).sig.outParams.size, 
            "Calling procedure " + id + " with incorrect number of results")
        UclidUtils.assert((lhss zip c.procedures(id).sig.outParams.map(i => i._2)).
            forall { i => typeOf(i._1, c) == i._2 }, 
            "Calling procedure " + id + " with results of incorrect type");
        UclidUtils.assert(args.size == c.procedures(id).sig.inParams.size, 
            "Calling procedure " + id + " with incorrect number of arguments")
        UclidUtils.assert((args zip c.procedures(id).sig.inParams.map(i => i._2)).
            forall { i => typeOf(i._1, c) == i._2 }, 
            "Calling procedure " + id + " with arguments of incorrect type")
    }
  }
  
  def typeOf(e: UclExpr, c: Context) : UclType = {
    def assertBoolArgs(l: Iterable[UclExpr]) : Unit = {
      l.foreach{ i => UclidUtils.assert(typeOf(i,c) == UclBoolType(), 
          "Expected expression " + i + " to have type Bool");
      }
    }
    def assertIntArgs(l: Iterable[UclExpr]) : Unit = {
      l.foreach{ i => UclidUtils.assert(typeOf(i,c) == UclIntType(), 
          "Expected expression " + i + " to have type Int");
      }
    }
    e match {
      case UclBiImplication(l,r) => assertBoolArgs(List(l,r)); return UclBoolType()
      case UclImplication(l,r) => assertBoolArgs(List(l,r)); return UclBoolType()
      case UclConjunction(l,r) => assertBoolArgs(List(l,r)); return UclBoolType()
      case UclDisjunction(l,r) => assertBoolArgs(List(l,r)); return UclBoolType()
      case UclNegation(e) => assertBoolArgs(List(e)); return UclBoolType()
      case UclEquality(l,r) => assertIntArgs(List(l,r)); return UclBoolType()
      case UclIFuncApplication(op,es) =>
        op match {
          case UclLTOperator() => return UclBoolType()
          case UclLEOperator() => return UclBoolType()
          case UclGTOperator() => return UclBoolType()
          case UclGEOperator() => return UclBoolType()
          case UclAddOperator() => return UclIntType()
          case UclMulOperator() => return UclIntType()
          case UclExtractOperator(_,_) => throw new UclidUtils.UnimplementedException("bvextract unimplemented")
          case UclConcatOperator() => throw new UclidUtils.UnimplementedException("bvconcat unimplemented")
        }
      case UclArraySelectOperation(a,index) =>
        UclidUtils.assert(transitiveType(typeOf(a,c),c).isInstanceOf[UclArrayType], 
            "expected array type: " + e)
        UclidUtils.assert((typeOf(a,c).asInstanceOf[UclArrayType].inTypes zip index).
            forall { x => x._1 == typeOf(x._2,c) }, "Array Select operand type mismatch: " + e)
        return typeOf(a,c).asInstanceOf[UclArrayType].outType //select returns the range type
      case UclArrayStoreOperation(a,index,value) =>
        UclidUtils.assert(transitiveType(typeOf(a,c),c).isInstanceOf[UclArrayType], "expected array type: " + e)
        UclidUtils.assert((a.asInstanceOf[UclArrayType].inTypes zip index).
            forall { x => x._1 == typeOf(x._2,c) }, "Array Store operand type mismatch: " + e)
        UclidUtils.assert(a.asInstanceOf[UclArrayType].outType == typeOf(value,c), 
            "Array Store value type mismatch")
        return typeOf(a,c) //store returns the new array
      case UclFuncApplication(f,args) =>
        UclidUtils.assert(transitiveType(typeOf(f,c),c).isInstanceOf[UclMapType],"Expected Map Type " + e);
        val t = typeOf(f,c).asInstanceOf[UclMapType];
        UclidUtils.assert((t.inTypes.size == args.size), 
          "Function application has bad number of arguments: " + e);
        UclidUtils.assert((t.inTypes zip args).forall{i => i._1 == typeOf(i._2,c)}, 
          "Function application has bad types of arguments: " + e)
        return t.outType
      case UclITE(cond,t,f) =>
        assertBoolArgs(List(cond));
        UclidUtils.assert(typeOf(t,c) == typeOf(f,c), 
            "ITE true and false expressions have different types: " + e)
        return typeOf(t,c)
      case UclLambda(ids,le) =>
        var c2: Context = c.copyContext()
        c2.inputs = c.inputs ++ (ids.map(i => i._1 -> i._2).toMap)
        UclidUtils.assert(ids.forall { i => transitiveType(i._2,c) == UclBoolType() || 
          transitiveType(i._2,c) == UclIntType() }, 
            "Cannot construct Lambda expressions of non-primitive types: " + le)
        return UclMapType(ids.map(i => i._2), typeOf(le,c2)) //Lambda expr returns a map type
      case UclIdentifier(id) => (c.constants ++ c.variables ++ c.inputs ++ c.outputs)(UclIdentifier(id))
      case UclNumber(n) => UclIntType()
      case UclBoolean(b) => UclBoolType()
    }    
  }
  
  def check(e: UclExpr, c: Context) : Unit = {
    val externalDecls : List[UclIdentifier] = c.externalDecls()
     e match { //check that all identifiers in e have been declared
       case UclBiImplication(l,r) => check(r,c); check(l,c);
       case UclImplication(l,r) => check(r,c); check(l,c);
       case UclConjunction(l,r) => check(r,c); check(l,c);
       case UclDisjunction(l,r) => check(r,c); check(l,c);
       case UclNegation(l) => check(l,c);
       case UclEquality(l,r) => check(r,c); check(l,c);
       case UclIFuncApplication(op,args) => args.foreach { x => check(x,c) }
       case UclArraySelectOperation(a,index) => check(a,c); index.foreach { x => check(x,c) }
       case UclArrayStoreOperation(a,index,value) => 
         check(a,c); index.foreach { x => check(x,c) }; check(value, c);
       case UclFuncApplication(f,args) => check(f,c); args.foreach { x => check(x,c) }
       case UclITE(cond,t,f) => check(cond,c); check(t,c); check(f,c);
       case UclLambda(ids,le) => ids.foreach { 
           x => check(x._2,c);
           UclidUtils.assert(transitiveType(x._2,c).isInstanceOf[UclIntType] || 
               transitiveType(x._2,c).isInstanceOf[UclBoolType],
             "Lambda indexed by non-primitive type");
         }
         ids.foreach{ x => UclidUtils.assert((externalDecls ++ ids.map(i => i._1)).
             count { i => i.value == x._1.value } == 1, "Lambda argument has a redeclaration") }
         var c2: Context = c.copyContext()
         c2.inputs = c.inputs ++ (ids.map(i => i._1 -> i._2).toMap)
         check(le,c2);
       case UclIdentifier(id) => 
         UclidUtils.assert((c.constants.keys ++ c.inputs.keys ++ c.outputs.keys ++ c.variables.keys).
         exists{i => i.value == id}, "Identifier " + id + " not found");
       case _ => ()
     }
    typeOf(e,c) //do type checking on e
  }
}