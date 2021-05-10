package com.external.plugins;

import com.appsmith.external.models.ActionConfiguration;
import com.appsmith.external.models.ActionExecutionResult;
import com.appsmith.external.models.DBAuth;
import com.appsmith.external.models.DatasourceConfiguration;
import com.appsmith.external.models.DatasourceTestResult;
import com.appsmith.external.models.Endpoint;
import com.appsmith.external.plugins.BasePlugin;
import com.appsmith.external.plugins.PluginExecutor;
import lombok.extern.slf4j.Slf4j;
import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.common.IOUtils;
import net.schmizz.sshj.connection.ConnectionException;
import net.schmizz.sshj.connection.channel.direct.Session;
import net.schmizz.sshj.transport.TransportException;
import net.schmizz.sshj.transport.verification.PromiscuousVerifier;
import org.pf4j.Extension;
import org.pf4j.PluginWrapper;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

import java.io.IOException;
import java.util.Set;
import java.util.concurrent.TimeUnit;

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
            final Session.Command cmd;
            ActionExecutionResult result = new ActionExecutionResult();
            try {
                cmd = session.exec(actionConfiguration.getBody());
                String responseBody = IOUtils.readFully(cmd.getInputStream()).toString();
                cmd.join(actionConfiguration.getTimeoutInMillisecond(), TimeUnit.MILLISECONDS);
                result.setBody(responseBody);
                result.setIsExecutionSuccess((cmd.getExitStatus() == 0));
            } catch (ConnectionException e) {
                e.printStackTrace();
            } catch (TransportException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return Mono.just(result);
        }

        @Override
        public Mono<Session> datasourceCreate(DatasourceConfiguration datasourceConfiguration) {

            try {

                final SSHClient ssh = new SSHClient();
                ssh.addHostKeyVerifier(new PromiscuousVerifier());

                System.out.println("Going to connect DS");
                Endpoint endpoint = datasourceConfiguration.getEndpoints().get(0);
                ssh.connect(endpoint.getHost(), endpoint.getPort().intValue());
                System.out.println("Connected to localhost");

                Session session = null;
                DBAuth auth = (DBAuth) datasourceConfiguration.getAuthentication();
                ssh.authPassword(auth.getUsername(), auth.getPassword());
                session = ssh.startSession();
                System.out.println("Started session");

                return Mono.just(session);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return Mono.error(new Exception("Should not happen"));
        }

        @Override
        public void datasourceDestroy(Session session) {
            try {
                session.close();
            } catch (TransportException e) {
                e.printStackTrace();
            } catch (ConnectionException e) {
                e.printStackTrace();
            }
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
