package com.projectdocupro.mobile.models.localFilters;

import android.util.Log;

import java.util.ArrayList;

public class WordContentModel {
    public ArrayList<ImageId_VS_Input> getInputsList() {
        return inputsList;
    }

    public void setInputsList(ArrayList<ImageId_VS_Input> inputsList) {
        this.inputsList = inputsList;
    }

    private ArrayList<ImageId_VS_Input>  inputsList = new ArrayList<>();

    public ImageId_VS_Input findByImageId(String imgID, String title){
        ImageId_VS_Input result  = null;
        for(ImageId_VS_Input wordId_vs_input:inputsList){
            if(wordId_vs_input.getImageID().equals(imgID)&&title.equals(wordId_vs_input.getKeywordTitle()))
            {

                result = wordId_vs_input;
                break;
            }
        }
        return result;
    }
}
