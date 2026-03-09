package com.projectdocupro.mobile.adapters;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.fragment.app.DialogFragment;

import com.projectdocupro.mobile.R;
import com.projectdocupro.mobile.activities.DefectsActivity;
import com.projectdocupro.mobile.models.mangel_filters.ChildRowModel;
import com.projectdocupro.mobile.models.mangel_filters.GroupheadingModel;
import com.projectdocupro.mobile.utility.Utils;

import java.util.Calendar;
import java.util.List;

//For expandable list view use BaseExpandableListAdapter
public class ExpandableListDefectDetailDialogAdapter extends BaseExpandableListAdapter {
    private static Context _context;
    private static List<GroupheadingModel> header; // header titles
    // Child data in format of header title, child title

    // 4 Child types
    private static final int CHILD_TYPE_1 = 0;
    private static final int CHILD_TYPE_2 = 1;
    private static final int CHILD_TYPE_3 = 2;
    private static final int CHILD_TYPE_UNDEFINED = 3;


    private static final String STATUS = "Status";
    private static final String DEADLINE = "Deadline";
    private static final String DATE = "Date";

    private static final String START_DATE = "start_date";
    private static final String END_DATE = "end_date";

    public ExpandableListDefectDetailDialogAdapter(Context context, List<GroupheadingModel> listDataHeader) {
        this._context = context;
        this.header = listDataHeader;

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
                case CHILD_TYPE_1:
                    convertView = inflater.inflate(R.layout.filter_defect_status_row_item, null);
                    convertView.setTag(childType);
                    break;
                case CHILD_TYPE_2:
                    convertView = inflater.inflate(R.layout.filter_defect_normal_row_item_2, null);
                    convertView.setTag(childType);
                    break;
                case CHILD_TYPE_3:
                    convertView = inflater.inflate(R.layout.filter_defect_date_row_item, null);
                    convertView.setTag(childType);
                    break;
                case CHILD_TYPE_UNDEFINED:
                    convertView = inflater.inflate(R.layout.filter_defect_deadline_date_row_item, null);
                    convertView.setTag(childType);
                    break;
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
            case CHILD_TYPE_1:
                ImageView iv_status = (ImageView) convertView.findViewById(R.id.iv_status);
                ImageView iv_selection = (ImageView) convertView.findViewById(R.id.iv_selected_word);
                TextView tv_title = (TextView) convertView.findViewById(R.id.sub_title);

                tv_title.setText(childRowModel.getTitle());
                if (childRowModel.getId().equals("0")) {
                    iv_status.setImageDrawable(_context.getResources().getDrawable(R.drawable.green_circle_background));
//                    iv_status.setVisibility(View.GONE);
                } else if (childRowModel.getId().equals("1")) {
                    iv_status.setImageDrawable(_context.getResources().getDrawable(R.drawable.orange_circle_background));
//                    iv_status.setVisibility(View.GONE);

                } else if (childRowModel.getId().equals("2")) {
                    iv_status.setImageDrawable(_context.getResources().getDrawable(R.drawable.red_circle_background));
//                    iv_status.setVisibility(View.GONE);

                }
                if (selectedIDList != null && childRowModel.getId() != null && selectedIDList.contains(childRowModel.getId())) {
                    iv_selection.setBackgroundResource(R.drawable.ic_selected_word);

                } else {
                    iv_selection.setBackgroundResource(R.drawable.ic_unselected_filter);

                }

                break;
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
                header.get(groupPosition).setStart_date(Utils.convertStringToTimestamp(c_date.get(Calendar.YEAR) + "-" + (c_date.get(Calendar.MONTH) + 1) + "-" + c_date.get(Calendar.DAY_OF_MONTH)).getTime());
                if(header.get(groupPosition).getListChildDataSelected().size()>0){
                    ll_date.setAlpha(1f);
                }else{
                    ll_date.setAlpha(0.3f);
                }

                ll_date.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                    }
                });
                if (selectedIDList != null && childRowModel.getId() != null && selectedIDList.contains("1")) {
                    iv_selection_date.setBackgroundResource(R.drawable.ic_selected_word);

                } else {
                    iv_selection_date.setBackgroundResource(R.drawable.ic_unselected_filter);

                }
                iv_date.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        DialogFragment newFragment = new DatePickerFragment(tv_date, groupPosition, START_DATE);
                        newFragment.show(((DefectsActivity) _context).getSupportFragmentManager(), "datePicker");
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
                            ((DefectsActivity) _context).applyFilter();
                        }

                    }
                });


                break;
            case CHILD_TYPE_UNDEFINED:
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
                if(header.get(groupPosition).getListChildDataSelected().size()>0){
                    ll_start_date.setAlpha(1f);
                    ll_end_date.setAlpha(1f);
                    iv_start_date.setEnabled(true);
                    iv_end_date.setEnabled(true);


                }else{
                    ll_start_date.setAlpha(0.3f);
                    ll_end_date.setAlpha(0.3f);
                    iv_start_date.setEnabled(false);
                    iv_end_date.setEnabled(false);

                    final Calendar c = Calendar.getInstance();
                    int year = c.get(Calendar.YEAR);
                    int month = c.get(Calendar.MONTH);
                    int day = c.get(Calendar.DAY_OF_MONTH);

                    tv_start_date.setText(formatedDate(year, (month + 1), day));
                    tv_end_date.setText(formatedDate(year, (month + 1), day));
                    header.get(groupPosition).setStart_date(Utils.convertStringToTimestamp(year + "-" + (month + 1) + "-" + day).getTime());
                    header.get(groupPosition).setEnd_date(Utils.convertStringToTimestamp(year + "-" + (month + 1) + "-" + day).getTime());

                }

                if (selectedIDList != null && childRowModel.getId() != null && selectedIDList.contains("1")) {
                    iv_selected_word_noticedate.setBackgroundResource(R.drawable.ic_selected_word);

                } else {
                    iv_selected_word_noticedate.setBackgroundResource(R.drawable.ic_unselected_filter);

                }

                if (selectedIDList != null && childRowModel.getId() != null && selectedIDList.contains("2")) {
                    iv_selected_word_notifiedate.setBackgroundResource(R.drawable.ic_selected_word);

                } else {
                    iv_selected_word_notifiedate.setBackgroundResource(R.drawable.ic_unselected_filter);

                }

                if (selectedIDList != null && childRowModel.getId() != null && selectedIDList.contains("3")) {
                    iv_selected_word_donedate.setBackgroundResource(R.drawable.ic_selected_word);

                } else {
                    iv_selected_word_donedate.setBackgroundResource(R.drawable.ic_unselected_filter);

                }

                iv_start_date.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        DialogFragment newFragment = new DatePickerFragment(tv_start_date, groupPosition, START_DATE);
                        newFragment.show(((DefectsActivity) _context).getSupportFragmentManager(), "datePicker");
                    }
                });

                iv_end_date.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        DialogFragment newFragment = new DatePickerFragment(tv_end_date, groupPosition, END_DATE);
                        newFragment.show(((DefectsActivity) _context).getSupportFragmentManager(), "datePicker");
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
                            ((DefectsActivity) _context).applyFilter();
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
                            ((DefectsActivity) _context).applyFilter();
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
                            ((DefectsActivity) _context).applyFilter();
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
        String headerTitle = ((GroupheadingModel) getGroup(groupPosition)).getTitle();

        // Inflating header layout and setting text
        if (convertView == null) {
            LayoutInflater infalInflater = (LayoutInflater) this._context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = infalInflater.inflate(R.layout.filter_header_view, parent, false);
        }

        TextView header_text = (TextView) convertView.findViewById(R.id.tv_title);
        ImageView iv_arrow = (ImageView) convertView.findViewById(R.id.iv_arrow);
        // If group is expanded then change the text into bold and change the
        // icon
        iv_arrow.setVisibility(View.INVISIBLE);
//        if (isExpanded) {
//            iv_arrow.setRotation(180);
//        } else {
//            iv_arrow.setRotation(0);
//        }
        header_text.setText(headerTitle);
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


        if (header.get(groupPosition).getType().equalsIgnoreCase(STATUS)) {
            return CHILD_TYPE_1;

        } else if (header.get(groupPosition).getType().equalsIgnoreCase(DEADLINE)) {
            return CHILD_TYPE_3;
        } else if (header.get(groupPosition).getType().equalsIgnoreCase(DATE)) {
            return CHILD_TYPE_UNDEFINED;
        } else {
            return CHILD_TYPE_2;
        }

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
            if (type.equalsIgnoreCase(START_DATE)) {
                header.get(gPos).setStart_date(Utils.convertStringToTimestamp(year + "-" + (month + 1) + "-" + day).getTime());
            } else if (type.equalsIgnoreCase(END_DATE)) {
                header.get(gPos).setEnd_date(Utils.convertStringToTimestamp(year + "-" + (month + 1) + "-" + day).getTime());
            }
            if (_context != null) {
                ((DefectsActivity) _context).applyFilter();
            }
        }
    }

    private static String formatedDate(int year, int month, int day) {
        String formatedDate = year + "/" + month + "/" + day;
        return formatedDate;
    }
}
