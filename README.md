
 Pareto4j
==========

 Intro: short short version
----------------------------

Allocating Lists, Maps or Sets to store no or a single value is wasteful in terms of memory.
Furthermore one can sometimes observe a very biased use of the Collections API. For instance a small fraction of
the code will allocate most of the instances. Or most of the Collections instances will store few value why a few store
all the rest.

The project provides an analysis tool as well as a efficient and light weight implementation of the Collection API


 Context: a few general ideas/observations
-------------------------------------------

- [Pareto principle](http://en.wikipedia.org/wiki/Pareto_principle)
- [Building Memory-efficient Java Applications: Practices and Challenges](http://www.cs.virginia.edu/kim/publicity/pldi09tutorials/memory-efficient-java-tutorial.pdf)


 The idea behind this project
------------------------------

The Pareto principle (sometimes referred to as 80/20 principle) point out that in many of the human activities an uneven
 distribution resources can be observed. This can be in terms of time spent, money etc. At the moment Pareto4j focuses on
  the use of the Collections classes and asked the question: "is the uneven usage of the Collections classes present in
  the application?". The question be put many different ways depending on the aspect one wishes to focus on:

- does the application use 20% of the Collections to store 80% of the data
- 80% of the Collections is short lived while the remaining 20% lives till the end
- etc

The JDK's collection API is an important aspect of Java's success. The platform provides a standard (the API) and
efficient implementations to help the programmer get started. Yet, as described in the second link above, when one
stores no or only one value using the standard collection classes the overhead in term of memory can be high (relatively
speaking). See the slides for a much better explanation.

Now combine this with the Pareto principle and then the question becomes: are Java programs, or parts of it,
subject to this very biased use of the collection classes. In particular are there places where:
 - most of collections classes are used?
 - collections classes are used to store one or no value at all?

Consider for instance the excellent IntelliJ IDEA 13 Community Edition. Analysis of the IDE's run-time use of ArrayList
 instances shows that more than 80,2% of all ArrayList instances are created in
 2,0% of the sites. Or put differently out of the 1263 points in the code where "new ArrayList()" is called 25 points
 create 80% of the ArrayList instances. But if does not stop there... most of those ArrayList-s are EMPTY!
You read it well:  the average "size()" is 0!!! The hottest allocation site for HashMap-s and ArrayList-s in
IntelliJ 13 CE is show below:

    24,5% of instances com.intellij.psi.augment.PsiAugmentProvider.collectAugments(PsiAugmentProvider.java:35) count:528641 avg. sz:0.0
    55,0% of instances com.intellij.util.containers.HashMap.<init>(HashMap.java:29)count:182358 avg. sz:0.0

This data was collected after a sufficiently long normal usage of the IDE. No automated scenarios, plain editing and
compiling. Those numbers show 2 things:

1. real cases exist where the Collections API is used to store just a value or less.
2. the Collections API is in this case used in a very biased fashion.

It is worth mentioning that IntelliJ also uses the GNU Trove implementation in other places.

 What this project provides
----------------------------

- An inspection tool called "Inspector"
- Low memory footprint Collections implementation

 Inspector
===========

A tool to inspect the use of the JDK's Collections classes in existing applications/libraries at run-time.
An agent instruments the code a load time and provides statistics via JMX (information dumps using
java.util.logging). The inspection focuses on instances of the Collections classes that store fewer than 2 values (i.e.
none or just one) versus the general case.

Using the inspector is simple, add the __javaagent__ JVM option. For instance:

-javaagent:path/to/inspector-1.0-SNAPSHOT-jar-with-dependencies.jar

By default the inspector will instrument all the classes except those of the JDK. One can influence the instrumentation
with several system properties:

- -Dinspector.excludes=simple_regex
- -Dinspector.includes=simple_regex
- -Dskip{HASHSET|HASHMAP|LINKEDHASHSET|LINKEDHASHMAP|LINKEDLIST|VECTOR|ARRAYLIST|HASHTABLE|CONCURRENTHASHMAP}

To filter on package or class name use -Dinspector.excludes and/or -Dinspector.includes . Both can be combined.
 One can globally avoid collection class from using -DskipXYZ where XYZ is the fully capitalized JDK class name.
 Several -DskipXYZ properties can be combined. For instance -DskipARRAYLIST -DskipHASHSET will prevent ArrayList and
 HashSet instances from being instrumented.

Example: only instrument classes in com.foo.bar and subpackages and do NOT instrument HashMap instances

java -javaagent:path/to/inspector-1.0-SNAPSHOT-jar-with-dependencies.jar -Dinspector.include=com.foo.bar.* -DskipHASHMAP

Through JMX one can read statistics at run-time. The instrumentation is focuses on the distinction between instances
 containing fewer than 2 values (considered "SMALL") and the more general case. For each instrumented type statistics
 are expose via JMX.

The statistics also distinguishes between live instances (as not GC-ed) and dead instances. In other words one can see at
run-time what use is made of the live instances.

JMX operations generate reports regarding the location where the most relevant instances are instantiated. Reports are
created using the java.util.logging API. If one wishes to write the reports to another destination add the appropriate
 logging.properties to the classpath.

To save memory the inspector will only record the site where collection classes are instantiated. The usage analysis can
however be greatly simplified when the full stacktraces leading up to the instantiation is recorded. The cost is an
increased memory usage. To use full stacktraces add the -Dinspector.fullStacktrace=true system property. Enabling this option will
 also add more JMX operations; both _fullstracktraces_ dumps and _normal_ dumps will be possible

Some programs are short lived and one does not always have the time to connect with JMX. To obtain data in this case on
 can use the -Dinspector.dumpOnExit=true system property. This will dump the collected information to stderr upon JVM
 shutdown.

 __IMPORTANT NOTE__ give the limited guarantees offered by the JVM shutdown hook _specification_ in some rare
 case one can not get any information upon shutdown.

 Performance
-------------

The inspector is tool development tool. Due to both the bytecode modification at load-time and the run-time monitoring
  there is some overhead. Depending on the nature of the applications this can be noticeable. This overhead can be any
  combination of:

  * higher memory usage
  * increased latency mostly due to the the use of WeakReference which give more work to the garbage collector

 Collections
=============

A special purpose implementation of the Collections API. A counterpart has been implemented for the most
common JDK Collections classes. The HashMap can be replace by the ParetoHashMap etc.

The implementations provided here are optimized for two specific cases: Collections the containing no value (empty)
or a single value. The implementations are lightweight and efficient. What if more than one value needs to be stored?
The implementation will fallback on the JDK implementation.

The point of these implementation is that they don't use any array or other structure to store the
data. This means that it avoids the overhead of 16 bytes + type_size * default_array_size (32bits arch) of the array. In
 cases where no data is stored at all or just one value is stored in the array there is relatively high memory overhead.
 Furthermore the assumption that that the Collections class only stores at most one value allows for a simple and efficient
 implementation of the different methods.

Here too JMX is used to provide some run-time statistics. This allows to validate whether the Pareto Collections are
used correctly. If the statistics show that most of the instance exceed the 1 element per instance threshold one should
 rather use the JDK implmentation instead.



 What is the point exactly?
----------------------------

Avoid unnecessary performance costs: reduce the memory footprint when possible, reduce GC pressure. But why bother when
experts will tell you "new" in Java in faster than "malloc()" in C?

Be pragmatic of course. The point is not to avoid instantiating a few ArrayList for instance. Think
"death by thousand cuts". Look whether your application(s) exhibits a behaviour that can be characterized as "Pareto".



