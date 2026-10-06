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

/** Cach 4: bang idempotency_record, key + request hash + order da tao. */
@Service
@RequiredArgsConstructor
public class IdempotentOrderService {

    private final IdempotencyRecordRepository recordRepository;
    private final OrderCreator creator;
    private final OrderRepository orderRepository;

    @Transactional
    public Order create(CreateOrderRequest req, String key) {
        String hash = sha256(req.canonical());

        // Request thu hai dung key nay se bi chan o day cho toi khi request dau commit hoac rollback
        if (recordRepository.insertIfAbsent(key, req.userId(), hash) == 0) {
            IdempotencyRecord rec = recordRepository.findById(key).orElseThrow();
            if (rec.getUserId() != req.userId() || !rec.getRequestHash().equals(hash)) {
                // Cung key nhung noi dung khac: loi cua client (hoac bi gia mao)
                throw new IdempotencyConflictException("Idempotency-Key da duoc dung cho request khac");
            }
            return orderRepository.findById(rec.getOrderId()).orElseThrow();
        }

        // Record va order nam cung transaction: cung commit hoac cung rollback
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
