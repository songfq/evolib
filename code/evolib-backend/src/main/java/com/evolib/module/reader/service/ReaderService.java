package com.evolib.module.reader.service;

import com.evolib.module.reader.dto.RegisterRequest;
import com.evolib.module.reader.dto.ReaderDTO;

public interface ReaderService {
    ReaderDTO register(RegisterRequest request);
    ReaderDTO getById(String readerId);
    void updatePhone(String readerId, String phone);
}