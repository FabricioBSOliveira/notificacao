package com.Fabricio.notificacao.infrastructure.messages.kafka.producer;


import com.Fabricio.notificacao.business.dto.StatusTarefaDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

@Service
@RequiredArgsConstructor
public class KafkaProducer {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private  final ObjectMapper objectMapper;

    public void enviarStatusTarefa(StatusTarefaDTO dto) {
        String json = objectMapper.writeValueAsString(dto);
        kafkaTemplate.send("status-notificacao", json);
    }
}
