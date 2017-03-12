package sample;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by narey on 12.03.2017.
 */
public class TableItemTest {
    @Test
    public void getName() throws Exception {
        TableItem t = new TableItem();
        t.setName("test");
        assertEquals(t.getName(), "test");
    }

}