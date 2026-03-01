package com.mall.controller.user;

import com.mall.common.Result;
import com.mall.entity.dto.PaymentDTO;
import com.mall.entity.vo.PaymentVO;
import com.mall.security.LoginUser;
import com.mall.service.IPaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/payment")
@RequiredArgsConstructor
@Tag(name = "支付模块", description = "提供支付相关功能")
public class PaymentController {

    private final IPaymentService paymentService;

    @PostMapping("/pay")
    @Operation(summary = "提交支付", description = "支持余额支付及其他扩展方式")
    public Result<PaymentVO> pay(
            @Validated @RequestBody PaymentDTO paymentDTO,
            @AuthenticationPrincipal LoginUser loginUser
    ) {
        return Result.success(paymentService.processPayment(paymentDTO, loginUser.getUserId()));
    }

    @GetMapping("/status")
    @Operation(summary = "查询支付状态", description = "根据订单号查询支付流水状态")
    public Result<PaymentVO> getPaymentStatus(@RequestParam String orderNo) {
        return Result.success(paymentService.getPaymentStatus(orderNo));
    }
}
