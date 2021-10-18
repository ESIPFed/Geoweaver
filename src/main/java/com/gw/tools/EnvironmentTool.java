package com.gw.tools;

import java.util.Optional;

import com.gw.database.EnvironmentRepository;
import com.gw.jpa.Environment;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class EnvironmentTool {

    @Autowired
    EnvironmentRepository envrep;

    public Environment getEnvironmentById(String id){

        Optional<Environment> envop = envrep.findById(id);

        if(envop.isPresent()){

            return envop.get();
            
        }else{
            return null;
        }

    }
    
}
