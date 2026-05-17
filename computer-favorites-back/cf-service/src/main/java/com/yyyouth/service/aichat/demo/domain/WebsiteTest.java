package com.yyyouth.service.aichat.demo.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Author:忧郁的青年(yyyouth) --zg
 * @Date:2026/05/12
 */
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class WebsiteTest {
    private String id;
    private String name;
    private String url;
    private String description;
    private String author;
}
