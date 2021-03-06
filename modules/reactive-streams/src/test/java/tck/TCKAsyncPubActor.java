package tck;

import org.nustaq.kontraktor.reactivestreams.EventSink;
import org.reactivestreams.Publisher;
import org.testng.annotations.Test;

/**
 * Created by ruedi on 03/07/15.
 */
@Test
public class TCKAsyncPubActor extends TCKAsyncPubTestEventSink {

    public TCKAsyncPubActor() {
        super();
    }

    @Override
    public Publisher<Long> createPublisher(long elements) {
        return ((EventSink<Long>)super.createPublisher(elements)).map(l -> l);
    }

}
