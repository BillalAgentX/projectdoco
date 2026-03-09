package com.projectdocupro.mobile.fragments;

import static com.projectdocupro.mobile.activities.DefectDetailsActivity2.isDataUpdated;
import static com.projectdocupro.mobile.fragments.DefectsListFragment.BR_ACTION_UPDATE_DEFECT_DATA;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.projectdocupro.mobile.ProjectsDatabase;
import com.projectdocupro.mobile.R;
import com.projectdocupro.mobile.activities.DefectDetailsActivity;
import com.projectdocupro.mobile.activities.DefectDetailsActivity2;
import com.projectdocupro.mobile.activities.DefectsActivity;
import com.projectdocupro.mobile.adapters.ExpandableListDefectDetailDialogAdapter;
import com.projectdocupro.mobile.dao.DefectsDao;
import com.projectdocupro.mobile.dao.DefectsTradesDao;
import com.projectdocupro.mobile.dao.PhotoDao;
import com.projectdocupro.mobile.dao.ProjectUsersDao;
import com.projectdocupro.mobile.managers.AppConstantsManager;
import com.projectdocupro.mobile.managers.SharedPrefsManager;
import com.projectdocupro.mobile.models.DefectTradeModel;
import com.projectdocupro.mobile.models.DefectsModel;
import com.projectdocupro.mobile.models.PhotoModel;
import com.projectdocupro.mobile.models.mangel_filters.ChildRowModel;
import com.projectdocupro.mobile.models.mangel_filters.GroupheadingModel;
import com.projectdocupro.mobile.models.mangel_filters.ProjectUserModel;
import com.projectdocupro.mobile.repos.DefectRepository;
import com.projectdocupro.mobile.repos.DefectTradesRepository;
import com.projectdocupro.mobile.repos.LocalPhotosRepository;
import com.projectdocupro.mobile.repos.ProjectDetailRepository;
import com.projectdocupro.mobile.utility.Utils;
import com.projectdocupro.mobile.viewModels.DefectDetailsDatesFraagViewModel;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;





public class DefectDetailsDatesFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private static SimpleDateFormat simpleDateFormat;

    // TODO: Rename and change types of parameters
    private String projectId;
    private String mParam2;
    boolean is_from_mangel_create;



    private EditText et_description_text;
    private TextView tv_deadline;

    private TextView tv_art_text;

    private TextView tv_res_user_name_text;

    private ImageView iv_status_red;

    private ImageView iv_status_orange;

    private ImageView iv_status_green;

    private RelativeLayout rl_art_view;

    private RelativeLayout rl_trade_view;

    private RelativeLayout rl_users_view;

    private TextView tv_date_1;

    private TextView tv_date_2;

    private TextView tv_date_3;

    private TextView tv_date_4;

    private TextView et_days;

    private TextView tv_end_date;

    private ImageView iv_end_date;

    private ImageView iv_close;

    private LinearLayout ll_priority_view;

    private TextView tv_company_text;

    private EditText et_flaw_name;

    private Spinner sp_days;

    static int noOfDays = 0;
    static boolean isFirstTime;
    public static boolean isTradeChildClicked = false;

    public boolean afterSaveKillActivity = true;


    DefectDetailsDatesFraagViewModel defectDetailsDatesFraagViewModel;

    ProjectDetailRepository projectDetailRepository;
    DefectTradesRepository defectTradesRepository;
    DefectRepository defectRepository;

    public List<GroupheadingModel> groupheadingMangelList = new ArrayList<>();
    public List<GroupheadingModel> groupheadingGewerkList = new ArrayList<>();
    public List<GroupheadingModel> groupheadingProjectUsersList = new ArrayList<>();

    boolean isTradesLoaded;
    boolean isUsersLoaded;
    boolean isFlawTypeLoaded;

    private Dialog customDialog;
    private ExpandableListDefectDetailDialogAdapter expandableListAdapter;
    private String projectStatus = "";

    public static String defect_type = "";
    private DefectsModel defectsModelObj;
    private boolean isFromPhoto;
    private String photoId = "";

    static MutableLiveData<Boolean> isDateSelected = new MutableLiveData<>();
    private static SharedPrefsManager sharedPrefsManager;
    ArrayList<String> stringArrayList = new ArrayList<>();
    private boolean isfirstTimeCallback = false;
    private AsyncTask<Void, Void, List<DefectTradeModel>> retriveGewerkAsyncTask;
    private AsyncTask<Void, Void, List<ProjectUserModel>> retriveDistinctProjectUsersAsyncTask;
    private AsyncTask<String, Void, List<PhotoModel>> reterivePhotoDesAsyncTask;
    private AsyncTask<String, Void, ProjectUserModel> retriveProjectUsersAsyncTask;
    public boolean isDefectNameAdded;
    private String fristDateToSave = "";
    private long lastInsertedDefectID = -1;


    public DefectDetailsDatesFragment() {
        // Required empty public constructor
    }

    public static DefectDetailsDatesFragment newInstance(String param1, String param2, boolean is_from_mangel_create) {
        DefectDetailsDatesFragment fragment = new DefectDetailsDatesFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        args.putBoolean(DefectsActivity.IS_CREATED_MANGEL_KEY, is_from_mangel_create);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            projectId = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
            is_from_mangel_create = getArguments().getBoolean(DefectsActivity.IS_CREATED_MANGEL_KEY);
        }
        if (getActivity() != null && getActivity() instanceof DefectDetailsActivity) {
            defect_type = ((DefectDetailsActivity) getActivity()).defect_type;
        } else if (getActivity() != null && getActivity() instanceof DefectDetailsActivity2) {
            defect_type = ((DefectDetailsActivity2) getActivity()).defect_type;

        }
        if (defect_type == null)
            defect_type = "";
        sharedPrefsManager = new SharedPrefsManager(getActivity());
        isDateSelected.postValue(false);

        if (sharedPrefsManager.getStringValue(AppConstantsManager.APP_LANGUAGE, "de").equals("de")) {
            simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy");

        } else {
            simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        }


        projectDetailRepository = new ProjectDetailRepository(getActivity().getApplication(), projectId);
        defectTradesRepository = new DefectTradesRepository(getActivity().getApplication(), projectId);
        defectRepository = new DefectRepository(getActivity().getApplication(), projectId);
        isFirstTime = false;



    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {

      inflater.inflate(R.menu.save_action_menu, menu);
        if (getActivity() != null && getActivity() instanceof DefectDetailsActivity) {
            photoId = ((DefectDetailsActivity) getActivity()).photoId;

        } else if (getActivity() != null && getActivity() instanceof DefectDetailsActivity2) {
            photoId = ((DefectDetailsActivity2) getActivity()).photoId;
        }

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.save_action) {
            if (et_flaw_name.getText().toString().equals("")) {
                Toast.makeText(getContext(), getResources().getString(R.string.defect_screen_flaw_name_empty_msg), Toast.LENGTH_SHORT).show();

                return true;
                //    new UpdatePhotoAsyncTask(projectsDatabase.photoDao()).execute(projectId, photoId);
            }

            if (photoId != null && !photoId.equals("")) {
                ProjectsDatabase projectsDatabase = ProjectsDatabase.getDatabase(getActivity());
                saveDefect();

            } else {
                saveDefect();
            }
            if (getActivity() != null) {
                getActivity().setResult(Activity.RESULT_OK);
                getActivity().finish();
            }
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_defect_details_dates_fragment, container, false);
        bindView(view);

        defectDetailsDatesFraagViewModel = ViewModelProviders.of(this).get(DefectDetailsDatesFraagViewModel.class);
        loadPhotosData();
        defectDetailsDatesFraagViewModel.init(projectId, mParam2);

        Observer observer = new Observer<DefectsModel>() {
            @Override
            public void onChanged(DefectsModel photoModels) {
                populateDefectDetail(photoModels);

//                defectDetailsDatesFraagViewModel.getListLiveData().removeObservers(getActivity());
            }
        };

        defectDetailsDatesFraagViewModel.getListLiveData().observe(this, observer);
        isDateSelected.observe(this, aBoolean -> {

            if (aBoolean) {
                tv_date_1.setBackground(null);
                tv_date_2.setBackground(null);
                tv_date_3.setBackground(null);
                tv_date_4.setBackground(null);
            }

        });
        addEvent();
        return view;
    }

    private void addEvent() {
        for (int i = 1; i <= 60; i++) {
            stringArrayList.add(i + "   " + "");
        }


        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, stringArrayList);
        // Drop down layout style - list view with radio button
        dataAdapter.setDropDownViewResource(android.R.layout.simple_list_item_1);
        // attaching data adapter to spinner
        sp_days.setAdapter(dataAdapter);
        rl_art_view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadFlawType();

            }
        });

        iv_close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                et_description_text.setText("");
                isDataUpdated = true;
            }
        });

        rl_trade_view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isTradesLoaded) {
                    showCustomListDialog(getActivity(), groupheadingGewerkList, getResources().getString(R.string.heading_gewerk));
                } else {
                    loadTrades();
                }
            }
        });

        rl_users_view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isUsersLoaded) {
                    showCustomListDialog(getActivity(), groupheadingProjectUsersList, getResources().getString(R.string.heading_responsible));
                } else {
                    loadUsers();
                }
            }
        });

        iv_status_red.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isDataUpdated = true;
                projectStatus = "2";

                iv_status_orange.setImageResource(0);
                iv_status_red.setImageResource(R.drawable.red_circle_selected);
                iv_status_green.setImageResource(0);

            }
        });

        iv_status_orange.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isDataUpdated = true;
                projectStatus = "1";
                iv_status_orange.setImageResource(R.drawable.yellow_circle_selected);
                iv_status_red.setImageResource(0);
                iv_status_green.setImageResource(0);
            }
        });

        iv_status_green.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isDataUpdated = true;
                projectStatus = "0";
                iv_status_orange.setImageResource(0);
                iv_status_red.setImageResource(0);
                iv_status_green.setImageResource(R.drawable.green_circle_selected);
                Date date = new Date();
                //tv_end_date.setText(simpleDateFormat.format(date));
                defectsModelObj.setDoneDate(simpleDateFormat.format(date));
                defectsModelObj.setDonedate_df(date.getTime());


            }
        });

        tv_date_1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isDataUpdated = true;
                noOfDays = 3;
                et_days.setText(noOfDays + "");
                tv_end_date.setText(getPriority(noOfDays));
                tv_date_1.setBackground(getResources().getDrawable(R.drawable.add_defect_rounded_cornor_gray_background));
                tv_date_2.setBackground(null);
                tv_date_3.setBackground(null);
                tv_date_4.setBackground(null);

            }
        });

        tv_date_2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isDataUpdated = true;
                noOfDays = 7;
                et_days.setText(noOfDays + "");
                tv_end_date.setText(getPriority(noOfDays));
                tv_date_2.setBackground(getResources().getDrawable(R.drawable.add_defect_rounded_cornor_gray_background));
                tv_date_1.setBackground(null);
                tv_date_3.setBackground(null);
                tv_date_4.setBackground(null);

            }
        });

        tv_date_3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isDataUpdated = true;
                noOfDays = 10;
                et_days.setText(noOfDays + "");
                tv_end_date.setText(getPriority(noOfDays));
                tv_date_3.setBackground(getResources().getDrawable(R.drawable.add_defect_rounded_cornor_gray_background));
                tv_date_2.setBackground(null);
                tv_date_1.setBackground(null);
                tv_date_4.setBackground(null);

            }
        });

        tv_date_4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isDataUpdated = true;
                noOfDays = 14;
                et_days.setText(noOfDays + "");
                tv_end_date.setText(getPriority(noOfDays));
                tv_date_4.setBackground(getResources().getDrawable(R.drawable.add_defect_rounded_cornor_gray_background));
                tv_date_2.setBackground(null);
                tv_date_3.setBackground(null);
                tv_date_1.setBackground(null);

            }
        });

        iv_end_date.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogFragment newFragment = new DatePickerFragment(tv_end_date, et_days);
                newFragment.show(getFragmentManager(), "datePicker");
            }
        });
        tv_end_date.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                DialogFragment newFragment = new DatePickerFragment(tv_end_date, et_days);
                newFragment.show(getFragmentManager(), "datePicker");
            }
        });
        et_flaw_name.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
//                isDataUpdated=true;
                if (defectsModelObj != null && defectsModelObj.getDefectName() != null && defectsModelObj.getDefectName().equals(s.toString())) {
                   Utils.showLogger("isDataUpdate=false from textchange");

                    isDataUpdated = false;//from text change
                } else {
                    isDataUpdated = true;
                }
                if (s.toString().length() > 0)
                    isDefectNameAdded = true;
                else
                    isDefectNameAdded = false;

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        et_description_text.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
//                isDataUpdated=true;
                if (defectsModelObj != null && defectsModelObj.getDescription() != null && defectsModelObj.getDescription().equals(s.toString())) {
                    Utils.showLogger("isDataUpdate=false from2");
                    isDataUpdated = false;//from text change
                } else {
                    isDataUpdated = true;
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        if (defect_type.equals(DefectsActivity.TYPE_DEFECT_ADD) || defect_type.equals(DefectsActivity.TYPE_DEFECT_UPDATE)) {

            sp_days.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    if (isfirstTimeCallback) {
                        if (position > 0) {
                            tv_end_date.setText(getPriority(sp_days.getSelectedItemPosition() + 1));
                            et_days.setText(stringArrayList.get(sp_days.getSelectedItemPosition()) + "");
                        } else {
                            tv_end_date.setText(getPriority(1));
                            if (!isfirstTimeCallback) {
                                if (et_days.getText().equals("")) {
                                    et_days.setText(0 + "");
                                }
                            } else {
                                et_days.setText(stringArrayList.get(sp_days.getSelectedItemPosition()) + "");
                            }
                        }
                    }
                    isfirstTimeCallback = true;
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                }
            });

            et_days.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    sp_days.performClick();
                }
            });

        }

       /* et_days.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

//                isDataUpdated=true;
                if (s.length() == 0) {
//                    et_days.setText("");
                    tv_end_date.setText(getPriority(0));
                } else {
//                    if(Integer.valueOf(s.toString())>1) {
//                        et_days.setText(et_days.getText() + " Days");
//                    }else{
//                        et_days.setText(et_days.getText() + " Day");

//                    }
                    tv_end_date.setText(getPriority(Integer.valueOf(s.toString())));
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });*/


    }

    private void bindView(View bindSource) {
        et_description_text = bindSource.findViewById(R.id.description_text);
        tv_deadline = bindSource.findViewById(R.id.first_text);
        tv_art_text = bindSource.findViewById(R.id.art_text);
        tv_res_user_name_text = bindSource.findViewById(R.id.name_text);
        iv_status_red = bindSource.findViewById(R.id.iv_status_red);
        iv_status_orange = bindSource.findViewById(R.id.iv_status_orange);
        iv_status_green = bindSource.findViewById(R.id.iv_status_green);
        rl_art_view = bindSource.findViewById(R.id.rl_art_view);
        rl_trade_view = bindSource.findViewById(R.id.rl_trade_view);
        rl_users_view = bindSource.findViewById(R.id.rl_users_view);
        tv_date_1 = bindSource.findViewById(R.id.tv_date_1);
        tv_date_2 = bindSource.findViewById(R.id.tv_date_2);
        tv_date_3 = bindSource.findViewById(R.id.tv_date_3);
        tv_date_4 = bindSource.findViewById(R.id.tv_date_4);
        et_days = bindSource.findViewById(R.id.et_days);
        tv_end_date = bindSource.findViewById(R.id.tv_end_date);
        iv_end_date = bindSource.findViewById(R.id.iv_end_date);
        iv_close = bindSource.findViewById(R.id.iv_close);
        ll_priority_view = bindSource.findViewById(R.id.ll_priority_view);
        tv_company_text = bindSource.findViewById(R.id.company_text);
        et_flaw_name = bindSource.findViewById(R.id.et_flaw_name);
        sp_days = bindSource.findViewById(R.id.sp_days);
    }

    public static class DatePickerFragment extends DialogFragment
            implements DatePickerDialog.OnDateSetListener {

        TextView tvLabel;
        TextView et_days;
        int gPos = 0;
        String type = "";

        public DatePickerFragment(TextView tvLabel, TextView et_days) {
            this.tvLabel = tvLabel;
            this.et_days = et_days;

        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            final Calendar c = Calendar.getInstance();
            int year = c.get(Calendar.YEAR);
            int month = c.get(Calendar.MONTH);
            int day = c.get(Calendar.DAY_OF_MONTH);
            DatePickerDialog dialog = new DatePickerDialog(getActivity(), this, year, month, day);
//            dialog.getDatePicker().setMaxDate(c.getTimeInMillis());
            return dialog;
        }

        public void onDateSet(DatePicker view, int year, int month, int day) {

            isDataUpdated = true;
            final Calendar c = Calendar.getInstance();
            int yearCurrent = c.get(Calendar.YEAR);
            int monthCurrent = c.get(Calendar.MONTH);
            int dayCurrent = c.get(Calendar.DAY_OF_MONTH);
            int hours = c.get(Calendar.HOUR_OF_DAY);
            int minutes = c.get(Calendar.MINUTE);
            int seconds = c.get(Calendar.SECOND);

            isDateSelected.postValue(true);
            this.tvLabel.setText(formatedDate(year, (month + 1), day, hours, minutes, seconds));

            noOfDays = noOfDays(formatedDate(yearCurrent, (monthCurrent + 1), dayCurrent), formatedDate(year, (month + 1), day), defect_type);
//            String temp = Math.abs(noOfDays) + "";
            this.et_days.setText(noOfDays + "");

        }
    }

    private static int noOfDays(String dateBeforeString, String dateAfterString, String defect_type) {
        int noOfDay;
        float daysBetween = 0.0f;
        SimpleDateFormat myFormat = null;
        String type = "";
//        if (defect_type.equalsIgnoreCase(DefectsActivity.TYPE_DEFECT_ADD)) {
//            myFormat = new SimpleDateFormat("yyyy/MM/dd");
//        } else {
//        myFormat = new SimpleDateFormat("yyyy-MM-dd");

//        }
        String before = dateBeforeString.replace("-", ".");
        String after = dateAfterString.replace("-", ".");
        if (sharedPrefsManager.getStringValue(AppConstantsManager.APP_LANGUAGE, "de").equals("de")) {
            simpleDateFormat = new SimpleDateFormat("dd.MM.yyyy");

        } else {
            simpleDateFormat = new SimpleDateFormat("yyyy.MM.dd");
        }

        try {

            Date dateBefore = simpleDateFormat.parse(before);
            Date dateAfter = simpleDateFormat.parse(after);
            long difference = dateAfter.getTime() - dateBefore.getTime();
            daysBetween = (difference / (1000 * 60 * 60 * 24));
            /* You can also convert the milliseconds to days using this method
             * float daysBetween =
             *         TimeUnit.DAYS.convert(difference, TimeUnit.MILLISECONDS)
             */
            System.out.println("Number of Days between dates: " + daysBetween);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return (int) daysBetween;
    }

    private static String formatedDate(int year, int month, int day) {
        String formatedDate = "";
        if (sharedPrefsManager.getStringValue(AppConstantsManager.APP_LANGUAGE, "de").equals("de")) {

            formatedDate = day + "." + month + "." + year;
        } else {
            formatedDate = year + "." + month + "." + day;
        }
        return formatedDate;
    }

    private static String formatedDate(int year, int month, int day, int hours, int minutes, int seconds) {
        String formatedDate = "";
        if (sharedPrefsManager.getStringValue(AppConstantsManager.APP_LANGUAGE, "de").equals("de")) {

            formatedDate = day + "." + month + "." + year + " " + hours + ":" + minutes + ":" + seconds;
        } else {
            formatedDate = year + "-" + month + "-" + day + " " + hours + ":" + minutes + ":" + seconds;
        }
        return formatedDate;
    }

    private String getPriority(Integer days) {

        String dateInString = "2011-11-30";  // Start date
        SimpleDateFormat simpleDateFormat2;
        if (sharedPrefsManager.getStringValue(AppConstantsManager.APP_LANGUAGE, "de").equals("de")) {
            simpleDateFormat2 = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
        } else {
            simpleDateFormat2 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        }

        Date date = null;
        Calendar c = Calendar.getInstance();
//                try {
//                    c.setTime(sdf.parse(dateInString));
//                } catch (ParseException e) {
//                    e.printStackTrace();
//                }
        c.add(Calendar.DATE, days);
        //     sdf = new SimpleDateFormat("MM/dd/yyyy");
        Date resultdate = new Date(c.getTimeInMillis());
        dateInString = simpleDateFormat2.format(resultdate);
        System.out.println("String date:" + dateInString);
        fristDateToSave = dateInString;
        return dateInString.split(" ")[0];
    }

    private void populateDefectDetail(DefectsModel defectsModel) {
        if (defectsModel != null) {
            defectsModelObj = defectsModel;

            if (defectsModel.getDefectType() != null && !defectsModel.getDefectType().equals("")) {
                if (defectsModel.getDefectType().equals("1")) {
                    tv_art_text.setText(getActivity().getResources().getString(R.string.mangel_art));
                } else if (defectsModel.getDefectType().equals("2")) {
                    tv_art_text.setText(getActivity().getResources().getString(R.string.restleistung_art));
                }
            } else {
//                tv_art_text.setText(sharedPrefsManager.getStringValue(AppConstantsManager.USER_LAST_USED_FLAW_TYPE,""));
                String flawType = sharedPrefsManager.getStringValue(AppConstantsManager.USER_LAST_USED_FLAW_TYPE, "1");
                defectsModelObj.setDefectType(flawType);
                if (flawType.equals("1")) {
                    tv_art_text.setText(getActivity().getResources().getString(R.string.mangel_art));
                } else if (flawType.equals("2")) {
                    tv_art_text.setText(getActivity().getResources().getString(R.string.restleistung_art));
                } else {
                    tv_art_text.setText(getActivity().getResources().getString(R.string.mangel_art));
                    sharedPrefsManager.setStringValue(AppConstantsManager.USER_LAST_USED_FLAW_TYPE, "1");

                }
            }

            if (defectsModel.getDescription() != null) {

                et_description_text.setText(defectsModel.getDescription());
            }

            if (defectsModel.getDefectName() != null) {

                if (defectsModel.getDefectName().equals("")) {
                    if (photoId != null && !photoId.equals("")) {
                        ProjectsDatabase projectsDatabase = ProjectsDatabase.getDatabase(getActivity());
                        reterivePhotoDesAsyncTask = new ReterivePhotoDesAsyncTask(projectsDatabase.photoDao()).execute(projectId, photoId);
                    }
                } else
                    et_flaw_name.setText(defectsModel.getDefectName());
            }
            if (defectsModel.getFristDate() != null) {
                if (defectsModel.getFristDate().equals("0000-00-00") || defectsModel.getFristDate().equals("0000-00-00 00:00:00")) {
                    tv_end_date.setText("2001.01.01 00:00:00");
                } else {
//                    tv_end_date.setText(defectsModel.getFristDate());
                    tv_end_date.setText(getDateAccordingToLanguage(defectsModel.getFristDate()));
                }

            }


            if (defectsModel.getDiscipline() != null) {
                tv_company_text.setText(defectsModel.getDiscipline());
            } else {
                if (!sharedPrefsManager.getStringValue(AppConstantsManager.USER_LAST_USED_GEWERK_VALUES, "").equals("")) {
                    tv_company_text.setText(sharedPrefsManager.getStringValue(AppConstantsManager.USER_LAST_USED_GEWERK_VALUES, ""));
                    defectsModel.setDiscipline(sharedPrefsManager.getStringValue(AppConstantsManager.USER_LAST_USED_GEWERK_VALUES, ""));
                    defectsModel.setDiscipline_id(sharedPrefsManager.getStringValue(AppConstantsManager.USER_LAST_USED_GEWERK_IDs, ""));
                }
                tv_company_text.setText(defectsModel.getDiscipline());
            }


            if (!defect_type.equals("")) {

                if (defect_type.equals(DefectsActivity.TYPE_DEFECT_ADD)) {
                    ll_priority_view.setVisibility(View.VISIBLE);

                    final Calendar c = Calendar.getInstance();
                    int yearCurrent = c.get(Calendar.YEAR);
                    int monthCurrent = c.get(Calendar.MONTH);
                    int dayCurrent = c.get(Calendar.DAY_OF_MONTH);
                    int hours = c.get(Calendar.HOUR_OF_DAY);
                    int minutes = c.get(Calendar.MINUTE);
                    int seconds = c.get(Calendar.SECOND);


                    tv_end_date.setText(formatedDate(yearCurrent, (monthCurrent + 1), dayCurrent, hours, minutes, seconds));
                    et_days.setText("0");
                    tv_date_1.performClick();
                    if (!sharedPrefsManager.getStringValue(AppConstantsManager.USER_LAST_USED_GEWERK_VALUES, "").equals("")) {
                        tv_company_text.setText(sharedPrefsManager.getStringValue(AppConstantsManager.USER_LAST_USED_GEWERK_VALUES, ""));
                        defectsModel.setDiscipline(sharedPrefsManager.getStringValue(AppConstantsManager.USER_LAST_USED_GEWERK_VALUES, ""));
                        defectsModel.setDiscipline_id(sharedPrefsManager.getStringValue(AppConstantsManager.USER_LAST_USED_GEWERK_IDs, ""));
                    }
                    tv_company_text.setText(defectsModel.getDiscipline());

                }
                if (defect_type.equals(DefectsActivity.TYPE_DEFECT_UPDATE)) {
                    ll_priority_view.setVisibility(View.GONE);

                    if (defectsModel.fristDate != null && !defectsModel.fristDate.equals("") && !defectsModel.fristDate.contains("0000")) {
                        final Calendar c = Calendar.getInstance();

                        if (defectsModel.fristDate != null && !defectsModel.fristDate.equals("")) {
                            try {
                                Date date1 = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").parse(defectsModel.fristDate);
//                                Date date1 = new SimpleDateFormat("dd.MM.yyyy").parse(defectsModel.fristDate);
                                c.setTime(date1);
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }
                        }
                        int yearCurrent = c.get(Calendar.YEAR);
                        int monthCurrent = c.get(Calendar.MONTH);
                        int dayCurrent = c.get(Calendar.DAY_OF_MONTH);

                        fristDateToSave = defectsModel.fristDate;
                        noOfDays = noOfDays(yearCurrent + "." + (monthCurrent + 1) + "." + dayCurrent, defectsModel.fristDate, defect_type);
//                        String temp = Math.abs(noOfDays) + "";
                        et_days.setText(noOfDays +"");//billal

                    } else {
                        if (defectsModel.fristdate_df == 0 && defectsModel.fristDate.equals("")) {
                            et_days.setText("3");
                            tv_end_date.setText(getPriority(3));
//                            et_days.setText(stringArrayList.get(sp_days.getSelectedItemPosition()) + "");
                        } else
                            et_days.setText("0");
                    }

                    if (defectsModel.fristdate_df > 0) {
//                tv_end_date.setText(defectsModel.getFristDate());

                        if (sharedPrefsManager.getStringValue(AppConstantsManager.APP_LANGUAGE, "de").equals("de")) {
//                            simpleDateFormat = new SimpleDateFormat("dd.MM.yyyy");
                            simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy");
                        } else {
//                            simpleDateFormat = new SimpleDateFormat("yyyy.MM.dd");
                            simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
                        }

                        Date date = null;
                        date = new Date(defectsModel.fristdate_df);
//                        tv_end_date.setText(simpleDateFormat.format(date));
                        tv_end_date.setText(getDateAccordingToLanguage(simpleDateFormat.format(date)));
                    }


                } else if (defect_type.equals(DefectsActivity.TYPE_DEFECT_VIEW)) {
                    iv_status_orange.setEnabled(false);

                    iv_status_red.setEnabled(false);
                    iv_status_green.setEnabled(false);
                    et_days.setEnabled(false);
                    tv_end_date.setEnabled(false);
                    iv_end_date.setEnabled(false);
                    rl_trade_view.setEnabled(false);
                    rl_art_view.setEnabled(false);
                    rl_users_view.setEnabled(false);
                    et_flaw_name.setEnabled(false);
                    et_days.setEnabled(false);
                    et_description_text.setEnabled(false);
                    iv_close.setVisibility(View.INVISIBLE);

                    ll_priority_view.setVisibility(View.GONE);

                    if (defectsModel.fristDate != null && !defectsModel.fristDate.equals("")) {
                        final Calendar c = Calendar.getInstance();
                        int yearCurrent = c.get(Calendar.YEAR);
                        int monthCurrent = c.get(Calendar.MONTH);
                        int dayCurrent = c.get(Calendar.DAY_OF_MONTH);

                        noOfDays = noOfDays(yearCurrent + "." + (monthCurrent + 1) + "." + dayCurrent, defectsModel.fristDate, defect_type);
//                        String temp = Math.abs(noOfDays) + "";
                        et_days.setText(noOfDays + "");
                    }
                }
            }

            if (defectsModel.getStatus() != null && defectsModel.getStatus().equalsIgnoreCase("1")) {
                iv_status_orange.setImageResource(R.drawable.yellow_circle_selected);
            } else if (defectsModel.getStatus() != null && defectsModel.getStatus().equalsIgnoreCase("2")) {
                iv_status_red.setImageResource(R.drawable.red_circle_selected);
            } else if (defectsModel.getStatus() != null && defectsModel.getStatus().equalsIgnoreCase("0")) {
                iv_status_green.setImageResource(R.drawable.green_circle_selected);
            }
            if (defectsModel.getResponsibleUser() == null || defectsModel.getResponsibleUser().equals("")) {
                defectsModel.setResponsibleUser(sharedPrefsManager.getStringValue(AppConstantsManager.PD_USER_ID, ""));
                defectsModelObj.setResponsibleUser(sharedPrefsManager.getStringValue(AppConstantsManager.PD_USER_ID, ""));
            }

            if (defectsModel.getResponsibleUser() != null && !defectsModel.getResponsibleUser().equals("")) {
                retriveProjectUsersAsyncTask = new RetriveProjectUsersAsyncTask(projectDetailRepository.getWordDao()).executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, projectId, defectsModel.getResponsibleUser());
            }

            if (sharedPrefsManager.getStringValue(AppConstantsManager.APP_LANGUAGE, "de").equals("de")) {
                simpleDateFormat = new SimpleDateFormat("dd.MM.yyyy");

            } else {
                simpleDateFormat = new SimpleDateFormat("yyyy.MM.dd");
            }
            Date date = null;
            if (defectsModel != null) {

                date = new Date(defectsModel.getCreateDate_df());
                if (defectsModel.getCreated() != null && defectsModel.getCreated().contains("0000")) {
                    String[] strDate = defectsModel.getCreated().split(" ");
                    if (strDate.length == 2) {
                        tv_deadline.setText(strDate[0]);

                    } else {
                        tv_deadline.setText(defectsModel.getCreated());
                    }
                } else
                    tv_deadline.setText(simpleDateFormat.format(date));

            }

            if (defectsModel.getDefectDate() != null && !defectsModel.getDefectDate().equals("")
                    && defectsModel.fristDate != null && !defectsModel.fristDate.equals("") && !defectsModel.fristDate.contains("0000") && !defectsModel.getCreated().contains("0000")) {
                noOfDays = noOfDaysTimeStamp(defectsModel.createDate_df, defectsModel.fristdate_df, defect_type);
//                String []str=defectsModel.getDefectDate().split(" ");
//
//                if(str.length>1) {
//                    String strResult=str[0].replace("-",".");
//
//                    noOfDays = noOfDays(strResult, defectsModel.fristDate, defect_type);
//                }else
//                    noOfDays = noOfDays(defectsModel.getDefectDate(), defectsModel.fristDate, defect_type);

//                        String temp = Math.abs(noOfDays) + "";
                et_days.setText(noOfDays + "");
              //  sdfas
            } else {
                if (defectsModel.fristdate_df == 0 && defectsModel.fristDate.equals("")) {
                    et_days.setText("3");
                    tv_end_date.setText(getPriority(3));
//                            et_days.setText(stringArrayList.get(sp_days.getSelectedItemPosition()) + "");
                } else
                    et_days.setText("0");
            }
            if (defectsModel.getDefectDate() != null && !defectsModel.getDefectDate().equals("")) {
                long defect_date = Utils.convertStringToTimestamp(defectsModel.getDefectDate()).getTime();
                date = new Date(defect_date);
                if (defectsModel.getCreated() != null && defectsModel.getCreated().contains("0000")) {
                    String[] strDate = defectsModel.getCreated().split(" ");
                    if (strDate.length == 2) {
                        tv_deadline.setText(strDate[0]);

                    } else {
                        tv_deadline.setText(defectsModel.getCreated());
                    }
                } else
                    tv_deadline.setText(simpleDateFormat.format(date));
            }
        }
    }

    private class RetriveProjectUsersAsyncTask extends AsyncTask<String, Void, ProjectUserModel> {

        private ProjectUsersDao mAsyncTaskDao;

        RetriveProjectUsersAsyncTask(ProjectUsersDao dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        protected ProjectUserModel doInBackground(final String... params) {
            ProjectUserModel stringList = mAsyncTaskDao.getProjectUserInfo(params[0], params[1]);
            return stringList;
        }

        @Override
        protected void onPostExecute(ProjectUserModel params) {
            super.onPostExecute(params);

            if (getActivity() == null)
                return;

            if (params != null) {

                if (params.getFirstname() != null && !params.getLastname().equals("")) {
                    SharedPrefsManager sharedPrefsManager = new SharedPrefsManager(getActivity());
                    String firstName = params.getFirstname();
                    String lastName = params.getLastname();
// String firstName = sharedPrefsManager.getStringValue(AppConstantsManager.USER_FIRST_NAME, "");
//                    String lastName = sharedPrefsManager.getStringValue(AppConstantsManager.USER_LAST_NAME, "");

//                    if (params.getLastname() != null && !params.getLastname().equals("")) {
//                        tv_res_user_name_text.setText(params.getFirstname() + " " + params.getLastname());
//                    } else
//                        tv_res_user_name_text.setText(params.getFirstname());

                    if (firstName != null && !lastName.equals("")) {
                        tv_res_user_name_text.setText(firstName + " " + lastName);
                    } else
                        tv_res_user_name_text.setText(lastName);

                }


            }

        }


    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

    }

    @Override
    public void onDetach() {
        super.onDetach();

    }

    @Override
    public void onDestroy() {
        if (retriveGewerkAsyncTask != null)
            retriveGewerkAsyncTask.cancel(true);

        if (retriveDistinctProjectUsersAsyncTask != null)
            retriveDistinctProjectUsersAsyncTask.cancel(true);

        if (reterivePhotoDesAsyncTask != null)
            reterivePhotoDesAsyncTask.cancel(true);

        if (retriveProjectUsersAsyncTask != null)
            retriveProjectUsersAsyncTask.cancel(true);



        super.onDestroy();
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onLcoalPhotoFragmentInteraction(Uri uri);
    }

    private void loadFlawType() {
        groupheadingMangelList.clear();
        List<ChildRowModel> childRowModelList = new ArrayList<>();
        ChildRowModel childRowModel = new ChildRowModel();
        ChildRowModel childRowModel2 = new ChildRowModel();
        childRowModel.setId("1");
        childRowModel.setTitle(getResources().getString(R.string.mangel_art));

        childRowModel2.setId("2");
        childRowModel2.setTitle(getResources().getString(R.string.restleistung_art));

        childRowModelList.add(childRowModel);
        childRowModelList.add(childRowModel2);

        groupheadingMangelList.add(new GroupheadingModel(getResources().getString(R.string.heading_art), getResources().getString(R.string.heading_art), false, new HashMap<String, List<ChildRowModel>>() {{
            put(getResources().getString(R.string.heading_art), childRowModelList);
        }}));
//        if (defect_type.equals(DefectsActivity.TYPE_DEFECT_UPDATE) && defectsModelObj.getDefectType() != null) {

        groupheadingMangelList.get(0).getListChildDataSelected().add(defectsModelObj.getDefectType());
//        }
        showCustomListDialog(getActivity(), groupheadingMangelList, getResources().getString(R.string.heading_art));
    }

    private void loadTrades() {
        retriveGewerkAsyncTask = new RetriveGewerkAsyncTask(defectTradesRepository.getmDefectsTradeDao()).execute();
    }

    private void loadUsers() {
        retriveDistinctProjectUsersAsyncTask = new RetriveDistinctProjectUsersAsyncTask(projectDetailRepository.getWordDao()).execute();
    }

    private class RetriveGewerkAsyncTask extends AsyncTask<Void, Void, List<DefectTradeModel>> {

        private DefectsTradesDao mAsyncTaskDao;

        RetriveGewerkAsyncTask(DefectsTradesDao dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        protected List<DefectTradeModel> doInBackground(final Void... params) {
            List<DefectTradeModel> listOfGewerk = mAsyncTaskDao.getUniqueDefect(projectId);
            return listOfGewerk;
        }

        @Override
        protected void onPostExecute(List<DefectTradeModel> defectTradeModelList) {
            List<ChildRowModel> childRowModelList = new ArrayList<>();

            for (int i = 0; i < defectTradeModelList.size(); i++) {
                for (int j = i + 1; j < defectTradeModelList.size(); j++) {
                    if (defectTradeModelList.get(i).getPdservicetitle() != null && defectTradeModelList.get(i).getPdservicetitle().equals(defectTradeModelList.get(j).getPdservicetitle())) {
                        defectTradeModelList.remove(j);
                        j--;
                    } else {
                        if (defectTradeModelList.get(i).getPdservicetitle() == null || defectTradeModelList.get(i).getPdservicetitle().equalsIgnoreCase("")) {
                            defectTradeModelList.remove(j);
                            j--;
                        }
                    }
                }
            }

            for (int i = 0; i < defectTradeModelList.size(); i++) {
                ChildRowModel childRowModel = new ChildRowModel();
                DefectTradeModel defectTradeModel = defectTradeModelList.get(i);
//                if(defectTradeModelList.get(i)!=null&&defectTradeModelList.get(i).getCompany()!=null)
//                childRowModel.setTitle(defectTradeModel.getPdservicetitle()+" ["+ defectTradeModel.getServicenumber()+"]"+" - "+defectTradeModelList.get(i).getCompany());
//                else
//                    childRowModel.setTitle(defectTradeModel.getPdservicetitle()+" ["+ defectTradeModel.getServicenumber()+"]");
                childRowModel.setTitle(defectTradeModel.getPdservicetitle());
                childRowModel.setId(defectTradeModel.getSelectvalue());
//                childRowModel.setId(defectTradeModel.getPdservicearea_id());
                if (defectTradeModel.getPdservicetitle() != null && !defectTradeModel.getPdservicetitle().equals(""))
                    childRowModelList.add(childRowModel);

            }
            groupheadingGewerkList.add(new GroupheadingModel(getResources().getString(R.string.heading_gewerk), getResources().getString(R.string.heading_gewerk), true, new HashMap<String, List<ChildRowModel>>() {{
                put(getResources().getString(R.string.heading_gewerk), childRowModelList);
            }}));
            if (defectsModelObj.getDiscipline_id() != null) {
                if (!defectsModelObj.getDiscipline_id().equals("")) {
                    List<String> items = Arrays.asList(defectsModelObj.getDiscipline_id().split("\\s*,\\s*"));

                    groupheadingGewerkList.get(0).getListChildDataSelected().addAll(items);
                }
            }
            isTradesLoaded = true;
            showCustomListDialog(getActivity(), groupheadingGewerkList, getResources().getString(R.string.heading_gewerk));
        }

    }

    private class RetriveDistinctProjectUsersAsyncTask extends AsyncTask<Void, Void, List<ProjectUserModel>> {

        private ProjectUsersDao mAsyncTaskDao;

        RetriveDistinctProjectUsersAsyncTask(ProjectUsersDao dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        protected List<ProjectUserModel> doInBackground(final Void... params) {
            List<ProjectUserModel> stringList = mAsyncTaskDao.getDistinctProjectUserListInfo();
            return stringList;
        }

        @Override
        protected void onPostExecute(List<ProjectUserModel> params) {
            super.onPostExecute(params);
            if (getActivity() == null)
                return;
            List<ChildRowModel> childRowModelList = new ArrayList<>();
            for (int i = 0; i < params.size(); i++) {
                ChildRowModel childRowModel = new ChildRowModel();
                childRowModel.setId(params.get(i).getPduserid());
                childRowModel.setTitle(params.get(i).getFirstname() + " " + params.get(i).getLastname());

                childRowModelList.add(childRowModel);
            }


            groupheadingProjectUsersList.add(new GroupheadingModel(getResources().getString(R.string.heading_responsible), getResources().getString(R.string.heading_responsible), false, new HashMap<String, List<ChildRowModel>>() {{
                put(getResources().getString(R.string.heading_responsible), childRowModelList);
            }}));
//            if (defect_type.equals(DefectsActivity.TYPE_DEFECT_UPDATE) && defectsModelObj.getResponsibleUser() != null) {
//
            groupheadingProjectUsersList.get(0).getListChildDataSelected().add(defectsModelObj.getResponsibleUser());
//            }
            isUsersLoaded = true;
            showCustomListDialog(getActivity(), groupheadingProjectUsersList, getResources().getString(R.string.heading_responsible));
        }
    }

    String listType = "";

    public void showCustomListDialog(final Activity act, List<GroupheadingModel> groupheadingModels, String type) {
        //customDialog = new Dialog(act, R.style.customDialogTheme);

        listType = type;
        customDialog = new Dialog(act, R.style.MyDialogTheme);
        customDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        customDialog.setContentView(R.layout.defect_custom_expandable_dialog_view);

        customDialog.setCanceledOnTouchOutside(false);
        WindowManager.LayoutParams params = customDialog.getWindow().getAttributes();
        params.y = 5;

        ExpandableListView expandableListView = customDialog.findViewById(R.id.simple_expandable_listview);
        TextView tvFilter = customDialog.findViewById(R.id.tv_reset_filter);
        TextView tvDone = customDialog.findViewById(R.id.tv_done);
        tvFilter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isDataUpdated = true;
                if (listType.equals(getResources().getString(R.string.heading_art))) {
                    if (groupheadingMangelList.size() > 0) {
                        groupheadingMangelList.get(0).getListChildDataSelected().clear();
                    }
                } else if (listType.equals(getResources().getString(R.string.heading_gewerk))) {
                    if (groupheadingGewerkList.size() > 0) {
                        groupheadingGewerkList.get(0).getListChildDataSelected().clear();
                    }
                } else if (listType.equals(getResources().getString(R.string.heading_responsible))) {
                    if (groupheadingProjectUsersList.size() > 0) {
                        groupheadingProjectUsersList.get(0).getListChildDataSelected().clear();
                    }
                }

                if (expandableListAdapter != null) {
                    expandableListAdapter.notifyDataSetChanged();

                }
            }
        });
        tvDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isDataUpdated = true;
                if (listType.equals(getResources().getString(R.string.heading_art))) {

                    if (groupheadingMangelList.size() > 0 && groupheadingMangelList.get(0).getListChildDataSelected().size() > 0) {
                        String responUser = groupheadingMangelList.get(0).getListChildDataSelected().get(0);
                        defectsModelObj.setDefectType(responUser);


                        List<ChildRowModel> childRowModelList = groupheadingMangelList.get(0).getListChildData().get(getResources().getString(R.string.heading_art));
                        for (int i = 0; i < childRowModelList.size(); i++) {
                            if (childRowModelList.get(i).getId().equals(responUser)) {
                                tv_art_text.setText(childRowModelList.get(i).getTitle());
                                sharedPrefsManager.setStringValue(AppConstantsManager.USER_LAST_USED_FLAW_TYPE, responUser);
                                break;

                            }
                        }
                    } else {
                        defectsModelObj.setDefectType("");
                        tv_art_text.setText("");
                    }
                } else if (listType.equals(getResources().getString(R.string.heading_gewerk))) {

                    if (groupheadingGewerkList.size() > 0 && groupheadingGewerkList.get(0).getListChildDataSelected().size() > 0) {
                        String responUser = groupheadingGewerkList.get(0).getListChildDataSelected().get(0);
                        StringBuilder sb = new StringBuilder();
                        StringBuilder sb_ids = new StringBuilder();
                        int counter = 0;
                        List<ChildRowModel> childRowModelList = groupheadingGewerkList.get(0).getListChildData().get(getResources().getString(R.string.heading_gewerk));
                        List<String> stringList = groupheadingGewerkList.get(0).getListChildDataSelected();
                        //bug continue
                        for (int j = 0; j < stringList.size(); j++) {

                            for (int i = 0; i < childRowModelList.size(); i++) {

                                if (stringList.get(j).equals(childRowModelList.get(i).getId())) {
                                    counter++;

                                    String str_title = "";
                                    String str_ids = "";
                                    if (stringList.size() == counter) {
                                        str_title = childRowModelList.get(i).getTitle();
                                        str_ids = childRowModelList.get(i).getId();
                                    } else {
                                        str_title = childRowModelList.get(i).getTitle() + ", ";
                                        str_ids = childRowModelList.get(i).getId() + ", ";
                                    }
                                    sb.append(str_title);
                                    sb_ids.append(str_ids);

                                }
                            }
                        }
                        defectsModelObj.setDiscipline(sb.toString());
                        defectsModelObj.setDiscipline_id(sb_ids.toString());
                        sharedPrefsManager.setStringValue(AppConstantsManager.USER_LAST_USED_GEWERK_VALUES, sb.toString());
                        sharedPrefsManager.setStringValue(AppConstantsManager.USER_LAST_USED_GEWERK_IDs, sb_ids.toString());

                        tv_company_text.setText(sb.toString());
                    } else {
                        defectsModelObj.setDiscipline("");
                        defectsModelObj.setDiscipline_id("");
                        tv_company_text.setText("");
                    }
                } else if (listType.equals(getResources().getString(R.string.heading_responsible))) {

                    if (groupheadingProjectUsersList.size() > 0 && groupheadingProjectUsersList.get(0).getListChildDataSelected().size() > 0) {
                        String responUser = groupheadingProjectUsersList.get(0).getListChildDataSelected().get(0);

                        defectsModelObj.setResponsibleUser(responUser);
                        List<ChildRowModel> childRowModelList = groupheadingProjectUsersList.get(0).getListChildData().get(getResources().getString(R.string.heading_responsible));
                        for (int i = 0; i < childRowModelList.size(); i++) {
                            if (childRowModelList.get(i).getId().equals(responUser)) {
                                tv_res_user_name_text.setText(childRowModelList.get(i).getTitle());
                            }
                        }

                    } else {
                        defectsModelObj.setResponsibleUser("");
                        tv_res_user_name_text.setText("");
                    }
                }
                customDialog.dismiss();
            }
        });

        expandableListAdapter = new ExpandableListDefectDetailDialogAdapter(act, groupheadingModels);
        expandableListView.setAdapter(expandableListAdapter);
        expandableListView.expandGroup(0);
        expandableListView.setOnGroupClickListener(new ExpandableListView.OnGroupClickListener() {
            @Override
            public boolean onGroupClick(ExpandableListView parent, View v,
                                        int groupPosition, long id) {
                return true; // This way the expander cannot be collapsed
            }
        });
        expandableListView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View v, int groupPosition,
                                        int childPosition, long id) {
                isDataUpdated = true;
                isTradeChildClicked = true;
                if (listType.equals(getResources().getString(R.string.heading_art))) {


                    if (groupheadingMangelList.get(groupPosition).isMultiSelect()) {
                        ChildRowModel childRowModel = groupheadingMangelList.get(groupPosition).getListChildData().get(groupheadingMangelList.get(groupPosition).getType()).get(childPosition);
                        if (groupheadingMangelList.get(groupPosition).getListChildDataSelected() != null) {
                            if (groupheadingMangelList.get(groupPosition).getListChildDataSelected().contains(childRowModel.getId())) {
                                groupheadingMangelList.get(groupPosition).getListChildDataSelected().remove(childRowModel.getId());

                            } else {
                                groupheadingMangelList.get(groupPosition).getListChildDataSelected().add(childRowModel.getId());
                            }
                        }
                    } else {

                        ChildRowModel childRowModel = groupheadingMangelList.get(groupPosition).getListChildData().get(groupheadingMangelList.get(groupPosition).getType()).get(childPosition);
                        if (groupheadingMangelList.get(groupPosition).getListChildDataSelected() != null) {

                            if (groupheadingMangelList.get(groupPosition).getListChildDataSelected().size() > 0) {
                                if (groupheadingMangelList.get(groupPosition).getListChildDataSelected().get(0).equals(childRowModel.getId())) {
                                    groupheadingMangelList.get(groupPosition).getListChildDataSelected().remove(childRowModel.getId());
                                } else {
                                    groupheadingMangelList.get(groupPosition).getListChildDataSelected().set(0, childRowModel.getId());

                                }
                            } else {
                                groupheadingMangelList.get(groupPosition).getListChildDataSelected().add(childRowModel.getId());
                            }
                        }

                    }
                } else if (listType.equals(getResources().getString(R.string.heading_gewerk))) {

                    if (groupheadingGewerkList.get(groupPosition).isMultiSelect()) {
                        ChildRowModel childRowModel = groupheadingGewerkList.get(groupPosition).getListChildData().get(groupheadingGewerkList.get(groupPosition).getType()).get(childPosition);
                        if (groupheadingGewerkList.get(groupPosition).getListChildDataSelected() != null) {
                            if (groupheadingGewerkList.get(groupPosition).getListChildDataSelected().contains(childRowModel.getId())) {
                                groupheadingGewerkList.get(groupPosition).getListChildDataSelected().remove(childRowModel.getId());

                            } else {
                                groupheadingGewerkList.get(groupPosition).getListChildDataSelected().add(childRowModel.getId());
                            }
                        }
                    } else {
                        ChildRowModel childRowModel = groupheadingGewerkList.get(groupPosition).getListChildData().get(groupheadingGewerkList.get(groupPosition).getType()).get(childPosition);
                        if (groupheadingGewerkList.get(groupPosition).getListChildDataSelected() != null) {

                            if (groupheadingGewerkList.get(groupPosition).getListChildDataSelected().size() > 0) {
                                if (groupheadingGewerkList.get(groupPosition).getListChildDataSelected().get(0).equals(childRowModel.getId())) {
                                    groupheadingGewerkList.get(groupPosition).getListChildDataSelected().remove(childRowModel.getId());
                                } else {
                                    groupheadingGewerkList.get(groupPosition).getListChildDataSelected().set(0, childRowModel.getId());

                                }
                            } else {
                                groupheadingGewerkList.get(groupPosition).getListChildDataSelected().add(childRowModel.getId());
                            }
                        }

                    }

                } else if (listType.equals(getResources().getString(R.string.heading_responsible))) {


                    if (groupheadingProjectUsersList.get(groupPosition).isMultiSelect()) {
                        ChildRowModel childRowModel = groupheadingProjectUsersList.get(groupPosition).getListChildData().get(groupheadingProjectUsersList.get(groupPosition).getType()).get(childPosition);
                        if (groupheadingProjectUsersList.get(groupPosition).getListChildDataSelected() != null) {
                            if (groupheadingProjectUsersList.get(groupPosition).getListChildDataSelected().contains(childRowModel.getId())) {
                                groupheadingProjectUsersList.get(groupPosition).getListChildDataSelected().remove(childRowModel.getId());

                            } else {
                                groupheadingProjectUsersList.get(groupPosition).getListChildDataSelected().add(childRowModel.getId());
                            }
                        }
                    } else {
                        ChildRowModel childRowModel = groupheadingProjectUsersList.get(groupPosition).getListChildData().get(groupheadingProjectUsersList.get(groupPosition).getType()).get(childPosition);
                        if (groupheadingProjectUsersList.get(groupPosition).getListChildDataSelected() != null) {

                            if (groupheadingProjectUsersList.get(groupPosition).getListChildDataSelected().size() > 0) {
                                if (groupheadingProjectUsersList.get(groupPosition).getListChildDataSelected().get(0).equals(childRowModel.getId())) {
                                    groupheadingProjectUsersList.get(groupPosition).getListChildDataSelected().remove(childRowModel.getId());
                                } else {
                                    groupheadingProjectUsersList.get(groupPosition).getListChildDataSelected().set(0, childRowModel.getId());

                                }
                            } else {
                                groupheadingProjectUsersList.get(groupPosition).getListChildDataSelected().add(childRowModel.getId());
                            }
                        }

                    }
                }

                if (expandableListAdapter != null)
                    expandableListAdapter.notifyDataSetChanged();


                return false;

            }
        });


        customDialog.show();
    }

    public boolean saveDefect() {

        Utils.showLogger("saveDefect>>DefectDetailsDatesFragment");

        isDataUpdated = false;//while saving defect
        if (et_flaw_name.getText().toString().equals("")) {
            Toast.makeText(getContext(), getResources().getString(R.string.defect_screen_flaw_name_empty_msg), Toast.LENGTH_SHORT).show();
            return true;
        }
        if (!projectStatus.equals(""))
            defectsModelObj.setStatus(projectStatus);
        defectsModelObj.setDescription(et_description_text.getText().toString());

        defectsModelObj.setFristDate(fristDateToSave);

        defectsModelObj.setDefectName(et_flaw_name.getText().toString());

        if (tv_end_date.getText().toString() != null && !tv_end_date.getText().toString().equals("")) {

            if (sharedPrefsManager.getStringValue(AppConstantsManager.APP_LANGUAGE, "de").equals("de")) {
                simpleDateFormat = new SimpleDateFormat("dd.MM.yyyy");
            } else {
                simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
            }

            Date date = null;
            Date lFromDate1 = null;
            try {
                lFromDate1 = simpleDateFormat.parse(tv_end_date.getText().toString());
            } catch (ParseException e) {

            }
            System.out.println("gpsdate :" + lFromDate1);
//            Timestamp fromTS1 = new Timestamp(lFromDate1.getTime());
            defectsModelObj.setFristdate_df(lFromDate1.getTime());
        } else {
            defectsModelObj.setFristdate_df(Utils.getCurrentTimeStamp().getTime());
        }
        new CreateLocalDefectAsyncTask().execute();

        return  false;

    }

    private int getDays(long time) {
        Calendar cal = Calendar.getInstance(Locale.ENGLISH);
        cal.setTimeInMillis(time);
        Date today = new Date();
        long diff = today.getTime() - cal.getTime().getTime();
        int numOfDays = (int) (diff / (1000 * 60 * 60 * 24));
        int hours = (int) (diff / (1000 * 60 * 60));
        int minutes = (int) (diff / (1000 * 60));
        int seconds = (int) (diff / (1000));
        return numOfDays;
    }

    private class CreateLocalDefectAsyncTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(final Void... params) {

            ProjectsDatabase db = ProjectsDatabase.getDatabase(getActivity());
//            DefectTradesRepository mRepositoryDefecttrade = new DefectTradesRepository(getActivity(), projectId);
            defectsModelObj.setUploadStatus(DefectRepository.UN_SYNC_PHOTO);
//            Date c = Calendar.getInstance().getTime();
//            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
//            String formattedDate = df.format(c);
//            ///////////////////\
//            SimpleDateFormat utcTimeFormate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH);
//            df.setTimeZone(TimeZone.getTimeZone("UTC"));
//            Date date = null;
//            try {
//                date = df.parse(formattedDate);
//            } catch (ParseException e) {
//                e.printStackTrace();
//            }
//            utcTimeFormate.setTimeZone(TimeZone.getDefault());
//            String formattedDate1 = utcTimeFormate.format(date);
//
//            Date date1 = new Date();
//            SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
//            sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
//            Date gmt = new Date(sdf.format(date1));
//            //////////////////

            Date date1 = new Date();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");

            sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
            Date utc = new Date(sdf.format(date1));

            Calendar calendar = Calendar.getInstance();
            calendar.setTime(utc);
            calendar.add(Calendar.HOUR, 1);
            Date dateAfterOneHour = calendar.getTime();
            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            String formattedDate = df.format(dateAfterOneHour);

            defectsModelObj.setLastupdate(formattedDate);
            db.defectsDao().deleteUsingLocalDefectId(defectsModelObj.defectLocalId);
            lastInsertedDefectID = db.defectsDao().insert(defectsModelObj);
            if (getActivity() != null && getActivity() instanceof DefectDetailsActivity) {
                ((DefectDetailsActivity) getActivity()).updateDefectSyncStatus(mParam2, DefectRepository.UN_SYNC_PHOTO, false);
            } else if (getActivity() != null && getActivity() instanceof DefectDetailsActivity2) {
                ((DefectDetailsActivity2) getActivity()).updateDefectSyncStatus(mParam2, DefectRepository.UN_SYNC_PHOTO, false);
            }
            new AddLocalMangelGeewerksAsyncTask(db.defectTradeDao()).execute(defectsModelObj.getProjectId(), defectsModelObj.getDefectLocalId() + "");


            return null;
        }
    }

    private class UpdatePhotoAsyncTask extends AsyncTask<String, Void, Void> {
        private PhotoDao mAsyncTaskDao;

        UpdatePhotoAsyncTask(PhotoDao dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(final String... params) {

            List<PhotoModel> defectTradeModelListt = mAsyncTaskDao.getDefectPhotosListUsingLoalID(projectId, photoId);

            if (defectTradeModelListt != null && defectTradeModelListt.size() > 0) {

                String temp = "";
//                defectTradeModelListt.get(0).setLocal_flaw_id("");
//                if ((defectTradeModelListt.get(0).getLocal_flaw_id() == null || defectTradeModelListt.get(0).getLocal_flaw_id().equals("")) && !String.valueOf(defectsModelObj.getDefectLocalId()).equals("")) {
//                    temp = defectsModelObj.defectLocalId + "";
//                    defectTradeModelListt.get(0).setLocal_flaw_id(temp);
//                } else {
//                    temp = defectTradeModelListt.get(0).getLocal_flaw_id() + "," + defectsModelObj.defectLocalId;
//                    defectTradeModelListt.get(0).setLocal_flaw_id(temp);
//                }
                defectTradeModelListt.get(0).setLocal_flaw_id(defectsModelObj.defectLocalId + "");
                defectTradeModelListt.get(0).setDefectAdded(true);
                defectTradeModelListt.get(0).setPhotoSynced(false);
                defectTradeModelListt.get(0).setPhotoUploadStatus(LocalPhotosRepository.UN_SYNC_PHOTO);
                mAsyncTaskDao.update(defectTradeModelListt.get(0));

            }


            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            if (getActivity() != null) {

                Intent intentt = new Intent("updateFlawStatus");
                intentt.putExtra("local_flaw_id", defectsModelObj.defectLocalId);
                getActivity().sendBroadcast(intentt);

                Intent intent = new Intent(BR_ACTION_UPDATE_DEFECT_DATA);
                getActivity().sendBroadcast(intent);

                getActivity().finish();
            }

        }
    }

    private class ReterivePhotoDesAsyncTask extends AsyncTask<String, Void, List<PhotoModel>> {
        private PhotoDao mAsyncTaskDao;

        ReterivePhotoDesAsyncTask(PhotoDao dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        protected List<PhotoModel> doInBackground(final String... params) {

            List<PhotoModel> defectTradeModelListt = mAsyncTaskDao.getDefectPhotosListUsingLoalID(projectId, photoId);


            return defectTradeModelListt;
        }

        @Override
        protected void onPostExecute(List<PhotoModel> photoModels) {
            super.onPostExecute(photoModels);

            if (getActivity() != null) {

                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (photoModels != null && photoModels.size() > 0) {
                            if (photoModels.get(0) != null && photoModels.get(0).getDescription() != null)
//                                Toast.makeText(getActivity(), photoModels.get(0).getDescription(), Toast.LENGTH_SHORT).show();
                                et_flaw_name.setText(photoModels.get(0).getDescription());
                        }
                    }
                });
            }

        }
    }


    private class AddLocalMangelGeewerksAsyncTask extends AsyncTask<String, Void, Void> {
        private DefectsTradesDao mAsyncTaskDao;

        AddLocalMangelGeewerksAsyncTask(DefectsTradesDao dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(final String... params) {
            groupheadingGewerkList.add(new GroupheadingModel(getResources().getString(R.string.heading_gewerk), getResources().getString(R.string.heading_gewerk), true, new HashMap<String, List<ChildRowModel>>() {{
                put(getResources().getString(R.string.heading_gewerk), new ArrayList<>());
            }}));
            if (defectsModelObj.getDiscipline_id() != null) {
                if (!defectsModelObj.getDiscipline_id().equals("")) {
                    List<String> items = Arrays.asList(defectsModelObj.getDiscipline_id().split("\\s*,\\s*"));
                    groupheadingGewerkList.get(0).getListChildDataSelected().addAll(items);
                }
            }

            List<DefectTradeModel> defectTradeModelListt = mAsyncTaskDao.getAllDefectTradeWithLocalDefectIdList(params[0], params[1]);

            if (defectTradeModelListt != null && defectTradeModelListt.size() > 0
                    && groupheadingGewerkList != null && groupheadingGewerkList.size() > 0 && groupheadingGewerkList.get(0).getListChildDataSelected() != null) {

                for (int i = 0; i < defectTradeModelListt.size(); i++) {

                    defectTradeModelListt.get(i).setSelected(0);

                    mAsyncTaskDao.update(defectTradeModelListt.get(i));
                }
                defectTradeModelListt = mAsyncTaskDao.getAllDefectTradeWithLocalDefectIdList(params[0], params[1]);


                for (int j = 0; j < groupheadingGewerkList.get(0).getListChildDataSelected().size(); j++) {

                    for (int i = 0; i < defectTradeModelListt.size(); i++) {
                        if (defectTradeModelListt.get(i).getSelectvalue().equalsIgnoreCase(groupheadingGewerkList.get(0).getListChildDataSelected().get(j))) {

                            defectTradeModelListt.get(i).setSelected(1);
                            mAsyncTaskDao.update(defectTradeModelListt.get(i));
                            break;
                        }
                    }
                }
            }
            if (getActivity() != null) {
                getActivity().setResult(Activity.RESULT_OK);

                if (afterSaveKillActivity)
                    getActivity().finish();
                else
                    afterSaveKillActivity = true;

            }
            return null;
        }
    }

    private class RetrieveAsyncTask extends AsyncTask<Void, Void, Void> {
        private DefectsDao mAsyncTaskDao;

        RetrieveAsyncTask() {
            mAsyncTaskDao = ProjectsDatabase.getDatabase(getActivity()).defectsDao();
        }

        @Override
        protected Void doInBackground(final Void... params) {
//            setmDefectedPhotos( mAsyncTaskDao.getAllDefectPhotoModel());
            // if( getmDefectedPhotos().getValue()!=null)
            DefectsModel defectsModel = mAsyncTaskDao.getDefectsObjectt(projectId, mParam2);
            defectDetailsDatesFraagViewModel.listLiveData.postValue(defectsModel);
            return null;
        }

    }

    public void loadPhotosData() {
        new RetrieveAsyncTask().execute();
    }


    private int noOfDaysTimeStamp(long dateBeforeString, long dateAfterString, String defect_type) {
        int noOfDay;
        float daysBetween = 0.0f;
        SimpleDateFormat myFormat = null;
        String type = "";
//        if (defect_type.equalsIgnoreCase(DefectsActivity.TYPE_DEFECT_ADD)) {
//            myFormat = new SimpleDateFormat("yyyy/MM/dd");
//        } else {
//        myFormat = new SimpleDateFormat("yyyy-MM-dd");

//        }
        try {
            String before = "";
            String after = "";
            Date dateBefore = null;
            Date dateAfter = null;
//        if(dateAfterString>0&&sharedPrefsManager.getStringValue(AppConstantsManager.APP_LANGUAGE, "de").equals("de")){
            if (dateAfterString > 0) {
                if (dateAfterString > 0) {
                    Calendar cal = Calendar.getInstance(Locale.ENGLISH);
                    cal.setTimeInMillis(dateAfterString);
                    cal.set(Calendar.HOUR_OF_DAY, 0);
                    cal.set(Calendar.MINUTE, 0);
                    cal.set(Calendar.SECOND, 0);
                    cal.set(Calendar.MILLISECOND, 0);
                    dateAfter = cal.getTime();
                    after = DateFormat.format("yyyy.MM.dd", cal).toString();
                }
            }
//        if(dateBeforeString>0&&sharedPrefsManager.getStringValue(AppConstantsManager.APP_LANGUAGE, "de").equals("de")){
            if (dateBeforeString > 0) {
                if (dateBeforeString > 0) {
                    Calendar cal = Calendar.getInstance(Locale.ENGLISH);
                    cal.setTimeInMillis(dateBeforeString);
                    cal.set(Calendar.HOUR_OF_DAY, 0);
                    cal.set(Calendar.MINUTE, 0);
                    cal.set(Calendar.SECOND, 0);
                    cal.set(Calendar.MILLISECOND, 0);
                    dateBefore = cal.getTime();
                    before = DateFormat.format("yyyy.MM.dd", cal).toString();
                }
            }

            if (sharedPrefsManager.getStringValue(AppConstantsManager.APP_LANGUAGE, "de").equals("de")) {
                simpleDateFormat = new SimpleDateFormat("dd.MM.yyyy");

            } else {
                simpleDateFormat = new SimpleDateFormat("yyyy.MM.dd");
            }

            if (dateAfter != null && dateBefore != null) {
                long difference = dateAfter.getTime() - dateBefore.getTime();
                daysBetween = (difference / (1000 * 60 * 60 * 24));
                /* You can also convert the milliseconds to days using this method
                 * float daysBetween =
                 *         TimeUnit.DAYS.convert(difference, TimeUnit.MILLISECONDS)
                 */
            }
            System.out.println("Number of Days between dates: " + daysBetween);
        } catch (Exception e) {
            e.printStackTrace();
        }
//        float flCeli= (float) Math.ceil(daysBetween);
        return (int) daysBetween;
    }

    private String getDateAccordingToLanguage(String date) {
        String modifiedDate = "";
        if (date != null) {
            if (date.contains("-")) {
                if (sharedPrefsManager.getStringValue(AppConstantsManager.APP_LANGUAGE, "de").equals("de")) {
                    modifiedDate = date.replace("-", ".");
                } else {
                    modifiedDate = date;
                }
            }
        }


        return modifiedDate;
    }
}
