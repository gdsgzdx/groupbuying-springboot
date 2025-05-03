-- 创建拼团表
CREATE TABLE group_buys (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    invite_code VARCHAR(8) NOT NULL UNIQUE,
    product_id BIGINT NOT NULL,
    leader_id BIGINT NOT NULL,
    group_price DECIMAL(10,2) NOT NULL,
    required_members INT NOT NULL DEFAULT 5,
    current_members INT NOT NULL DEFAULT 1,
    status VARCHAR(20) NOT NULL DEFAULT 'pending',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    end_time TIMESTAMP NOT NULL,
    FOREIGN KEY (product_id) REFERENCES products(id),
    FOREIGN KEY (leader_id) REFERENCES users(id)
);

-- 创建拼团成员关联表
CREATE TABLE group_buy_members (
    group_buy_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    joined_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (group_buy_id, user_id),
    FOREIGN KEY (group_buy_id) REFERENCES group_buys(id),
    FOREIGN KEY (user_id) REFERENCES users(id)
);

-- 创建索引
CREATE INDEX idx_group_buys_invite_code ON group_buys(invite_code);
CREATE INDEX idx_group_buys_status ON group_buys(status);
CREATE INDEX idx_group_buys_end_time ON group_buys(end_time);
CREATE INDEX idx_group_buys_product_id ON group_buys(product_id);
CREATE INDEX idx_group_buys_leader_id ON group_buys(leader_id); 