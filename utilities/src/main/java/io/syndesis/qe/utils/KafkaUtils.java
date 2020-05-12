package io.syndesis.qe.utils;

import java.util.Base64;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class KafkaUtils {

    public static String extractValue(String word, String input) {
        final String pattern = String.format(".*%s=.*?,", word);
        Pattern r = Pattern.compile(pattern);
        Matcher m = r.matcher(input);

        StringBuilder sb = new StringBuilder();
        while (m.find()) {
            sb.append(m.group(0).replaceAll(String.format(".*%s=", word), "").replaceAll("[,}]", ""));
        }
        return decode(sb.toString());
    }

    private static String decode(String encrypted) {
        byte[] result = Base64.getDecoder().decode(encrypted);
        return new String(result);
    }
}
