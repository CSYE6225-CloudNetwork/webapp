package com.CSYE6225.webapp.Repository;

import org.springframework.stereotype.Repository;
import com.CSYE6225.webapp.Entity.Profile;
import org.springframework.data.jpa.repository.JpaRepository;


@Repository
public interface ProfileRepo extends JpaRepository<Profile, String>{

}





