package com.p2.product_service.controller;

import com.p2.product_service.dto.SellerListCreateDTO;
import com.p2.product_service.dto.SellerListDTO;
import com.p2.product_service.service.SellerListService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/lists")
public class ListController {
    private final SellerListService listService;

    public ListController(SellerListService listService) {
        this.listService = listService;
    }

//    private Long getUserId(HttpServletRequest request) {
//        return 1L;
//    }
//
//    private boolean isSeller(HttpServletRequest request) {
//        return true;
//    }

    private Long getUserId(HttpServletRequest request) {
        // We cast the attribute to Long because that's how it was stored by the Interceptor
        Object userId = request.getAttribute("userId");
        return userId != null ? (Long) userId : null;
    }

    private boolean isSeller(HttpServletRequest request) {
        // We check the "role" attribute set by our security layer
        String role = (String) request.getAttribute("userRole");
        return "SELLER".equalsIgnoreCase(role) || "ADMIN".equalsIgnoreCase(role);
    }

    private boolean isAdmin(HttpServletRequest request) {
        String role = (String) request.getAttribute("userRole");
        return "ADMIN".equalsIgnoreCase(role);
    }

    @GetMapping
    public ResponseEntity<List<SellerListDTO>> getAllLists(HttpServletRequest request) {
        Long userId = getUserId(request);
        if (userId == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        List<SellerListDTO> lists = listService.getAllListsByUserId(userId);
        return ResponseEntity.ok(lists);
    }

    @GetMapping("/{listId}")
    public ResponseEntity<SellerListDTO> getList(@PathVariable Long listId, HttpServletRequest request) {
        Long userId = getUserId(request);
        if (userId == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        return listService.getListById(listId, userId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<SellerListDTO> createList(@RequestBody SellerListCreateDTO dto, HttpServletRequest request) {
        if (!isSeller(request)) return ResponseEntity.status(HttpStatus.FORBIDDEN).build();

        try {
            SellerListDTO createdList = listService.createList(getUserId(request), dto);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdList);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    @PutMapping("/{listId}")
    public ResponseEntity<SellerListDTO> updateList(
            @PathVariable Long listId,
            @RequestBody SellerListCreateDTO dto,
            HttpServletRequest request) {
        if (!isSeller(request)) return ResponseEntity.status(HttpStatus.FORBIDDEN).build();

        try {
            SellerListDTO updatedList = listService.updateList(listId, getUserId(request), dto);
            return ResponseEntity.ok(updatedList);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{listId}")
    public ResponseEntity<Void> deleteList(@PathVariable Long listId, HttpServletRequest request) {
        if (!isSeller(request)) return ResponseEntity.status(HttpStatus.FORBIDDEN).build();

        try {
            listService.deleteList(listId, getUserId(request));
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/{listId}/skus/{skuId}")
    public ResponseEntity<SellerListDTO> addSKUToList(
            @PathVariable Long listId,
            @PathVariable Long skuId,
            HttpServletRequest request) {
        if (!isSeller(request)) return ResponseEntity.status(HttpStatus.FORBIDDEN).build();

        try {
            SellerListDTO updatedList = listService.addSKUToList(listId, skuId, getUserId(request));
            return ResponseEntity.ok(updatedList);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    @DeleteMapping("/{listId}/skus/{skuId}")
    public ResponseEntity<SellerListDTO> removeSKUFromList(
            @PathVariable Long listId,
            @PathVariable Long skuId,
            HttpServletRequest request) {
        if (!isSeller(request)) return ResponseEntity.status(HttpStatus.FORBIDDEN).build();

        try {
            SellerListDTO updatedList = listService.removeSKUFromList(listId, skuId, getUserId(request));
            return ResponseEntity.ok(updatedList);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }
}