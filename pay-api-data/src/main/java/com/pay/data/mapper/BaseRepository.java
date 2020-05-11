package com.pay.data.mapper;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
public interface BaseRepository<T, A> extends JpaRepository<T, A>, JpaSpecificationExecutor<T>, CrudRepository<T, A> {
}
