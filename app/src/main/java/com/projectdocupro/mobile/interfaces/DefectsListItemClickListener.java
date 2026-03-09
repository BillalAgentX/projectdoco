package com.projectdocupro.mobile.interfaces;

import com.projectdocupro.mobile.models.DefectsModel;

public interface DefectsListItemClickListener {
    void onListItemClick(DefectsModel defectsModel);
    void onSyncIconClick(DefectsModel defectsModel);
    void onDeleteIconClick(DefectsModel defectsModel);
}
