package com.lyrascore.dictionary.controller;

import com.lyrascore.common.R;
import com.lyrascore.dictionary.entity.Dictionary;
import com.lyrascore.dictionary.mapper.DictionaryMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/dictionary")
@RequiredArgsConstructor
public class DictionaryController {

    private final DictionaryMapper dictionaryMapper;

    @GetMapping("/{type}")
    public R<List<Dictionary>> listByType(@PathVariable String type) {
        return R.ok(dictionaryMapper.selectActiveByType(type));
    }
}
