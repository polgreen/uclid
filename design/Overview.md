# Overview

Uclid5 is a software toolkit for the formal modeling, specification, verification, and
synthesis of computational systems. The Uclid5 toolchain aims to:
1. Enable compositional (modular) modeling of finite and infinite state transition
systems across a range of concurrency models and background logical theories;
2. Verification of a range of properties, including assertions, invariants, and temporal
properties, and
3. Integrate modeling and verification with algorithmic and inductive synthesis.

Uclid5 draws inspiration from the earlier UCLID system for modeling and verification
of systems, in particular the idea of modeling concurrent systems in first-order
logic with a range of background theories, and the use of proof scripts within the model.
However, the Uclid5 modeling language and verification capabilities go beyond the
original modeling language, and the planned integration with synthesis is novel.
This set of notes serves as a detailed documentation of the internals of the tool, covering
topics ranging from compile passes to verification techniques.
With the Uclid5 system under active development, we expect this document to undergo
several changes as the system and its applications evolve.
