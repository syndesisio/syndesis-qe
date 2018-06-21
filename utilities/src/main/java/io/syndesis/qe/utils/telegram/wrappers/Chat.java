package io.syndesis.qe.utils.telegram.wrappers;

import lombok.Data;

@Data
public class Chat {
    private Long id;
    private String title;
    private String username;
    private String type;
}
