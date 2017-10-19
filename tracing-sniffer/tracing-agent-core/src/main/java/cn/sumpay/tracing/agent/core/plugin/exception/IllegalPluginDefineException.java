package cn.sumpay.tracing.agent.core.plugin.exception;

/**
 * Thrown to indicate that a illegal format plugin definition has been defined in tracing-plugin.define.
 */
public class IllegalPluginDefineException extends Exception {
    public IllegalPluginDefineException(String define) {
        super("Illegal plugin define : " + define);
    }
}
