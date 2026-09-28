package com.Fabricio.notificacao.business.dto;

import com.Fabricio.notificacao.business.enums.StatusNotificacaoEnum;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class StatusTarefaDTO {
    private String id;
    private StatusNotificacaoEnum statusNotificacaoEnum;

}
