package com.projectdocupro.mobile;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.projectdocupro.mobile.dao.CustomerDao;
import com.projectdocupro.mobile.dao.DefectsDao;
import com.projectdocupro.mobile.dao.DefectsPhotosDao;
import com.projectdocupro.mobile.dao.DefectsTradesDao;
import com.projectdocupro.mobile.dao.GewerkFirmDao;
import com.projectdocupro.mobile.dao.OnlinePhotoDao;
import com.projectdocupro.mobile.dao.PdFlawFLagListDao;
import com.projectdocupro.mobile.dao.PhotoDao;
import com.projectdocupro.mobile.dao.PlansDao;
import com.projectdocupro.mobile.dao.PlansPhotosDao;
import com.projectdocupro.mobile.dao.ProjectDao;
import com.projectdocupro.mobile.dao.ProjectDetailDao;
import com.projectdocupro.mobile.dao.ProjectUsersDao;
import com.projectdocupro.mobile.dao.RecordAudioDao;
import com.projectdocupro.mobile.dao.ReferPointPlansDao;
import com.projectdocupro.mobile.dao.WordDao;
import com.projectdocupro.mobile.models.CustomersModel;
import com.projectdocupro.mobile.models.DefectPhotoModel;
import com.projectdocupro.mobile.models.DefectTradeModel;
import com.projectdocupro.mobile.models.DefectsModel;
import com.projectdocupro.mobile.models.GewerkFirmModel;
import com.projectdocupro.mobile.models.OnlinePhotoModel;
import com.projectdocupro.mobile.models.Pdflawflag;
import com.projectdocupro.mobile.models.PhotoModel;
import com.projectdocupro.mobile.models.PlansModel;
import com.projectdocupro.mobile.models.PlansPhotoModel;
import com.projectdocupro.mobile.models.ProjectModel;
import com.projectdocupro.mobile.models.RecordAudioModel;
import com.projectdocupro.mobile.models.ReferPointJSONPlanModel;
import com.projectdocupro.mobile.models.WordModel;
import com.projectdocupro.mobile.models.mangel_filters.ProjectUserModel;

@Database(entities = {ProjectModel.class, DefectsModel.class, PlansModel.class, CustomersModel.class,
        WordModel.class, PhotoModel.class, RecordAudioModel.class,
        DefectPhotoModel.class, PlansPhotoModel.class, DefectTradeModel.class,
        ProjectUserModel.class, Pdflawflag.class, GewerkFirmModel.class, OnlinePhotoModel.class, ReferPointJSONPlanModel.class}, version = 2)
public abstract class ProjectsDatabase extends RoomDatabase {

    public abstract ProjectDao projectDao();

    public abstract ProjectDetailDao projectDetailDao();

    public abstract DefectsDao defectsDao();

    public abstract PlansDao plansDao();

    public abstract CustomerDao customerDao();

    public abstract WordDao wordDao();

    public abstract PhotoDao photoDao();

    public abstract RecordAudioDao recordAudioDao();

    public abstract DefectsPhotosDao defectsPhotosDao();

    public abstract PlansPhotosDao planPhotosDao();

    public abstract DefectsTradesDao defectTradeDao();

    public abstract ProjectUsersDao projectUsersDao();

    public abstract GewerkFirmDao gewerkFirmDao();

    public abstract PdFlawFLagListDao pdFlawFLagDao();

    public abstract OnlinePhotoDao onlinePhotoDao();

    public abstract ReferPointPlansDao referPointPlansDao();

    private static volatile ProjectsDatabase INSTANCE;

    public static ProjectsDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (ProjectsDatabase.class) {
                if (INSTANCE == null) {
                    // Create database here
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                            ProjectsDatabase.class, "projects_database")
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}
