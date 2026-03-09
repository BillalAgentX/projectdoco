package com.projectdocupro.mobile.interfaces;

import com.projectdocupro.mobile.models.ProjectModel;

import java.util.List;

public interface IGetProjectsList {

    public void onLoadProjectList(List<ProjectModel> projectModels);
    public void onFailure();
}
