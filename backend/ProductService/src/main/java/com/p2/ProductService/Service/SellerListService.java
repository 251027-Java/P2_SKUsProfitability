package com.p2.ProductService.Service;

import com.p2.ProductService.DTO.SKUDTO;
import com.p2.ProductService.DTO.SellerListCreateDTO;
import com.p2.ProductService.DTO.SellerListDTO;
import com.p2.ProductService.Exception.ResourceNotFoundException;
import com.p2.ProductService.Model.SKU;
import com.p2.ProductService.Model.SellerList;
import com.p2.ProductService.Model.SellerListItem;
import com.p2.ProductService.Repository.SKURepository;
import com.p2.ProductService.Repository.SellerListItemRepository;
import com.p2.ProductService.Repository.SellerListRepository;
import com.p2.ProductService.Util.DataTransformUtil;
import com.p2.ProductService.Util.ValidationUtil;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class SellerListService {
    private final SellerListRepository listRepository;
    private final SellerListItemRepository listItemRepository;
    private final SKURepository skuRepository;
    private final SKUService skuService;

    public SellerListService(SellerListRepository listRepository, SellerListItemRepository listItemRepository,
                       SKURepository skuRepository, SKUService skuService) {
        this.listRepository = listRepository;
        this.listItemRepository = listItemRepository;
        this.skuRepository = skuRepository;
        this.skuService = skuService;
    }

    public SellerListDTO createList(Long userId, SellerListCreateDTO dto) {
        ValidationUtil.validateRequired(dto.name(), "List Name");
        if (dto.name().length() > 200) {
            throw new IllegalArgumentException("List name must be 200 characters or less");
        }

        if (listRepository.existsByUserIdAndName(userId, dto.name())) {
            throw new IllegalArgumentException("List with this name already exists");
        }

        com.p2.ProductService.Model.SellerList list = new com.p2.ProductService.Model.SellerList(
                userId,
                DataTransformUtil.normalizeString(dto.name()),
                dto.description() != null ? DataTransformUtil.normalizeString(dto.description()) : null
        );
        com.p2.ProductService.Model.SellerList savedList = listRepository.save(list);
        return convertToDTO(savedList);
    }

    public List<SellerListDTO> getAllListsByUserId(Long userId) {
        return listRepository.findByUserId(userId).stream()
                .map(this::convertToDTO)
                .toList();
    }

    public Optional<SellerListDTO> getListById(Long listId, Long userId) {
        return listRepository.findById(listId)
                .filter(list -> list.getUserId().equals(userId))
                .map(this::convertToDTO);
    }

    public SellerListDTO updateList(Long listId, Long userId, SellerListCreateDTO dto) {
        ValidationUtil.validateRequired(dto.name(), "List Name");
        if (dto.name().length() > 200) {
            throw new IllegalArgumentException("List name must be 200 characters or less");
        }

        com.p2.ProductService.Model.SellerList list = listRepository.findById(listId)
                .filter(l -> l.getUserId().equals(userId))
                .orElseThrow(() -> new ResourceNotFoundException("List", listId));

        if (!list.getName().equals(dto.name()) && listRepository.existsByUserIdAndName(userId, dto.name())) {
            throw new IllegalArgumentException("List with this name already exists");
        }

        list.setName(DataTransformUtil.normalizeString(dto.name()));
        list.setDescription(dto.description() != null ? DataTransformUtil.normalizeString(dto.description()) : null);
        com.p2.ProductService.Model.SellerList updatedList = listRepository.save(list);
        return convertToDTO(updatedList);
    }

    public void deleteList(Long listId, Long userId) {
        com.p2.ProductService.Model.SellerList list = listRepository.findById(listId)
                .filter(l -> l.getUserId().equals(userId))
                .orElseThrow(() -> new ResourceNotFoundException("List", listId));
        listRepository.delete(list);
    }

    public SellerListDTO addSKUToList(Long listId, Long skuId, Long userId) {
        SellerList list = listRepository.findById(listId)
                .filter(l -> l.getUserId().equals(userId))
                .orElseThrow(() -> new ResourceNotFoundException("List", listId));

        SKU sku = skuRepository.findById(skuId)

                .orElseThrow(() -> new ResourceNotFoundException("SKU", skuId));

        if (listItemRepository.findBySellerList_ListIdAndSku_SkuId(listId, skuId).isPresent()) {
            throw new IllegalArgumentException("SKU already in list");
        }

        SellerListItem listItem = new SellerListItem(list, sku);
        listItemRepository.save(listItem);
        return convertToDTO(list);
    }

    public SellerListDTO removeSKUFromList(Long listId, Long skuId, Long userId) {
        com.p2.ProductService.Model.SellerList list = listRepository.findById(listId)
                .filter(l -> l.getUserId().equals(userId))
                .orElseThrow(() -> new ResourceNotFoundException("List", listId));

        listItemRepository.deleteBySellerList_ListIdAndSku_SkuId(listId, skuId);
        return convertToDTO(list);
    }

    private SellerListDTO convertToDTO(com.p2.ProductService.Model.SellerList list) {
        List<SellerListItem> items = listItemRepository.findBySellerList_ListId(list.getListId());
        List<SKUDTO> skuDTOs = items.stream()
                .map(item -> DataTransformUtil.toSKUDTO(item.getSku()))
                .toList();

        return new SellerListDTO(
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
