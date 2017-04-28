package com.pavelsklenar;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.RSAPublicKeySpec;
import java.util.Collections;

import org.apache.commons.codec.binary.Base64;
import org.apache.sshd.common.NamedFactory;
import org.apache.sshd.common.file.virtualfs.VirtualFileSystemFactory;
import org.apache.sshd.server.Command;
import org.apache.sshd.server.SshServer;
import org.apache.sshd.server.auth.pubkey.PublickeyAuthenticator;
import org.apache.sshd.server.keyprovider.SimpleGeneratorHostKeyProvider;
import org.apache.sshd.server.scp.ScpCommandFactory;
import org.apache.sshd.server.session.ServerSession;
import org.apache.sshd.server.subsystem.sftp.SftpSubsystemFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.SmartLifecycle;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.SocketUtils;
import org.springframework.util.StreamUtils;

/**
 * SFTP server for integration testing
 * @author Artem Bilan
 * @author pavel.sklenar
 */
public class EmbeddedSftpServer implements InitializingBean, SmartLifecycle {

    public static final int PORT = SocketUtils.findAvailableTcpPort();

    private final SshServer server = SshServer.setUpDefaultServer();

    private volatile int port;

    private volatile boolean running;

    public void setPort(int port) {
        this.port = port;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        final PublicKey allowedKey = decodePublicKey();
        this.server.setPublickeyAuthenticator(new PublickeyAuthenticator() {

            @Override
            public boolean authenticate(String username, PublicKey key, ServerSession session) {
                return key.equals(allowedKey);
            }

        });
        this.server.setPort(this.port);
        this.server.setKeyPairProvider(new SimpleGeneratorHostKeyProvider(Files.createTempFile("host_file", ".ser")));
        this.server.setSubsystemFactories(Collections.<NamedFactory<Command>>singletonList(new SftpSubsystemFactory()));
        server.setFileSystemFactory(new VirtualFileSystemFactory(Files.createTempDirectory("SFTP_TEMP")));
        server.setCommandFactory(new ScpCommandFactory());
    }

    public void setHomeFolder(Path path) {
        server.setFileSystemFactory(new VirtualFileSystemFactory(path));
    }


    private PublicKey decodePublicKey() throws Exception {
        InputStream stream = new ClassPathResource("/keys/sftp_rsa.pub").getInputStream();
        byte[] decodeBuffer = Base64.decodeBase64(StreamUtils.copyToByteArray(stream));
        ByteBuffer bb = ByteBuffer.wrap(decodeBuffer);
        int len = bb.getInt();
        byte[] type = new byte[len];
        bb.get(type);
        if ("ssh-rsa".equals(new String(type))) {
            BigInteger e = decodeBigInt(bb);
            BigInteger m = decodeBigInt(bb);
            RSAPublicKeySpec spec = new RSAPublicKeySpec(m, e);
            return KeyFactory.getInstance("RSA").generatePublic(spec);

        }
        else {
            throw new IllegalArgumentException("Only supports RSA");
        }
    }

    private BigInteger decodeBigInt(ByteBuffer bb) {
        int len = bb.getInt();
        byte[] bytes = new byte[len];
        bb.get(bytes);
        return new BigInteger(bytes);
    }

    @Override
    public boolean isAutoStartup() {
        return PORT == this.port;
    }

    @Override
    public int getPhase() {
        return Integer.MAX_VALUE;
    }

    @Override
    public void start() {
        try {
            server.start();
            this.running  = true;
        }
        catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public void stop(Runnable callback) {
        stop();
        callback.run();
    }

    @Override
    public void stop() {
        if (this.running) {
            try {
                server.stop(false);
            }
            catch (IOException e) {
                throw new IllegalStateException(e);
            }
            finally {
                this.running = false;
            }
        }
    }

    @Override
    public boolean isRunning() {
        return this.running;
    }

    public SshServer getServer() {
        return server;
    }

}
