package com.external.plugins;

import com.appsmith.external.models.ActionConfiguration;
import com.appsmith.external.models.ActionExecutionResult;
import com.appsmith.external.models.DBAuth;
import com.appsmith.external.models.DatasourceConfiguration;
import com.appsmith.external.models.Endpoint;
import net.schmizz.sshj.connection.channel.direct.Session;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.testcontainers.containers.GenericContainer;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class SSHPluginTest {
    private static String host;
    private static Integer port;
    private static final String USERNAME = "root";
    private static final String PASSWORD = "root";

        @ClassRule
    // Working!
    public static final GenericContainer sshServer = new GenericContainer(CompletableFuture.completedFuture("sickp/alpine-sshd:7.9-r1"))
//            .withEnv(Map.of("CNTUSER", "username", "CNTPASS", "password"))
            .withExposedPorts(22);
//    public static final GenericContainer sshServer = new GenericContainer(CompletableFuture.completedFuture("woahbase/alpine-ssh:x86_64"))
//            .withEnv(Map.of(
//                    "CNTUSER", USERNAME,
//                    "CNTPASS", PASSWORD,
//                    "PGID", "1000",
//                    "PUID", "1000"
//            ))
//            .withExposedPorts(22, 68422);

    private SSHPlugin.SSHPluginExecutor pluginExecutor = new SSHPlugin.SSHPluginExecutor();
//    private static SshServer sshd;

    @BeforeClass
    public static void setup() throws IOException {
        System.out.println("In the setup");
//        sshServer.start();
        host = sshServer.getContainerIpAddress();
        port = sshServer.getFirstMappedPort();
        System.out.println("Finished the setup");
//        setupSSHServer();
    }

//    private static void setupSSHServer() throws IOException {
//        sshd = SshServer.setUpDefaultServer();
//        sshd.setPort(port);
//        sshd.setKeyPairProvider(new SimpleGeneratorHostKeyProvider(Paths.get(testFolder.newFile("hostkey.ser").getAbsolutePath())));
//        sshd.setPasswordAuthenticator((username, password, session) ->
//                StringUtils.equals(username, USERNAME) && StringUtils.equals(password, PASSWORD)
//        );
//        CommandFactory testCommandFactory = new CommandFactory() {
//
//            public Command createCommand(ChannelSession channelSession, String command) {
//                System.out.println("command: " + command);
//                return null;
//            }
//        };
//        sshd.setShellFactory(new ProcessShellFactory("/bin/sh", "-i", "-l"));
//        sshd.setCommandFactory(testCommandFactory);
//        sshd.setSubsystemFactories(Collections.<NamedFactory<Command>>singletonList(new SftpSubsystemFactory()));
////        list<namedfactory<command>> namedfactorylist = new arraylist<namedfactory<command>>();
////        namedfactorylist.add(new sftpsubsystem.factory());
////        sshd.setsubsystemfactories(namedfactorylist);
//
//        sshd.start();
//    }

    @AfterClass
    public static void cleanup() throws IOException {
        System.out.println("Going to stop all containers");
//        sshd.stop(true);
        sshServer.stop();
    }

    private DatasourceConfiguration createDatasourceConfiguration() {
        Endpoint endpoint = new Endpoint();
        endpoint.setHost(host);
        endpoint.setPort(Long.valueOf(port));
        DBAuth dbAuth = new DBAuth();
        dbAuth.setUsername(USERNAME);
        dbAuth.setPassword(PASSWORD);
        DatasourceConfiguration datasourceConfiguration = new DatasourceConfiguration();
        datasourceConfiguration.setEndpoints(Collections.singletonList(endpoint));
        datasourceConfiguration.setAuthentication(dbAuth);

        return datasourceConfiguration;
    }

    @Test
    public void itShouldCreateDatasource() {
        DatasourceConfiguration datasourceConfiguration = createDatasourceConfiguration();
        Mono<Session> dsMono = pluginExecutor.datasourceCreate(datasourceConfiguration).cache();

        StepVerifier.create(dsMono)
                .assertNext(session -> {
                    Assert.assertNotNull(session);
                    Assert.assertTrue(session.isOpen());
                })
                .verifyComplete();

        pluginExecutor.datasourceDestroy(dsMono.block());
    }

    @Test
    public void executeTest() {
        DatasourceConfiguration datasourceConfiguration = createDatasourceConfiguration();
        ActionConfiguration actionConfiguration = new ActionConfiguration();
        actionConfiguration.setTimeoutInMillisecond("50000");
        actionConfiguration.setBody("echo 'hello world'");
        Mono<Session> dsMono = pluginExecutor.datasourceCreate(datasourceConfiguration).cache();
        Mono<ActionExecutionResult> resultMono =
                dsMono.flatMap(session -> pluginExecutor.execute(session, datasourceConfiguration, actionConfiguration));
        StepVerifier.create(resultMono)
                .assertNext(result -> {
                    System.out.println(result.toString());
                    Assert.assertNotNull(result);
                    Assert.assertTrue(result.getIsExecutionSuccess());
                })
                .verifyComplete();

        pluginExecutor.datasourceDestroy(dsMono.block());
    }
}
