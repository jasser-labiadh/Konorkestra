package dev.jasser.configDaemon;
import org.junit.jupiter.api.Test;
import dev.jasser.configDaemon.WriteManager;
import org.junit.jupiter.api.*;
import org.mockito.Mockito.*;
import java.io.RandomAccessFile;

import static org.mockito.Mockito.mock;

public class WriteManagerTest {
    @BeforeAll
    static void setUpBeforeClass() throws Exception {
        RandomAccessFile metaDataFile = mock(RandomAccessFile.class);
        RandomAccessFile dataFile = mock(RandomAccessFile.class);
        RandomAccessFile indexFile = mock(RandomAccessFile.class);
        LockManager lockManager = mock(LockManager.class);
        WriteManager writeManager =  new WriteManager(lockManager, metaDataFile, dataFile, indexFile);
    }
    @Test
    void testPut() throws Exception {
        // this acquires the lock
    }
}
