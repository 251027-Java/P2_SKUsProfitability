package com.project2.SKUProfitability.Repository;

import com.project2.SKUProfitability.Model.ListItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ListItemRepository extends JpaRepository<ListItem, Long> {
    List<ListItem> findByList_ListId(Long listId);
    Optional<ListItem> findByList_ListIdAndSku_SkuId(Long listId, Long skuId);
    void deleteByList_ListIdAndSku_SkuId(Long listId, Long skuId);
}

