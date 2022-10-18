package com.example.userservice.service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.cloud.client.circuitbreaker.CircuitBreaker;
import org.springframework.cloud.client.circuitbreaker.CircuitBreakerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.example.userservice.client.OrderServiceClient;
import com.example.userservice.dto.UserDto;
import com.example.userservice.error.FeignErrorDecoder;
import com.example.userservice.jpa.UserEntity;
import com.example.userservice.jpa.UserRepository;
import com.example.userservice.vo.ResponseOrder;

import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

	private final UserRepository userRepository;
	private final BCryptPasswordEncoder passwordEncoder;
	private final Environment env;
	private final OrderServiceClient orderServiceClient;
	// private final RestTemplate restTemplate;
	private final CircuitBreakerFactory circuitBreakerFactory;
	
	@Override
	public UserDto createUser(UserDto userDto) {
		userDto.setUserId(UUID.randomUUID().toString());

		ModelMapper mapper = new ModelMapper();
		mapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);
		UserEntity userEntity = mapper.map(userDto, UserEntity.class);
		userEntity.setEncryptedPwd(passwordEncoder.encode(userDto.getPwd()));

		userRepository.save(userEntity);
		return mapper.map(userEntity, UserDto.class);
	}

	@Override
	public UserDto getUserByUserId(String userId) {
		UserEntity userEntity = userRepository.findByUserId(userId)
			.orElseThrow(() -> new UsernameNotFoundException("User not found"));

		UserDto userDto = new ModelMapper().map(userEntity, UserDto.class);
		/*  Rest Template */
		// List<ResponseOrder> orders = new ArrayList<>();
		// String orderUrl = String.format(env.getProperty("order_service.url"), userId);
		// List<ResponseOrder> orderListResponse = restTemplate.exchange(orderUrl, HttpMethod.GET, null,
		// 	new ParameterizedTypeReference<List<ResponseOrder>>() {
		// 	}).getBody();

		/* Feign Client */
		// List<ResponseOrder> orderListResponse = orderServiceClient.getOrders(userId);

		/* Feign exception handling*/
		// List<ResponseOrder> orderListResponse = null;
		// try {
		// 	orderListResponse = orderServiceClient.getOrdersNg(userId);
		// } catch (FeignException e) {
		// 	log.error(e.getMessage());
		// }

		/* ErrorDecoder */
//		List<ResponseOrder> orderListResponse = orderServiceClient.getOrders(userId);

		log.info("Before call orders microservice");
		CircuitBreaker circuitbreaker = circuitBreakerFactory.create("circuitbreaker");
		List<ResponseOrder> orderListResponse = circuitbreaker.run(() -> orderServiceClient.getOrders(userId),
				throwable -> new ArrayList<>());
		log.info("After called orders microservice");
		userDto.setOrders(orderListResponse);
		return userDto;
	}

	@Override
	public Iterable<UserEntity> getUserByAll() {
		return userRepository.findAll();
	}

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		UserEntity userEntity = userRepository.findByEmail(username);
		if (userEntity == null) {
			throw new UsernameNotFoundException(username);
		}
		return new User(userEntity.getEmail(), userEntity.getEncryptedPwd(), true, true, true, true, new ArrayList<>());
	}

	@Override
	public UserDto getUserDetailsByEmail(String username) {
		UserEntity userEntity = userRepository.findByEmail(username);
		if (userEntity == null) {
			throw new UsernameNotFoundException(username);
		}

		return new ModelMapper().map(userEntity, UserDto.class);
	}
}
