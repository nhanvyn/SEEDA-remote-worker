package com.sereem.remoteworker.ui.ui_for_main.worksites;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.sereem.remoteworker.R;
import com.sereem.remoteworker.databinding.ContactParentItemBinding;
import com.sereem.remoteworker.model.Database;
import com.sereem.remoteworker.model.User;
import com.sereem.remoteworker.model.siteAttendance.Attendance;
import com.sereem.remoteworker.model.siteAttendance.attendanceDatabase;
import com.sereem.remoteworker.model.workSite.SiteDatabase;
import com.sereem.remoteworker.model.workSite.WorkSite;
import com.sereem.remoteworker.ui.ColorPalette;

import java.util.ArrayList;
import java.util.List;


public class ContactsViewFragment extends Fragment {
    Context globalContext = null;
    private FragmentActivity myContext;
    SiteDatabase siteDB;
    attendanceDatabase attendanceDB;
    Database db;
    WorkSite userWorkSite = null;
    List<Attendance> attendances;
    List<User> userList = new ArrayList<>();
    ListView listView;
    User me;
    private ColorPalette colorPalette;




    public class userListAdapter extends ArrayAdapter<User>{
        public userListAdapter() {super(globalContext, R.layout.contact_parent_item, userList);}

        @SuppressLint("SetTextI18n")
        @Override
        public View getView(int position, View convertView, ViewGroup parent){
            View itemView = convertView;
            if (itemView == null)
                itemView = getLayoutInflater().inflate(R.layout.contact_parent_item, parent, false);

            ContactParentItemBinding binding = ContactParentItemBinding.bind(itemView);
            colorPalette = new ColorPalette(getContext(), binding, ColorPalette.TYPE.CONTACT);
            binding.setColorPalette(colorPalette);

            // get user
            User user = userList.get(position);
            System.out.println(user.getFirstName());
            // user name
            TextView userTextName = itemView.findViewById(R.id.parentHeader);
            userTextName.setText(user.getFirstName() + user.getLastName());
            colorPalette.registerListener();

            // fill image
            return itemView;
        }
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.fragment_contacts_view, container, false);
        globalContext = root.getContext().getApplicationContext();
        listView = root.findViewById(R.id.contactListView);
        siteDB = new SiteDatabase(root.getContext());
        db = new Database(root.getContext());



        getUser();
        attendanceDB = new attendanceDatabase(root.getContext());
        getSite();
        if (userList.isEmpty())
            getConnectedUser();
        populateListView();
        setUpListClick(root);

        return root;
    }

    private void getSite() {
        SharedPreferences prefs = getActivity().getSharedPreferences(
                "user", Context.MODE_PRIVATE);

        String id = prefs.getString("last_accessed_site_id", "NONE");
        userWorkSite = siteDB.getSite(id);
    }

    private void getConnectedUser(){
        attendances = attendanceDB.getAllAttendanceList();
        for (Attendance a : attendances)
            if (a.getSiteID().equals(userWorkSite.getSiteId())){
                User user = db.getUserByEmail(a.getWorkerEmail());
                System.out.println(user.getEmail() + " " + me.getEmail());
                if (!user.getEmail().equals(me.getEmail()))
                    {
                        userList.add(user);
                    }
            }
    }

    private void populateListView(){
        ArrayAdapter<User> adapter = new userListAdapter();
        listView.setAdapter(adapter);
    }

    private void getUser(){
        SharedPreferences prefs = globalContext.getSharedPreferences("user", Context.MODE_PRIVATE);
        String email = prefs.getString("email", "NONE");
        String password = prefs.getString("password", "NONE");
        me = db.getUser(email, password);
    }

    private void setUpListClick(View root){
        ListView list = root.findViewById(R.id.contactListView);
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                FragmentManager manager = getFragmentManager();
                User user = userList.get(position);
                PopupFragment dialog = new PopupFragment();
                dialog.showDialog(user.getEmail(), user.getPhone(), manager);
            }
        });
    }


    @Override
    public void onPause() {
        super.onPause();
        if(colorPalette != null) {
            colorPalette.unregisterListener();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if(colorPalette != null) {
            colorPalette.registerListener();
        }
    }

}