package com.projectdocupro.mobile.activities;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProviders;

import com.projectdocupro.mobile.ProjectNavigator;
import com.projectdocupro.mobile.R;
import com.projectdocupro.mobile.fragments.AllFragment;
import com.projectdocupro.mobile.interfaces.FinishCallback;
import com.projectdocupro.mobile.viewModels.NewProjectViewModel;



public class NewProjectActivity extends AppCompatActivity {

    
    NewProjectViewModel newProjectViewModel;
    ArrayAdapter<String> customersSpinnerAdapter;

    private Toolbar toolbar;

    private Spinner customerList;

    private EditText projectName;

    private EditText cityName;
    private Dialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_project);

        bindView();

        setSupportActionBar(toolbar);

        newProjectViewModel = ViewModelProviders.of(this).get(NewProjectViewModel.class);
        newProjectViewModel.initRepo(this, new FinishCallback() {
            @Override
            public void onFinishSuccess() {
                if (customerList != null) {
                    customersSpinnerAdapter = new ArrayAdapter<>(NewProjectActivity.this, R.layout.text_view, newProjectViewModel.getCustomerTitles());
                    customerList.setAdapter(customersSpinnerAdapter);
                }
            }

            @Override
            public void onFinishFailure() {

            }
        });

        toolbar.setNavigationOnClickListener(v -> {
            onBackPressed();
        });

        customerList.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View v, int postion, long arg3) {
                // TODO Auto-generated method stub
                ((TextView) v).setTextColor(Color.parseColor("#000000"));
                String spinnerValue = parent.getItemAtPosition(postion).toString();
                Log.d("item selected", spinnerValue);

            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
                // TODO Auto-generated method stub

                Log.d("item selected", "nothing");
            }
        });



    }


    void createProjectClick() {
        if (customerList.getSelectedItemPosition() == 0) {
            Toast.makeText(this, getString(R.string.select_customer), Toast.LENGTH_SHORT).show();
            return;
        }

        if (projectName.getText().toString().isEmpty()) {
//            MessageDialog messageDialog = new MessageDialog(this, R.style.Dialog_Theme, () -> {
//            });
//            messageDialog.setText(getString(R.string.dialog_title), getString(R.string.enter_project_name));
//            messageDialog.show();
            Toast.makeText(this, getString(R.string.enter_project_name), Toast.LENGTH_SHORT).show();
            return;
        }

        if (cityName.getText().toString().isEmpty()) {
//            MessageDialog messageDialog = new MessageDialog(this, R.style.Dialog_Theme, () -> {
//            });
//            messageDialog.setText(getString(R.string.dialog_title), getString(R.string.enter_city));
//            messageDialog.show();
            Toast.makeText(this, getString(R.string.enter_city), Toast.LENGTH_SHORT).show();
            return;
        }

        Log.d("customer name", newProjectViewModel.getAllCustomers().get(customerList.getSelectedItemPosition() - 1).getCompanyName());
        dialog = ProjectNavigator.showCustomProgress(this, "", false);
        newProjectViewModel.SaveProject(newProjectViewModel.getAllCustomers().get(customerList.getSelectedItemPosition() - 1).getCustomerId(), newProjectViewModel.getAllCustomers().get(customerList.getSelectedItemPosition() - 1).getCompanyName(), projectName.getText().toString(), cityName.getText().toString(), new FinishCallback() {
            @Override
            public void onFinishSuccess() {
//                MessageDialog messageDialog = new MessageDialog(NewProjectActivity.this, R.style.Dialog_Theme, NewProjectActivity.this::finish);
//                messageDialog.setText(getString(R.string.message), getString(R.string.project_created));
//                messageDialog.hideCancel();
//                messageDialog.show();
                if (dialog != null)
                    dialog.dismiss();
                Toast.makeText(NewProjectActivity.this, getString(R.string.project_created), Toast.LENGTH_SHORT).show();
                setResult(RESULT_OK);
                NewProjectActivity.this.finish();

                Intent intentt = new Intent(AllFragment.BR_ACTION_ADD_NEW_PROJECT);
                sendBroadcast(intentt);

            }

            @Override
            public void onFinishFailure() {
                if (dialog != null)
                    dialog.dismiss();
                Toast.makeText(NewProjectActivity.this, getString(R.string.error_create_project), Toast.LENGTH_SHORT).show();
            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.save_action_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.save_action) {
            hideKeyboard();
            createProjectClick();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        hideKeyboard();
        return true;
    }

    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null && getCurrentFocus() != null) {
            imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

    }


    private void bindView() {
        toolbar =  findViewById(R.id.toolbar);
        customerList =  findViewById(R.id.customer_list);
        projectName =  findViewById(R.id.project_name);
        cityName =  findViewById(R.id.city_name);
    }
}
