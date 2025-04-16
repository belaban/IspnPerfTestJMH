
package org.ispnperftestjmh;

import org.infinispan.Cache;
import org.infinispan.context.Flag;
import org.infinispan.manager.DefaultCacheManager;
import org.infinispan.manager.EmbeddedCacheManager;
import org.infinispan.notifications.Listener;
import org.infinispan.notifications.cachemanagerlistener.annotation.ViewChanged;
import org.infinispan.notifications.cachemanagerlistener.event.ViewChangedEvent;
import org.jgroups.util.Util;
import org.openjdk.jmh.annotations.*;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@Listener
@State(Scope.Benchmark)
@Measurement(timeUnit=TimeUnit.MILLISECONDS,iterations=10)
@Threads(25)
// @OutputTimeUnit(TimeUnit.MICROSECONDS)
public class IspnBenchmark {
    protected EmbeddedCacheManager       mgr;
    protected Cache<Integer,byte[]>      cache;
    protected static final int           msg_size=1000;
    protected static final int           num_keys=20000; // [1 .. num_keys]
    protected byte[]                     BUFFER=new byte[msg_size];
    protected static final String        cfg="dist-sync.xml";
    protected final AtomicInteger        num_reads=new AtomicInteger(0), num_writes=new AtomicInteger(0);

    // this value can be changed, e.g. by passing -p "read_percentage=0.8,1.0" to the test runner
    @Param("0.8")
    protected double                     read_percentage;

    @Setup
    public void setup() throws Exception {
        mgr=new DefaultCacheManager(cfg);
        mgr.addListener(this);

        Cache<Integer,byte[]> c=mgr.getCache("perf-cache");
        // for a put(), we don't need the previous value
        this.cache=c.getAdvancedCache().withFlags(Flag.IGNORE_RETURN_VALUES);

        if(cache.isEmpty()) {
            System.out.printf("adding keys [1 .. %d]: ", num_keys);
            for(int i=1; i <= num_keys; i++)
                cache.put(i, BUFFER);
            System.out.println("OK");
        }
        else
            System.out.printf("Cache is already populated: %d keys\n", cache.size());
    }

    @TearDown
    public void destroy() {
        System.out.printf("num_reads: %d, num_writes: %d\n", num_reads.get(), num_writes.get());
        mgr.stop();
    }

    @Benchmark
    @BenchmarkMode({Mode.Throughput}) @OutputTimeUnit(TimeUnit.MILLISECONDS)
    @Fork(1)
    @Warmup(time=10,timeUnit=TimeUnit.SECONDS)
    public void testMethod() throws Exception {
        // This is a demo/sample template for building your JMH benchmarks. Edit as needed.
        // Put your benchmark code here.

        // get a random key in range [1 .. num_keys]
        int key=Util.random(num_keys) -1;
        boolean is_this_a_read=Util.tossWeightedCoin(read_percentage);

        if(is_this_a_read) {
            cache.get(key);
            num_reads.incrementAndGet();
        }
        else {
            cache.put(key, BUFFER);
            num_writes.incrementAndGet();
        }
    }



    public static void main(String[] args) throws Exception {
        IspnBenchmark b=new IspnBenchmark();
        b.setup();
        System.out.println("-- started as server");
        Util.keyPress("enter to terminate");

        b.destroy();
    }


    @ViewChanged
    public static void viewChanged(ViewChangedEvent evt) {
        System.out.printf("-- joined: %s, left: %s\n", evt.getNewMembers(), evt.getOldMembers());
    }

}
