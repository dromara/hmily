package com.happylifeplat.tcc.motan.filter;



import com.happylifeplat.tcc.annotation.Tcc;
import com.happylifeplat.tcc.annotation.TccPatternEnum;
import com.happylifeplat.tcc.common.bean.context.TccTransactionContext;
import com.happylifeplat.tcc.common.bean.entity.Participant;
import com.happylifeplat.tcc.common.bean.entity.TccInvocation;
import com.happylifeplat.tcc.common.constant.CommonConstant;
import com.happylifeplat.tcc.common.enums.TccActionEnum;
import com.happylifeplat.tcc.common.exception.TccRuntimeException;
import com.happylifeplat.tcc.common.utils.GsonUtils;
import com.happylifeplat.tcc.core.concurrent.threadlocal.TransactionContextLocal;
import com.happylifeplat.tcc.core.helper.SpringBeanUtils;
import com.happylifeplat.tcc.core.service.handler.TccTransactionManager;
import com.weibo.api.motan.common.MotanConstants;
import com.weibo.api.motan.core.extension.Activation;
import com.weibo.api.motan.core.extension.SpiMeta;
import com.weibo.api.motan.filter.Filter;
import com.weibo.api.motan.rpc.Caller;
import com.weibo.api.motan.rpc.Request;
import com.weibo.api.motan.rpc.Response;
import com.weibo.api.motan.util.ReflectUtil;
import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.Method;
import java.util.Objects;
import java.util.stream.Stream;


/**
 * @author xiaoyu
 */
@SpiMeta(name = "motanTccTransactionFilter")
@Activation(key = {MotanConstants.NODE_TYPE_REFERER})
public class MotanTccTransactionFilter implements Filter {

    /**
     * 实现新浪的filter接口 rpc传参数
     *
     * @param caller  caller
     * @param request 请求
     * @return Response
     */
    @Override
    @SuppressWarnings("unchecked")
    public Response filter(Caller<?> caller, Request request) {

        final String interfaceName = request.getInterfaceName();
        final String methodName = request.getMethodName();
        final Object[] arguments = request.getArguments();

        Class[] args = null;
        Method method = null;
        Tcc tcc = null;
        Class clazz = null;
        try {
            //他妈的 这里还要拿方法参数类型
            clazz = ReflectUtil.forName(interfaceName);
            final Method[] methods = clazz.getMethods();
            args =
                    Stream.of(methods)
                            .filter(m -> m.getName().equals(methodName))
                            .findFirst()
                            .map(Method::getParameterTypes).get();
            method = clazz.getDeclaredMethod(methodName, args);
            tcc = method.getAnnotation(Tcc.class);
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (Objects.nonNull(tcc)) {

            try {

                final TccTransactionContext tccTransactionContext =
                        TransactionContextLocal.getInstance().get();
                if (Objects.nonNull(tccTransactionContext)) {
                    request
                            .setAttachment(CommonConstant.TCC_TRANSACTION_CONTEXT,
                                    GsonUtils.getInstance().toJson(tccTransactionContext));
                }

                final Response response = caller.call(request);
                final Participant participant = buildParticipant(tccTransactionContext,tcc, method, clazz, arguments, args);
                if (Objects.nonNull(participant)) {
                    SpringBeanUtils.getInstance().getBean(TccTransactionManager.class).enlistParticipant(participant);
                }

                return response;
            } catch (Exception e) {
                e.printStackTrace();
                throw e;
            }
        } else {
            return caller.call(request);
        }
    }


    @SuppressWarnings("unchecked")
    private Participant buildParticipant(TccTransactionContext tccTransactionContext, Tcc tcc,
                                         Method method, Class clazz,
                                         Object[] arguments,
                                         Class... args) throws TccRuntimeException {

        if (Objects.nonNull(tccTransactionContext)) {
            if (TccActionEnum.TRYING.getCode() == tccTransactionContext.getAction()) {
                //获取协调方法
                String confirmMethodName = tcc.confirmMethod();

                if (StringUtils.isBlank(confirmMethodName)) {
                    confirmMethodName = method.getName();
                }

                String cancelMethodName = tcc.cancelMethod();

                if (StringUtils.isBlank(cancelMethodName)) {
                    cancelMethodName = method.getName();
                }

                //设置模式
                final TccPatternEnum pattern = tcc.pattern();

                SpringBeanUtils.getInstance().getBean(TccTransactionManager.class)
                        .getCurrentTransaction().setPattern(pattern.getCode());


                TccInvocation confirmInvocation = new TccInvocation(clazz,
                        confirmMethodName,
                        args, arguments);

                TccInvocation cancelInvocation = new TccInvocation(clazz,
                        cancelMethodName,
                        args, arguments);

                //封装调用点
                return new Participant(
                        tccTransactionContext.getTransId(),
                        confirmInvocation,
                        cancelInvocation);
            }

        }
        return null;

    }
}
