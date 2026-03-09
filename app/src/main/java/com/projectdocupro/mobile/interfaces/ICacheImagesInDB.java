package com.projectdocupro.mobile.interfaces;

import com.projectdocupro.mobile.models.ProjectModel;

import java.util.List;

public interface ICacheImagesInDB {
    void cacheImagesInDB(List<ProjectModel> projectModelList);
}
