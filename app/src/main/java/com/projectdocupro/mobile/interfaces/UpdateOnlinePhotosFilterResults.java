package com.projectdocupro.mobile.interfaces;

import com.projectdocupro.mobile.models.OnlinePhotoModel;

import java.util.List;

public interface UpdateOnlinePhotosFilterResults {

    public void updatePhotosResults(List<OnlinePhotoModel> onlinePhotoModelList,boolean isClearPrevList);
}
