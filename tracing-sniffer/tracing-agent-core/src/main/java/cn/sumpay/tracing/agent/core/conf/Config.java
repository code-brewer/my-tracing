package cn.sumpay.tracing.agent.core.conf;

import java.util.LinkedList;
import java.util.List;

/**
 * @author heyc
 * @date 2017/10/19 10:53
 */
public class Config {

    public static class Agent {
        /**
         * Suggestion: set an unique name for each application, one application's nodes share the same code.
         */
        public static String APPLICATION_CODE = "";

        /**
         * Negative or zero means off, by default.
         */
        public static int SAMPLE_N_PER_3_SECS = -1;

        /**
         * If the operation name of the first span is included in this set,
         * this segment should be ignored.
         */
        public static String IGNORE_SUFFIX = ".jpg,.jpeg,.js,.css,.png,.bmp,.gif,.ico,.mp3,.mp4,.html,.svg";
    }

    public static class Collector {
        /**
         * grpc channel status check interval
         */
        public static long GRPC_CHANNEL_CHECK_INTERVAL = 30;
        /**
         * application and service registry check interval
         */
        public static long APP_AND_SERVICE_REGISTER_CHECK_INTERVAL = 10;
        /**
         * discovery rest check interval
         */
        public static long DISCOVERY_CHECK_INTERVAL = 60;
        /**
         * Collector REST-Service address.
         * e.g.
         * SERVERS="127.0.0.1:8080"  for single collector node.
         * SERVERS="10.2.45.126:8080,10.2.45.127:7600"  for multi collector nodes.
         */
        public static String SERVERS = "";

        /**
         * Collector service discovery REST service name
         */
        public static String DISCOVERY_SERVICE_NAME = "/agentstream/grpc";
    }

    public static class Jvm {
        /**
         * The buffer size of collected JVM info.
         */
        public static int BUFFER_SIZE = 60 * 10;
    }

    public static class Buffer {
        public static int CHANNEL_SIZE = 5;

        public static int BUFFER_SIZE = 300;
    }

    public static class Dictionary {
        /**
         * The buffer size of application codes and peer
         */
        public static int APPLICATION_CODE_BUFFER_SIZE = 10 * 10000;

        public static int OPERATION_NAME_BUFFER_SIZE = 1000 * 10000;
    }

    public static class Plugin {

        /**
         * Name of disabled plugin, The value spilt by <code>,</code>
         * if you have multiple plugins need to disable.
         *
         * Here are the plugin names :
         * tomcat-7.x/8.x, dubbo, jedis-2.x, motan, httpclient-4.x, jdbc, mongodb-3.x.
         */
        public static List DISABLED_PLUGINS = new LinkedList();

        /**
         * Name of force enable plugin, The value spilt by <code>,</code>
         * if you have multiple plugins need to enable.
         */
        public static List FORCE_ENABLE_PLUGINS = new LinkedList();

    }
}
