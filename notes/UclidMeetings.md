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

