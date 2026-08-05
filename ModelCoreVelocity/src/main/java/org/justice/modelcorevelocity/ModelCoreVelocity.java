package org.justice.modelcorevelocity;

import com.google.inject.Inject;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.proxy.ProxyServer;
import org.justice.modelapicorereloaded.restapi.RestApiServer;
import org.justice.modelapicorereloaded.service.ModelService;

import java.io.IOException;
import java.util.logging.Logger;

@Plugin(
        id = "modelcorevelocity",
        name = "ModelCoreVelocity",
        version = "1.0-SNAPSHOT",
        description = "A plugin to execute commands on backend servers from Velocity.",
        authors = {"YourName"}
)
public class ModelCoreVelocity {
    private final ProxyServer server;
    private final Logger logger;
    RestApiServer restApiServer;
    ModelService modelService;

    @Inject
    public ModelCoreVelocity(ProxyServer server, Logger logger) {
        this.server = server;
        this.logger = logger;
        logger.info("ModelCore is enabled!");
    }

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) throws IOException, ClassNotFoundException {

    }

}