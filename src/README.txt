
TODO:
+ Assign Latin name to project. Rename packages.
+ Finish parser (escape sequences and error location reporting).
+ Test ConfigSchema
+ Test TopicID
+ Test Parser
+ Implement Logging
+ Test Main
+ Implement Help Message
+ Improve and Test MessageQueue (ideally no GC needed)
+ Improve statistics monitoring in the Controller.


Modules:
+ Statistics: Receive messages from multiple topics, count them (etc).
+ Funnel: Receive messages from multiple topics and then send them on a single topic.
+ Fanout: Receive a message from a single topic and then copy it to multiple topics.
+ FileStore: Facilitate key-value store on disk with a client-server API.
+ Subprocess: Execute BASH scripts.
+ ZeroMQ: Inter-Process-Communication
+ Plugins: Reflectively instantiate classes on the class-path, obtain lambdas, use them as message-handlers.
+ Worklets: Receive lambda instances, as messages from specific topics, and execute them on daemon threads.


