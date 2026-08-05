package org.justice.modelapicorereloaded.restapi;

public class ControllerDefinition {
    final Class<?> controllerClass;
    final Object instance;

    public ControllerDefinition(Class<?> controllerClass, Object instance) {
        this.controllerClass = controllerClass;
        this.instance = instance;
    }
}