package com.example.catalogservice.messagequeue;

import java.util.HashMap;
import java.util.Map;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import com.example.catalogservice.jpa.CatalogEntity;
import com.example.catalogservice.jpa.CatalogRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class KafkaConsumer {

	private final CatalogRepository catalogRepository;

	@KafkaListener(topics = "example-catalog-topic")
	public void updateQty(String kafkaMsg) {
		log.info("Kafka Message : {}", kafkaMsg);

		Map<Object, Object> map = new HashMap<>();
		ObjectMapper mapper = new ObjectMapper();
		try {
			map = mapper.readValue(kafkaMsg, new TypeReference<Map<Object, Object>>() {});
		} catch (Exception e) {
			e.printStackTrace();
		}
		CatalogEntity entity = catalogRepository.findByProductId((String)map.get("productId"));
		if (entity != null) {
			entity.setStock(entity.getStock() - (Integer)map.get("qty"));
			catalogRepository.save(entity);
		}
	}
}
