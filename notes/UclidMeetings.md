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
1. Implement constant declarations with literals associated with them.
1. Constant declarations and type declarations that can exist outside a module.
1. While loops.
1. Synthesis.
1. Check if the LTL property contains only G, X after conversion to NNF; If so don't generate the liveness proof obligations.  

## Comments

1. Investigate profiling infrastructure.
1. Why do we have both RuntimeError and AssertionError? We should only have one.
1. Try Boolector as another backend solver.
1. Create individual publish.sh files for each folder. Don't publish tutorial sources.
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

- [Kevin/Cameron] Fix branch_level decrement after mis-speculation.
- [Kevin/Cameron] Remove next from inside procedures.


