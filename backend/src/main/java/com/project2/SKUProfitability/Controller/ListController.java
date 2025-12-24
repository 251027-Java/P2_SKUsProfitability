package com.project2.SKUProfitability.Controller;

import com.project2.SKUProfitability.DTO.ListCreateDTO;
import com.project2.SKUProfitability.DTO.ListDTO;
import com.project2.SKUProfitability.JwtUtil;
import com.project2.SKUProfitability.Service.ListService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/lists")
public class ListController {
    private final ListService listService;
    private final JwtUtil jwtUtil;

    public ListController(ListService listService, JwtUtil jwtUtil) {
        this.listService = listService;
        this.jwtUtil = jwtUtil;
    }

    private Long getUserIdFromRequest(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
            return jwtUtil.getUserIdFromToken(token);
        }
        throw new IllegalArgumentException("Invalid or missing authentication token");
    }

    @GetMapping
    public ResponseEntity<List<ListDTO>> getAllLists(HttpServletRequest request) {
        try {
            Long userId = getUserIdFromRequest(request);
            List<ListDTO> lists = listService.getAllListsByUserId(userId);
            return ResponseEntity.ok(lists);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    @GetMapping("/{listId}")
    public ResponseEntity<ListDTO> getList(@PathVariable Long listId, HttpServletRequest request) {
        try {
            Long userId = getUserIdFromRequest(request);
            Optional<ListDTO> list = listService.getListById(listId, userId);
            return list.map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    @PostMapping
    public ResponseEntity<ListDTO> createList(@RequestBody ListCreateDTO dto, HttpServletRequest request) {
        try {
            Long userId = getUserIdFromRequest(request);
            ListDTO createdList = listService.createList(userId, dto);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdList);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    @PutMapping("/{listId}")
    public ResponseEntity<ListDTO> updateList(
            @PathVariable Long listId,
            @RequestBody ListCreateDTO dto,
            HttpServletRequest request) {
        try {
            Long userId = getUserIdFromRequest(request);
            ListDTO updatedList = listService.updateList(listId, userId, dto);
            return ResponseEntity.ok(updatedList);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    @DeleteMapping("/{listId}")
    public ResponseEntity<Void> deleteList(@PathVariable Long listId, HttpServletRequest request) {
        try {
            Long userId = getUserIdFromRequest(request);
            listService.deleteList(listId, userId);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    @PostMapping("/{listId}/skus/{skuId}")
    public ResponseEntity<ListDTO> addSKUToList(
            @PathVariable Long listId,
            @PathVariable Long skuId,
            HttpServletRequest request) {
        try {
            Long userId = getUserIdFromRequest(request);
            ListDTO updatedList = listService.addSKUToList(listId, skuId, userId);
            return ResponseEntity.ok(updatedList);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(null);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    @DeleteMapping("/{listId}/skus/{skuId}")
    public ResponseEntity<ListDTO> removeSKUFromList(
            @PathVariable Long listId,
            @PathVariable Long skuId,
            HttpServletRequest request) {
        try {
            Long userId = getUserIdFromRequest(request);
            ListDTO updatedList = listService.removeSKUFromList(listId, skuId, userId);
            return ResponseEntity.ok(updatedList);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }
}

