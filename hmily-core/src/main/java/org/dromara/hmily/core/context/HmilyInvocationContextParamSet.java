package org.dromara.hmily.core.context;

import java.util.Map;

@FunctionalInterface
public interface HmilyInvocationContextParamSet {
  Boolean setContextParam(Map<String, Object> map);
}
