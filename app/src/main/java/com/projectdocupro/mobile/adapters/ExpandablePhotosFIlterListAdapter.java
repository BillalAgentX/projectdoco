package com.projectdocupro.mobile.adapters;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.BaseExpandableListAdapter;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;

import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.projectdocupro.mobile.ProjectsDatabase;
import com.projectdocupro.mobile.R;

import com.projectdocupro.mobile.activities.PhotosActivity;
import com.projectdocupro.mobile.dao.WordDao;
import com.projectdocupro.mobile.managers.AppConstantsManager;
import com.projectdocupro.mobile.managers.SharedPrefsManager;
import com.projectdocupro.mobile.models.WordModel;
import com.projectdocupro.mobile.models.mangel_filters.ChildRowModel;
import com.projectdocupro.mobile.models.mangel_filters.GroupheadingModel;
import com.projectdocupro.mobile.utility.Utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;

//For expandable list view use BaseExpandableListAdapter
public class ExpandablePhotosFIlterListAdapter extends BaseExpandableListAdapter {
    private static Context _context;
    private static List<GroupheadingModel> header; // header titles
    // Child data in format of header title, child title
    private Timer timer = new Timer();
    private final long DELAY = 1000; // milliseconds
    // 4 Child types
    private static final int CHILD_TYPE_1 = 0;
    private static final int CHILD_TYPE_2 = 1;
    private static final int CHILD_TYPE_3 = 2;
    private static final int CHILD_TYPE_UNDEFINED = 3;


    private static final int HEADER_TYPE_1 = 0;
    private static final int HEADER_TYPE_2 = 1;
    private static final int HEADER_TYPE_3 = 2;
    private static final int HEADER_TYPE_4 = 3;
    private static final int HEADER_TYPE_5 = 4;


    private static final String STATUS = "Status";
    private static final String DEADLINE = "Deadline";
    private static final String DATE = "Date";

    private static final String START_DATE = "start_date";
    private static final String END_DATE = "end_date";

    boolean isKeyWorkloaded = false;
    private EditText et_search;
    private EditText et_search_2;
    static SharedPrefsManager sharedPrefsManager;
    private SimpleDateFormat simpleDateFormat;
    Date startDate = null;
    Date endDate = null;
    String dateOf5YearsBack;
    String currentDate;
    String defaultStartDateString = "";
    String defaultEndDateString = "";
    Date defaultStartDate = null;
    Date defaultEndDate = null;


    public ExpandablePhotosFIlterListAdapter(Context context, List<GroupheadingModel> listDataHeader) {
        this._context = context;
        this.header = listDataHeader;
        sharedPrefsManager = new SharedPrefsManager(context);

        final Calendar c = Calendar.getInstance();
        c.add(Calendar.DAY_OF_MONTH, +1);
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        currentDate = year + "-" + month + "-" + day;

        final Calendar c2 = Calendar.getInstance();
        c2.add(Calendar.YEAR, -5);
        int year2 = c2.get(Calendar.YEAR);
        int month2 = c2.get(Calendar.MONTH);
        int day2 = c2.get(Calendar.DAY_OF_MONTH);

        dateOf5YearsBack = year2 + "-" + month2 + "-" + day2;

        if (sharedPrefsManager.getStringValue(AppConstantsManager.APP_LANGUAGE, "de").equals("de")) {
            dateOf5YearsBack = day2 + "." + month2 + "." + year2;
            currentDate = day + "." + month + "." + year;
        } else {
            dateOf5YearsBack = year2 + "-" + month2 + "-" + day2;
            currentDate = year + "-" + month + "-" + day;
        }

        startDate = null;
        endDate = null;

        if (sharedPrefsManager.getStringValue(AppConstantsManager.APP_LANGUAGE, "de").equals("de")) {
            simpleDateFormat = new SimpleDateFormat("dd.MM.yyyy");

        } else {
            simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        }


        try {
            startDate = simpleDateFormat.parse(dateOf5YearsBack);
            endDate = simpleDateFormat.parse(currentDate);
        } catch (ParseException e) {
            e.printStackTrace();
        }


    }

    public ExpandablePhotosFIlterListAdapter(Context context, List<GroupheadingModel> listDataHeader, String defaultStartDateString, String defaultEndDateString) {
        this._context = context;
        this.header = listDataHeader;
        sharedPrefsManager = new SharedPrefsManager(context);
        this.defaultStartDateString = defaultStartDateString;
        this.defaultEndDateString = defaultEndDateString;


        final Calendar c = Calendar.getInstance();
        c.add(Calendar.DAY_OF_MONTH, +1);
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        currentDate = year + "-" + month + "-" + day;

        final Calendar c2 = Calendar.getInstance();
        c2.add(Calendar.YEAR, -5);
        int year2 = c2.get(Calendar.YEAR);
        int month2 = c2.get(Calendar.MONTH);
        int day2 = c2.get(Calendar.DAY_OF_MONTH);

        dateOf5YearsBack = year2 + "-" + month2 + "-" + day2;

        if (sharedPrefsManager.getStringValue(AppConstantsManager.APP_LANGUAGE, "de").equals("de")) {
            dateOf5YearsBack = day2 + "." + month2 + "." + year2;
            currentDate = day + "." + month + "." + year;
        } else {
            dateOf5YearsBack = year2 + "-" + month2 + "-" + day2;
            currentDate = year + "-" + month + "-" + day;
        }

        startDate = null;
        endDate = null;

        SimpleDateFormat englishDateFormat = new SimpleDateFormat("yyyy-MM-dd");



        if (sharedPrefsManager.getStringValue(AppConstantsManager.APP_LANGUAGE, "de").equals("de")) {
            simpleDateFormat = new SimpleDateFormat("dd.MM.yyyy");
            this.defaultStartDateString = Utils.dateFormatConversion(defaultStartDateString, Utils.ENGLISH_DATE_FORMAT, Utils.GERMAN_DATE_FORMAT);
            this.defaultEndDateString = Utils.dateFormatConversion(defaultEndDateString, Utils.ENGLISH_DATE_FORMAT, Utils.GERMAN_DATE_FORMAT);

        } else {
            simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
//            this.defaultStartDateString = Utils.dateFormatConversion(defaultStartDateString, Utils.GERMAN_DATE_FORMAT, Utils.ENGLISH_DATE_FORMAT);
//            this.defaultEndDateString = Utils.dateFormatConversion(defaultEndDateString, Utils.GERMAN_DATE_FORMAT, Utils.ENGLISH_DATE_FORMAT);
            this.defaultStartDateString = defaultStartDateString;
            this.defaultEndDateString = defaultEndDateString;
        }

        try {
            startDate = simpleDateFormat.parse(dateOf5YearsBack);
            endDate = simpleDateFormat.parse(currentDate);

            Date formattedStartDate = englishDateFormat.parse(defaultStartDateString);
            Date formattedEndDate = englishDateFormat.parse(defaultEndDateString);

//            this.defaultStartDateString = simpleDateFormat.format(formattedStartDate);
//            this.defaultEndDateString = simpleDateFormat.format(formattedEndDate);


//            defaultStartDate = simpleDateFormat.parse(defaultStartDateString);
//            defaultEndDate = simpleDateFormat.parse(defaultEndDateString);
        } catch (ParseException e) {
            e.printStackTrace();
        }


    }

    @Override
    public Object getChild(int groupPosition, int childPosititon) {

        // This will return the child
        return this.header.get(groupPosition).getListChildData().get(header.get(groupPosition).getType()).get(childPosititon);
    }


    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public View getChildView(int groupPosition, final int childPosition,
                             boolean isLastChild, View convertView, ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        // Getting child text
        int childType = getChildType(groupPosition, childPosition);
        ChildRowModel childRowModel = (ChildRowModel) getChild(groupPosition, childPosition);
        List<String> selectedIDList = this.header.get(groupPosition).getListChildDataSelected();

        // We need to create a new "cell container"
        if (convertView == null || (Integer) convertView.getTag() != childType) {
            switch (childType) {
                case CHILD_TYPE_2:
                    convertView = inflater.inflate(R.layout.filter_defect_normal_row_item, null);
                    convertView.setTag(childType);
                    break;
                case CHILD_TYPE_1:
                    convertView = inflater.inflate(R.layout.filter_photos_created_date_row_item, null);
                    convertView.setTag(childType);
                    break;
//                case CHILD_TYPE_3:
//                    convertView = inflater.inflate(R.layout.filter_defect_date_row_item, null);
//                    convertView.setTag(childType);
//                    break;
//                case CHILD_TYPE_UNDEFINED:
//                    convertView = inflater.inflate(R.layout.filter_defect_deadline_date_row_item, null);
//                    convertView.setTag(childType);
//                    break;
                default:
                    // Maybe we should implement a default behaviour but it should be ok we know there are 4 child types right?
                    break;
            }
        }
        // We'll reuse the existing one
        else {
            // There is nothing to do here really we just need to set the content of view which we do in both cases
        }

        switch (childType) {
//            case CHILD_TYPE_2:
//                ImageView iv_status = (ImageView) convertView.findViewById(R.id.iv_status);
//                ImageView iv_selection = (ImageView) convertView.findViewById(R.id.iv_selected_word);
//                TextView tv_title = (TextView) convertView.findViewById(R.id.sub_title);
//
//                tv_title.setText(childRowModel.getTitle());
//                if (childRowModel.getId().equals("0")) {
//                    iv_status.setImageDrawable(_context.getResources().getDrawable(R.drawable.green_circle_background));
////                    iv_status.setVisibility(View.GONE);
//                } else if (childRowModel.getId().equals("1")) {
//                    iv_status.setImageDrawable(_context.getResources().getDrawable(R.drawable.orange_circle_background));
////                    iv_status.setVisibility(View.GONE);
//
//                } else if (childRowModel.getId().equals("2")) {
//                    iv_status.setImageDrawable(_context.getResources().getDrawable(R.drawable.red_circle_background));
////                    iv_status.setVisibility(View.GONE);
//
//                }
//                if (selectedIDList != null && childRowModel.getId() != null && selectedIDList.contains(childRowModel.getId())) {
//                    iv_selection.setBackgroundResource(R.drawable.ic_selected_word);
//
//                } else {
//                    iv_selection.setBackgroundResource(R.drawable.ic_unselected_filter);
//
//                }
//
//                break;
            case CHILD_TYPE_2:
                //Define how to render the data on the CHILD_TYPE_2 layout

                ImageView iv_selection_normal = (ImageView) convertView.findViewById(R.id.iv_selected_word);
                TextView tv_title_normal = (TextView) convertView.findViewById(R.id.sub_title);

                tv_title_normal.setText(childRowModel.getTitle());

                if (selectedIDList != null && childRowModel.getId() != null && selectedIDList.contains(childRowModel.getId())) {
                    iv_selection_normal.setBackgroundResource(R.drawable.ic_selected_word);

                } else {
                    iv_selection_normal.setBackgroundResource(R.drawable.ic_unselected_filter);
                }

                break;
            case CHILD_TYPE_3:
                //Define how to render the data on the CHILD_TYPE_3 layout
                ImageView iv_selection_date = (ImageView) convertView.findViewById(R.id.iv_selected_word);
                ImageView iv_date = (ImageView) convertView.findViewById(R.id.iv_selected_word_date);
                TextView tv_title_date = (TextView) convertView.findViewById(R.id.sub_title);

                RelativeLayout rl_date = (RelativeLayout) convertView.findViewById(R.id.rl_normal);
                LinearLayout ll_date = (LinearLayout) convertView.findViewById(R.id.ll_date);
                TextView tv_date = (TextView) convertView.findViewById(R.id.tv_date);

                final Calendar c_date = Calendar.getInstance();


                tv_date.setText(formatedDate(c_date.get(Calendar.YEAR), (c_date.get(Calendar.MONTH) + 1), c_date.get(Calendar.DAY_OF_MONTH)));
//                header.get(groupPosition).setStart_date(Utils.convertStringToTimestamp(c_date.get(Calendar.YEAR) + "-" + (c_date.get(Calendar.MONTH) + 1) + "-" + c_date.get(Calendar.DAY_OF_MONTH)).getTime());
                header.get(groupPosition).setStart_date(startDate.getTime());
//                if(header.get(groupPosition).getListChildDataSelected().size()>0){
//                    ll_date.setAlpha(1f);
//                }else{
//                    ll_date.setAlpha(0.3f);
//                }

                ll_date.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                    }
                });
//                if (selectedIDList != null && childRowModel.getId() != null && selectedIDList.contains("1")) {
//                    iv_selection_date.setBackgroundResource(R.drawable.ic_selected_word);
//
//                } else {
//                    iv_selection_date.setBackgroundResource(R.drawable.ic_unselected_filter);
//
//                }
                iv_date.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        DialogFragment newFragment = new DatePickerFragment(tv_date, groupPosition, START_DATE);
                        newFragment.show(((PhotosActivity) _context).getSupportFragmentManager(), "datePicker");
                    }
                });

                rl_date.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ChildRowModel childRowModel = header.get(groupPosition).getListChildData().get(header.get(groupPosition).getType()).get(childPosition);
                        if (header.get(groupPosition).getListChildDataSelected() != null) {
                            if (header.get(groupPosition).getListChildDataSelected().contains("1")) {
                                header.get(groupPosition).getListChildDataSelected().remove("1");
                            } else {
                                if (header.get(groupPosition).getListChildDataSelected().size() > 0) {
                                    header.get(groupPosition).getListChildDataSelected().set(0, "1");
                                } else {
                                    header.get(groupPosition).getListChildDataSelected().add("1");
                                }
                            }
                        }
                        notifyDataSetChanged();
                        if (_context != null) {
                            ((PhotosActivity) _context).applyFilter();

//                            Toast.makeText(_context, "rl_date.setOnClickListener",
//                                    Toast.LENGTH_SHORT).show();
                        }

                    }
                });


                break;
            case CHILD_TYPE_1:
                //Define how to render the data on the CHILD_TYPE_UNDEFINED layout
                ImageView iv_selected_word_noticedate = (ImageView) convertView.findViewById(R.id.iv_selected_word_noticedate);
                TextView sub_title_noticedate = (TextView) convertView.findViewById(R.id.sub_title_noticedate);

                ImageView iv_selected_word_notifiedate = (ImageView) convertView.findViewById(R.id.iv_selected_word_notifiedate);
                TextView sub_title_notifiedate = (TextView) convertView.findViewById(R.id.sub_title_notifiedate);

                ImageView iv_selected_word_donedate = (ImageView) convertView.findViewById(R.id.iv_selected_word_donedate);
                TextView sub_title_donedate = (TextView) convertView.findViewById(R.id.sub_title_donedate);

                LinearLayout ll_start_date = (LinearLayout) convertView.findViewById(R.id.ll_start_date);
                TextView tv_start_date = (TextView) convertView.findViewById(R.id.tv_start_date);


                LinearLayout ll_end_date = (LinearLayout) convertView.findViewById(R.id.ll_end_date);
                TextView tv_end_date = (TextView) convertView.findViewById(R.id.tv_end_date);

                ImageView iv_start_date = (ImageView) convertView.findViewById(R.id.iv_selected_word_start_date);
                ImageView iv_end_date = (ImageView) convertView.findViewById(R.id.iv_selected_end_date);

                RelativeLayout rl_notice_date = (RelativeLayout) convertView.findViewById(R.id.rl_normal_noticedate);
                RelativeLayout rl_notified_date = (RelativeLayout) convertView.findViewById(R.id.rl_normal_notifiedate);
                RelativeLayout rl_done_date = (RelativeLayout) convertView.findViewById(R.id.rl_normal_donedate);


                ll_start_date.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                    }
                });
                ll_end_date.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                    }
                });

//                final Calendar c = Calendar.getInstance();
//                c.add(Calendar.DAY_OF_MONTH, +1);
//                int year = c.get(Calendar.YEAR);
//                int month = c.get(Calendar.MONTH);
//                int day = c.get(Calendar.DAY_OF_MONTH);
//
//                String currentDate = year + "-" + month + "-" + day;
//
//                final Calendar c2 = Calendar.getInstance();
//                c2.add(Calendar.YEAR, -5);
//                int year2 = c2.get(Calendar.YEAR);
//                int month2 = c2.get(Calendar.MONTH);
//                int day2 = c2.get(Calendar.DAY_OF_MONTH);
//
//                String dateOf5YearsBack = year2 + "-" + month2 + "-" + day2;
//
//                if (sharedPrefsManager.getStringValue(AppConstantsManager.APP_LANGUAGE, "de").equals("de")) {
//                    dateOf5YearsBack = day2 + "." + month2 + "." + year2;
//                    currentDate = day + "." + month + "." + year;
//                } else {
//                    dateOf5YearsBack = year2 + "-" + month2 + "-" + day2;
//                    currentDate = year + "-" + month + "-" + day;
//                }
//
//                Date startDate = null;
//                Date endDate = null;

                if (header.get(groupPosition).getListChildData().get(header.get(groupPosition).getType()).get(0).getId() != null
                        && !header.get(groupPosition).getListChildData().get(header.get(groupPosition).getType()).get(0).getId().equals("")) {
//                tv_end_date.setText(defectsModel.getFristDate());

//                    if (sharedPrefsManager.getStringValue(AppConstantsManager.APP_LANGUAGE, "de").equals("de")) {
//                        simpleDateFormat = new SimpleDateFormat("dd.MM.yyyy");
//
//                    } else {
//                        simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
//                    }
//
//
//                    try {
//                        startDate = simpleDateFormat.parse(dateOf5YearsBack);
//                        endDate = simpleDateFormat.parse(currentDate);
//                    } catch (ParseException e) {
//                        e.printStackTrace();
//                    }

//                    date = new Date(Long.valueOf(header.get(groupPosition).getListChildData().get(header.get(groupPosition).getType()).get(0).getId()));
//                    tv_start_date.setText(simpleDateFormat.format(startDate));
                    tv_start_date.setText(defaultStartDateString);
                }

//                tv_start_date.setText(header.get(groupPosition).getListChildData().get(header.get(groupPosition).getType()).get(0).getTitle());
//                tv_end_date.setText(formatedDate(year, (month + 1), day));
                tv_end_date.setText(defaultEndDateString);
                header.get(groupPosition).setStart_date(startDate.getTime());
                header.get(groupPosition).setEnd_date(Utils.getCurrentTimeStamp().getTime());
//                header.get(groupPosition).setStart_date( Utils.getCurrentTimeStamp().getTime());

                iv_start_date.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        DialogFragment newFragment = new DatePickerFragment(tv_start_date, groupPosition, START_DATE);
                        newFragment.show(((PhotosActivity) _context).getSupportFragmentManager(), "datePicker");
                    }
                });

                iv_end_date.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        DialogFragment newFragment = new DatePickerFragment(tv_end_date, groupPosition, END_DATE);
                        newFragment.show(((PhotosActivity) _context).getSupportFragmentManager(), "datePicker");
                    }
                });

                rl_notice_date.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ChildRowModel childRowModel = header.get(groupPosition).getListChildData().get(header.get(groupPosition).getType()).get(childPosition);
                        if (header.get(groupPosition).getListChildDataSelected() != null) {
                            if (header.get(groupPosition).getListChildDataSelected().contains("1")) {
                                header.get(groupPosition).getListChildDataSelected().remove("1");
                            } else {
                                if (header.get(groupPosition).getListChildDataSelected().size() > 0) {
                                    header.get(groupPosition).getListChildDataSelected().set(0, "1");
                                } else {
                                    header.get(groupPosition).getListChildDataSelected().add("1");
                                }
                            }

                        }
                        notifyDataSetChanged();
                        if (_context != null) {
                            ((PhotosActivity) _context).applyFilter();
//                            Toast.makeText(_context, "rl_notice_date.setOnClickListener",
//                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });

                rl_notified_date.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ChildRowModel childRowModel = header.get(groupPosition).getListChildData().get(header.get(groupPosition).getType()).get(childPosition);
                        if (header.get(groupPosition).getListChildDataSelected() != null) {

                            if (header.get(groupPosition).getListChildDataSelected().contains("2")) {
                                header.get(groupPosition).getListChildDataSelected().remove("2");
                            } else {
                                if (header.get(groupPosition).getListChildDataSelected().size() > 0) {
                                    header.get(groupPosition).getListChildDataSelected().set(0, "2");
                                } else {
                                    header.get(groupPosition).getListChildDataSelected().add("2");
                                }
                            }
                        }
                        notifyDataSetChanged();
                        if (_context != null) {
                            ((PhotosActivity) _context).applyFilter();
//                            Toast.makeText(_context, "rl_notied_date.setOnClickListener",
//                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });

                rl_done_date.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ChildRowModel childRowModel = header.get(groupPosition).getListChildData().get(header.get(groupPosition).getType()).get(childPosition);
                        if (header.get(groupPosition).getListChildDataSelected() != null) {
                            if (header.get(groupPosition).getListChildDataSelected().contains("3")) {
                                header.get(groupPosition).getListChildDataSelected().remove("3");
                            } else {
                                if (header.get(groupPosition).getListChildDataSelected().size() > 0) {
                                    header.get(groupPosition).getListChildDataSelected().set(0, "3");
                                } else {
                                    header.get(groupPosition).getListChildDataSelected().add("3");
                                }
                            }
                        }
                        notifyDataSetChanged();
                        if (_context != null) {
                            ((PhotosActivity) _context).applyFilter();
//                            Toast.makeText(_context, "rl_done_date.setOnClickListener",
//                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });

                break;
        }
        return convertView;
    }

    @Override
    public int getChildrenCount(int groupPosition) {

        // return children count
        return this.header.get(groupPosition).getListChildData().get(header.get(groupPosition).getType()).size();
    }


    @Override
    public Object getGroup(int groupPosition) {

        // Get header position
        return this.header.get(groupPosition);
    }

    @Override
    public int getGroupCount() {

        // Get header size
        return this.header.size();
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded,
                             View convertView, ViewGroup parent) {

        // Getting header title

        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        String headerTitle = ((GroupheadingModel) getGroup(groupPosition)).getTitle();
        int groupType = getGroupType(groupPosition);

        if (convertView == null || (Integer) convertView.getTag() != groupType) {
            switch (groupType) {
                case HEADER_TYPE_1:
                    convertView = inflater.inflate(R.layout.filter_photo_edittext_group_row_item, null);
                    convertView.setTag(groupType);
                    break;
                case HEADER_TYPE_2:
                    convertView = inflater.inflate(R.layout.filter_header_view, null);
                    convertView.setTag(groupType);
                    break;
                case HEADER_TYPE_3:
                    convertView = inflater.inflate(R.layout.filter_group_header_switch_view, null);
                    convertView.setTag(groupType);
                    break;
                case HEADER_TYPE_4:
                    convertView = inflater.inflate(R.layout.filter_photos_words_list_row_item, null);
                    convertView.setTag(groupType);
                    break;
                case HEADER_TYPE_5:
                    convertView = inflater.inflate(R.layout.filter_photo_edittext_number_group_row_item, null);
                    convertView.setTag(groupType);
                    break;
                default:
                    // Maybe we should implement a default behaviour but it should be ok we know there are 4 child types right?
                    break;
            }
        }


        switch (groupType) {
            case HEADER_TYPE_1:
                TextView tv_sub_title = (TextView) convertView.findViewById(R.id.sub_title);
                et_search = (EditText) convertView.findViewById(R.id.et_search);

                //  if (header.get(groupPosition).getType().equalsIgnoreCase(_context.getResources().getString(R.string.heading_photo_number))) {
                tv_sub_title.setText(_context.getResources().getString(R.string.heading_photo_desc));
                et_search.setText(header.get(groupPosition).getKeyword());
                //    et_search.requestFocus();

                //   }
//                else if (header.get(groupPosition).getType().equalsIgnoreCase(_context.getResources().getString(R.string.heading_photo_desc))) {
//                    tv_sub_title.setText(_context.getResources().getString(R.string.heading_photo_desc));
//                    et_search.setInputType(InputType.TYPE_CLASS_TEXT);
//                    et_search.setText(header.get(groupPosition).getKeyword());
//                    et_search.requestFocus();
//                }


                convertView.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {


                        et_search.setInputType(InputType.TYPE_TEXT_FLAG_CAP_WORDS);

                        et_search.requestFocus();

                        return false;
                    }
                });
                et_search.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {


                        et_search.setInputType(InputType.TYPE_TEXT_FLAG_CAP_WORDS);

                        et_search.requestFocus();
                        InputMethodManager mImm = (InputMethodManager)
                                _context.getSystemService(Context.INPUT_METHOD_SERVICE);
                        mImm.showSoftInput(et_search, InputMethodManager.SHOW_IMPLICIT);
                        return false;
                    }
                });
                et_search.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {


                        header.get(groupPosition).setKeyword(et_search.getText().toString());

                    }

                    @Override
                    public void afterTextChanged(Editable s) {

//                        if (!et_search.getText().toString().equals("")) {
                        ((PhotosActivity) _context).applyFilter();
//                            Toast.makeText(_context, "et_searchafterTextChanged",
//                                    Toast.LENGTH_SHORT).show();
//                        }
                    }
                });
                break;
            case HEADER_TYPE_5:
                TextView tv_sub_title_2 = (TextView) convertView.findViewById(R.id.sub_title);
                et_search_2 = (EditText) convertView.findViewById(R.id.et_search_number);

//                if (header.get(groupPosition).getType().equalsIgnoreCase(_context.getResources().getString(R.string.heading_photo_number))) {
//                    tv_sub_title_2.setText(_context.getResources().getString(R.string.heading_photo_number));
//                    et_search_2.setInputType(InputType.TYPE_CLASS_NUMBER);
//                    et_search_2.setText(header.get(groupPosition).getKeyword());
//                    et_search_2.requestFocus();
//
//                } else
                //   if (header.get(groupPosition).getType().equalsIgnoreCase(_context.getResources().getString(R.string.heading_photo_desc))) {
                tv_sub_title_2.setText(_context.getResources().getString(R.string.heading_photo_number));

                et_search_2.setText(header.get(groupPosition).getKeyword());
                //  et_search_2.requestFocus();
                //    }

                et_search_2.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        et_search_2.requestFocus();

                        et_search_2.setInputType(InputType.TYPE_CLASS_NUMBER);
                        InputMethodManager mImm = (InputMethodManager)
                                _context.getSystemService(Context.INPUT_METHOD_SERVICE);
                        mImm.showSoftInput(et_search_2, InputMethodManager.SHOW_IMPLICIT);
                        return false;
                    }
                });
                et_search_2.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
//                        if(s.length()>2){

                        header.get(groupPosition).setKeyword(et_search_2.getText().toString());
//                        }
                    }

                    @Override
                    public void afterTextChanged(Editable s) {
//                        if (!et_search_2.getText().toString().equals("")) {
                        ((PhotosActivity) _context).applyFilter();
//                            Toast.makeText(_context, "et_search_2 afterTextChanged",
//                                    Toast.LENGTH_SHORT).show();
//                        }

                    }
                });
                break;

            case HEADER_TYPE_2:
                TextView header_text = (TextView) convertView.findViewById(R.id.tv_title);
                ImageView iv_arrow = (ImageView) convertView.findViewById(R.id.iv_arrow);
                // If group is expanded then change the text into bold and change the
                // icon
                if (isExpanded) {
                    iv_arrow.setRotation(180);
                } else {
                    iv_arrow.setRotation(0);
                }
                header_text.setText(headerTitle);
                break;

            case HEADER_TYPE_3:
                TextView header_text_3 = (TextView) convertView.findViewById(R.id.tv_title);
                Switch aSwitch = (Switch) convertView.findViewById(R.id.swh);

                if (header.get(groupPosition).getType().equalsIgnoreCase(_context.getResources().getString(R.string.heading_photo_switch_decs))) {
                    header_text_3.setText(_context.getResources().getString(R.string.heading_photo_switch_decs));
                    aSwitch.setChecked(header.get(groupPosition).isSwitchOn());
                } else if (header.get(groupPosition).getType().equalsIgnoreCase(_context.getResources().getString(R.string.heading_photo_switch_keyword))) {
                    header_text_3.setText(_context.getResources().getString(R.string.heading_photo_switch_keyword));
                    aSwitch.setChecked(header.get(groupPosition).isSwitchOn());

                } else if (header.get(groupPosition).getType().equalsIgnoreCase(_context.getResources().getString(R.string.heading_localized_photo))) {
                    header_text_3.setText(_context.getResources().getString(R.string.heading_localized_photo));
                    aSwitch.setChecked(header.get(groupPosition).isSwitchOn());

                }
                aSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                        if (isChecked) {
                            if (header.get(groupPosition).getType().equalsIgnoreCase(_context.getResources().getString(R.string.heading_photo_switch_decs))) {
                                header.get(groupPosition).setSwitchOn(true);

                            } else if (header.get(groupPosition).getType().equalsIgnoreCase(_context.getResources().getString(R.string.heading_photo_switch_keyword))) {
                                header.get(groupPosition).setSwitchOn(true);

                            } else if (header.get(groupPosition).getType().equalsIgnoreCase(_context.getResources().getString(R.string.heading_localized_photo))) {
                                header.get(groupPosition).setSwitchOn(true);

                            }
                        } else {
                            if (header.get(groupPosition).getType().equalsIgnoreCase(_context.getResources().getString(R.string.heading_photo_switch_decs))) {
                                header.get(groupPosition).setSwitchOn(false);

                            } else if (header.get(groupPosition).getType().equalsIgnoreCase(_context.getResources().getString(R.string.heading_photo_switch_keyword))) {
                                header.get(groupPosition).setSwitchOn(false);

                            } else if (header.get(groupPosition).getType().equalsIgnoreCase(_context.getResources().getString(R.string.heading_localized_photo))) {
                                header.get(groupPosition).setSwitchOn(false);

                            }
                        }
                        if (_context != null) {
                            ((PhotosActivity) _context).applyFilter();
//                            Toast.makeText(_context, "aSwitch ",
//                                    Toast.LENGTH_SHORT).show();
                        }

                    }
                });
                break;

            case HEADER_TYPE_4:
                TextView header_text2 = (TextView) convertView.findViewById(R.id.tv_title);
                ImageView iv_arrow2 = (ImageView) convertView.findViewById(R.id.iv_arrow);
                RecyclerView recyclerView = (RecyclerView) convertView.findViewById(R.id.words_rv);
                // If group is expanded then change the text into bold and change the
                // icon
//                if (isExpanded) {
//                    iv_arrow2.setRotation(180);
//                    recyclerView.setVisibility(View.VISIBLE);
//                } else {
//                    iv_arrow2.setRotation(0);
//                    recyclerView.setVisibility(View.GONE);
//                }
                iv_arrow2.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if(recyclerView.getVisibility() == View.VISIBLE) {
                            iv_arrow2.setRotation(0);
                            recyclerView.setVisibility(View.GONE);
                        } else {
                            iv_arrow2.setRotation(180);
                            recyclerView.setVisibility(View.VISIBLE);
                        }
                    }
                });
                header_text2.setText(headerTitle);

                if (!isKeyWorkloaded)
                    new GetKeywordsAsyncTask(recyclerView, groupPosition).execute(header.get(groupPosition).getProject_id());
                break;
            // Inflating header layout and setting text
            default:
                break;

        }
        return convertView;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }

    @Override
    public int getChildType(int groupPosition, int childPosition) {


        if (header.get(groupPosition).getType().equalsIgnoreCase(_context.getResources().getString(R.string.heading_photo_number)) || header.get(groupPosition).getType().equalsIgnoreCase(_context.getResources().getString(R.string.heading_photo_desc))) {
            return CHILD_TYPE_3;

        } else if (header.get(groupPosition).getType().equalsIgnoreCase(_context.getResources().getString(R.string.heading_created_date))) {
            return CHILD_TYPE_1;

        } else {
            return CHILD_TYPE_2;
        }

    }

    @Override
    public int getGroupType(int groupPosition) {
        if (header.get(groupPosition).getType().equalsIgnoreCase(_context.getResources().getString(R.string.heading_photo_number))) {
            return HEADER_TYPE_5;

        } else if (header.get(groupPosition).getType().equalsIgnoreCase(_context.getResources().getString(R.string.heading_photo_switch_decs))
                || header.get(groupPosition).getType().equalsIgnoreCase(_context.getResources().getString(R.string.heading_photo_switch_keyword))
                || header.get(groupPosition).getType().equalsIgnoreCase(_context.getResources().getString(R.string.heading_localized_photo))) {
            return HEADER_TYPE_3;

        } else if (header.get(groupPosition).getType().equalsIgnoreCase(_context.getResources().getString(R.string.heading_photo_keyword))) {
            return HEADER_TYPE_4;

        } else if (header.get(groupPosition).getType().equalsIgnoreCase(_context.getResources().getString(R.string.heading_photo_desc))) {
            return HEADER_TYPE_1;

        } else {
            return HEADER_TYPE_2;
        }
    }

    @Override
    public int getGroupTypeCount() {
        return 5;
    }

    @Override
    public int getChildTypeCount() {
        return 4;
    }

    public static class DatePickerFragment extends DialogFragment
            implements DatePickerDialog.OnDateSetListener {

        TextView tvLabel;
        int gPos = 0;
        String type = "";

        public DatePickerFragment(TextView tvLabel, int groupPosition, String dateType) {
            this.tvLabel = tvLabel;
            gPos = groupPosition;
            type = dateType;
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            final Calendar c = Calendar.getInstance();
            int year = c.get(Calendar.YEAR);
            int month = c.get(Calendar.MONTH);
            int day = c.get(Calendar.DAY_OF_MONTH);
            DatePickerDialog dialog = new DatePickerDialog(getActivity(), this, year, month, day);
            dialog.getDatePicker().setMaxDate(c.getTimeInMillis());
            return dialog;
        }

        public void onDateSet(DatePicker view, int year, int month, int day) {
            tvLabel.setText(formatedDate(year, month + 1, day));
            Calendar calendar = Calendar.getInstance();
            calendar.set(year, month, day);

            if (type.equalsIgnoreCase(START_DATE)) {
                header.get(gPos).setStart_date(calendar.getTimeInMillis());
            } else if (type.equalsIgnoreCase(END_DATE)) {
                header.get(gPos).setEnd_date(calendar.getTimeInMillis());
            }
            if (_context != null) {
                ((PhotosActivity) _context).applyFilter();
//                Toast.makeText(_context, "onDateSet ",
//                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    private static String formatedDate(int year, int month, int day) {
        String formatedDate = "";
        String monthWithZero = month + "";
        String dayWithZero = day + "";
        if(month < 10) {
            monthWithZero = "0" + month;
        }
        if(day < 10) {
            dayWithZero = "0" + day;
        }
        if (sharedPrefsManager.getStringValue(AppConstantsManager.APP_LANGUAGE, "de").equals("de")) {

            formatedDate = dayWithZero + "." + monthWithZero + "." + year;
        } else {
            formatedDate = year + "-" + monthWithZero + "-" + dayWithZero;
        }
        return formatedDate;
    }


    public class GetKeywordsAsyncTask extends AsyncTask<String, Void, List<WordModel>> {
        private long photoId;
        WordDao mAysncWordDao;
        RecyclerView rView;
        int groupPosition;

        GetKeywordsAsyncTask(RecyclerView recyclerView, int groupPos) {
            mAysncWordDao = ProjectsDatabase.getDatabase(_context).wordDao();
            rView = recyclerView;
            groupPosition = groupPos;
        }

        @Override
        protected List<WordModel> doInBackground(String... params) {
            List<WordModel> wordsSimpleList = mAysncWordDao.getAllWordsList(params[0]);
            return wordsSimpleList;
        }

        @Override
        protected void onPostExecute(List<WordModel> wordModels) {
            super.onPostExecute(wordModels);
            List<String> keys = new ArrayList<>();
            WordListFilterRecyclerAdapter wordListRecyclerAdapter = null;
            Map<String, List<WordModel>> map = new HashMap<>();

            List<WordModel> wordModelList = new ArrayList<>();
            wordListRecyclerAdapter = new WordListFilterRecyclerAdapter(_context, 0, keys, map, wordModel -> {
                //   Toast.makeText(_context, "Main Clicked " + wordModel.getName(), Toast.LENGTH_SHORT).show();

//                if (wordModel.getPhotoType() != null && !wordModel.getPhotoType().equals("")) {

                if (wordModel.isFavorite()) {
                    wordModelList.add(wordModel);
                    header.get(groupPosition).getWordModelArrayList().add(wordModel);
                } else {
                    wordModelList.remove(wordModel);
                    header.get(groupPosition).getWordModelArrayList().add(wordModel);
                }
                if (wordModelList.size() == 0) {
                    header.get(groupPosition).getListChildDataSelected().clear();
                    header.get(groupPosition).getWordModelArrayList().clear();
                }
                String strIds = "";

                List<String> items = new ArrayList<>();

                if (wordModel.getPhotoIds() != null && !wordModel.getPhotoIds().equals("")) {

                    strIds = wordModel.getPhotoIds().replaceFirst(",", "");
                    wordModel.setPhotoIds(strIds);
                    for (int i = 0; i < wordModelList.size(); i++) {
                        List<String> temp = new ArrayList<>(Arrays.asList(wordModel.getPhotoIds().split("\\s*,\\s*")));
                        items.addAll(temp);
                    }
                }
                   /* else if (wordModel.getPhotoType()!=null&&wordModel.getPhotoType().equalsIgnoreCase(LocalPhotosRepository.TYPE_ONLINE_PHOTO)) {

                        strIds = wordModel.getOnlinePhotoIds().replaceFirst(",", "");
                        wordModel.setPhotoIds(strIds);
                        for (int i = 0; i < wordModelList.size(); i++) {
                            List<String> temp = new ArrayList<>(Arrays.asList(wordModel.getOnlinePhotoIds().split("\\s*,\\s*")));
                            items.addAll(temp);
                        }
                    }*/
                else {
                    for (int i = 0; i < wordModelList.size(); i++) {

                        items.add(wordModelList.get(i).getProjectParamId());
                    }
                }
                for (int i = 0; i < items.size(); i++) {
                    if (!header.get(groupPosition).getListChildDataSelected().contains(items.get(i))) {
                        header.get(groupPosition).getListChildDataSelected().add(items.get(i));

                    }
                }
                notifyDataSetChanged();
                if (_context != null) {
                    ((PhotosActivity) _context).applyFilter();
                }
//                }
            });

            wordListRecyclerAdapter.setData(wordModels);

//            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(_context) {
//                @Override
//                public boolean canScrollVertically() {
//                    return false;
//                }
//            };
            rView.setLayoutManager(new LinearLayoutManager(_context));
            rView.setAdapter(wordListRecyclerAdapter);
            //rView.setNestedScrollingEnabled(true);//Does not make any difference
            rView.setHasFixedSize(true);
            notifyDataSetChanged();
            isKeyWorkloaded = true;
        }
    }

}
