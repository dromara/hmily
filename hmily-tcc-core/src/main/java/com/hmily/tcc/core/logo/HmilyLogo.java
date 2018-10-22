/*
 *   Licensed to the Apache Software Foundation (ASF) under one or more
 *   contributor license agreements.  See the NOTICE file distributed with
 *   this work for additional information regarding copyright ownership.
 *   The ASF licenses this file to You under the Apache License, Version 2.0
 *   (the "License"); you may not use this file except in compliance with
 *   the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *
 */

package com.hmily.tcc.core.logo;

import com.hmily.tcc.common.constant.CommonConstant;
import com.hmily.tcc.common.utils.VersionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The hmily logo.
 *
 * @author xiaoyu
 */
public class HmilyLogo {

    private static final String HMILY_LOGO = "\n" +
            "    __  __          _ __     \n" +
            "   / / / /___ ___  (_) /_  __\n" +
            "  / /_/ / __ `__ \\/ / / / / /\n" +
            " / __  / / / / / / / / /_/ / \n" +
            "/_/ /_/_/ /_/ /_/_/_/\\__, /  \n" +
            "                    /____/   \n";

    /**
     * logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(HmilyLogo.class);

    public void logo() {
        String bannerText = buildBannerText();
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info(bannerText);
        } else {
            System.out.print(bannerText);
        }
    }

    private String buildBannerText() {
        return CommonConstant.LINE_SEPARATOR
                + CommonConstant.LINE_SEPARATOR
                + HMILY_LOGO
                + CommonConstant.LINE_SEPARATOR
                + " :: Hmily :: (v" + VersionUtils.getVersion(getClass(), "1.0.0") + ")"
                + CommonConstant.LINE_SEPARATOR;
    }

}
