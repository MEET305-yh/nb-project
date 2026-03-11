package com.agricultural.controller;

import com.agricultural.common.Result;
import com.agricultural.entity.Address;
import com.agricultural.service.AddressService;
import com.agricultural.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/addresses")
public class AddressController {

    @Autowired
    private AddressService addressService;

    @Autowired
    private JwtUtil jwtUtil;

    @GetMapping
    public Result<List<Address>> getUserAddresses(HttpServletRequest request) {
        String token = request.getHeader("Authorization").substring(7);
        Long userId = jwtUtil.getUserIdFromToken(token);
        List<Address> addresses = addressService.getUserAddresses(userId);
        return Result.success(addresses);
    }

    @PostMapping
    public Result<Address> addAddress(@RequestBody Address address, HttpServletRequest request) {
        String token = request.getHeader("Authorization").substring(7);
        Long userId = jwtUtil.getUserIdFromToken(token);
        Address savedAddress = addressService.addAddress(userId, address);
        return Result.success("添加成功", savedAddress);
    }

    @PutMapping("/{id}")
    public Result<String> updateAddress(@PathVariable Long id, @RequestBody Address address) {
        addressService.updateAddress(id, address);
        return Result.success("更新成功");
    }

    @DeleteMapping("/{id}")
    public Result<String> deleteAddress(@PathVariable Long id) {
        addressService.deleteAddress(id);
        return Result.success("删除成功");
    }

    @PutMapping("/{id}/default")
    public Result<String> setDefaultAddress(@PathVariable Long id, HttpServletRequest request) {
        String token = request.getHeader("Authorization").substring(7);
        Long userId = jwtUtil.getUserIdFromToken(token);
        addressService.setDefaultAddress(userId, id);
        return Result.success("设置成功");
    }
}

















