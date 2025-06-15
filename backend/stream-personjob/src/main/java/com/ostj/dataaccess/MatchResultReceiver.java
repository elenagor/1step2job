package com.ostj.dataaccess;

import com.ostj.dataproviders.MatchResultProvider;
import com.ostj.entities.MatchResult;
import com.ostj.entities.MatchResultNotify;
import com.ostj.entities.PersonPositionEvent;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MatchResultReceiver {
    private static Logger log = LoggerFactory.getLogger(MatchResultReceiver.class);
    private MatchResultProvider resultProvider;

    public MatchResultReceiver(){
        log.debug("Start MatchResultProvider");
    }

    public MatchResultReceiver(String jdbcUrl, String username, String password) throws Exception{
        this.resultProvider = new MatchResultProvider(jdbcUrl, username, password);
    }

    public boolean saveMatchResult(PersonPositionEvent event){
        MatchResult result = new MatchResult();
        result.Person_Id = event.PersonId;
        result.Position_Id = event.PositionId;
        result.Profile_Id = event.ProfileId;
        result.overall_score = 0;
        result.date = new java.sql.Timestamp(System.currentTimeMillis()); // Current date
        try {
            resultProvider.saveMatchResult(result);
            return true;
        } catch (Exception e) {
            log.error("Save init match result in DB failed ", e);
        }
        return false;
    }

    public void updateMatchResultToSent(List<MatchResultNotify> results) {
        StringBuffer sb = new StringBuffer();
        for(MatchResultNotify result : results){
            sb.append( String.format("%d,", result.id));
        }
        sb.deleteCharAt(sb.length() - 1);
        try {
            resultProvider.updateMatchResult(true, sb.toString());
        } catch (Exception e) {
            log.error("Update match result in DB failed ", e);
        }
    }
}
