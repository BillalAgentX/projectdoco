package com.projectdocupro.mobile.interfaces;

import com.projectdocupro.mobile.adapters.ProjectsRecyclerAdapter;
import com.projectdocupro.mobile.models.ProjectModel;

public interface ProjectsListItemClickListener {
    void onListItemClick(ProjectModel   projectModel, boolean isMarkFavourite, ProjectsRecyclerAdapter adapter);
    void onSyncActionClick(ProjectModel   projectModel);
}
