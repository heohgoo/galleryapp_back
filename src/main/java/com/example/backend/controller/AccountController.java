package com.example.backend.controller;

import com.example.backend.dto.MemberDto;
import com.example.backend.entity.Member;
import com.example.backend.repository.MemberRepository;
import com.example.backend.service.JwtService;
import com.example.backend.service.JwtServiceImpl;
import io.jsonwebtoken.Claims;
import org.apache.coyote.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

@RestController
public class AccountController {
    @Autowired
    MemberRepository memberRepository;

    @Autowired
    JwtService jwtService;

    @Autowired
    HttpServletResponse res;

    @PostMapping("/api/account/signup")
    public ResponseEntity signup(@RequestBody MemberDto dto) {
        Member newMember = Member
                .builder().email(dto.getEmail()).password(dto.getPassword()).build();


        if (memberRepository.findByEmail(newMember.getEmail()) == null) {
            memberRepository.save(newMember);
            return new ResponseEntity<>(HttpStatus.OK);
        }

        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }


    @PostMapping("/api/account/login")
    public ResponseEntity login(@RequestBody MemberDto dto) {
        Member member = Member
                .builder().email(dto.getEmail()).password(dto.getPassword()).build();

        if (memberRepository.findByEmailAndPassword(member.getEmail(), member.getPassword()) != null) {
            JwtService jwtService = new JwtServiceImpl();
            int id = member.getId();
            String token = jwtService.getToken("id", id);
            //key, value

            Cookie cookie = new Cookie("token", token);
            cookie.setHttpOnly(true);
            cookie.setPath("/");

            res.addCookie(cookie);
            return new ResponseEntity<>(id, HttpStatus.OK);
        }

        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @PostMapping("/api/account/logout")
    public ResponseEntity logout(HttpServletResponse res) {
        Cookie cookie = new Cookie("token", null);
        cookie.setPath("/");
        cookie.setMaxAge(0);

        res.addCookie(cookie);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @GetMapping("/api/account/check")
    //ResponseEntity -> HttpEntity를 상속받으며, 결과 데이터와 http 상태를 제어한다.
    public ResponseEntity check(@CookieValue(value = "token", required = false) String token) {
        Claims claims = jwtService.getClaims(token);

        if (claims != null){
            int id = Integer.parseInt(claims.get("id").toString());
            return new ResponseEntity<>(id, HttpStatus.OK);
        }

        return new ResponseEntity<>(null, HttpStatus.OK);
    }

}
