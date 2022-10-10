package com.example.userservice.dto;

import java.util.Date;

import org.springframework.stereotype.Service;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class UserDto {

	private String email;
	private String name;
	private String pwd;
	private String userId;
	private Date createAt;

	private String encryptedPwd;

}
