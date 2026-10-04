-- KEYS[1] = stock:{sku}, ARGV[1] = qty
local stock = tonumber(redis.call('GET', KEYS[1]))
if stock == nil then
    return -1   -- key chua duoc warm-up
end
if stock < tonumber(ARGV[1]) then
    return 0    -- khong du hang
end
redis.call('DECRBY', KEYS[1], ARGV[1])
return 1        -- tru thanh cong
