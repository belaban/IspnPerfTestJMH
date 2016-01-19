# IspnPerfTestJMH

Tests Infinispan and Hazelcast with OpenJDK's JMH benchmark framework.
 
The idea is to run a cluster of N instances with one of the instances being run under JMH's control. The JMH node
will join the cluster when started, then the JMH tests are run, and when done, the node will leave the cluster again.

## Example

* Run 3 instances of Infinispan in a cluster:
```
bin/ispn.sh
```
* Then run the JMH test:
```
bin/jmh.sh
```

Make sure that you see 4 nodes in the cluster: if the JMH node doesn't find the other 3 already started node, then it
will form a _singleton cluster_ and numbers will be much better than if run in a cluster!

## Todos
This was a quick-n-dirty test; could use some refactoring like IspnPerfTest...

