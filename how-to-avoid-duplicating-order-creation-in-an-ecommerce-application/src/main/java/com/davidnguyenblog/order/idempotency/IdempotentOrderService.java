package com.davidnguyenblog.order.idempotency;

import com.davidnguyenblog.order.order.CreateOrderRequest;
import com.davidnguyenblog.order.order.Order;
import com.davidnguyenblog.order.order.OrderCreator;
import com.davidnguyenblog.order.order.OrderRepository;
import com.davidnguyenblog.order.web.IdempotencyConflictException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Cách 4: bảng idempotency_record, key + request hash + order đã tạo. */
@Service
@RequiredArgsConstructor
public class IdempotentOrderService {

    private final IdempotencyRecordRepository recordRepository;
    private final OrderCreator creator;
    private final OrderRepository orderRepository;

    @Transactional
    public Order create(CreateOrderRequest req, String key) {
        String hash = sha256(req.canonical());

        // Request thứ hai dùng key này sẽ bị chặn ở đây cho tới khi request đầu commit hoặc rollback
        if (recordRepository.insertIfAbsent(key, req.userId(), hash) == 0) {
            IdempotencyRecord rec = recordRepository.findById(key).orElseThrow();
            if (rec.getUserId() != req.userId() || !rec.getRequestHash().equals(hash)) {
                // Cùng key nhưng nội dung khác: lỗi của client (hoặc bị giả mạo)
                throw new IdempotencyConflictException("Idempotency-Key da duoc dung cho request khac");
            }
            return orderRepository.findById(rec.getOrderId()).orElseThrow();
        }

        // Record và order nằm cùng transaction: cùng commit hoặc cùng rollback
        Order order = creator.create(req, null);
        recordRepository.attachOrder(key, order.getId());
        return order;
    }

    private static String sha256(String s) {
        try {
            byte[] d = MessageDigest.getInstance("SHA-256").digest(s.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(d);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }
}
