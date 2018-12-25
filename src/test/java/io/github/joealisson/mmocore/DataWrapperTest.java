package io.github.joealisson.mmocore;

import org.junit.Assert;
import org.junit.Test;

import static org.junit.Assert.fail;

public class DataWrapperTest {

    @Test
    public void  testnonReadablePacket() {
        DataWrapper wrapper = DataWrapper.wrap(new byte[10]);
        Assert.assertFalse(wrapper.read());
    }


    @Test
    public void testnonRunnablePacket()  {
        DataWrapper wrapper = DataWrapper.wrap(new byte[10]);
        try {
            wrapper.run();
            fail("The DataWrapper can't invoke run method");
        } catch (UnsupportedOperationException ex) {
            // expected
        }
    }
}
