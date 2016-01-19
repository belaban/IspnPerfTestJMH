

package org.ispnperftestjmh;

import com.hazelcast.config.ClasspathXmlConfig;
import com.hazelcast.config.Config;
import com.hazelcast.config.FileSystemXmlConfig;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import org.infinispan.notifications.Listener;
import org.jgroups.util.Util;
import org.openjdk.jmh.annotations.*;

import java.io.FileNotFoundException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@Listener
@State(Scope.Benchmark)
@Measurement(timeUnit=TimeUnit.SECONDS,iterations=10)
// @OutputTimeUnit(TimeUnit.MICROSECONDS)
@Threads(25)
public class HcBenchmark {
    protected HazelcastInstance          hc;
    protected IMap<Integer,byte[]>       cache;
    protected static final int           msg_size=1000;
    protected static final int           num_keys=20000; // [1 .. num_keys]
    protected byte[]                     BUFFER=new byte[msg_size];
    protected static final String        cfg="hazelcast.xml";
    protected static final double        read_percentage=0.8;
    protected final AtomicInteger        num_reads=new AtomicInteger(0), num_writes=new AtomicInteger(0);


    @Setup
    public void setup() throws Exception {
        Config conf=null;

        try {
            conf=new FileSystemXmlConfig(cfg);
        }
        catch(FileNotFoundException ex) {

        }

        if(conf == null)
            conf=new ClasspathXmlConfig(cfg);

        hc=Hazelcast.newHazelcastInstance(conf);
        this.cache=hc.getMap("hc_cache");

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
        hc.shutdown();
    }

   // @Benchmark
    @BenchmarkMode({Mode.Throughput}) @OutputTimeUnit(TimeUnit.SECONDS)
    @Fork(value=1)
    @Warmup(time=10,timeUnit=TimeUnit.SECONDS)
    public void testMethod() throws Exception {
        // This is a demo/sample template for building your JMH benchmarks. Edit as needed.
        // Put your benchmark code here.

        // get a random key in range [1 .. num_keys]
        int key=(int)Util.random(num_keys) -1;
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
        HcBenchmark b=new HcBenchmark();
        b.setup();
        System.out.println("-- started as server");
        Util.keyPress("enter to terminate");

        b.destroy();
    }


}
