package com.lbu.student_service.clients;

import com.lbu.student_service.dto.LibraryMemberDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**

 * This client integrates the Student microservice with the Library microservice using REST.
 *
 * The business service receives this interface through constructor injection, so it is not tightly
 * coupled to networking code.
 *
 * The Library service location is configured externally using services.library.url.
 */
@FeignClient(name = "library-service", url = "${services.library.url}")
public interface LibraryClient {

    @PostMapping("/api/members")
    LibraryMemberDto createMember(@RequestBody LibraryMemberDto memberData);

    @GetMapping("/api/members/{id}")
    LibraryMemberDto getMember(@PathVariable("id") Long id);
}
