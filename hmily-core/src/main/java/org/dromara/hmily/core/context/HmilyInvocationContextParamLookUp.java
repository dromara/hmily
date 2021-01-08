package org.dromara.hmily.core.context;

import java.util.Map;

@FunctionalInterface
public interface HmilyInvocationContextParamLookUp {
    Map<String, Object> getContextParams();
}
