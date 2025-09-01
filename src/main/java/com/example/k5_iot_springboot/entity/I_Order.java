package com.example.k5_iot_springboot.entity;

import com.example.k5_iot_springboot.common.enums.OrderStatus;
import com.example.k5_iot_springboot.entity.base.BaseTimeEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;


@Entity
@Table(
        name = "orders",
        indexes = {
                @Index(name = "idx_orders_user", columnList = "user_id"),
                @Index(name = "idx_orders_status", columnList = "order_status"),
                @Index(name = "idx_orders_created_at", columnList = "created_at")
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class I_Order extends BaseTimeEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false,
        foreignKey = @ForeignKey(name = "fk_orders_user"))
    private G_User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "order_status", nullable = false, length = 50)
    private OrderStatus orderStatus = OrderStatus.PENDING;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    // - mappedBy: 주인관계 지정(양방향 매핑에서 연관관계의 주인을 I_OrderItem 으로 지정)
    //  >> "order"는 I_OrderItem 의 order 필드명을 가리킴
    // orphanRemoval=true -> items 리스트에서 요소 제거시, 해당 요소의 DB 에서 OrderItem 레코드가 삭제됨
//    @Builder.Default
    private List<I_OrderItem> items = new ArrayList<>();

    @Builder
    private I_Order(@NotNull G_User user, OrderStatus orderStatus) {
        this.user = user;
        this.orderStatus = (orderStatus != null) ? orderStatus : OrderStatus.PENDING;
    }

    public void addItem(I_OrderItem item) {
        items.add(item);
        item.setOrder(this);
    }

    public void removeItem(I_OrderItem item) {
        items.remove(item);
        item.setOrder(null);
    }

}
