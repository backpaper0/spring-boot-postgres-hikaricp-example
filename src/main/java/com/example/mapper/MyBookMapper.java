package com.example.mapper;

import org.apache.ibatis.annotations.Mapper;

import com.example.generated.model.Book;

@Mapper
public interface MyBookMapper {

	Book selectByPrimaryKeyForUpdate(Integer id);
}