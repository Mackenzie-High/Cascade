

TODO:
+ debugInfo() on actors?
+ User-defined attribute set/has/get methods on actors?
+ Better access to the settings used to create scripts somehow?
+ More well-defined mechanism for stack serialization.
+ Add blocking consumers that receive values from actor.
+ Rename CommandChain, since it could conflict conceptually with Commandable.
+ onSetup() / Scheduler is broken. 













OLD TODO:
+ No messages should be processed before onSetup() is invoked on every reactor.
+ What happens, if you try to send a message in onSetup() or onDestroy()???
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
+ Add unit-tests for the new push(char) and asChar() methods in OperandStack.
+ The selfTest() method in ConcreteCascade is extremely slow when a large number of nodes are present (~100K).
  This was found during testing of CascadeX.
  The profiler said it has something to do with the cotainsAll() call that is going to a ConcurrentSkippedListMap.