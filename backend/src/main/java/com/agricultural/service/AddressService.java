package com.agricultural.service;

import com.agricultural.entity.Address;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

public interface AddressService extends IService<Address> {
    List<Address> getUserAddresses(Long userId);
    Address addAddress(Long userId, Address address);
    boolean updateAddress(Long addressId, Address address);
    boolean deleteAddress(Long addressId);
    boolean setDefaultAddress(Long userId, Long addressId);
}


