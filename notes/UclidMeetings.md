# Feb 16 Meeting Notes

## Queue Verification
Ankush: Should we consider adding syntactic sugar to invoke methods from outside a module? 
Decision is to not do this for now. 

Sanjit: UCLID is a verification language not a programming language; so therefore this is not really within in its ambit. Also no real point in making a verification language object-oriented, it just adds complexity without gaining new features.

## Issue 57: Should consts be static?

Kevin: Problem related to issue #57. Should constants be static (in the C++/Java sense) or not?

Pramod: Right now constants are in fact static; we've fallen into this behavior without really making an explicit decision to have it this way.

Sanjit: There is no need for constants to be static, if we really want a static constant, we can just push it into a different module and use it from there.

## Spectre/Meltdown Project

Sanjit: There are some existing processor models in the old UCLID syntax that might worth looking at:

  - A very simple DLX processor (from the Patterson/Hennessy book) model.
  - A "y86" model from Randy Bryant. (He might re-implement this himself).
  - Finally, Sanjit and Shuvendu implemented an out-of-order processor model.

Kevin: How complex should our models be? Are we missing important detail by focusing on abstractions rather than detailed implementations?

Sanjit: Our eventual goal is to verify RTL implementations, which are detailed enough. But we don't want to manually build models with RTL complexity, instead we will use synthesis for these models. For now, let's build a simple model and start investigating its properties. 

## Hyperproperties

Markus/Sanjit: What is the simplest extension to hyperproperties we can have in UCLID?

Pramod: We could try to implement something like the Ironclad/Dafny extension where we can refer to left(var) and right(var) in our post-conditions.

Sanjit: Let us use a more principled approach in our specification language. We will start with 2-safety and then implement HyperLTL. The module instantiation should be useful for this.

# Feb 23 Meeting Notes

## Synthesis Functions
Pramod: How do we setup synthesis function postconditions? 

Sanjit: Use synthesis functions in the code, or in invariant and we will autogenerate the conditions.

## LTL Liveness
Pramod: should we have a predicate for checking state equality?

Sanjit: Skip this for now.

## 219c Project on Modeling Speculation

Sanjit's advice: have much clearer sequence of steps for the project, and clear division of labour. Need to have a model up quickly (2 weeks) to allow subsequent iteration.

# Mar 02 Meeting Notes

## 219c Modeling Project

Modeling of load queue: should we model a load queue?  Probably not, because we're not modeling TLBs, or cache miss latency. We can simulate the same effects by picking an appropriate out of order schedule. 

Cache model
 - model cache tags, cache associativity
 - reuse TAP model

Modeling reorder buffer
 - Use the Smith and Sohi paper
 - Model a register map, reorder buffer, and instruction selection

## Concurrency in UCLID5

  - can we have a scheduler process?
  - how about next[atomic], next[interleaved]
  - another option is that:
    global instantiations of two modules should be in parallel

# Notes on CPU Model from Kevin

The command line to run the model is:

    uclid /home/pramod/research/enclaves/vectre/src/main.ucl /home/pramod/research/enclaves/vectre/src/cpu/cpu.ucl /home/pramod/research/enclaves/vectre/src/cpu/stages/issue.ucl /home/pramod/research/enclaves/vectre/src/cpu/stages/renamer.ucl /home/pramod/research/enclaves/vectre/src/cpu/stages/execute.ucl /home/pramod/research/enclaves/vectre/src/cpu/stages/write_result.ucl /home/pramod/research/enclaves/vectre/src/cpu/stages/commit.ucl /home/pramod/research/enclaves/vectre/src/cpu/buffers/reorder_buffer.ucl /home/pramod/research/enclaves/vectre/src/cpu/buffers/reservation_station.ucl /home/pramod/research/enclaves/vectre/src/cpu/datastructures/register_stat.ucl /home/pramod/research/enclaves/vectre/src/memory/cache.ucl /home/pramod/research/enclaves/vectre/src/utils/common.ucl

This model blows up inside the call to exprToZ3. The top-level operation in the AST being computed upon is an equality between ``state_1_$inst:cpu1_var_$inst:issue_var_$inst:rob_unbound_output_ptind_out_15_36_31`` and some complex expression on the right hand side of the equality. Even attempting to print out this expression causes the Java runtime to run out of memory, so there is clearly some exponential blowup here. In fact, the problem is in just computing a hash of the expression (inside Scala code) -- this comes into the picture because we use a HashMap to memoize the results of exprToZ3. 

Some of this can probably be solved using CSE, but we will need to introduce a let expression in SMTLanguage to make this work.

Some other notes: if we add a counter to exprToZ3, then expression #51 when run on the above file is the one that causes the problem.

# Mar 09 Meeting Notes

## Memoizing SMT Expressions to Z3 conversion

1. We're overriding hashCode so that the hash value of each AST node is computed only once. This should hopefully solve the issue with Kevin's CPU model.
1. Comment from Sanjit: Make sure to document the magic constants used in hashBaseId.

## CPU Model

1. Semantics of next; should we have primed variables?
1. Code structure: have next(module), call (next\_v), v = next\_v.
1. How does Z3 handle record types? The ROB is implemented as a set of arrays: should this be an array of records.
1. How is the ready state of each instruction tracked? Look

# Mar 13

## Todo List from Meeting

1. Implement primed variables. [done]
1. Implement change from :: and -> to '.' operators. [done]
1. Implement constant declarations with literals associated with them. [done]
1. Constant declarations and type declarations that can exist outside a module.
1. While loops. [done]
1. Synthesis. [in-progress]
1. Scripting language improvements.
   * asserts and assumes in the control block.
   * print strings
1. Hyper-properties.
1. Check if the LTL property contains only G, X after conversion to NNF; If so don't generate the liveness proof obligations.  

## Comments

1. Investigate profiling infrastructure.
1. Why do we have both RuntimeError and AssertionError? We should only have one.
1. Try Boolector as another backend solver. [done]
1. Create individual publish.sh files for each folder. Don't publish tutorial sources. [done]
1. Document CoverDecorator.

Further TODOs
1. More informative names for counterexamples to induction.
1. Need to check that primed variables are not present in module-level invariants and properties.
1. Need to check there is only effective assignment to a primed variable in the init/next blocks.
1. We should not have error messages using single quotes in places where primed variables can occur.

# March 23

- Discussion of primed variable changes to uclid5.
- [Pramod TODO]: Need to double check RHS use of primed variables in procedures. [done]
- [Pramod TODO]: Disallow next statements inside procedures. [done]
- [Pramod TODO]: assert/assume in the control block?

## Vectre Notes

- [Kevin/Cameron] Fix branch\_level decrement after mis-speculation.
- [Kevin/Cameron] Remove next from inside procedures.

# April 06

- [uclid5 TODO] Consider adding distinct operator.
- [uclid5 TODO] Boogie's option to sprinkle assume false thoughout the model.
- Add uninterpreted cache tags

# April 12 [Meeting with Alan]

Topic: ABC as a backend for UCLID5
ABC can take word-level input
+ verilog file
+ internal buffer format (NDR)
  * internal word-level aiger format 
  * design should be flat
  * no support for memories
  * sequentials are conditionally supported
  * are memories are small (< 1000 flops)
    > yes? just blast the memory as bit-level flops
    > no? convert to a memory with the ABC memory interface
  * alan is developing a memory abstraction package
    > which will support "memory semantics"
    > cegar based approach for memory.
    > relevant: fmcad 2012 -- small, short world paper
  * documentation on constructing a buffer
    > kinda like extended
    > create a big array and fill it up with inputs, outputs, etc.
    > and pass this into ABC
    > operators
      - similar to verific
      - more than a 100 operators are supported
      - can add operators easily
    > not yet supporting memories, but will need to extend this format
  * ndr.h: https://github.com/berkeley-abc/abc/blob/master/src/aig/miniaig/ndr.h
    > every node has a unique id
    > fanins are identified by unique ids
    > NDR can't take a slice of an input signal as an input to an adder
    > we would have to output a new node that creates the slice, new node for concat etc.
    > every node must have a single output

# April 13

## Vectre Model

- Use constant literals for ROB size, number of phyiscal registers etc.
- Update instruction "picking" to use uninterpreted functions, rather than having assumes
- Remove the use of integers in speculation levels

## UCLID5 Notes

- Debug where Z3 is spending time
- [much later] consider adding a boogie backend to UCLID5

# April 23

## ABC Word-Level Integration

1. Need CEX translation.
2. CEX format
   - array of bits ordered by time
   - to get values: can also simulate using ABC.
3. Every operator has only one output.
4. But sub-modules can have multiple outputs
5. Alan will add a new way of specifying flip-flops.
   - ABC_OPER_DFF
   - two inputs to a flop
   - data input and initial state input
6. Some of the outputs can be designated as "constraints"
   - Property outputs go first
   - Constraint outputs always go last
   - Have to specify the number of constraint outputs
   - The property checked is: Globally(cnst1 && cnst2 && ...) ==> Globally(prop1 && prop2 && .. )

# April 26

1. Document log XML. 
1. Concept of non-atomic procedures with atomic blocks.

# April 27

1. Print out what is proved.
1. Parse Z3 asts and bring them back to Z3 AST.  

# April 30

1. Introduce local variables to next.
1. Change universal binary packager to output package in directory that is not version specific. 
1. Result parsing

# May 04

## UCLID Notes

1. Make sure we havoc uninterpreted functions.
    
    For now we can fake this by passing in the step index to a UF.

1. Profiling infrastructure.

    export JAVA_OPTS="-Xmx256M -Xms32M -Xprof"

    and then run uclid.

## Vectre Notes

1. Trick to havoc a specific function.

    havoc idx;
    arr[idx] = uf(idx);
    assume (idx \in Set)

1. Allow the ROB to not pick an instruction in any particular cycle. This is required to model structural hazards.

1. Consider making some of the types uninterpreted.

# June 1

## ADEPT Retreat

1. Comparison with Coq
1. KeyStone [Open Source Sanctum Implementation]

## Feedback

1. R. Bryant tried to port Y86 to Boogie
   Looking more promising than Boogie.

### Tutorial

1. Mention uninterpreted functions in the beginning.
1. Description of specification [assume, assert, invariants].
1. Use CmdId, and list of valid commands.
1. Inject concrete values to symbolic constants.
   > set command.
1. Update grammar.

# June 5

## ADEPT Retreat

1. Kevin: People would like to see how we can derive new bugs from this model:
  * That is the aim: generalize spectre to a broader class of attacks, but we're not there yet.
  * Most of the other work was like that.
1. Kevin: How do we find new bugs?
1. Concrete thing: interact closely with Krste and his group and build a systems stack on top of it. [Called KeyStone].
1. Apple are interested in the people less in the work.

## Kevin/Cameron

1. Look at the tutorial and see if it makes sense.

### SyGuS 

1. Convert from SMT AST to Uclid format.
1. Support general SyGuS format.
1. Test out bitvectors.

### Kevin TODO

1. Print the result.
1. Print the invariant.

Next step: interface general SyGuS format.

### Cameron TODO

1. Try extending the Vectre model.
   * Do this in a way that the Uclid model is not specialized to each variant.
   * Give us some insight in a general attacker model.

## Tutorial

* Does not mention `sharedvar`.

# July 01, 2018

1. add support for:

    type _ = common._

    [DONE] but syntax is now:

    type * = common.*;

2. add support for importing constants.

3. Usage of post-conditions:
    
 * Should we have a noinline/inline declaration?
 * Preferably need a way which preserves inline as the default.
 * Let's go with procedure {:noinline} foo(...)

 [DONE] but syntax is procedure [inline] and procedure [noinline].

4. Need to add distinct keyword that translates to SMT's distinct operator.

 [DONE]

5. Flexible ordering of requires, ensures and modifies. [DONE]


# September 7, 2018

Kevin has a 2-safety proof that goes through.
+ Path src2/kkmc_var1_2safety.ucl; branch: experimental_kkmc
+ Proof of confidentiality
    - cache is flushed after sysret
    - no stores
    - registers are restored from stash
+ Proof is by induction.
+ Takes 2-3 minutes.
+ [TODO/Pramod] Look into whether we can do any easy parallelization.
+ [TODO/Pramod] Look into installing on a bigger server to see if it helps.

# October 10, 2018

# Vectre

1. speculative vs. out of order execution
2. look at e-QED, symbolic QED work: test generators for processors
3. verifying ooo processor was FMCAD 2002

# Hyperproperty syntax:
   - currently: hyperproperty[k] ...
   - change to: hypersafety[k] ...
       similarly: hyperinvariant[k] ...
       similarly: hyperliveness[k] ...
   - The .1, .2 notation: should we use :1, :2 instead to avoid confusion with field access?
     It's not a parsing issue, just a way of keeping notation clearly distinct for users.
   - later if we support HyperLTL, we could simply have: property[HyperLTL] or something like that?

* Implement Z3 Horn clause solver as a back-end? Use of PDR?
  Qn: How is the support for BV+Arrays?

* Implement BTOR/word-level interface to ABC, etc.
  Pramod: this may be a lot of work to interface to C. 
  -- Ask Alan to help?


