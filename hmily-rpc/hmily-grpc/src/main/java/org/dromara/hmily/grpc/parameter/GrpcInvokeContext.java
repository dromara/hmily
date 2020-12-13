package org.dromara.hmily.grpc.parameter;

/**
 * Grpc GrpcInvokeContext.
 *
 * @author tydhot
 */
public class GrpcInvokeContext {

    /**
     * grpc args.
     */
    private Object[] args;

    public GrpcInvokeContext(final Object[] args) {
        this.args = args;
    }

    /**
     * get args.
     *
     * @return args args
     */
    public Object[] getArgs() {
        return args;
    }

    /**
     * set args.
     *
     * @param args args
     */
    public void setArgs(final Object[] args) {
        this.args = args;
    }
}
