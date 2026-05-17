package com.yyyouth.service.aichat.demo.domain;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Data;

/**
 * @Author:忧郁的青年(yyyouth) --zg
 * @Date:2026/05/04
 */
//@JsonPropertyOrder({"actor", "movies"})
@Data
public class ChatResult {

    private String id;
    private String context;
    private String code;
    private String type;
    private String text;
    private String mute;

}
