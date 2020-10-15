package org.dromara.hmily.grpc.parameter;

import io.grpc.Metadata;

/**
 * GrpcHmilyContext.
 *
 * @author tydhot
 */
public class GrpcHmilyContext {

    public static final Metadata.Key<String> HMILY_META_DATA =
            Metadata.Key.of("hmily-meta", Metadata.ASCII_STRING_MARSHALLER);

    private static ThreadLocal<String> hmilyContext = new ThreadLocal<>();

    private static ThreadLocal<Object> hmilyParam = new ThreadLocal<>();

    /**
     * get hmilyContext conext.
     *
     * @return ThreadLocal
     */
    public static ThreadLocal<String> getHmilyContext() {
        return hmilyContext;
    }

    /**
     * get hmilyParam conext.
     *
     * @return ThreadLocal
     */
    public static ThreadLocal<Object> getHmilyParam() {
        return hmilyParam;
    }
}
