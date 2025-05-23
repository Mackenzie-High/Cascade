# Cascade

Cascade is an embeddable actor framework contained within a single Java source-file. 

**Quick Links:**
+ [JavaDoc](https://mackenzie-high.github.io/Cascade/javadoc/index.html)
+ [Code Coverage](https://mackenzie-high.github.io/Cascade/jacoco/index.html)

**Maven Central:**

```xml
<dependency>
    <groupId>com.mackenziehigh</groupId>
    <artifactId>cascade</artifactId>
    <version>2.1</version>
</dependency>
```

## Compilation and Installation

```bash
git clone 'https://github.com/Mackenzie-High/Cascade.git'
cd Cascade/
git checkout REL_CASCADE_2_0_2_BETA
mvn clean install
```

## Actors

An actor is an object that has: (1) an input connector, (2) an input queue, (3) an output connector, (4) a function that transforms input messages into output messages. 

Actors can be connected to one another by connecting their input/output connectors to one another. Each actor can be connected to an arbitrary number of inputs and/or outputs. 

## Examples

**Hello World**
* [Single Actor](documentation/SingleActor.md)
* [Network of Actors - Pipeline](documentation/Pipeline.md)  
* [Network of Actors - Tree](documentation/Tree.md)

**Consumer Script based Actors:**
* [Actor defined using a Class](documentation/ConsumerClass.md)
* [Actor defined using a Method Reference](documentation/ConsumerMethod.md)
* [Actor defined using a Lambda Expression](documentation/ConsumerLambda.md)

**Function Script based Actors:**
* [Actor defined using a Class](documentation/FunctionClass.md)
* [Actor defined using a Method Reference](documentation/FunctionMethod.md)
* [Actor defined using a Lambda Expression](documentation/FunctionLambda.md)

**Context Script based Actors:**
* [Actor defined using a Class](documentation/ContextClass.md)
* [Actor defined using a Method Reference](documentation/ContextMethod.md)
* [Actor defined using a Lambda Expression](documentation/ContextLambda.md)

**Advanced Topics:**
* [Custom Stage](documentation/CustomStage.md)
