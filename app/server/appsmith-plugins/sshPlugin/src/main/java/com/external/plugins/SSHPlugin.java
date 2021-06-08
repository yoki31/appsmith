package com.external.plugins;

import com.appsmith.external.models.ActionConfiguration;
import com.appsmith.external.models.ActionExecutionResult;
import com.appsmith.external.models.DBAuth;
import com.appsmith.external.models.DatasourceConfiguration;
import com.appsmith.external.models.DatasourceTestResult;
import com.appsmith.external.models.Endpoint;
import com.appsmith.external.models.SSHConnection;
import com.appsmith.external.plugins.BasePlugin;
import com.appsmith.external.plugins.PluginExecutor;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.pf4j.Extension;
import org.pf4j.PluginWrapper;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Set;

public class SSHPlugin extends BasePlugin {

    public SSHPlugin(PluginWrapper wrapper) {
        super(wrapper);
    }

    @Slf4j
    @Extension
    public static class SSHPluginExecutor implements PluginExecutor<Session> {

        private final Scheduler scheduler = Schedulers.elastic();

        @Override
        public Mono<ActionExecutionResult> execute(Session session,
                                                   DatasourceConfiguration datasourceConfiguration,
                                                   ActionConfiguration actionConfiguration) {
//            final Session.Command cmd;
            ActionExecutionResult result = new ActionExecutionResult();
//            try {
//                cmd = session.exec(actionConfiguration.getBody());
//                String responseBody = IOUtils.readFully(cmd.getInputStream()).toString();
//                cmd.join(actionConfiguration.getTimeoutInMillisecond(), TimeUnit.MILLISECONDS);
//                result.setBody(responseBody);
//                result.setIsExecutionSuccess((cmd.getExitStatus() == 0));
//            } catch (ConnectionException e) {
//                e.printStackTrace();
//            } catch (TransportException e) {
//                e.printStackTrace();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }

            try {
                ChannelExec channel = (ChannelExec) session.openChannel("exec");
                channel.setCommand(actionConfiguration.getBody());
                StringBuilder outputBuffer = new StringBuilder();
                ByteArrayOutputStream responseStream = new ByteArrayOutputStream();
                channel.setOutputStream(responseStream);
                channel.connect();
                InputStream inputStream = channel.getInputStream();
                int readByte = inputStream.read();
                while (readByte != 0xffffffff) {
                    outputBuffer.append((char) readByte);
                    readByte = inputStream.read();
//                    String responseString = new String(responseStream.toByteArray());
                }
                String responseString = outputBuffer.toString();
                System.out.println("Got the response: " + responseString + " with exit status: " + channel.getExitStatus());
                result.setBody(responseString);
                result.setIsExecutionSuccess(channel.getExitStatus() == 0);
            } catch (JSchException e) {
                e.printStackTrace();
            }
//            catch (InterruptedException e) {
//                e.printStackTrace();
//            }
            catch (IOException e) {
                e.printStackTrace();
            }
            return Mono.just(result);
        }

        @Override
        public Mono<Session> datasourceCreate(DatasourceConfiguration datasourceConfiguration) {

            Session session = null;
            try {

//                final SSHClient ssh = new SSHClient();
//                ssh.addHostKeyVerifier(new PromiscuousVerifier());
                JSch jSch = new JSch();
                String privateKeyPem = "user's private pem file data";
                SSHConnection sshProxy = datasourceConfiguration.getSshProxy();
                if (sshProxy != null && sshProxy.getAuthType() == SSHConnection.AuthType.IDENTITY_FILE) {
                    String privateKey = sshProxy.getPrivateKey().getKeyFile().getBase64Content();
                    String privateKeyName = sshProxy.getPrivateKey().getKeyFile().getName();
                    jSch.addIdentity(privateKeyName, privateKey.getBytes(), null, null);
                }

                System.out.println("Going to connect DS");
                Endpoint endpoint = datasourceConfiguration.getEndpoints().get(0);
                DBAuth auth = (DBAuth) datasourceConfiguration.getAuthentication();
                session = jSch.getSession(auth.getUsername(), endpoint.getHost(), endpoint.getPort().intValue());
//                session.setPassword(auth.getPassword());
                session.setConfig("StrictHostKeyChecking", "no");
                session.connect();

//                ssh.connect(endpoint.getHost(), endpoint.getPort().intValue());
                System.out.println("Connected to localhost");


//                ssh.authPassword(auth.getUsername(), auth.getPassword());
//                PublicKey publicKey;
//                PrivateKey privateKeyPem;
//                KeyPair keyPair = new KeyPair(publicKey, privateKeyPem);
//                ssh.authPublickey();
//                session = ssh.startSession();
                System.out.println("Started session");

                return Mono.just(session);
            } catch (JSchException e) {
                e.printStackTrace();
            }
            return Mono.error(new Exception("Should not happen"));
        }

        @Override
        public void datasourceDestroy(Session session) {
            session.disconnect();
        }

        @Override
        public Set<String> validateDatasource(DatasourceConfiguration datasourceConfiguration) {
            return null;
        }

        @Override
        public Mono<DatasourceTestResult> testDatasource(DatasourceConfiguration datasourceConfiguration) {
            return null;
        }

    }
}
