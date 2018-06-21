package io.syndesis.qe.utils.telegram.wrappers;

import java.util.List;

import lombok.Data;

@Data
public class TelegramAllUpdates {

    private boolean ok;
    private List<TelegramUpdate> result;
}
