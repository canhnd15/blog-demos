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
            // Token cung la idempotency key o DB: lop bao ve cuoi neu Redis mat du lieu
            return dbService.create(req, token);
        }
        // Token khong con: da dung roi (retry hop le) hoac gia mao
        return orderRepository.findByIdempotencyKey(token)
                .filter(o -> o.getUserId() == req.userId())
                .orElseThrow(() -> new InvalidTokenException("Token khong hop le hoac het han"));
    }
}
