package com.lbu.student_service.clients;

import com.lbu.student_service.dto.AccountDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

@FeignClient(name = "library-service", url = "${services.library.url}")
public interface LibraryClient {
    @PostMapping("/accounts")
    void createAccount(@RequestBody Object accountData);

    @PostMapping("/accounts")
    void createLibraryAccount(@RequestBody Object accountData);

    @GetMapping("/accounts/student/{studentId}")
    Map<String, Object> getLibraryAccount(@PathVariable("studentId") String studentId);
}