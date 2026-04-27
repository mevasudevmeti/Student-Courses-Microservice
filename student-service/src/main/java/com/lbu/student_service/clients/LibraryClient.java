package com.lbu.student_service.clients;

import com.lbu.student_service.dto.AccountDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "library-service", url = "${services.library.url}")
public interface LibraryClient {
    @PostMapping("/accounts")
    void createAccount(@RequestBody Object accountData);
}