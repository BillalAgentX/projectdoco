package com.projectdocupro.mobile.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.projectdocupro.mobile.models.Pdflawflag;

import java.util.List;

@Dao
public interface PdFlawFLagListDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(Pdflawflag pdflawflag);

   // @Insert(onConflict = OnConflictStrategy.)
   // long insertOnly(Pdflawflag pdflawflag);


    @Query("DELETE FROM Pdflawflag")
    void deleteAll();

    @Query("DELETE FROM Pdflawflag WHERE Pdflawflag.pdProjectid = :project_id AND  Pdflawflag.localPdflawflagId = :local_flaw_flag_id ")
    void deleteUsingLocalFlawFlagID(String project_id,String local_flaw_flag_id);

    @Query("DELETE FROM Pdflawflag WHERE Pdflawflag.pdProjectid = :project_id AND  Pdflawflag.local_photo_id = :local_photo_id ")
    void deleteUsingLocalDeletePhotoId(String project_id,String local_photo_id);

    @Query("DELETE FROM Pdflawflag WHERE Pdflawflag.pdProjectid = :project_id ")
    void deleteUsingProjectId(String project_id);

    @Query("DELETE FROM Pdflawflag WHERE Pdflawflag.local_flaw_Id = :local_flaw_id ")
    void deleteUsingLocalFlaw_id(String local_flaw_id);

    @Query("SELECT * from Pdflawflag WHERE Pdflawflag.pdProjectid = :project_id AND  Pdflawflag.local_flaw_Id IN  (:flawId)")
    LiveData<List<Pdflawflag>> getFlawFlagObvList(String project_id, List<String> flawId);

    @Update(onConflict = OnConflictStrategy.REPLACE)
    void update(Pdflawflag photoModel);

    @Query("SELECT * from Pdflawflag WHERE Pdflawflag.pdProjectid = :project_id AND  Pdflawflag.local_flaw_Id IN  (:flawId)")
    List<Pdflawflag> getFlawFlagList(String project_id, List<String> flawId);

    @Query("SELECT * from Pdflawflag WHERE Pdflawflag.pdProjectid = :project_id AND Pdflawflag.pdplanid = :planId AND Pdflawflag.local_flaw_Id IN  (:flawId)")
    List<Pdflawflag> getFlawFlagListWithPlanID(String project_id,String planId, List<String> flawId);


    //Worked perfectly
    @Query("SELECT DISTINCT * from Pdflawflag WHERE Pdflawflag.pdProjectid = :project_id AND  Pdflawflag.pdplanid =:planId AND  Pdflawflag.local_photo_id =:photo_id")
    Pdflawflag getFlawFlagObjUsingPhotoID(String project_id,String planId, String photo_id);

    @Query("SELECT DISTINCT pdplanid from Pdflawflag WHERE Pdflawflag.pdProjectid = :project_id AND  Pdflawflag.local_flaw_Id IN  (:flawId)")
    List<String> getDistinctPlanIdsList(String project_id, List<String> flawId);

    @Query("SELECT * from Pdflawflag WHERE Pdflawflag.pdProjectid = :project_id AND  Pdflawflag.pdplanid IN  (:planIds)")
    List<Pdflawflag> getFlawFlagUsingPlanIdList(String project_id, List<String> planIds);

    @Query("SELECT * from Pdflawflag WHERE Pdflawflag.pdProjectid = :project_id   AND  Pdflawflag.local_flaw_Id=:localflawId")
    Pdflawflag getFlawFlagOBJExist(String project_id, String localflawId);

    //Not worked properly
    @Query("SELECT * from Pdflawflag WHERE Pdflawflag.pdProjectid = :project_id   AND  Pdflawflag.pdplanid=:planId AND  Pdflawflag.local_photo_id=:localPhotoId ")
    Pdflawflag getFlawFlagOBJExistWithPlanId(String project_id, String planId, String localPhotoId);

    @Query("SELECT * from Pdflawflag WHERE Pdflawflag.pdProjectid = :project_id")
    List<Pdflawflag> getAllFlawFlagList(String project_id);

//    @Query("SELECT DISTINCT Pdflawflag.pdplanid from Pdflawflag WHERE Pdflawflag.pdProjectid = :project_id AND  Pdflawflag.flaw_Id IN  (:flawId)")
//    List<Pdflawflag> getFlawFlagUniqueList(String project_id, List<String> flawId);

    @Query("DELETE FROM Pdflawflag WHERE pdprojectid = :projectId")
    void deleteByProjectId(String projectId);

}
