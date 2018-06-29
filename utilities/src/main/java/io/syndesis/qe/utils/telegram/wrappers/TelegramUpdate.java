package io.syndesis.qe.utils.telegram.wrappers;

import java.util.List;

import lombok.Data;

@Data
public class TelegramUpdate {
    private Long update_id;
    private ChannelPost channel_post;

}
