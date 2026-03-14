package com.example.demo.repository;

import com.example.demo.entity.OrderStatus;
import com.example.demo.entity.ServiceOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ServiceOrderRepository extends JpaRepository<ServiceOrder, Long> {

    List<ServiceOrder> findByCustomer_IdOrderByIdDesc(Long customerId);

    List<ServiceOrder> findByMechanic_IdOrderByIdDesc(Long mechanicId);

    List<ServiceOrder> findByMechanic_IdAndStatus(Long mechanicId, OrderStatus status);

    @Query("SELECT o FROM ServiceOrder o LEFT JOIN FETCH o.orderParts op LEFT JOIN FETCH op.part WHERE o.id = :id")
    ServiceOrder findByIdWithParts(@Param("id") Long id);
}
