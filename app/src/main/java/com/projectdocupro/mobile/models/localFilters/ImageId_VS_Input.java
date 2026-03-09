package com.projectdocupro.mobile.models.localFilters;

public class ImageId_VS_Input {
    public ImageId_VS_Input(String imageID, String inputFields, String title) {
        this.imageID = imageID;
        this.inputFields = inputFields;
        this.keywordTitle = title;

    }

    private String imageID;

    public String getKeywordTitle() {
        return keywordTitle;
    }

    public void setKeywordTitle(String keywordTitle) {
        this.keywordTitle = keywordTitle;
    }

    private String keywordTitle;

    public String getInputFields() {
        return inputFields;
    }

    public void setInputFields(String inputFields) {
        this.inputFields = inputFields;
    }

    private String inputFields;

    public String getImageID() {
        return imageID;
    }

    public void setImageID(String imageID) {
        this.imageID = imageID;
    }
}
