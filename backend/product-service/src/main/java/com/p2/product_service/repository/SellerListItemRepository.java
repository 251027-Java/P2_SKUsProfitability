package com.p2.product_service.repository;

import com.p2.product_service.model.SellerListItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SellerListItemRepository extends JpaRepository<SellerListItem, Long> {
    List<SellerListItem> findBySellerList_ListId(Long listId);
    Optional<SellerListItem> findBySellerList_ListIdAndSku_SkuId(Long listId, Long skuId);
    void deleteBySellerList_ListIdAndSku_SkuId(Long listId, Long skuId);
}
