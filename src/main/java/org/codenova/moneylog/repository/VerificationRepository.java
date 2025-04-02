package org.codenova.moneylog.repository;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.codenova.moneylog.entity.Verification;


@Mapper
public interface VerificationRepository {

    int create(Verification verification);

    Verification selectByToken(@Param("token") String token);
}
