package com.sphere.result;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;


@Data
@EqualsAndHashCode(callSuper = true)
public class PageResult<T> extends BaseResult {

    private Long total;

    private List<T> data;

}
