package com.project2.SKUProfitability.Service;

import com.project2.SKUProfitability.DTO.ListCreateDTO;
import com.project2.SKUProfitability.DTO.ListDTO;
import com.project2.SKUProfitability.DTO.SKUDTO;
import com.project2.SKUProfitability.Model.ListItem;
import com.project2.SKUProfitability.Model.SKU;
import com.project2.SKUProfitability.Repository.ListItemRepository;
import com.project2.SKUProfitability.Repository.ListRepository;
import com.project2.SKUProfitability.Repository.SKURepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ListService {
    private final ListRepository listRepository;
    private final ListItemRepository listItemRepository;
    private final SKURepository skuRepository;
    private final SKUService skuService;

    public ListService(ListRepository listRepository, ListItemRepository listItemRepository, 
                       SKURepository skuRepository, SKUService skuService) {
        this.listRepository = listRepository;
        this.listItemRepository = listItemRepository;
        this.skuRepository = skuRepository;
        this.skuService = skuService;
    }

    public ListDTO createList(Long userId, ListCreateDTO dto) {
        if (listRepository.existsByUserIdAndName(userId, dto.name())) {
            throw new IllegalArgumentException("List with this name already exists");
        }

        com.project2.SKUProfitability.Model.List list = new com.project2.SKUProfitability.Model.List(userId, dto.name(), dto.description());
        com.project2.SKUProfitability.Model.List savedList = listRepository.save(list);
        return convertToDTO(savedList);
    }

    public List<ListDTO> getAllListsByUserId(Long userId) {
        return listRepository.findByUserId(userId).stream()
                .map(this::convertToDTO)
                .toList();
    }

    public Optional<ListDTO> getListById(Long listId, Long userId) {
        return listRepository.findById(listId)
                .filter(list -> list.getUserId().equals(userId))
                .map(this::convertToDTO);
    }

    public ListDTO updateList(Long listId, Long userId, ListCreateDTO dto) {
        com.project2.SKUProfitability.Model.List list = listRepository.findById(listId)
                .filter(l -> l.getUserId().equals(userId))
                .orElseThrow(() -> new IllegalArgumentException("List not found"));

        if (!list.getName().equals(dto.name()) && listRepository.existsByUserIdAndName(userId, dto.name())) {
            throw new IllegalArgumentException("List with this name already exists");
        }

        list.setName(dto.name());
        list.setDescription(dto.description());
        com.project2.SKUProfitability.Model.List updatedList = listRepository.save(list);
        return convertToDTO(updatedList);
    }

    public void deleteList(Long listId, Long userId) {
        com.project2.SKUProfitability.Model.List list = listRepository.findById(listId)
                .filter(l -> l.getUserId().equals(userId))
                .orElseThrow(() -> new IllegalArgumentException("List not found"));
        listRepository.delete(list);
    }

    public ListDTO addSKUToList(Long listId, Long skuId, Long userId) {
        com.project2.SKUProfitability.Model.List list = listRepository.findById(listId)
                .filter(l -> l.getUserId().equals(userId))
                .orElseThrow(() -> new IllegalArgumentException("List not found"));

        SKU sku = skuRepository.findById(skuId)
                .filter(s -> s.getUserId().equals(userId))
                .orElseThrow(() -> new IllegalArgumentException("SKU not found"));

        if (listItemRepository.findByList_ListIdAndSku_SkuId(listId, skuId).isPresent()) {
            throw new IllegalArgumentException("SKU already in list");
        }

        ListItem listItem = new ListItem(list, sku);
        listItemRepository.save(listItem);
        return convertToDTO(list);
    }

    public ListDTO removeSKUFromList(Long listId, Long skuId, Long userId) {
        com.project2.SKUProfitability.Model.List list = listRepository.findById(listId)
                .filter(l -> l.getUserId().equals(userId))
                .orElseThrow(() -> new IllegalArgumentException("List not found"));

        listItemRepository.deleteByList_ListIdAndSku_SkuId(listId, skuId);
        return convertToDTO(list);
    }

    private ListDTO convertToDTO(com.project2.SKUProfitability.Model.List list) {
        List<ListItem> items = listItemRepository.findByList_ListId(list.getListId());
        List<SKUDTO> skuDTOs = items.stream()
                .map(item -> skuService.convertToDTO(item.getSku()))
                .toList();

        return new ListDTO(
                list.getListId(),
                list.getUserId(),
                list.getName(),
                list.getDescription(),
                items.size(),
                skuDTOs,
                list.getCreatedAt(),
                list.getUpdatedAt()
        );
    }
}

