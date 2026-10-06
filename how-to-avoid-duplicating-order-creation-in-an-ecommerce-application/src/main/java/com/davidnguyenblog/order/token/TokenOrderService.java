package com.davidnguyenblog.order.token;

import com.davidnguyenblog.order.order.CreateOrderRequest;
import com.davidnguyenblog.order.order.Order;
import com.davidnguyenblog.order.order.OrderRepository;
import com.davidnguyenblog.order.order.UniqueKeyOrderService;
import com.davidnguyenblog.order.web.InvalidTokenException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TokenOrderService {

    private final CheckoutTokenService tokenService;
    private final UniqueKeyOrderService dbService;
    private final OrderRepository orderRepository;

    public Order create(CreateOrderRequest req, String token) {
        if (tokenService.consume(token, req.userId(), req.cartId())) {
            // Token cũng là idempotency key ở DB: lớp bảo vệ cuối nếu Redis mất dữ liệu
            return dbService.create(req, token);
        }
        // Token không còn: đã dùng rồi (retry hợp lệ) hoặc giả mạo
        return orderRepository.findByIdempotencyKey(token)
                .filter(o -> o.getUserId() == req.userId())
                .orElseThrow(() -> new InvalidTokenException("Token khong hop le hoac het han"));
    }
}
