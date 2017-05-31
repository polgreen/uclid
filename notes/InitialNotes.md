# UCLID5 Initial Goals (From Sanjit)

## Current Language:
The current language allows one to define sequential procedures, and concurrent modules and simple types of assertions, all with a fairly rich set of types reflecting the current common theories supported by SMT solvers.  The current tool simply parsers this and checks that the input is valid syntax.  It does not do anything with it. We still need to add functionality to represent the parsed model as an abstract syntax DAG and then implement basic symbolic simulation (execution) on top of which we can implement basic verification strategies such as BMC and k-induction.

## Richer Specifications:
1. Simple temporal logic assertions. These would be used by BMC/k-induction engines.  A special case could be an invariant. 

2. Simple hyperproperties: this could be a fragment of HyperLTL, e.g. just starting out with 2-safety properties. The back-end should do the self-composition required for the proof, it should not be needed to be done manually.

3. Assumptions/guarantees could be additional annotations on specifications.

## Synthesis:
1. We could add the notion of a "synthesis function". This would be declared as a typed function symbol along with an associated grammar of expressions to replace it with. E.g., an invariant would be a Boolean function of specified arguments.

2. If a synthesis function appears in an invariant or property to be proved, then we turn that into a SyGuS problem to be solved. 

3. The expression synthesized for a synthesis function should be cached and re-used as long as there is no change to any part of the model that it does not depend upon. This dependency may be a tricky notion that we should think about more carefully.

## Richer Oracles:
1. Given the basic machinery above, we can already synthesize both the system/environment descriptions in the models as well as some specifications (e.g. invariants). But SyGuS can be constraining. So we should consider expanding the range of oracles we can use on the back-end.

2. As an experiment: consider replicating your (Pramod's) PhD thesis work in this framework. We'll need to invoke a model checker like ABC and a RTL simulator as oracles. So one idea is to extend the language with a capability to script the use of oracles in a natural way, e.g.  to code up a CEGIS loop or some other oracle-guided synthesis loop.
