package com.pavelsklenar;

import java.io.File;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.integration.annotation.InboundChannelAdapter;
import org.springframework.integration.annotation.Poller;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.core.MessageSource;
import org.springframework.integration.file.filters.AcceptOnceFileListFilter;
import org.springframework.integration.file.remote.session.CachingSessionFactory;
import org.springframework.integration.file.remote.session.SessionFactory;
import org.springframework.integration.sftp.filters.SftpSimplePatternFileListFilter;
import org.springframework.integration.sftp.inbound.SftpInboundFileSynchronizer;
import org.springframework.integration.sftp.inbound.SftpInboundFileSynchronizingMessageSource;
import org.springframework.integration.sftp.session.DefaultSftpSessionFactory;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHandler;
import org.springframework.messaging.MessagingException;

import com.jcraft.jsch.ChannelSftp.LsEntry;

@Configuration
public class SftpConfig {

    @Value("${sftp.host}")
    private String sftpHost;

    @Value("${sftp.port:22}")
    private int sftpPort;

    @Value("${sftp.user}")
    private String sftpUser;

    @Value("${sftp.privateKey:#{null}}")
    private Resource sftpPrivateKey;

    @Value("${sftp.privateKeyPassphrase:}")
    private String sftpPrivateKeyPassphrase;

    @Value("${sftp.password:#{null}}")
    private String sftpPasword;

    @Value("${sftp.remote.directory.download:/}")
    private String sftpRemoteDirectoryDownload;

    @Value("${sftp.local.directory.download:${java.io.tmpdir}/localDownload}")
    private String sftpLocalDirectoryDownload;

    @Value("${sftp.remote.directory.download.filter:*.*}")
    private String sftpRemoteDirectoryDownloadFilter;

    @Bean
    public SessionFactory<LsEntry> sftpSessionFactory() {
        DefaultSftpSessionFactory factory = new DefaultSftpSessionFactory(true);
        factory.setHost(sftpHost);
        factory.setPort(sftpPort);
        factory.setUser(sftpUser);
        if (sftpPrivateKey != null) {
            factory.setPrivateKey(sftpPrivateKey);
            factory.setPrivateKeyPassphrase(sftpPrivateKeyPassphrase);
        } else {
            factory.setPassword(sftpPasword);
        }
        factory.setAllowUnknownKeys(true);
        return new CachingSessionFactory<LsEntry>(factory);
    }

    @Bean
    public SftpInboundFileSynchronizer sftpInboundFileSynchronizer() {
        SftpInboundFileSynchronizer fileSynchronizer = new SftpInboundFileSynchronizer(sftpSessionFactory());
        fileSynchronizer.setDeleteRemoteFiles(true);
        fileSynchronizer.setRemoteDirectory(sftpRemoteDirectoryDownload);
        fileSynchronizer
                .setFilter(new SftpSimplePatternFileListFilter(sftpRemoteDirectoryDownloadFilter));
        return fileSynchronizer;
    }

    @Bean
    @InboundChannelAdapter(channel = "fromSftpChannel", poller = @Poller(cron = "0/5 * * * * *"))
    public MessageSource<File> sftpMessageSource() {
        SftpInboundFileSynchronizingMessageSource source = new SftpInboundFileSynchronizingMessageSource(
                sftpInboundFileSynchronizer());
        source.setLocalDirectory(new File(sftpLocalDirectoryDownload));
        source.setAutoCreateLocalDirectory(true);
        source.setLocalFilter(new AcceptOnceFileListFilter<File>());
        return source;
    }

    @Bean
    @ServiceActivator(inputChannel = "fromSftpChannel")
    public MessageHandler resultFileHandler() {
        return new MessageHandler() {
            @Override
            public void handleMessage(Message<?> message) throws MessagingException {
                System.err.println(message.getPayload());
            }
        };
    }

}
