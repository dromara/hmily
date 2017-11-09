/*
 *
 * Copyright 2017-2018 549477611@qq.com(xiaoyu)
 *
 * This copyrighted material is made available to anyone wishing to use, modify,
 * copy, or redistribute it subject to the terms and conditions of the GNU
 * Lesser General Public License, as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this distribution; if not, see <http://www.gnu.org/licenses/>.
 *
 */

package com.happylifeplat.tcc.admin.controller;

import com.happylifeplat.tcc.admin.annotation.Permission;
import com.happylifeplat.tcc.admin.dto.CompensationDTO;
import com.happylifeplat.tcc.admin.page.CommonPager;
import com.happylifeplat.tcc.admin.query.CompensationQuery;
import com.happylifeplat.tcc.admin.service.ApplicationNameService;
import com.happylifeplat.tcc.admin.service.CompensationService;
import com.happylifeplat.tcc.admin.vo.TccCompensationVO;
import com.happylifeplat.tcc.common.utils.httpclient.AjaxResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * <p>Description: .</p>
 * 事务恢复controller
 *
 * @author xiaoyu(Myth)
 * @version 1.0
 * @date 2017/10/18 10:31
 * @since JDK 1.8
 */
@RestController
@RequestMapping("/compensate")
public class CompensationController {


    private final CompensationService compensationService;

    private final ApplicationNameService applicationNameService;

    @Value("${compensation.retry.max}")
    private Integer recoverRetryMax;

    @Autowired
    public CompensationController(CompensationService compensationService, ApplicationNameService applicationNameService) {
        this.compensationService = compensationService;
        this.applicationNameService = applicationNameService;
    }

    @Permission
    @PostMapping(value = "/listPage")
    public AjaxResponse listPage(@RequestBody CompensationQuery recoverQuery) {
        final CommonPager<TccCompensationVO> pager =
                compensationService.listByPage(recoverQuery);
        return AjaxResponse.success(pager);
    }


    @PostMapping(value = "/batchRemove")
    @Permission
    public AjaxResponse batchRemove(@RequestBody CompensationDTO compensationDTO) {

        final Boolean success = compensationService.batchRemove(compensationDTO.getIds(), compensationDTO.getApplicationName());
        return AjaxResponse.success(success);

    }

    @PostMapping(value = "/update")
    @Permission
    public AjaxResponse update(@RequestBody CompensationDTO compensationDTO) {
        if (recoverRetryMax < compensationDTO.getRetry()) {
            return AjaxResponse.error("重试次数超过最大设置，请您重新设置！");
        }
        final Boolean success = compensationService.updateRetry(compensationDTO.getId(),
                compensationDTO.getRetry(), compensationDTO.getApplicationName());
        return AjaxResponse.success(success);

    }

    @PostMapping(value = "/listAppName")
    @Permission
    public AjaxResponse listAppName() {
        final List<String> list = applicationNameService.list();
        return AjaxResponse.success(list);
    }


}
