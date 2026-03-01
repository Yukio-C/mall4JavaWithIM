package com.mall.controller.admin;

import com.mall.common.PageResult;
import com.mall.common.Result;
import com.mall.entity.dto.AfterSaleDTO;
import com.mall.entity.dto.AfterSalePageDTO;
import com.mall.entity.vo.AfterSaleVO;
import com.mall.service.IAfterSaleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/after-sale")
@RequiredArgsConstructor
@Tag(name = "商家售后管理")
@PreAuthorize("@ss.hasPerm('order')") // 售后通常归属于订单管理权限
@Validated
public class AdminAfterSaleController {

    private final IAfterSaleService afterSaleService;

    /**
     * 分页查询所有用户的售后申请
     */
    @GetMapping("/list")
    @Operation(summary = "获取全量售后列表")
    public Result<PageResult<AfterSaleVO>> list(@ParameterObject AfterSalePageDTO pageDTO) {
        // 由于是管理端，传入 null 绕过用户 ID 过滤逻辑
        // 我们需要微调一下 Service 以支持管理端查询全量
        return Result.success(afterSaleService.getAfterSaleList(pageDTO, null));
    }

    /**
     * 处理售后申请 (同意/拒绝)
     */
    @PostMapping("/handle")
    @Operation(summary = "处理售后申请", description = "状态变更为 2(已完成) 或 3(已拒绝)")
    public Result<String> handle(@RequestBody AfterSaleDTO dto) {
        afterSaleService.handleAfterSale(dto);
        return Result.success("处理成功");
    }
}
