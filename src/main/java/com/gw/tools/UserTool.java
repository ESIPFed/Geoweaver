package com.gw.tools;

import java.util.Optional;

import com.gw.database.UserRepository;
import com.gw.jpa.GWUser;
import com.gw.utils.BaseTool;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserTool {
    
    @Autowired
    UserRepository userRepository;

    @Autowired
    BaseTool bt;

    @Autowired
    ProcessTool pt;

    @Autowired
    HostTool ht;

    @Autowired
    WorkflowTool wt;

    Logger logger = Logger.getLogger(this.getClass());

    public GWUser getUserById(String id){

        GWUser u = null;

        Optional<GWUser> og = userRepository.findById(id);

        if(!og.isEmpty()){

            u = og.get();

        }

        return u;

    }

    public void save(GWUser user){

        userRepository.save(user);

    }

    public void belongToPublicUser(){

        logger.debug("Belong the no-owner resources to public user..");

        pt.getAllProcesses().forEach(p->{

            if(bt.isNull(p.getOwner()==null)){ 

                p.setOwner("111111");

                p.setConfidential("FALSE");

                pt.save(p);

            }else if(bt.isNull(p.getConfidential()==null)){

                p.setConfidential("FALSE");

                pt.save(p);

            }

        });

        ht.getAllHosts().forEach(h->{

            if(bt.isNull(h.getOwner()==null)){

                h.setOwner("111111");

                h.setConfidential("FALSE");

                ht.save(h);

            }else if(bt.isNull(h.getConfidential())){

                h.setConfidential("FALSE");

                ht.save(h);

            }

        });;

        wt.getAllWorkflow().forEach(w->{

            if(bt.isNull(w.getOwner())){

                w.setOwner("111111");

                w.setConfidential("FALSE");

                wt.save(w);

            }

        });


    }

}
