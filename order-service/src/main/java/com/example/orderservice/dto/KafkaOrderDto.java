package com.example.orderservice.dto;

import java.io.Serializable;

import org.springframework.format.annotation.DateTimeFormat;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class KafkaOrderDto implements Serializable {
	private Schema schema;
	private Payload payload;
}
