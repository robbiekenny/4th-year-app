package finalproject.homesecurity;

import android.app.Dialog;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.hardware.Camera;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.FacebookSdk;
import com.facebook.login.LoginManager;

/**
 * Created by Robbie on 13/08/2015.
 */
public class DecisionActivity extends ActionBarActivity {
    private SharedPreferences.Editor editor,edit; //edit is used for saving dont show me again, editor used for saving whether the device is security or personal
    private SecurityDetailsFragment frag;
    private SecurityHelpFragment helpFrag;
    private PersonalHelpFragment personalHelpFrag;
    private FragmentManager fragmentManager;
    private LinearLayout linearLayout;
    private TextView singedInAs;
    private Toolbar toolbar;
    private SharedPreferences sharedPref,sharedPrefs,prefs;
    private CoordinatorLayout coordinatorLayout;
    private AlertDialog b; //this is used to close the custom dialog displayed to the user

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.decision_activity);
        //http://www.android4devs.com/2014/12/how-to-make-material-design-app.html
        toolbar = (Toolbar) findViewById(R.id.tool_bar2);
        setSupportActionBar(toolbar);

        sharedPrefs = getSharedPreferences("DontShowAgain", Context.MODE_PRIVATE); //save dont show me again value
        sharedPref = getSharedPreferences("AuthenticatedUserDetails", Context.MODE_PRIVATE);
        prefs = this.getSharedPreferences("PhoneMode", Context.MODE_PRIVATE); //indicates whether phone is security device or personal
        editor = prefs.edit();

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            if(extras.getString("comingFrom").equals("registering")) //DISPLAY A MESSAGE TO THE USER INFORMING THEM THEY MUST VERIFY THEIR ACCOUNT
            {
                registerDialog();
            }
        }

        if(sharedPrefs.getBoolean("showAgain",true) == true)
        {
            messageDialog();
        }
        linearLayout = (LinearLayout) findViewById(R.id.linear_layout_decision);
        coordinatorLayout = (CoordinatorLayout) findViewById(R.id.coordinatorLayout);

        //check if security details fragment was displayed
        frag = (SecurityDetailsFragment) getFragmentManager().findFragmentByTag("frag");
        helpFrag = (SecurityHelpFragment) getFragmentManager().findFragmentByTag("helpFrag");
        personalHelpFrag = (PersonalHelpFragment) getFragmentManager().findFragmentByTag("personalHelpFrag");

        if(frag != null)
            linearLayout.setVisibility(View.INVISIBLE);
        else if(helpFrag != null)
            linearLayout.setVisibility(View.INVISIBLE);
        else if(personalHelpFrag != null)
            linearLayout.setVisibility(View.INVISIBLE);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.decision_activity__menu, menu);
        return true;
    }

    @Override
    public void onBackPressed() {
        frag = (SecurityDetailsFragment) getFragmentManager().findFragmentByTag("frag");
        helpFrag = (SecurityHelpFragment) getFragmentManager().findFragmentByTag("helpFrag");
        personalHelpFrag = (PersonalHelpFragment) getFragmentManager().findFragmentByTag("personalHelpFrag");
        if(frag != null && frag.isVisible()) //remove SecurityDetailsFragment from view and make other elements visible again
        {
            fragmentManager = getFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.remove(frag);
            fragmentTransaction.commit();

            toolbar.setTitle(R.string.app_name);
            linearLayout.setVisibility(View.VISIBLE);
        }
        else if(helpFrag != null && helpFrag.isVisible())
        {
            fragmentManager = getFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.remove(helpFrag);
            fragmentTransaction.commit();

            linearLayout.setVisibility(View.VISIBLE);
        }
        else if(personalHelpFrag != null && personalHelpFrag.isVisible())
        {
            fragmentManager = getFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.remove(personalHelpFrag);
            fragmentTransaction.commit();

            linearLayout.setVisibility(View.VISIBLE);
        }
        else
        {
            moveTaskToBack(true);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_signout) { //Remove user from shared preferences thus making the user signs in the next time

            System.out.println("LOGIN TYPE: " + sharedPref.getString("loginType", null));

            new AlertDialog.Builder(this)
                    .setTitle(R.string.sign_out)
                    .setMessage(R.string.sign_out_message)
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            if(sharedPref.getString("loginType",null).equals("facebook"))
                            {
                                try
                                {
                                    LoginManager.getInstance().logOut();
                                }catch(Exception e)
                                {
                                    FacebookSdk.sdkInitialize(getApplicationContext());
                                    LoginManager.getInstance().logOut();
                                }

                            }
                            sharedPref.edit().putString("userId", null).apply(); //remove userId that was associated with this user
                            sharedPrefs.edit().putBoolean("showAgain", true).apply(); //reset the show again boolean so that it should be displayed the next time

                            Intent intent = new Intent(getApplicationContext(),MainActivity.class);
                            startActivity(intent);
                            finish();
                        }
                    })
                    .setNegativeButton(android.R.string.no,new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    })
                    .setIcon(android.R.drawable.ic_dialog_info)
                    .show();
            return true;
        }
        else if(id == R.id.action_security)
        {
            linearLayout.setVisibility(View.INVISIBLE);
            toolbar.setTitle(R.string.app_name);

            helpFrag = (SecurityHelpFragment) getFragmentManager().findFragmentByTag("helpFrag");
            if(helpFrag == null)
            {
                fragmentManager = getFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                helpFrag = new SecurityHelpFragment();
                fragmentTransaction.replace(R.id.security_details_fragment_container, helpFrag, "helpFrag");
                fragmentTransaction.commit();
            }
            else
            {
                fragmentManager = getFragmentManager();
                FragmentTransaction ft = fragmentManager.beginTransaction();
                ft.replace(R.id.security_details_fragment_container, helpFrag,"helpFrag");
                ft.commit();
            }
        }
        else //personal option
        {
            linearLayout.setVisibility(View.INVISIBLE);
            toolbar.setTitle(R.string.app_name);

            personalHelpFrag = (PersonalHelpFragment) getFragmentManager().findFragmentByTag("personalHelpFrag");
            if(personalHelpFrag == null)
            {
                fragmentManager = getFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                personalHelpFrag = new PersonalHelpFragment();
                fragmentTransaction.replace(R.id.security_details_fragment_container, personalHelpFrag, "personalHelpFrag");
                fragmentTransaction.commit();
            }
            else
            {
                fragmentManager = getFragmentManager();
                FragmentTransaction ft = fragmentManager.beginTransaction();
                ft.replace(R.id.security_details_fragment_container, personalHelpFrag,"personalHelpFrag");
                ft.commit();
            }
        }

        return super.onOptionsItemSelected(item);
    }

    public void security(View v)
    {
        System.out.println("security method called");
        if (sharedPref.getBoolean("verified", false) == false) {
            displaySnackbar();
        } else {
            linearLayout.setVisibility(View.INVISIBLE);
            toolbar.setTitle(R.string.roomDetails);

            frag = (SecurityDetailsFragment) getFragmentManager().findFragmentByTag("frag");
            if (frag == null) {
                fragmentManager = getFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                frag = new SecurityDetailsFragment();
                fragmentTransaction.add(R.id.security_details_fragment_container, frag, "frag");
                fragmentTransaction.commit();
            } else {
                fragmentManager = getFragmentManager();
                FragmentTransaction ft = fragmentManager.beginTransaction();
                ft.replace(R.id.security_details_fragment_container, frag);
                ft.commit();
            }
        }
    }

    public void personal(View v)
    {
        System.out.println("personal method called");
        if(sharedPref.getBoolean("verified",false) == false)
        {
            displaySnackbar();
        }
        else
        {
            editor.putString("DeviceMode", "Personal"); //this device will be listed as personal
            editor.commit();
            Intent it = new Intent(this,PersonalDeviceActivity.class);
            startActivity(it);
        }

    }

    public void messageDialog() //displays a dialog informing them of the choice between security and personal
    {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.message_dialog_layout, null);
        dialogBuilder.setView(dialogView);

        final CheckBox cb = (CheckBox) dialogView.findViewById(R.id.dontShowAgainCBox);

        dialogBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                if(cb.isChecked())
                {
                    SharedPreferences.Editor editor = sharedPrefs.edit();
                    editor.putBoolean("showAgain", false); //user doesnt want to see the message again
                    editor.commit();
                }
                b.dismiss();
            }
        });

        b = dialogBuilder.create();
        b.show();
    }

    public void registerDialog() { //displays a dialog informing the user they must verify their account

        new AlertDialog.Builder(this)
                .setTitle(R.string.validateEmail)
                .setMessage(R.string.emailVerificationMessage)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();

    }

    public void displaySnackbar() //very simply notifys the user that they must verify their account before continuing
    {
        Snackbar snackbar = Snackbar
                .make(coordinatorLayout, R.string.verify, Snackbar.LENGTH_LONG);
        View sbView = snackbar.getView();
        TextView textView = (TextView) sbView.findViewById(android.support.design.R.id.snackbar_text);
        textView.setTextColor(Color.parseColor("#0288D1"));
        snackbar.show();
    }
}
