
Jenkins:
+ live
+ erin


Processors:
+ Dedicated - (N) threads are dedicated to each queue. Only useful for high-throughput queues.
+ Pooled - (N) threads share the load of servicing a multitude of queues.
+ Direct - Queues are serviced by whatever threads is adding a message to the queue. The queue has capacity of zero.
+ Spawning - Upto (N) threads will be spawned in order to handle messages. When no work is available, the threads die.


Messages:
+ Composite Byte Arrays, for GC performance. Create a large byte-array by combining smaller arrays behind a facade.
+ Three types of messages: objects, byte-arrays, Sexpr.



Modules:
+ ZeroMQ: Inter-Process-Communication
+ Expression: Evaluate Arithmetic/Comparison/Logical expressions based on lazy data from queues.
+ SexprTransfom: Structurally transform Sexpr objects (get, set, etc).
+ State Machine
+ Stack Node, etc?
+ Database Module
+ LDAP Module
+ Globals Module
+ Hasher (MD5, SHA1, etc) Module
+ Encryption Modules
+ Decryption Modules
+ Terminal Message Sender/Receiver

Modules - Extensible (Abstract Modules):
+

Modules - Flow:
+ Forwarder - 1x1, 1xN, Mx1, MxN forwarding.
+ Switcher - Like a railroad switch for queues.
+ Valve - Like a water valve for queues. A simply form of switcher really.
+ Syncer - Given (A -> B, X -> Y, Z). Wait for a value to be ready from Z (control), then forward (A -> B) and (X -> Y) simultaneously.
+ Batcher - Given (X -> Y, Z), store all messages from X, until a tick is received from Z, then forward the messages to Y.
+ Repeater - Copy messages (reference copy, not deep copy) and then forward them.
+ Copier - Deep Copy????
+ Rate Throttle - Send messages to an alternate queue when one queue is receiving messages too frequently.
+ Size Throttle - Send messages to an alternate queue when one queue is receiving too much data.
+ Delay - Delay messages for a given fixed time-limit. If the time-limit is dynamic use Batcher instead.
+ Prioritizer - Given queues (X, Y, Z), messages from X will be forwarded before any from Y or Z. Requires a ticker to be attached.
+ Buffer - Stores up to (N) messages and forwards them one-by-one as requested.

Modules - Filters:
+ Change Filter - Only forward message if it changed *recently*.
+ Secure Hash Filter - Only forwards messages, if the hash value (MD5, etc) was not recently observed.
+ Size Filter - Only forward messages of a required size.
+ Condition Filter - Given queues (X, Y), receive a message from each, only forward (X), if (Y) is TRUE.
+ Pattern Filter - Only forwards string messages, if they match a regular-expression.
+ Range Filer - Only forwards numeric messages, if they match a numerical range.

Modules - Balancers:
+ Round Robin Balancer
+ Size Balancer - Given (X -> Y | Z), select (Y | Z) depending on which one has received fewest bytes.
+ Rate Balancer - Given (X -> Y | Z), select (Y | Z) depending on which one has received fewest messages. Useful, if Y and Z may overflow.
+ Blowout Preventor

Modules - Timing:
+ Ticker
+ Merged Ticker - Synchronize monotonic time from multiple clocks. Useful for fail-over.
+ Heartbeat Monitor

Modules - Expressions:
+ Variable - Optional clear control queue.
+ Summation - Optional clear control queue.
+ Product - Lazily Multiply Inputs. Optional clear control queue.
+ Any - Given queues (X, Y, Z, O), if either X, Y, or Z receive a message, then send TRUE to O. Optional clear control queue.
+ All - Given queues (X, Y, Z, O), if X, Y, and Z receive a message, then send TRUE to O. Optional clear control queue.
+ Not
+ And
+ Or
+ Xor
+ Divide
+ Modulo
+ Multiply
+ Add
+ Subtract
+ Negate
+ Less Than
+ Less Equal
+ Greater Equal
+ Greater Than
+ Equals
+ Not Equals
+ Expression ????

Modules - Strings:
+ Matches

Modules - Conversions:
+ JSON To Sexpr
+ Sexpr To JSON

Modules - Monitoring:
+ Counter
+ Queue Monitor - Stat Tool

Modules - Basic IO
+ Resource Poller - Periodically read a message from a file or URL.
+ File Watcher - Watch for newly completed files. Can this be merged into Resource Poller???
+ Printer - Print messages to standard-output.
+ Appender - Append messages onto a file.
+ REST Server
+ Configuration Loader - Reads Symbolic-Expressions from a file on startup and sends them to queue(s).
+ Function - Invokes a Java method given a message and output queue as inputs.
+ GET/POST Sender - Given a message, issue an HTTP GET/POST based on the message.
+ Apache Servlet - A standard request/reply enabled servlet implementation.

Modules - OS:
+ Predefined Command - Executes predefined shell commands.
+ Dynamic Command - Executes a shell command received in a message. Potentially dangerous from a security perspective.
+ File Store - Save messages to files and read files into messages.

Storage:
+ ZIP Map File - Use a ZIP file as a persistent key-value store.

Modules - Buffers/Caches:
+ In-Memory Key-Value Store - For performance improvement.
+ Data Buffer - Circular Byte Buffer with an optional resident minimum-time.
+ Copy Buffer?? - Copy binary messages into newly allocated byte array messages.

Modules - ZeroMQ:
+ Publisher Server - Stable
+ Publisher Client - Unstable
+ Subscriber Server - Stable
+ Subscriber Client - Unstable
+ Request Reply Server
+ Request Reply Client

Modules - Data Generator:
+ Random
+ Fibonacci
+ Sequence

---------------------------------------------------------------------------

(import Sexpr : com.mackenziehigh.Sexpr)

(define node FileStore : com.mackenziehigh.FileStore
    (inQ : q Sexpr)
    (bufferSize : int)
    (outQ : q String))

(dedicated powerhouse P1)

(define region Recorder (X Y Z)
    (vertix input : Sexpr)
    (vertix output : DataMessage :- P1)
    (node FS : FileStore
        (inQ = q input)
        (bufferSize = 5)
        (log logQ))

(region IS : Recorder
    (X = inQ))

(set config = 123)


----------------------------------------------------------------------------


(layout Page1 (100 x 100) (P Z)

    (cell (P 1 1) = (timer ))

    (wire (1 2) (1 5) (2 5))

)

(root Page1)

----------------------------------------------------------------------------

include "ZMQ"

compiletime classpath  "/raid/mhigh/lib/X.jar"
runtime classpath "lib/X.jar"

module Main

datatype String
datatype TwoStrings : [String , String , END ]
datatype IntTwoStrings<E> : [ int , E ] & TwoStrings

actor interface Router<T : TwoStrings>
{
    input inQ : T

    output outQ : T[25] => MyClass::route

    actor dispatcher : Dispatcher<T>

    connect inQ -> dispatcher.inQ
    connect dispatcher.outQ -> outQ
}


actor factory RouterFactory : Router

allocator PrimaryAllocator =    0 to 1024 : dynamic
                           | 1024 to 2048 : block(512, 1024)
                           | 2048 to  MAX : chunk(512, 4096)
                           ;

spawning powerplant Nuke limit 10;
dedicated powerplant Terminus;
pooled powerplant MainLine.

actor A1 : Router<String> :- Nuke
actor A2 : Router<String> :- direct
actor TT : Ticker<String> :- Nuke

connect TT.outQ -> A1.inQ[5]
connect A1.outQ -> A2.inQ

----------------------------------------------------------------------------

include <path> ;

compiletime classpath <path> ;

runtime classpath <path> ;

package <full-name> ;

define struct <name>
{
    field <field-name> : <type> = <id> ;

    field <field-name> [ <count> ] : <type> = <id> ;
}

define enum <name>
{
    constant <field-name> = <id> ;
}

define union <name>
{
    option <field-name> : <type> = <id> ;
}

define message <name> = [ <type-1> , ... , <type-N> ] ;

define message <name> = [: <type-1> , ... , <type-N> :] ;

define actor <name>
{
    property <field-name> : <type> ;

    input <field-name> : <message-type> ;

    output <field-name> : <message-type> ;
}

default actor class <actor-name> = <class-name> ;

define grid <name>
{
    property <field-name> : <type> ;

    actor ( <xpos> , <ypos> , <rotation> ) : <actor-type> ;

    wire ( <xpos> , <ypos> , <port-in> , <port-out> ) ;
}

grid <name> : <grid-name> ;

set property <grid> . <property> = <value> ;

set property <grid> . <property> . <actor> = <value> ;




