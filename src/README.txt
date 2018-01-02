



TODO:
+ Test Orderly Atomic Sender (Atomicity, Ordering, Exception Handling).
+ Test Cascade Reactor.
+ + Verify that Interrupted Exceptions are properly handled.
+ Test Cascade Itself.
+ Add/modify basic throughput monitoring to Cascade Reactor.
  The in-depth monitoring will be in Cascade X via Core facades.
+ Finish and test the loggers.
+ Reintegrate the Checked Operand Stack code, which was temporarily disabled.
+ Clear label the interfaces that can be implemented (e.x. Core, Context, Schema) and those that merely hide implementation details (e.x. Reactor, Cascade, Allocate (ilk)).
+ Add convenience methods to Allocation Pool.
+ Add Input Stream and Output Stream push() methods to Operand Stack and/or Allocation Pool.
+ Finish the schema creation methods in Cascade Schema.
+ Test Cascade Token just for completeness. It should be okay already.
+ Look for TODOs!!!
+ Subscribe and probably initialSubscriptions() should be affected by namespaces.
+ Schema needs to detect duplicate entries. In particular, duplicate reactor appears possible now.

