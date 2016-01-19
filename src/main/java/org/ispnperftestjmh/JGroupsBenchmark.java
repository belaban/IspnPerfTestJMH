
package org.ispnperftestjmh;

import org.jgroups.JChannel;
import org.jgroups.ReceiverAdapter;
import org.jgroups.View;
import org.jgroups.blocks.MethodCall;
import org.jgroups.blocks.RequestOptions;
import org.jgroups.blocks.RpcDispatcher;
import org.jgroups.util.RspList;
import org.jgroups.util.Util;
import org.openjdk.jmh.annotations.*;

import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;

@State(Scope.Benchmark)
@Measurement(timeUnit=TimeUnit.SECONDS,iterations=10)
// @OutputTimeUnit(TimeUnit.MICROSECONDS)
@Threads(25)
public class JGroupsBenchmark extends ReceiverAdapter {
    protected JChannel ch;
    protected RpcDispatcher disp;
    protected static final String cfg="/home/bela/fast.xml";
    protected int cnt=1;
    protected static final Method meth;


    static {
        try {
            meth=JGroupsBenchmark.class.getDeclaredMethod("getCount");
        }
        catch(NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }


    public int getCount() {
        return cnt++;
    }

    @Setup
    public void setup() throws Exception {
        ch=new JChannel(cfg);
        disp=new RpcDispatcher(ch, null, this, this);
        disp.setMethodLookup(id -> meth);
        ch.connect("jmh-demo");
    }

    @TearDown
    public void destroy() {
        Util.close(ch);
    }

   /* @Benchmark
    @BenchmarkMode({Mode.Throughput}) @OutputTimeUnit(TimeUnit.SECONDS)
    @Fork(value=1)*/
    public void testMethod() throws Exception {
        // This is a demo/sample template for building your JMH benchmarks. Edit as needed.
        // Put your benchmark code here.
        MethodCall call=new MethodCall((short)1);
        RspList<Integer> rsps=disp.callRemoteMethods(null, call, RequestOptions.SYNC());
        // System.out.printf("%d rsps: %s\n", rsps.size(), rsps);
        if(rsps.size() < 0)
            throw new IllegalStateException("should have gotten at least 1 response");
    }


    public void viewAccepted(View view) {
        System.out.printf("-- view: %s\n", view);
    }

    public static void main(String[] args) throws Exception {
        JGroupsBenchmark b=new JGroupsBenchmark();
        b.setup();
        //for(int i=1; i <= 10; i++) {
          //  b.testMethod();
        //}
        System.out.println("-- started as server");
        Util.keyPress("enter to terminate");

        b.destroy();
    }

}
