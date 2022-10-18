package com.example.orderservice.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.orderservice.dto.OrderDto;
import com.example.orderservice.jpa.OrderEntity;
import com.example.orderservice.messagequeue.KafkaProducer;
import com.example.orderservice.messagequeue.OrderProducer;
import com.example.orderservice.service.OrderService;
import com.example.orderservice.vo.RequestOrder;
import com.example.orderservice.vo.ResponseOrder;

import lombok.RequiredArgsConstructor;

@Slf4j
@RestController
@RequestMapping("/order-service")
@RequiredArgsConstructor
public class OrderController {

	private final Environment env;
	private final OrderService orderService;
	private final KafkaProducer kafkaProducer;
	private final OrderProducer orderProducer;




	@GetMapping("/health_check")
	public String status() {
		return String.format("It's Working in Catalog Service on PORT %s",
			env.getProperty("local.server.port"));
	}


	@PostMapping("/{userId}/orders")
	public ResponseEntity<ResponseOrder> createOrder(@PathVariable("userId") String userId, @RequestBody RequestOrder order) {
		ModelMapper mapper = new ModelMapper();
		mapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);
		log.info("Before add orders data");
		OrderDto orderDto = mapper.map(order, OrderDto.class);
		orderDto.setUserId(userId);
		/* jpa */

		 OrderDto createdOrder = orderService.createOrder(orderDto);
		 ResponseOrder responseOrder = mapper.map(createdOrder, ResponseOrder.class);
		log.info("After add orders data");
		/* kafka */
//		orderDto.setOrderId(UUID.randomUUID().toString());
//		orderDto.setTotalPrice(order.getUnitPrice() * order.getQty());

//		kafkaProducer.send("example-catalog-topic", orderDto);
//		orderProducer.send("orders", orderDto);
//		ResponseOrder responseOrder = mapper.map(orderDto, ResponseOrder.class);

		return ResponseEntity.status(HttpStatus.CREATED).body(responseOrder);
	}

	@GetMapping("/{userId}/orders")
	public ResponseEntity<List<ResponseOrder>> getOrder(@PathVariable("userId") String userId) {
		log.info("Before retrieve orders data");
		Iterable<OrderEntity> orderList = orderService.getAllOrdersByUserId(userId);

		List<ResponseOrder> result = new ArrayList<>();
		orderList.forEach(o-> {
			result.add(new ModelMapper().map(o, ResponseOrder.class));
		});
		try {
			Thread.sleep(1000);
			throw new Exception("장애 발생");
		} catch (Exception e) {
			log.error(e.getMessage());
		}
		log.info("After retrieve orders data");
		return ResponseEntity.status(HttpStatus.OK).body(result);
	}
}
