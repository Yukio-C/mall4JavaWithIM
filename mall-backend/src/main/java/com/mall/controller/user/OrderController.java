package com.mall.controller.user;

import com.mall.common.PageResult;
import com.mall.common.Result;
import com.mall.entity.dto.OrderCancelDTO;
import com.mall.entity.dto.OrderDTO;
import com.mall.entity.dto.OrderPageDTO;
import com.mall.entity.vo.OrderVO;
import com.mall.security.LoginUser;
import com.mall.service.IOrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/order")
@RequiredArgsConstructor
@Tag(name = "用户订单接口")
@Validated
public class OrderController {

    private final IOrderService orderService;

    @GetMapping("/token")
    @Operation(summary = "获取下单令牌 (防止重复提交)")
    public Result<Map<String, String>> getToken(@AuthenticationPrincipal LoginUser loginUser) {
        String token = orderService.createOrderToken(loginUser.getUserId());
        return Result.success(Map.of("token", token));
    }

    @PostMapping("/create")
    @Operation(summary = "提交订单")
    public Result<OrderVO> create(@RequestBody @Validated OrderDTO orderDTO, @AuthenticationPrincipal LoginUser loginUser) {
        return Result.success(orderService.createOrder(orderDTO, loginUser.getUserId()));
    }

    @GetMapping("/list")
    @Operation(summary = "分页获取我的订单")
    public Result<PageResult<OrderVO>> list(@ParameterObject OrderPageDTO orderPageDTO, @AuthenticationPrincipal LoginUser loginUser) {
        return Result.success(orderService.getOrderList(orderPageDTO, loginUser.getUserId()));
    }

    @PostMapping("/cancel")
    @Operation(summary = "用户取消订单", description = "仅限待支付状态")
    public Result<String> cancel(
            @RequestBody @Validated OrderCancelDTO cancelDTO,
            @AuthenticationPrincipal LoginUser loginUser
    ) {
        orderService.cancelOrder(cancelDTO.getOrderId(), loginUser.getUserId(), cancelDTO.getReason());
        return Result.success("订单已取消");
    }

    @PostMapping("/confirm/{id}")
    @Operation(summary = "确认收货")
    public Result<String> confirm(@PathVariable Long id, @AuthenticationPrincipal LoginUser loginUser) {
        orderService.confirmReceipt(id, loginUser.getUserId());
        return Result.success("确认收货成功");
    }

    @GetMapping("/detail-by-no")
    @Operation(summary = "根据单号查详情")
    public Result<OrderVO> detailByNo(@RequestParam String orderNo, @AuthenticationPrincipal LoginUser loginUser) {
        return Result.success(orderService.getOrderDetailByNo(orderNo, loginUser.getUserId()));
    }
}
