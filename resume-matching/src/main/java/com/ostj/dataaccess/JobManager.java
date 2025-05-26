package com.ostj.dataaccess;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.ostj.dataentity.Job;
import com.ostj.dataentity.Person;
import com.ostj.dataentity.Profile;
import com.ostj.resumeprocessing.events.ResumeProcessEvent;
import com.ostj.utils.Utils;

public class JobManager {
    private static Logger log = LoggerFactory.getLogger(PersonManager.class);

    @Autowired
	SQLAccess dbConnector;

    public JobManager(){

    }

    public JobManager(SQLAccess dbConnector){
        this.dbConnector = dbConnector;
    }

    public Job getJob(ResumeProcessEvent event) throws Exception{
        Job job = new Job();
        if(event.jdFilePath != null && event.jdFilePath.length() > 0){
            job.description = Utils.getPromptByFileName(event.jdFilePath);
        }
        else{
            ResultSet rs = dbConnector.getJob(event.JobId );
            if (rs.next()) {
                Utils.convertToObject( rs, job, job.getClass() );
            }
        }
        return job;
    }

    public List<Job> getJobs(Person person, Profile resume)  throws Exception {
        List<Job> list = new ArrayList<Job>();
        PreparedStatement pstmt = dbConnector.createQuerySearchByTitle(resume.Title);
        ResultSet rs = dbConnector.getJobs(pstmt );
        while (rs.next()) {
            Job job = new Job();
            Utils.convertToObject( rs, job, job.getClass());
            list.add(job);
        }
        return list;
     }
}
