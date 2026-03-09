package com.projectdocupro.mobile.interfaces;

import com.projectdocupro.mobile.models.DefectsModel;

import java.util.List;

public interface DefectsShortDetailListItemClickListener {
    void onListItemClick(List<DefectsModel> defectsModelList, DefectsModel defectsModel);
}
