package com.agricultural.service.impl;

import com.agricultural.entity.Address;
import com.agricultural.mapper.AddressMapper;
import com.agricultural.service.AddressService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AddressServiceImpl extends ServiceImpl<AddressMapper, Address> implements AddressService {

    @Override
    public List<Address> getUserAddresses(Long userId) {
        LambdaQueryWrapper<Address> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Address::getUserId, userId);
        wrapper.eq(Address::getDeleted, 0);
        wrapper.orderByDesc(Address::getIsDefault);
        wrapper.orderByDesc(Address::getCreateTime);
        return list(wrapper);
    }

    @Override
    public Address addAddress(Long userId, Address address) {
        address.setUserId(userId);
        address.setDeleted(0);
        address.setCreateTime(LocalDateTime.now());
        address.setUpdateTime(LocalDateTime.now());

        // 如果设置为默认地址，取消其他默认地址
        if (address.getIsDefault() != null && address.getIsDefault() == 1) {
            setDefaultAddress(userId, null);
        }

        save(address);
        return address;
    }

    @Override
    public boolean updateAddress(Long addressId, Address address) {
        Address existing = getById(addressId);
        if (existing == null) {
            throw new IllegalArgumentException("地址不存在");
        }

        if (address.getReceiverName() != null) {
            existing.setReceiverName(address.getReceiverName());
        }
        if (address.getReceiverPhone() != null) {
            existing.setReceiverPhone(address.getReceiverPhone());
        }
        if (address.getProvince() != null) {
            existing.setProvince(address.getProvince());
        }
        if (address.getCity() != null) {
            existing.setCity(address.getCity());
        }
        if (address.getDistrict() != null) {
            existing.setDistrict(address.getDistrict());
        }
        if (address.getDetail() != null) {
            existing.setDetail(address.getDetail());
        }
        if (address.getIsDefault() != null) {
            existing.setIsDefault(address.getIsDefault());
            if (address.getIsDefault() == 1) {
                setDefaultAddress(existing.getUserId(), addressId);
            }
        }

        existing.setUpdateTime(LocalDateTime.now());
        return updateById(existing);
    }

    @Override
    public boolean deleteAddress(Long addressId) {
        Address address = getById(addressId);
        if (address == null) {
            throw new IllegalArgumentException("地址不存在");
        }

        address.setDeleted(1);
        address.setUpdateTime(LocalDateTime.now());
        return updateById(address);
    }

    @Override
    public boolean setDefaultAddress(Long userId, Long addressId) {
        // 取消所有默认地址
        LambdaUpdateWrapper<Address> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(Address::getUserId, userId);
        updateWrapper.set(Address::getIsDefault, 0);
        update(updateWrapper);

        // 设置新的默认地址
        if (addressId != null) {
            Address address = getById(addressId);
            if (address != null && address.getUserId().equals(userId)) {
                address.setIsDefault(1);
                address.setUpdateTime(LocalDateTime.now());
                return updateById(address);
            }
        }

        return true;
    }
}

