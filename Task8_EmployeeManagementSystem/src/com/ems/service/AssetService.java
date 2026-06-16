package com.ems.service;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import com.ems.enums.AssetStatus;
import com.ems.exception.EMSException;
import com.ems.exception.NotFoundException;
import com.ems.model.Asset;
import com.ems.model.Employee;

public class AssetService {

    private final AtomicLong seq = new AtomicLong(3000);
    private final Map<String, Asset> assets = new ConcurrentHashMap<>();

    private final EmployeeService employeeService;

    public AssetService(EmployeeService es) {
        this.employeeService = es;
    }

    public Asset add(String name, String category) {
        String id = "A-" + seq.incrementAndGet();
        Asset a = new Asset(id, name, category);
        assets.put(id, a);
        return a;
    }

    public Asset get(String id) {
        Asset a = assets.get(id);
        if (a == null) throw new NotFoundException("Asset not found: " + id);
        return a;
    }

    public void assign(String assetId, String employeeId) {
        Asset a = get(assetId);
        Employee e = employeeService.get(employeeId);
        if (!e.isActive()) throw new EMSException("Only ACTIVE employees can receive assets.");
        if (a.getStatus() != AssetStatus.AVAILABLE) {
            throw new EMSException("Asset " + assetId + " not AVAILABLE (status=" + a.getStatus() + ")");
        }
        a.assignTo(employeeId);
    }

    public void returnAsset(String assetId) {
        Asset a = get(assetId);
        a.returnAsset();
    }

    public void markLost(String assetId) {
        get(assetId).markLost();
    }

    public Collection<Asset> getAll() { return Collections.unmodifiableCollection(assets.values()); }
}
