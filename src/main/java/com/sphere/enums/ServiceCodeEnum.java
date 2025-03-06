package com.sphere.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;

@Getter
@AllArgsConstructor
public enum ServiceCodeEnum {



    TRANSACTION_DEPOSIT("10",
            "/v1.0/transaction/deposit",
            "/sandbox/v1.0/deposit",
            "/v1.0/deposit"),

    DISBURSEMENT_PAYOUT("11",
            "/v1.0/disbursement/payout",
            "/sandbox/v1.0/payout",
            "/v1.0/payout"),

    INQUIRY_STATUS("12",
            "/v1.0/inquiry-status",
            "/sandbox/v1/inquiryStatus",
            "/v1/inquiryStatus"),

    INQUIRY_BALANCE("13",
            "/v1.0/inquiry-balance",
            "/sandbox/v1.0/inquiryBalance",
            "/v1.0/inquiryBalance"),

    /**
     * 未知
     */
    UNKNOWN("00", "/v1.0/unknown",
            "sandbox/unknown",
            "/unknown"),
    ;

    private final String serviceCode;
    private final String path;
    private final String sandboxRewritePath;
    private final String rewritePath;



    /**
     * pathToEnum
     */
    public static ServiceCodeEnum pathToEnum(String path) {
        if (StringUtils.isBlank(path)) {
            return null;
        }
        return Arrays.stream(ServiceCodeEnum.values())
                .filter(e -> path.contains(e.getPath()))
                .findAny()
                .orElse(null);
    }

}
