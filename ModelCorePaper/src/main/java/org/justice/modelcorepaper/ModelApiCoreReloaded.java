package org.justice.modelcorepaper;

import org.bukkit.plugin.java.JavaPlugin;
import org.justice.modelapicorereloaded.ErrorLogger;
import org.justice.modelapicorereloaded.exception.LoggedException;
import org.justice.modelapicorereloaded.restapi.RestApiServer;
import org.justice.modelapicorereloaded.service.ModelService;

import java.io.IOException;

public final class ModelApiCoreReloaded extends JavaPlugin {

    ErrorLogger errorLogger;
    RestApiServer restApiServer;
    ModelService modelService;

    @Override
    public void onEnable() {
        errorLogger = new ErrorLogger(getDataFolder());

        // Beállítjuk a LoggedException-nek az errorLoggert
        LoggedException.setErrorLogger(errorLogger);


    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
