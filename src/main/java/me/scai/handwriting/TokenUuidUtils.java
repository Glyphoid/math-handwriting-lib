package me.scai.handwriting;


import org.apache.commons.lang.RandomStringUtils;

import java.util.ArrayList;
import java.util.List;

public class TokenUuidUtils {
    public static String getRandomTokenUuid() {
        return RandomStringUtils.random(8, true, true);
    }

    public static List<String> getRandomTokenUuids(int n) {
        List<String> r = new ArrayList<>();

        ((ArrayList) r).ensureCapacity(n);

        for (int i = 0; i < n; ++i) {
            r.add(getRandomTokenUuid());
        }

        return r;
    }
}
