package com.pavelsklenar;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
@TestPropertySource(properties = { "sftp.port = 10022", "sftp.remote.directory.download.filter=*.xxx"})
public class SpringSftpDownloadDemoApplicationTests {

    private static EmbeddedSftpServer server;

    private static Path sftpFolder;

    @Value("${sftp.local.directory.download}")
    private String localDirectoryDownload;


    @BeforeClass
    public static void startServer() throws Exception {
        server = new EmbeddedSftpServer();
        server.setPort(10022);
        sftpFolder = Files.createTempDirectory("SFTP_DOWNLOAD_TEST");
        server.afterPropertiesSet();
        server.setHomeFolder(sftpFolder);
        // Starting SFTP
        if (!server.isRunning()) {
            server.start();
        }
    }

    @Before
    @After
    public void clean() throws IOException {
        Files.walk(Paths.get(localDirectoryDownload)).filter(Files::isRegularFile).map(Path::toFile)
                .forEach(File::delete);
    }

    @Test
    public void testDownload() throws IOException, InterruptedException, ExecutionException, TimeoutException {
        // Prepare phase
        Path tempFile = Files.createTempFile(sftpFolder, "TEST_DOWNLOAD_", ".xxx");

        // Run async task to wait for expected files to be downloaded to a file
        // system from a remote SFTP server
        Future<Boolean> future = Executors.newSingleThreadExecutor().submit(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                Path expectedFile = Paths.get(localDirectoryDownload).resolve(tempFile.getFileName());
                while (!Files.exists(expectedFile)) {
                    Thread.sleep(200);
                }
                return true;
            }
        });

        // Validation phase
        assertTrue(future.get(10, TimeUnit.SECONDS));
        assertTrue(Files.notExists(tempFile));
    }

    @AfterClass
    public static void stopServer() {
        if (server.isRunning()) {
            server.stop();
        }
    }

}
