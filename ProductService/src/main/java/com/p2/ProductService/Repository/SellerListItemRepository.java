package com.p2.ProductService.Repository;

import com.p2.ProductService.Model.SellerListItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SellerListItemRepository extends JpaRepository<SellerListItem, Long> {
    List<SellerListItem> findByList_ListId(Long listId);
    Optional<SellerListItem> findByList_ListIdAndSku_SkuId(Long listId, Long skuId);
    void deleteByList_ListIdAndSku_SkuId(Long listId, Long skuId);
}
