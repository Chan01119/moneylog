package org.codenova.moneylog.repository;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.codenova.moneylog.entity.User;

@Mapper
public interface UserRepository {
    int save(User user);
     User findByEmail(@Param("email") String email);
    User findByProviderAndProviderId(@Param("provider") String provider, @Param("providerId") String providerId);


    int updatePasswordByEmail(@Param("email") String email,
                              @Param("password") String password);

    int updateUserVerified(@Param("email") String email);

}
