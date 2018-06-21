package io.syndesis.qe.utils.telegram.wrappers;

import lombok.Data;

@Data
public class ChannelPost {

    private int message_id;
    private Chat chat;
    private Long date;
    private String text;

}
