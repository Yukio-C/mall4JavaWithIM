package com.mall.controller.admin;

import com.mall.common.PageResult;
import com.mall.common.Result;
import com.mall.entity.dto.OrderCancelDTO;
import com.mall.entity.dto.OrderPageDTO;
import com.mall.entity.dto.OrderShipDTO;
import com.mall.entity.vo.OrderVO;
import com.mall.service.IOrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/order")
@RequiredArgsConstructor
@Tag(name = "商家订单管理")
@Slf4j
@PreAuthorize("@ss.hasPerm('order')")
@Validated
public class AdminOrderController {

    private final IOrderService orderService;

    @GetMapping("/list")
    @Operation(summary = "全量订单查询", description = "支持按订单号、用户ID、状态过滤")
    public Result<PageResult<OrderVO>> list(@ParameterObject OrderPageDTO orderPageDTO) {
        return Result.success(orderService.getAdminOrderList(orderPageDTO));
    }

    @PostMapping("/ship/{id}")
    @Operation(summary = "订单发货")
    public Result<String> ship(@PathVariable Long id, @RequestBody @Valid OrderShipDTO shipDTO) {
        orderService.shipOrder(id, shipDTO);
        return Result.success("发货成功");
    }

    @PostMapping("/cancel")
    @Operation(summary = "管理员取消订单")
    public Result<String> cancelOrder(@RequestBody @Valid OrderCancelDTO cancelDTO) {
        orderService.cancelOrderAdmin(cancelDTO.getOrderId(), cancelDTO.getReason());
        return Result.success("取消成功");
    }
}
